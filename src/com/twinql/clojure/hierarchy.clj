(ns com.twinql.clojure.hierarchy
  (:refer-clojure))

;; Utilities for manipulating a global hierarchy.
(defn deriving-form [hierarchy [[parent & children] & remaining]]
  (letfn [(derive-form 
            [hier parent [child & remaining-children]]
            (if child
              `(derive ~(derive-form hier parent remaining-children)
                       ~child ~parent)
              hier))]
    (if parent
      (derive-form
        (deriving-form hierarchy remaining)
        parent children)
      hierarchy)))

(defmacro deriving
  "Returns a new hierarchy."
  [hierarchy & forms]
  (deriving-form hierarchy forms))
  
(defmacro deriving!
  "Alters the provided hierarchy, which must be a ref."
  [hierarchy & forms]
  (let [hier (gensym)]
    `(dosync
       (alter ~hierarchy
              (fn [~hier]
                ~(deriving-form hier forms))))))
