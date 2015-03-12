# facade
_Punches holes through Java classloader walls._

This library facilitates allowing Java object instances which have the same class files loaded by separate class loaders in the same VM to communicate.

When would this situation arise, I hear you ask? Good question. The original situation this was written to address was an application which allowed dynamic uploading and initialisation of jar files while the application was running. Each jar file was loaded in its own class loader, which had access to the shared application classes, but not to classes inside other plugins.

This library gets around this limitation by using a combination of proxying and reflection, creating a facade for known objects created by foreign class loaders.

See the [Home Page](https://github.com/randombits-org/facade/wiki/Release-History) for more details.
