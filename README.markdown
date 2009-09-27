## Introduction 

This is a library for writing SIP servlets in Clojure.

Its primary goal is to simplify the writing of *stateful* SIP servlets, though
it can be applied to stateless servlets.

In short: a generated class extends `SipServlet`, dispatching incoming requests
and responses to Clojure multimethods. This allows you to write handler methods
for particular states, SIP methods, and SIP responses, rather than `switch`ing
on these values within a single `doInvite`-style method.

Abstraction is introduced by making both your states *and the SIP methods and
responses themselves* into members of *hierarchies* — for example, there are
nodes in the hierarchy for “subsequent requests”, “request methods defined in
RFC 3265”, “responses that should result in a retry with credentials”, and so
on. You can also define your own relationships to extend this default
hierarchy. (Read `src/com/twinql/clojure/sip/responses.clj` for more.)

You can thus handle arbitrary swathes of messages by using the hierarchy, not
verbose switching logic. Similarly, you are encouraged to introduce a hierarchy
in your states; this allows you to, *e.g.*, write separate methods for
in-dialog and out-of-dialog `INVITE`s without repeating (in each method
definition) which states are in or out of a dialog. Let the hierarchy do the
heavy lifting.
 
In the `doc` directory you'll find some notes on its design (almost certainly
due for revision), as well as a little code for generating
[GraphViz](http://graphviz.org "GraphViz") dot files from Clojure hierarchies,
and checking that states have appropriate handlers defined.
 
There is an example servlet in the repository (which simply responds to an
`INVITE` with a 200, waits for an `ACK`, then sends a `BYE`). Use this as a
reference when building your own servlets.

This is not a complicated or large library: it simply encodes a particular way
of doing things that has been shown to be convenient, as well as saving you the
effort of building a hierarchy of SIP requests and responses. Feedback is most
welcome.

## Dependencies

Just a SIP servlets implementation, such as SailFin or SIPMethod.

The `lib` directory contains necessary dependencies from the SailFin project.
You may substitute any SIP servlet implementation (altering the build file as
appropriate) and things should work. Indeed, the generated servlets should work
with any servlet container. This library has only been tested against SailFin.

## Deployment

Copy `clj-sip-servlet.jar` (and Clojure *etc.*) somewhere in your app server's
classpath (*e.g.*, `/opt/sailfin/lib/`). You'll have to restart your server for
this to take effect.

The example builds its AOT-compiled Clojure code into a jar separate from the
servlet class itself, which is packaged into a sar. If you wish you can build
your applications differently.

The jar must be on the classpath. The sar is deployed like any other SIP
application (*e.g.*, by copying to `/opt/sailfin/domains/domain1/autodeploy/`).

The script `test-deploy.sh` does this for the example application. See the
declarations at the top for paths you might change.
