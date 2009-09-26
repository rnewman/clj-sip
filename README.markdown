This is a library for writing SIP servlets in Clojure.

In the doc directory you'll find some notes on its design, as well as a little
code for generating GraphViz dot files from hierarchies, and checking that 
states have appropriate handlers defined.

There is an example servlet in the repository (which simply responds to a
'ping').

The `lib` directory contains necessary dependencies from the SailFin project.
You may substitute any SIP servlet implementation (altering the build file as
appropriate) and things should work. Indeed, the generated servlets should work
with any servlet container.
