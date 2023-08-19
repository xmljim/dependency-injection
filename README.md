# Service Dependency Injection

I love Java's SPI Framework. It's super flexible and extensible, and is well-designed for building plugins, add-ons
and other kinds of dynamic applications that rely on available services at runtime. Yet, I want the ability to
build services that have dependencies on other services. I want to create singleton services that can be shared
with other services at creation time. So, this library scratches these itches.

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
ServiceLoader<MyService> serviceLoader=ServiceLoader.load(MyService.class);

    Iterator<MyService> myServiceIterator=serviceLoader.iterator();

    if(myServiceIterator.hasNext()){
    var myService=myServiceIterator.next();
    //do something with the service...
    myService.doSomething();
    }else{
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
interfaces ([Interface Segregation Principle](https://en.wikipedia.org/wiki/Interface_segregation_principle)) without
worrying about the underlying
implementation ([Dependency Inversion Principle](https://en.wikipedia.org/wiki/Dependency_inversion_principle)).

However, there the Java SPI layer imposes one key constraint: _Service provider classes must have a zero-argument
constructor (or default, undeclared constructor)_. Java compilers will complain about any provider that does meet
this requirement. What if a provider has dependencies on other services? Essentially, each provider would have to
load these after or during class instantiation:

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

## Google Guice

Google Guice is a well-known, and fantastic, library that provides some answers for creating classes using dependency
injection.  
While extremely powerful (I've used it on various projects), the API depends on a declarative model requiring you to
define these dependencies at compile time. There is a lot of flexibility in how you can declare these dependencies
including
singleton and transient services, but I needed something a little more... dynamic along the lines of what ServiceLoader
provides.

## Borrowing from .NET

Say what you will about .NET (I tend to be polyglot, so I am equally fluent in this language), but its Dependency
Injection
frameworks are a very slick. Define services through `IServiceCollection` and create classes with constructors with
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
by scanning the classpath and module path for all declared services using the `RegistryBootstrap`:

```java
RegistryBootstrap.load();
```

Now we can access all services using the `ServiceRegistry`:

```java
var serviceRegistry=ServiceRegistries.getInstance();
    var myService=serviceRegistry.loadServiceProvider(MyService.class);
```

