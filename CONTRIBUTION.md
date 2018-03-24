AOP 
=====================
AspectJ is used to reduce the amount of code for cross-cutting concerns.
For example, this is the case for logging, stringifying objects.

Logging & Stringifying
---------------------
- edu.arizona.biosemantics.log.* contains the relevant AOP code
- Injects a log method into classes for convinient access.
- Injects trace logging
- Injects ability of objects to stringify themselves
-- Currently this is done using <a href="http://jackson.codehaus.org/">Jackson</a>, turning an object into a JSON representation.

Caveat
-------
If not carefully configured, the Stringify aspect may cause undesirable results with Jackson as underlying library. 
Per default, to turn an object into a String representation, Jackson is configured as described <a href="http://wiki.fasterxml.com/JacksonFeaturesSerialization">here</a>. Specifically problematic is the  "AUTO_DETECT_GETTERS" feature, which considers all public non-static no-argument methods as getters to obtain a serializable pice of information to use upon stringifying. That means, if an object would change an internal state by a method call that is by Jackson considered as a property getter, the internal state may be changed.

E.g. consider serialization of a <a href="https://docs.oracle.com/javase/7/docs/api/java/util/ListIterator.html?is-external=true">java.util.ListIterator</a>. Jackson would use the next method of the iterator for serialization causing a state change in the iterator.

To overcome the issue
- The Stringify aspect has to be configured to only be injected into safe classes
- Unsafe classes that are still to be injected the aspect may use @JsonIgnore on those methods
- the Jackson serialization configuration could overall be changed, however more annotation effort would be necessary on really strinifyable properties
- A number of other possiblities to configurate Jackson for own liking is for example described <a href="http://www.cowtowncoder.com/blog/archives/2011/02/entry_443.html">here</a>
- Maybe there exists a serialization library that manages to act more carefully?
- A toString method could be added manually on all classes contrary to using AOP for this purpose
