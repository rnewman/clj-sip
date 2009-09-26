DOMAIN=domain1
SAILFIN=/opt/sailfin
CLOJURE=/opt/clojure/clojure.jar
CONTRIB=/opt/clojure-contrib/clojure-contrib.jar

ant clean examplesar
$SAILFIN/bin/asadmin stop-domain $DOMAIN
cp deploy/clj-echo-servlet-code.jar deploy/clj-sip-servlet.jar $CLOJURE $CONTRIB $SAILFIN/lib/
cp clj-echo-servlet.sar $SAILFIN/domains/$DOMAIN/autodeploy/
$SAILFIN/bin/asadmin start-domain $DOMAIN
