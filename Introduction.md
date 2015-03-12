# Introduction #

The 'Facade' API provides a way to let classes which are in separate `ClassLoader` instances access each other through Proxies and shared interfaces.

# `ClassLoader` Silos #

In a simple Java application, all classes are loaded with a single `ClassLoader`. This means that all classes can load all other classes in the application, because they all come from the same source.

However, in more complex applications, you may have scenarios where there are more than one `ClassLoader`, and although you can access object instances between classes, you get `ClassCastExceptions` when you try to assign them into each other.

Places where this happens are things like dynamicaly-loaded plugins, web servers or other places where multiple applications are being loaded.

Typically, such scenarios look something like this:

![http://lh6.ggpht.com/_pK_-ru7xLs8/SyElQW6_0hI/AAAAAAAAAF8/bssuDEJJcrY/s800/standard_classloaders.png](http://lh6.ggpht.com/_pK_-ru7xLs8/SyElQW6_0hI/AAAAAAAAAF8/bssuDEJJcrY/s800/standard_classloaders.png)

There is a shared 'Root' `ClassLoader` whose classes can be loaded and assigned by all `sub-ClassLoaders`. However, classes created here know nothing about those loaded in sub-loaders.

The classes in `ClassLoader A` and `ClassLoader B` know nothing about each other, but the one way they can work together is via classes or interfaces that were loaded from the Root loader.

Even though both A and B have an interface called `CommonInterface`, the instance of that Class that each ClassLoader has is different, so they are considered to be different types.

# Proxies #

The simplest solution is to simply use classes or interfaces from the Root loader. That way, everyone can work with each other. However, particularly if this is a plugin scenario, sometimes you want to provide new classes that are used in more than one plugin. They provide new functionality that was not included in the base product.

In this case, one way is to use a [dynamic proxy](http://java.sun.com/j2se/1.4.2/docs/api/java/lang/reflect/Proxy.html). This is a service available since Java 1.4, and it allow you to create a new class instance that implements any interface classes you define, whos method calls are then managed by an [InvocationHandler](http://java.sun.com/j2se/1.4.2/docs/api/java/lang/reflect/InvocationHandler.html).

So, assuming that both sub-loaders have a copy of the same interface class, we can use a Proxy to pass the method calls from the B loader to the A loader (or vice versa) using the Reflection API.

It might look something like this:

![http://lh3.ggpht.com/_pK_-ru7xLs8/SyElQio3etI/AAAAAAAAAGA/xvgXlS6ynl0/s800/proxy_access.png](http://lh3.ggpht.com/_pK_-ru7xLs8/SyElQio3etI/AAAAAAAAAGA/xvgXlS6ynl0/s800/proxy_access.png)

In this case, the `ClassAProxy` implements the copy of `CommonInterface` available to `ClassLoader` B. It has a link to the original Object from `ClassLoader` A, but can't access any of its members directly. Instead, it listens for method calls and looks for the equivalent method in the original `ClassA` in `ClassLoader` A.

This means than any other classes in `ClassLoader` B can treat `ClassA` as if it were a local class, via the `ClassAProxy`.

# Facade #

The FacadeAssistant does pretty much this task. It is smart enough to only 'facade' objects that are not local, and they must also be marked as '@Facadable', to ensure that it's intentional. Essentially, instead of having to create the Proxy yourself, you can simply call this:

```
Object fromLoaderA = someWayToAccessTheClass();
CommonInterface inLoaderB = FacadeAssistant.getInstance().prepareObject( fromLoaderA, CommonInterface.class );
```

If you want to send your object into another `ClassLoader`, you can also prepare it in the other direction:

```
ClassLoader targetClassLoader = someOtherClassLoader();
CommonInterface inLoaderA = someLocalClassInstance();
Object forLoaderB = FacadeAssistant.getInstance().prepareObject( inLoaderA, targetClassLoader );
```

# Limitations #

There are several limitations with using the Facade API.

  1. In general, interfaces are required to have the '@Facadable' annotation in all class loaders.
  1. Generally, all parameters should also either be shared or facadable, or you will have issues when calling methods.
  1. Only interfaces can be proxied, so any 'facades' must be via interfaces, not classes.
  1. The interfaces in both `ClassLoaders` are assumed to be identical (or at least, the 'target' interface is a superset of the 'facade' interface). If a method is on the facade that doesn't exist in the 'wrapped' class, a runtime exception will be thrown.

This means that interfaces you want to be accessible across `ClassLoader` boundaries must be designed with care, so that only appropriate classes are allowed 'across the wall', so to speak. That's why only @Facadable interfaces are supported.

Typically, you will have a shared library that is loaded separately in both `ClassLoaders`. This insures that the classes will be identical in both locations. Then, other classes unique to each class loader implement the common interfaces.

Below is a simple example:

```
// Loaded into both ClassLoaders
@Facadable
public interface CommonInterface {
  public void doSomething( String withThis );
}

// Loaded into ClassLoader A
public class ClassA implements CommonInterface {
  public void doSomething( String withThis ) {
    // Do something...
  }
}

// Loaded into ClassLoader B
public class ClassB implements CommonInterface {
  public void doSomething( String withThis ) {
    // Do something else...
  }
}
```