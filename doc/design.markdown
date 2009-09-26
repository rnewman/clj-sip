# State-based SIP message processing

This document outlines a proposal for implementing stateful SIP processing by
combining Clojure’s multiple dispatch and ad-hoc hierarchies to simplify SIP
servlet development.


## Operation

The connection with SIP servlets is via a generated class. This class
implements the SIP servlet interface, participates in servlet configurations,
and implements the `doRequest`/`doResponse` methods.

These two methods destructure the incoming request, extract the current state
from the application session, and dispatch accordingly to generic functions.

(Clojure’s proxy facilities might be sufficient to avoid explicitly defining a
class. New new certainly would be. TBD.)

States are arranged in an arbitrary hierarchy, as well as a statechart. This
allows for generic handlers to be defined (for example, “any message arriving
in any state”) with suitable defaults, with more specific handlers (*e.g.*,
“`INVITE`s arriving in the `:init` state”) providing application-specific
behavior.

The truly default implementation ties back in to SIP servlets, calling the
appropriate super method.

Note that the use of an arbitrary hierarchy could result in unexpected behavior
if adequate forethought is not given to the design. As always, software isn’t
easy.


# Application design

The task of building a SIP application in this model decomposes into two parts:

* Design the state hierarchy. States form two orthogonal arrangements: the
transition diagram for the system (such as `:init` → `:connecting` →
`:connected`), and the handling hierarchy. The transition diagram models the
application flow; the handling hierarchy is an abstraction mechanism which
allows for messages arriving whilst the application is in various states to be
handled in a similar way.

* Implement the SIP handling methods which generate appropriate requests and
responses and transition the application between states.

The primary distinction between this approach and conventional SIP servlets
(beyond the obvious expressiveness improvements of a more powerful language) is
that relationships between states can be explicitly defined; indeed, additional
states can be introduced purely to represent relationships between “leaf”
states. In a Java SIP servlet, states are typically enumerations or strings;
clustering these states into super-states, or otherwise introducing generic
behavior, is unnatural.

One additional advantage is offered, however. SIP messages themselves can
participate in an ad-hoc hierarchy; the 200-class responses are success
responses, `ACK`s and `PRACK`s belong to one category, and so on. This allows
handling to be generic without cumbersome comparisons against the message
method string.

Of course, application code can itself take advantage of multiple dispatch to
redistribute complexity. For example, an `INVITE`-handling method might extract
the request URI, and use some dispatch function over request URIs to have
another method handle inbound `INVITE`s in an extensible fashion.


## Example

Here is a trivial example of how multiple dispatch would work. I have
deliberately excluded ‘real’ SIP servlet classes and methods; the comments
should explain.

(This code corresponds quite closely to what is actually implemented in this
library.)

First we define our dispatch function. This takes the arguments to the method
and returns a vector upon which to dispatch through `isa?`. We want to dispatch
on the servlet class (perhaps not in the future… that’s what packages and
class generation are for!), the current state, and the request method.


    (defn msg-dispatcher [servlet app-session message]
      [(:class servlet)      ; Will be (class servlet).
       (:state app-session)  ; Will be getAttribute with a cast.
       (:method message)])   ; Will be getMethod() interned as a keyword.


Then we can define the multimethod. This is analogous to Common Lisp’s
`defgeneric`, but specifies the dispatch function we want to use.


    (defmulti do-request msg-dispatcher)


Since all of our do-request methods will have the same argument list, we define
a helper macro. The unquote-quote trick is necessary to break hygiene,
introducing anaphora.


    (defmacro defrequest [specializers & body]
      `(defmethod do-request ~specializers [~'servlet ~'app-session ~'message]
         ~@body))


Now we can define two example methods. The first matches all INVITEs in the
initial state.


    (defrequest [java.lang.Object ::init ::invite]
      (print (str "Got an INVITE in state " (:state app-session))))


The second is a default.


    (defrequest :default
      (print (str "Got some other message in state "
                  (:state app-session) ": " message)))


Now a moment in the REPL will demonstrate dispatch. Maps are used to fake
objects, making dispatch and demonstration simpler.


    Clojure=> (do-request {:class java.lang.Object}
                          {:state ::init}
                          {:method ::invite :foo ::bar})
    Got an INVITE in state :user/init
    nil

    Clojure=> (do-request {:class java.lang.Object}
                          {:state ::later}
                          {:method ::invite :foo ::bar})
    Got some other message in state :user/later:
    {:method :user/invite, :foo :user/bar}
    nil


Two more forms will demonstrate how hierarchy can be introduced for
abstraction. Imagine the initial state can be split into two: `::init1` and
`::init2`. We want both of these to behave the same with respect to handling
`INVITE`s.

    
    (derive ::init1 ::init) (derive ::init2 ::init)


And to demonstrate we supply an `INVITE` when in state `::init2`:


    Clojure=> (do-request {:class java.lang.Object}
                          {:state ::init2}
                          {:method ::invite :baz 5})
    Got an INVITE in state :user/init2
    nil


## Similarities

The astute reader will note the similarities to CCXML: CCXML applications are
defined in terms of *transitions*, which match in a generic way against
prefixed states and events.

This is slightly more convenient than expressing the hierarchy of states
explicitly, but conversely does not allow the introduction of arbitrary
hierarchy. Furthermore, a macro definition which permits the automatic
production of a hierarchy from a CCXML-esque transition signature is within the
realm of possibility. One might even go so far as to automatically generate
Clojure code from CCXML.
