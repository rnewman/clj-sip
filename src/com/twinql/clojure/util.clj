(ns com.twinql.clojure.util
  (:refer-clojure)
  (:import
     (java.io File)
     (java.util Scanner)
     (sun.misc BASE64Encoder)))

(defn between?
  "Inclusive lower, exclusive upper."
  [x min max]
  (and (<= min x)
       (<  x max)))

(defmacro unless [conditional & forms]
  `(when (not ~conditional)
     ~@forms))

(defn base64-encode [#^bytes x]
  (let [#^BASE64Encoder encoder (new BASE64Encoder)]
    (.encode encoder x)))

(defmacro with-scanner [[var filename] & body]
  `(let [~var (new Scanner (new File ~filename))]
     (try
       ~@body
       (finally
         (.close ~var)))))

(defmacro with-init-parameters [[[servlet config] & params] & body]
  (let [s (gensym)
        c (gensym)]
  `(let [~s ~servlet
         ~c ~config
         ~@(mapcat (if config
                     (fn [param]
                       [param `(.getInitParameter ~c ~(name param))])
                     (fn [param]
                       [param `(.getInitParameter ~s ~(name param))]))
                   params)]
     ~@body)))
