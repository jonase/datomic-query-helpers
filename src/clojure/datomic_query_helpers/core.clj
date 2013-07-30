(ns datomic-query-helpers.core
  (:require [clojure.string :as s]
            [clojure.set :as set]
                  [clojure.edn :as edn]
                                        
            [clojure.walk :as walk]))

(defn- normalize-vector [query result]
  (if (seq query)
    (let [key (first query)
          [val rest] (split-with (complement keyword?) (rest query))]
      (recur rest (assoc result key val)))
    result))

(defn normalize 
  "Normalize a query to its map form."
  [query]
  (cond
   (string? query) (normalize (edn/read-string query))
   (vector? query) (normalize-vector query {})
   (map? query) (select-keys query [:find :with :in :where])))

(defn pretty-query-string
  [query]
  (let [query (normalize query)]
    (str "[" 
         (when-let [clause (:find query)]
           (format ":find %s\n " (s/join " " clause)))
         (when-let [clause (:with query)]
           (format ":with %s\n " (s/join " " clause)))
         (when-let [clause (:in query)]
           (format ":in %s\n " (s/join " " clause)))
         (when-let [clause (:where query)]
           (format ":where\n %s" (s/join "\n " clause)))
         "]")))

(defn sym-starts-with? [sym ch]
  (and (nil? (namespace sym))
       (= (first (name sym)) ch)))

(defn pattern-variable? [sym]
  (sym-starts-with? sym \?))

(defn database-arg? [sym]
  (sym-starts-with? sym \$))

(defn safe-sym? [sym]
  (or (pattern-variable? sym)
      (database-arg? sym)
      (contains? '#{% _ ...} sym)))

(defn check-whitelist 
  "Returns a set of symbols that is not in the whitelist"
  [query whitelist]
  (assert (set? whitelist) "Whitelist must be a set of symbols")
  (let [query (normalize query) 
        syms (atom #{})]
    (walk/prewalk
     (fn [x]
       (when (and (symbol? x)
                  (not (safe-sym? x))
                  (not (whitelist x)))
         (swap! syms conj x))
       x)
     query)
    @syms))

(defn whitelist-safe?
  "Returns true if only symbols in whitelist is used in query"
  [query whitelist]
  (empty? (check-whitelist query whitelist)))

(defn check-query 
  "Check the query + args for non-whitelist symbols"
  [args whitelist]
  (apply set/union (map #(check-whitelist % whitelist) args)))

(defn safe-query? [args whitelist]
  (empty? (check-query args whitelist)))

;;;;;;;;;;;; This file autogenerated from src/cljx/datomic_query_helpers/core.cljx
