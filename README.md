# Service Dependency Injection

I love Java's SPI Framework. It's flexible and extensible, and is well-designed for building plugins, add-ons
and other kinds of dynamic applications that rely on available services at runtime. Yet, there are limitations. I want 
the ability to build services that have dependencies on other services. I want to create singleton services that can be 
shared with other services at creation time. This library scratches these itches.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.xmljim.service/dependency-injection.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.xmljim.service%22%20AND%20a:%22dependency-injection%22)

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
  * [Service Dependency Design](#service-dependency-design)
    * [`ServiceRegistry`](#serviceregistry)
      * [ServiceRegistries Class](#serviceregistries-class)
      * [Assignability Enforcement](#assignability-enforcement)
      * [Default Implementation](#default-implementation)
    * [`Scanner`](#scanner)
      * [`Scanners` Class](#scanners-class)
      * [`ClassFilter` Interface](#classfilter-interface)
      * [Custom Scanners](#custom-scanners)
      * [Appending a Scanner](#appending-a-scanner)
      * [Creating a Scanner Instance](#creating-a-scanner-instance)
      * [Running a Scanner](#running-a-scanner)
    * [`Service`](#service)
    * [`Provider`](#provider)
    * [`RegistryBootstrap`](#registrybootstrap)
      * [`RegistryBootstrap.Options`](#registrybootstrapoptions)
* [License](#license)
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

> **NOTE**
> 
> The `@ServiceProvider` annotation is not required for out-of-the-box discovery. However, service providers with
> this annotation are given preference over other providers.

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

## Service Dependency Design

The native `java.util.ServiceLoader` was implemented around the assumption that services will be lazily loaded
on demand.  It also follows the standard Java SPI framework assumptions around creating service provider instances
using a default or zero-argument constructor. While the `ServiceLoader` does do a small amount of caching for 
services previous requested, it has no global knowledge of any services other than at request time.  In other words,
it has to interrogate the classpath or module path for each service.  For most applications, the latency is negligible.
However, this approach does not lend itself well to creating complex services that have dependencies on other services.

The Service Dependency design assumes that it should _front-load_ discovery of services at startup and storing them
in a `ServiceRegistry` using `Scanner` classes to discover and load the services.  

When a service is requested, the `ServiceRegistry` will locate the `Service`, which in turn will select a `Provider` 
to return an instance of the requested service.

The `RegistryBootstrap` provides a mechanism for configuring and loading the `ServiceRegistry` at startup. It's also
designed in a way for other libraries who wish to use this library to integrate configuration options during
initialization. 

### `ServiceRegistry`

At the heart of the entire design is the `ServiceRegistry`.  This interface provides methods for configuring,
loading services, retrieving services and creating class instances with injected services. It is intended to be 
loaded once and shared as a singleton instance. In addition to storing services, the `ServiceRegistry` manages
all of the `Scanner` instances that are used for `load` and `reload` operations.

#### ServiceRegistries Class

The `ServiceRegistries` class is an abstract implementation that provides utility methods as well as the static
`getInstance()` method which allows the `ServiceRegistry` to be treated as a singleton instance.  Additionally,
this class contains static methods for creating custom `ServiceRegistry` implementations.  The built-in, out-of-the-box
`ServiceRegistry` implementation extends from this class.

#### Assignability Enforcement

As mentioned previously, the general convention is that a service is _assignable from_ a service provider. In other
words, a service provider instance _inherits from_ or _implements_ a service class.  Out of the box, there is no
presumption that this should be enforced. However, depending on the application you may have a requirement that
this requirement should be enforced.  In that event, you can initialize a `ServiceRegistry` to propagate this requirement
all the way down to each `Provider` instance at discovery time.  While the `ServiceRegistry` does not actually
enforce this requirement, it as a global configuration that services and providers _may_ recognize (as we'll see later,
these can be overridden at the `Service` level, or even by individual `Scanner` implementations).

This can be set at instantiation time using the `ServiceRegistries.newServiceRegistry(boolean)` method, or 
from the `RegistryBootstrap.Options` class at startup.

#### Default Implementation

The default implementation uses in-memory storage for holding references to each discovered `Service`. This implementation
initializes two default `Scanner` classes to discover services via the classpath and module path. 

### `Scanner`

`Scanner` implementations are designed for discovery of services, mapping providers to these services, and adding
these discovered services to the `ServiceRegistry`.  The `ServiceRegistry` orchestrates when each of these scanner 
implementations will run along with additional configurations including filtering and applying assignability enforcement.

There are two out-of-the-box `Scanner` implementations, which can be accessed from the `Scanners` class. All scanner 
implementations must have a unique name which allows a `ServiceRegistry` to each loaded scanner individually.

- Classpath Scanner (name: `Scanners.MODULE`): Scans for all services declared in `META-INF/services` resources.
- Module Scanner: (name: `Scanners.CLASSPATH`): Scans all loaded modules' module descriptor for `provides` declarations

#### `Scanners` Class

The `Scanners` class is an abstract implementation of the `Scanner` interface that includes some utility methods 
that can be shared across scanners as additional properties for configuring a scan operation including assignability
enforcement, and service and provider class filtering.  Additionally, the class provides static methods for creating
new custom and out-of-the-box scanner instances.

It provides default implementations for the following methods:

- `enforceProviderAssignableFromService()`: returns a boolean value indicating whether the provider should enforce
   assignability with the service class
- `getProviderClassFilter()`: returns a `ClassFilter` to filter provider classes against specific criteria
- `getServiceClassFilter()`: returns a `ClassFilter` to filter service classes against specific criteria

#### `ClassFilter` Interface

A `ClassFilter` is an extension of the `Predicate<Class<?>>` functional interface that provides filtering functionality
for scanners.  Filtering allows the scanner to process services and providers that meet specific criteria to 
be registered in the `ServiceRegistry`.  There are few built in `ClassFilter` instances available from the `ClassFilters`
class:

- `ClassFilters.DEFAULT`:  The default filter, returns true for all service or provider classes. In others, don't filter anything
- `ClassFilters.hasAnnotation(Class<? extends Annotation>)`: Looks for classes that have a specific annotation class
- `ClassFilters.hasServiceProviderAnnotation()`: Syntactic sugar for `hasAnnotation(ServiceProvider.class)`. Mostly used for provider filters
- `ClassFilters.implementsInterface(Class<?>)`: Filter for classes that implement a given interface class.

You can also create composite filters using the `and(ClassFilter)`, `or(ClassFilter)` and `not(ClassFilter)` predicate
functions.  For example, you might have service classes that all implement a base interface _and_ have an annotation
assigned:

```java 
ClassFilter filter = ClassFilter.hasAnnotation(MyAnnotation.class).and(ClassFilter.implementsInterface(MyInterface.class));
```

#### Custom Scanners

All scanners must implement the `Scanner` interface. However, it's advisable to extend the `Scanners` abstract class
which provides default implementation for the `getProviderFilter()`, `getServiceFilter()`, and 
`enableProviderAssignableFromService()` methods. As a result, a custom scanner needs to only implement the `getName()`
and `scan(ServiceRegistry)` methods. The `scan` method is the principle operation that must be implemented to discover
and registers services to a service registry.

#### Appending a Scanner

Since scanner configurations (filters and assignability) can be modified for each scan, appending a scanner to 
a service registry stores the key-value pair of the scanner's name and the scanner class. When a new load or reload
is requested, a new scanner instance is created with the requested configuration.

```java
var serviceRegistry = ServiceRegistries.getInstance();
serviceRegistry.appendScanner("MyScanner", MyScanner.class);
```

Since it was just appended, you can use `getScanners()` to list all registered scanners, or `isLoaded(String)`
to get the load status for that scanner.

#### Creating a Scanner Instance

The `Scanners` class contains several static methods to create a new `Scanner` instance:

- `newScanner(Class<? extends Scanner>)`: Creates a scanner instance with default service and provider filters, and
  inherits the assignability enforcement flag from the service registry.
- `newScanner(Class<? extends Scanner>, ClassFilter, ClassFilter, boolean)`: Creates a scanner instance using the 
  specified class, service and provider filters, and assignability enforcement

#### Running a Scanner

The `ServiceRegistry` provides several methods for invoking any or all scanners:

- `load()` - loads all registered scanners on the service registry. This is the equivalent of 
  `load(ClassFilters.DEFAULT, ClassFilters.DEFAULT)`
- `load(ClassFilter, ClassFilter)` - loads all registered scanners using the specified service and provider class filters.
- `load(String, ClassFilter, ClassFilter, boolean)`: runs the registered named scanner using the specified
  service and provider class filters, and a flag for enforcing assignability (potentially overriding the value 
  assigned to the registry on instantiation);
- `load(Scanner)` - runs any scanner (registered or not). This is useful for one-time ad-hoc loads that don't require
  scanner registration (via `ServiceRegistry.appendScanner(String, Class<? extends Scanner>)`)


### `Service`

A `Service` contains the service class reference and manages all corresponding `Provider` class references. In addition,
a `Service` controls the assignability enforcement for all provider classes. When a service is requested, the 
`Service` is responsible for locating the `Provider` class to use to fulfill the request.  For more information about
provider selection strategies, see [The `@ServiceProvider` Annotation](#the-serviceprovider-annotation)

### `Provider`

A `Provider` contains a reference to a service provider class and is responsible for creating instances of the class. 
When a `Service` selects a `Provider` for a service request, it will call `getInstance()`.  This method creates
an instance of the provider class, with all services injected. The `Provider` also uses the `@ServiceProvider`
to determine the scope of the provider.  If the `@ServiceProvider.lifetime()` is equal to `ServiceLifetime.SINGLETON`,
it is responsible for holding a reference to the instance and returning it via `getInstance()` when requested.

### `RegistryBootstrap`

The `RegistryBoostrap` provides static methods for configuring and loading a `ServiceRegistry` instance. It is intended
to be integrated into other libraries and applications as a mechanism for configuring a service registry instance.

#### `RegistryBootstrap.Options`

Stores all configuration options to be used to create a new `ServiceRegistry` instance. Using a bootstrap pattern
allows for initialization of the ServiceRegistry when the JVM is started.  For example, assume a standard Java 
entry point `main(String[] args)` method:

```java

public static void main(String[] args) {
    // uses the default options and loads a service registry
    RegistryBootstrap.load();    
    
    // now the service registry can be accessed:
    var serviceRegistry = ServiceRegistries.getInstance();
    
}

```

In this example, we'll use the `RegistryBootstrap.Options.configure()` to set options:

```java
public static void main(String[] args) {
    RegistryBootstrap.load(() -> RegistryBootstrap.Options.configure()
    	.enforceAssignability(true) //enforce assignability in the service registry
    	.appendScanner("MyPrimaryScanner", MyScannerPrimary.class) 
    	.appendScanner("MySecondaryScanner", MySecondaryScanner.class) //you can register more than one
    	.appendService(MyService.class, MyServiceImpl.class) 
    	.serviceClassFilter(ClassFilters.implementsInterface(MyServiceBase.class))
    	.providerClassFilter(ClassFilters.hasServiceProviderAnnotation())
    	.loadRegistry(false)
        .build();
    );
}
```

`RegistryBootstrap.Options` provides three static methods for instantiating a `Options` instance:

- `useDefaults()` - returns a new `Options` instance using default option settings.
- `configure()` - opens a Builder instance that is used to set options. Returns an `Options` instance when
  `Builder.build()` is called
- `merge(Options)` - provides an integration point for libraries that integrate with the Service Manager to initialize
  options required for their library's functionality and to expose any additional configuration options for other
  libraries as needed.


The following options are available from the `configure()` and `merge(Options)` methods:

| Option                          | Type                               | Description                                                                                                                                                                                     |
|---------------------------------|------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `serviceImplementation`         | `Class<? extends Service>`         | Specifies the implementation class for service classes. If not set, the default implementation is used                                                                                          |
| `providerImplementation`        | `Class<? extends Provider>`        | Specifies the implementation class for provider classes. If not set, the default implementation is used                                                                                         |
| `serviceRegistryImplementation` | `Class<? extends ServiceRegistry>` | Specifies the implementation class the service registry. If not set, the default implementation is used                                                                                         |
| `enforceAssignability`          | `boolean`                          | Allows you to specify if all service provider classes must be assignable from the service class during load. By default, this is set to `false`                                                 |
| `serviceClassFilter`            | `ClassFilter`                      | Specifies a class filter to apply for loading services. If not set, it will use the default filter (`ClassFilters.DEFAULT`) (load all). Only applied if `loadRegistry` is set to `true`         |
| `providerClassFilter`           | `ClassFilter`                      | Specifies a class filter to apply for loading provider classes. If not set, it will use the default filter (`ClassFilters.DEFAULT`) (load all). Only applied if `loadRegistry` is set to `true` |
| Custom Scanners                 | `Class<? extends Scanner>`         | Using the `appendScanner(String, Class<? extends Scanner>)` method, it will add a new scanner to the service registry when it's initialized                                                     |
| Custom Services                 | `ServiceDefinition`                | Using the `appendService(S, P)` method, services can be bootstrapped into the service registry rather than discovered                                                                           |
| `loadRegistry`                  | `boolean`                          | Load the registry uisng all defined scanners and filters after initialization. Set to `true` by default. If set to `false`, the service registry will be intialized without loading services    |

# License

Apache NON-AI License, Version 2.0

TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

1. **Definitions**.

    “License” shall mean the terms and conditions for use, reproduction, and distribution as defined by Sections 1 through 9 of this document.

    “Licensor” shall mean the copyright owner or entity authorized by the copyright owner that is granting the License.

    “Legal Entity” shall mean the union of the acting entity and all other entities that control, are controlled by,
    or are under common control with that entity. For the purposes of this definition, “control” means (i) the power, direct or indirect,
    to cause the direction or management of such entity, whether by contract or otherwise, or (ii) ownership of fifty percent (50%)
    or more of the outstanding shares, or (iii) beneficial ownership of such entity.

    “You” (or “Your”) shall mean an individual or Legal Entity exercising permissions granted by this License.

    “Source” form shall mean the preferred form for making modifications, including but not limited to software source code, documentation source,
    and configuration files.

    “Object” form shall mean any form resulting from mechanical transformation or translation of a Source form, including
    but not limited to compiled object code, generated documentation, and conversions to other media types.

    “Work” shall mean the work of authorship, whether in Source or Object form, made available under the License,
    as indicated by a copyright notice that is included in or attached to the work (an example is provided in the Appendix below).

    “Derivative Works” shall mean any work, whether in Source or Object form, that is based on (or derived from) the Work and
    for which the editorial revisions, annotations, elaborations, or other modifications represent, as a whole, an original work of authorship.
    For the purposes of this License, Derivative Works shall not include works that remain separable from, or merely link (or bind by name)
    to the interfaces of, the Work and Derivative Works thereof.

    “Contribution” shall mean any work of authorship, including the original version of the Work and any modifications or additions to
    that Work or Derivative Works thereof, that is intentionally submitted to Licensor for inclusion in the Work by the copyright owner or
    by an individual or Legal Entity authorized to submit on behalf of the copyright owner. For the purposes of this definition, “submitted”
    means any form of electronic, verbal, or written communication sent to the Licensor or its representatives, including but not limited to
    communication on electronic mailing lists, source code control systems, and issue tracking systems that are managed by, or on behalf of,
    the Licensor for the purpose of discussing and improving the Work, but excluding communication that is conspicuously marked or otherwise designated
    in writing by the copyright owner as “Not a Contribution.”

    “Contributor” shall mean Licensor and any individual or Legal Entity on behalf of whom a Contribution has been received by Licensor and
    subsequently incorporated within the Work.

2. **Grant of Copyright License.**

    Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive,
    no-charge, royalty-free, irrevocable copyright license to reproduce, prepare Derivative Works of, publicly display, publicly perform,
    sublicense, and distribute the Work and such Derivative Works in Source or Object form,
    under the following conditions:

    1. **You shall not use the Covered Software in the creation of an Artificial Intelligence training dataset,
         including but not limited to any use that contributes to the training or development of an AI model or algorithm,
         unless You obtain explicit written permission from the Contributor to do so.**

    2. **You acknowledge that the Covered Software is not intended for use in the creation of an Artificial Intelligence training dataset,
         and that the Contributor has no obligation to provide support or assistance for any use that violates this license.**

3. **Grant of Patent License.**

    Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge,
    royalty-free, irrevocable (except as stated in this section) patent license to make, have made, use, offer to sell, sell, import,
    and otherwise transfer the Work, where such license applies only to those patent claims licensable by such Contributor that are 
    necessarily infringed by their Contribution(s) alone or by combination of their Contribution(s) with the Work to which
    such Contribution(s) was submitted. If You institute patent litigation against any entity (including a cross-claim or counterclaim in a lawsuit)
    alleging that the Work or a Contribution incorporated within the Work constitutes direct or contributory patent infringement,
    then any patent licenses granted to You under this License for that Work shall terminate as of the date such litigation is filed.

4. **Redistribution.**

    You may reproduce and distribute copies of the Work or Derivative Works thereof in any medium, with or without modifications,
    and in Source or Object form, provided that You meet the following conditions:

    1. You must give any other recipients of the Work or Derivative Works a copy of this License; and

    2. You must cause any modified files to carry prominent notices stating that You changed the files; and

    3. You must retain, in the Source form of any Derivative Works that You distribute, all copyright, patent, trademark,
        and attribution notices from the Source form of the Work, excluding those notices that do not pertain to any part of the Derivative Works; and

    4. If the Work includes a “NOTICE” text file as part of its distribution, then any Derivative Works that You distribute
        must include a readable copy of the attribution notices contained within such NOTICE file, excluding those notices that
        do not pertain to any part of the Derivative Works, in at least one of the following places: within a NOTICE text file distributed
        as part of the Derivative Works; within the Source form or documentation, if provided along with the Derivative Works;
        or, within a display generated by the Derivative Works, if and wherever such third-party notices normally appear.
        The contents of the NOTICE file are for informational purposes only and do not modify the License.

    You may add Your own attribution notices within Derivative Works that You distribute,
    alongside or as an addendum to the NOTICE text from the Work, provided that such additional attribution notices cannot be construed as
    modifying the License.

    You may add Your own copyright statement to Your modifications and may provide additional or different license terms and conditions for use,
    reproduction, or distribution of Your modifications, or for any such Derivative Works as a whole, provided Your use, reproduction,
    and distribution of the Work otherwise complies with the conditions stated in this License.

5. **Submission of Contributions.**

    Unless You explicitly state otherwise, any Contribution intentionally submitted for inclusion in the Work by You to the Licensor
    shall be under the terms and conditions of this License, without any additional terms or conditions.
    Notwithstanding the above, nothing herein shall supersede or modify the terms of any separate license agreement you may have executed
    with Licensor regarding such Contributions.

6. **Trademarks.**

    This License does not grant permission to use the trade names, trademarks, service marks, or product names of the Licensor,
    except as required for reasonable and customary use in describing the origin of the Work and reproducing the content of the NOTICE file.

7. **Disclaimer of Warranty.**

    Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions)
    on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation,
    any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
    You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated
    with Your exercise of permissions under this License.

8. **Limitation of Liability.**

    In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise,
    unless required by applicable law (such as deliberate and grossly negligent acts) or agreed to in writing,
    shall any Contributor be liable to You for damages, including any direct, indirect, special, incidental, or consequential damages
    of any character arising as a result of this License or out of the use or inability to use the Work (including but not limited to damages
    for loss of goodwill, work stoppage, computer failure or malfunction, or any and all other commercial damages or losses),
    even if such Contributor has been advised of the possibility of such damages.

9. **Accepting Warranty or Additional Liability.**

    While redistributing the Work or Derivative Works thereof, You may choose to offer, and charge a fee for, acceptance of support,
    warranty, indemnity, or other liability obligations and/or rights consistent with this License.
    However, in accepting such obligations, You may act only on Your own behalf and on Your sole responsibility,
    not on behalf of any other Contributor, and only if You agree to indemnify, defend, and hold each Contributor harmless for any liability incurred by,
    or claims asserted against, such Contributor by reason of your accepting any such warranty or additional liability.

END OF TERMS AND CONDITIONS
