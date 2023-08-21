# Service Dependency Injection

I love Java's SPI Framework. It's flexible and extensible, and is well-designed for building plugins, add-ons
and other kinds of dynamic applications that rely on available services at runtime. Yet, there are limitations. I want 
the ability to build services that have dependencies on other services. I want to create singleton services that can be 
shared with other services at creation time. This library scratches these itches.

## Table of Contents
<!-- TOC -->
* [Service Dependency Injection](#service-dependency-injection)
  * [Table of Contents](#table-of-contents)
  * [Some History and Context](#some-history-and-context)
    * [The ServiceLoader](#the-serviceloader)
    * [Java SPI Restrictions](#java-spi-restrictions)
    * [Google Guice](#google-guice)
    * [Borrowing from .NET](#borrowing-from-net)
  * [Getting Started](#getting-started)
    * [Add the DI dependency to your project](#add-the-di-dependency-to-your-project)
    * [Bootstrapping The Service Registry](#bootstrapping-the-service-registry)
    * [Creating a Class that uses an Injected Service](#creating-a-class-that-uses-an-injected-service)
  * [Creating Dependency Injected Classes](#creating-dependency-injected-classes)
    * [Injecting a Service from a Constructor](#injecting-a-service-from-a-constructor)
      * [Constructor Mix-ins](#constructor-mix-ins)
    * [Field Injection](#field-injection)
  * [Creating Service Providers](#creating-service-providers)
    * [The `@ServiceProvider` Annotation](#the-serviceprovider-annotation)
      * [Selecting a Provider by Name](#selecting-a-provider-by-name)
      * [Selecting a Provider by Priority](#selecting-a-provider-by-priority)
    * [Service Scope/Lifetime](#service-scopelifetime)
    * [Injecting Services into Service Providers](#injecting-services-into-service-providers)
<!-- TOC -->

## Some History and Context

Java 6 introduced the _Service Provider Interface_ (SPI), which is a way of declaring and creating instances of
a service from one or more service providers. Providers were declared through resource files stored in the
`META-INF/services` folder using the service class name as the resource file name, and each provider class
declared within. Assume we have a service `com.example.services.MyService` and we have a single provider,
`com.example.providers.MyServiceProviderImpl`, we would create a service declaration as a resource file,
`META-INF/services/com.example.services.MyService` containing the following

```java
com.example.provides.MyServiceProviderImpl
```

Using the `java.util.ServiceLoader`, you could access the service provider using the Service class:

```java
ServiceLoader<MyService> serviceLoader = ServiceLoader.load(MyService.class);

Iterator<MyService> myServiceIterator = serviceLoader.iterator();

if (myServiceIterator.hasNext()) {
    var myService = myServiceIterator.next();
    //do something with the service...
    myService.doSomething();
} else {
    //handle cases where a provider is not found
}

```

With the introduction of the Module system in Java 9 onward, a new mechanism for declaring service providers was
introduced.
Instead of using `META-INF/services` resources files, service providers were declared with `provides` statements
in the `module-info.class`:

```java

provides MyService with MyServiceProviderImpl;
```

### The ServiceLoader

The `java.util.ServiceLoader` class provides a default reference implementation for loading services. It loads
requested services on demand (lazy-loading) by first locating the service declaring in either a `META-INF/services`  
or a loaded `modlule-info` declaration. If found, it will use the declaration's provider definitions to load
the declared provider classes. The ServiceLoader will cache any services that have been requested.

### Java SPI Restrictions

The Java SPI is generally very flexible and is a great way to create services that focus on building code using
interfaces ([Interface Segregation Principle](https://en.wikipedia.org/wiki/Interface_segregation_principle)) without worrying about the underlying 
implementation ([Dependency Inversion Principle](https://en.wikipedia.org/wiki/Dependency_inversion_principle)).

However, there the Java SPI layer imposes one key constraint: _Service provider classes must have a zero-argument
constructor (or default, undeclared constructor)_. Java compilers will complain about any provider that does meet
this requirement. What if a provider has dependencies on other services? Essentially, each provider would have to
load these during or after class instantiation, which adds a lot of overhead code:

```java 
public class MyServiceProviderImpl {

    //another service that this service depends on:
    private final MyOtherService otherService;

    //default zero-arg constructor required by the Java SPI
    public MyServiceProviderImpl() {
        ServiceLoader<MyOtherService> otherService = ServiceLoader.load(MyOtherService.class);
        this.otherService = otherService.next(); //I'm cheating here...
    }
}
```

The bottom line is that there is no way to _inject_ these services. What if my service should be treated as a singleton
service instance that should be shared across services? These scenarios aren't supported out of the box.

### Google Guice

Google Guice is a well-known, and fantastic, library that provides some answers for creating classes using dependency
injection. In fact, most of the time, it will provide exactly what you want without this library. While extremely 
powerful (I've used it on various projects), the API depends on a declarative model requiring you to
define these dependencies at compile time. There is a lot of flexibility in how you can declare these dependencies
including singleton and transient services, but I needed something a little more... dynamic along the lines of what 
ServiceLoader provides (but with some bells and whistles to make it easier).

### Borrowing from .NET

Say what you will about .NET (I tend to be polyglot, so I am equally fluent in this language), but its Dependency
Injection frameworks are a very slick. Define services through `IServiceCollection` and create classes with constructors with
parameters referencing the services you want injected.

## Getting Started

### Add the DI dependency to your project

In Maven, add the following dependency:

```xml

<dependency>
    <groupId>io.github.xmljim.service</groupId>
    <artifactId>dependency-injection</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

```

In Gradle:

```groovy
dependencies {
    implmentation("io.github.xmljim.service:dependency-injection:1.0-SNAPSHOT")
}

```

### Bootstrapping The Service Registry

Underpinning the entire Service Dependency Injection Framework is the `ServiceRegistry`. This must be loaded
by scanning the classpath and module path for all declared services using the `RegistryBootstrap`, which will create
a new `ServiceRegistry` instance and do the scanning and loading of all services. You can also configure 
options before loading the registry (discussed later):

```java
RegistryBootstrap.load();
```

Now we can access all services using the `ServiceRegistry`:

```java
var serviceRegistry = ServiceRegistries.getInstance();
var myService = serviceRegistry.loadServiceProvider(MyService.class);
```

### Creating a Class that uses an Injected Service

You can instantiate a class that has a dependency on a registered service from the `ServiceRegistry.loadClass` method
(we'll discuss how to create service-dependent classes a little bit further below):

```java
MyDependentClass dependentClass = serviceRegistry.loadClass(MyDependentClass.class);
```

## Creating Dependency Injected Classes

Any concrete class can benefit from dependency injection. There are two ways services can be injected into a 
class at creation time:

- As a constructor parameter
- As an injected field

### Injecting a Service from a Constructor

You can inject any registered service instance into a class at creation time using a constructor parameter:

```java
public class MyDependentClass {
    
    //variable to hold my MyService instance
    private final MyService myService;
    
    public MyDependentClass(MyService myService) {
        this.myService = myService;
    }
    
    public void doSomethingWithMyService() {
        myService.doesSomethingIWant();
    }
}
```

> **NOTE**
> 
> You can inject as many services as you need as parameters, provided that each references a service in the 
> service registry.

To use this class, we need to bootstrap our registry and load the class with the `ServiceRegistry.loadClass` method:

```java
// this only needs to be done once. After the registry is loaded, you can access it from the 
// ServiceRegistries.getInstance() method
RegistryBootstrap.load(); 

// get our service registry
var serviceRegistry = ServiceRegistries.getInstance();

// create an instance of our class:
MyDependentClass myDependentClass = serviceRegistry.loadClass(MyDependentClass.class);
```

You'll notice that the `loadClass` method doesn't include any arguments for the services to inject. When `loadClass`
is invoked, it interrogates the constructor's arguments to determine if they can be resolved with a service. If it 
finds the service, a new service provider instance is created and passed as the parameter value when the constructor
is invoked to create a new class.  But what if I have more than one constructor?  Using the `@DependencyInjection`
annotation, you can declare a constructor "eligible" for dependency injection.  Going back to our example class:

```java

import io.github.xmljim.service.di.annotations.DependencyInjection;

public class MyDependentClass {

    //variable to hold my MyService instance
    private final MyService myService;

    //declares this constructor is eligible for dependency injection
    @DependencyInjection
    public MyDependentClass(MyService myService) {
        this.myService = myService;
    }

    // another constructor for testing (and to demonstrate how to use @DependencyInjection)...
    public MyDependentClass() {
        this.myService = new MyTestServiceImpl();
    }

    public void doSomethingWithMyService() {
        myService.doesSomethingIWant();
    }
}

```

> **IMPORTANT**
> 
> A class should only have one constructor annotated with `@DependencyInjection`. If you annotate
> more than one constructor, there is no guarantee which constructor will be picked. Internally it just
> takes the first one it finds.


#### Constructor Mix-ins

Not all classes rely solely on injected services at creation time. You can define constructors that include
a mix of services and other non-injected parameters with the following requirements:

1. The constructor *must be annotated* with `@DependencyInjection`
2. All _non-injected_ parameters must be placed _after_ any services you want to inject

Extending our example class:

```java
import io.github.xmljim.service.di.annotations.DependencyInjection;

public class MyDependentClass {

    // variable to hold my MyService instance
    private final MyService myService;
    
    // other variables we'll supply to our constructor
    private final String messageTemplate;
    private final String messageValue;

    // declares this constructor is eligible for dependency injection
    @DependencyInjection
    public MyDependentClass(MyService myService, String messageTemplate, String messageValue) {
        this.myService = myService;
    }

    // another constructor for testing (and to demonstrate how to use @DependencyInjection)...
    public MyDependentClass() {
        this.myService = new MyTestServiceImpl();
        this.messageTemplate = "Hello %s";
        this.messageValue = "Jim";
    }

    public void doSomethingWithMyService() {
        myService.doesSomethingIWant(messageTemplate, messageValue);
    }
}
```

To create this class, we'll use the overloaded `loadClass(Class<?>, Object...)` method:

```java
String messageTemplate = "You are using Java Version: %s";
String messageValue = System.getProperty("java.version");

MyDependentClass dependentClass = serviceRegistry.loadClass(MyDependentClass.class, messageTemplate, messageValue);
```

### Field Injection

While constructor injection is the preferred mechanism for building classes with injected services, there are scenarios
where injecting services to declared fields is warranted. For example, you may have a subclass that extends another
class that already declares a constructor with injected services, and your subclass requires additional services.
Of course, you can create a new constructor, and invoke `super(...)` if your superclass doesn't rely on mix-ins (well,
you can technically...). However, in that case, field injection provides an alternative solution.

After the class is instantiated from the constructor, the injector service interrogates all declared fields in the class, 
looking for any that have been annotated with the `@Inject` annotation, when it finds one, it will set the field's 
value with an instance of the service.

In this example, we'll create a subclass of our `MyDependentClass` and inject another service, `MyOtherService`:

```java
import io.github.xmljim.service.di.annotations.DependencyInjection;
import io.github.xmljim.service.di.annotations.Inject;

public class MyDependentSubclass extends MyDependentClass {

    // inject an instance of MyOtherService into this field
    @Inject
    private MyOtherService myOtherService;

    // same rules apply for mix-ins on subclasses
    @DependencyInjection
    public MyDependentSubclass(MyService myService, String messageTemplate, String messageValue) {
        super(myService, messageTemplate, messageValue);
    }

    public MyOtherService getMyOtherService() {
        return myOtherService;
    }

    @Override
    public void doSomethingWithMyService() {
        myService.doesSomethingIWant(messageTemplate, messageValue);
        getMyOtherService().doSomethingElseWithTheData(messageTemplate, messageValue);
    }
}
```

## Creating Service Providers

Services are classes that provide an abstraction between a set of functionality requested by a _consumer_ and the
actual _provider_ who implements/fulfills that request. In _most_ cases a service is an interface or abstract class,
with one or more concrete _provider_ classes that implement/extend the service class. Consumers request a service, but
don't care about the underlying implementation that provides the service.  

You declare a class is a service by declaring that it has (one or more) corresponding provider classes. By default, 
services are declared using one of the two mechanisms supported by Java's SPI Framework.

- If you are using modules (and you should), use the `provides {Service Class} with {Provider Class}` in the `module-info.class` 
  file:
  ```java
  //module-info.java
  module myModule {
      provides ITeapotService with TeapotService;
  }
  ```
- If you aren't using modules, you must add a resource the `META-INF/services` folder, using the fully qualified
  _service_ class name as the name of the file, and within it, insert the fully qualified class names of each provider 
  you are defining on separate lines.

In either case, the power of this approach is that providers can live in separate jar files or modules and will only
be applied if they are on the runtime module or classpath.  

It's generally the case that a service has only one provider class. However, that's not always true. You may have 
service providers for different contexts or conditions that offer different implementations. Using the 
`java.util.ServiceLoader`, you would iterate through each provider class to determine the one you want.  

When a `ServiceRegistry` is loaded, it will store all `Service` classes along with all corresponding `Provider` classes.
The out-of-the-box strategy will select the first provider instance it finds with a `@ServiceProvider` annotation,
or, if none of the providers have this annotation, then the first provider found is selected.


### The `@ServiceProvider` Annotation

The `@ServiceProvider` annotation is used to identify service provider classes. In addition to tagging the class 
as a service provider, the annotation provides some additional metadata to assist in choosing a provider as well
as dictating the _scope_ or _lifetime_ of the provider.

The `@ServiceProvider` has three properties:

| Property     | Type                   | Description                                                                 |
|--------------|------------------------|-----------------------------------------------------------------------------|
| `name()`     | `String`               | Specifies a unique provider name, which can be used to select this provider |
| `lifetime()` | `enum ServiceLifetime` | Indicates if the service should be treated as a `SINGLETON` or `TRANSIENT`  |
| `priority()` | `int` (default = 1)    | Specifies a priority value as a tie-breaker between providers               |


#### Selecting a Provider by Name

Using the `@ServiceProvider` annotation, we can provide a unique name and use that to select a specific provider instance:

```java
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.util.ServiceLifetime;

@ServiceProvider(name = "MyService", lifetime = ServiceLifetime.TRANSIENT, priority = 100)
public class MyServiceProvider implements MyService {

}
```

When the provider is loaded, consumers can select this service provider by name:

```java
MyService myService = serviceManager.loadServiceProvider(MyService.class, "MyService");
```

#### Selecting a Provider by Priority

In cases where more than one provider class is annotated with the `@ServiceProvider` annotation, and 
a consumer requests a service without a name, the providers are sorted by `priority()` with the winner
being the one with the highest value.

For example, assume we have two providers for the same service, each annotated with `@ServiceProvider` a "default"
provider uses the default value of 1, another provider sets the `provider` property to 100:

```java
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.util.ServiceLifetime;

@ServiceProvider(name = "DefaultService", lifetime = ServiceLifetime.TRANSIENT) //default priority = 1
public class DefaultServiceProvider implements MyService {
    
}

@ServiceProvider(name = "MyService", lifetime = ServiceLifetime.TRANSIENT, priority = 100)
public class MyServiceProvider implements MyService {

}
```

When a consumer requests this service, the provider with the highest priority will be selected:

```java
MyService myService = serviceManager.loadServiceProvider(MyService.class);

//emit the service class
System.out.println(myService.getClass());
//emits 'MyServiceProvider'
```

### Service Scope/Lifetime

By default, when a consumer requests a service, a new instance of that service is created.  In other words, it's
a transient service. In this context, a new service instance will be created and will be disposed (garbage collected)
after the reference to it is no longer in use.  However, some services are designed to be created once and shared
across multiple consumers (and services), i.e., it's a singleton.

Using the `@ServiceProvider` annotation's `lifetime()` property, you can control the scope of the service's lifetime.
If the value is set to `ServiceLifetime.SINGLETON`, the first request for the service will instantiate the provider
instance, and instead of releasing it, the `Provider` holds a reference to it for any subsequent requests for that
service. 

### Injecting Services into Service Providers

Service injection into a service provider works just like service injection into any other class with a few key 
exceptions/constraints:

1. Java's SPI framework requires service provider classes to have a default or zero-argument constructor. To work
   around this constraint, your service provider class must include the zero-argument constructor to satisfy the
   core SPI requirement, _and_ to use service injection from a constructor, you must annotate that constructor
   with the `@DependencyInjection` annotation.
2. [Constructor Mix-ins](#constructor-mix-ins) are _not allowed_. All constructor parameters must resolve to a registered
   service.

```java
public class InjectedServiceA implements IInjectedServiceA {

    private ITeapotService teapotService;

    public InjectedServiceA() {

    }

    @DependencyInjection
    public InjectedServiceA(ITeapotService teapotService) {
        this.teapotService = teapotService;
    }

    @Override
    public boolean isInjected() {
        return teapotService != null;
    }

    @Override
    public String saySomething() {
        return teapotService.teapot();
    }
}
```

In the example above, our service provider has a dependency on the `ITeapotService`.  When a request for the
`IInjectedServiceA` is made, it will return the `InjectedServiceA` with the `ITeapotService` service provider instance
accessible from the `saySomething()` method:

```java
IInjectedServiceA injectedService = serviceRegistry.loadServiceProvider(IInjectedServiceA.class);
System.out.println(injectedService.saySomething());
//emits "I'm a little teapot"
```

## Service Dependency Components

### `ServiceRegistry`

### `Scanner`

### `Service`

### `Provider`

### `RegistryBootstrap`