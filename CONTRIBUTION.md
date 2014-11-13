AOP
---------------------
AspectJ is used to reduce the amount of code for cross-cutting concerns.
For example, this is the case for logging, stringifying objects.

Logging & Stringifying
---------------------
- edu.arizona.biosemantics.log.* contains the relevant AOP code
- Injects a log method into classes for convinient access.
- Injects trace logging
- Injects ability of objects to stringify themselves
-- Currently this is done using <a href="http://jackson.codehaus.org/">Jackson</a>

Caveat
-------
- If not carefully configured, the Stringify aspect may cause undesirable results with Jackson as underlying library. To turn an object into its String represtation Jackson .... .  such a (method call?) must not cause the object to change its internal state. 

JsonIgnore
Configure injection carefully

Library policies for method/field access (getters only?) Internal state?