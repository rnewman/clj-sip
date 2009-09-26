## Introduction 

This is a library for writing SIP servlets in Clojure.

In the `doc` directory you'll find some notes on its design (almost certainly
due for revision), as well as a little code for generating
[GraphViz](http://graphviz.org "GraphViz") dot files from Clojure hierarchies,
and checking that states have appropriate handlers defined.
 
There is an example servlet in the repository (which simply responds to a
'ping').

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
