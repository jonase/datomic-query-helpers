(ns datomic-query-helpers.core-test
  (:require #+clj [clojure.test :refer :all]
            [datomic-query-helpers.core :refer (normalize whitelist-safe? check-whitelist)])
  #+cljs (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)]))

(deftest normalize-test
  (testing "normalize"
    (let [q1 "[:find ?e :where [?e :foo/bar ?bar]]"
          q2 '[:find ?e :where [?e :foo/bar ?bar]]
          q3 '{:find [?e] :where [[?e :foo/bar ?bar]]}]
      (is (= (normalize q1) q3))
      (is (= (normalize q2) q3))
      (is (= (normalize q3) q3)))

    (let [q1 "[:find ?e (max ?b) :in $ ?f :where [?e :foo/bar ?bar]]"
          q2 '[:find ?e (max ?b) :in $ ?f :where [?e :foo/bar ?bar]]
          q3 '{:find [?e (max ?b)] :in [$ ?f] :where [[?e :foo/bar ?bar]]}]
      (is (= (normalize q1) q3))
      (is (= (normalize q2) q3))
      (is (= (normalize q3) q3)))

    (let [q1 "[:find ?e (max ?b) 
               :with ?baz 
               :in $ ?f 
               :where 
               [?e :foo/bar ?bar]
               [(< ?bar 10)]]"
          q2 '[:find ?e (max ?b) 
               :with ?baz 
               :in $ ?f 
               :where 
               [?e :foo/bar ?bar]
               [(< ?bar 10)]]
          q3 '{:find [?e (max ?b)] 
               :with [?baz] 
               :in [$ ?f] 
               :where [[?e :foo/bar ?bar]
                       [(< ?bar 10)]]}]
      (is (= (normalize q1) q3))
      (is (= (normalize q2) q3))
      (is (= (normalize q3) q3)))))

(deftest whitelist-test
  (testing "whitelist"
    (let [query '[:find ?a (max ?b) 
                  :with ?c 
                  :in $ $old % [[?a ?b]] [?c ...] 
                  :where 
                  [_ :foo/bar ?a]
                  [(.toUpper ?b) ?e]
                  [(< ?x 10)]]
          rules '[[[foo ?bar]
                   [(eval ?bar)]]]]
      (is (= (check-whitelist query '#{}) '#{max .toUpper <}))
      (is (= (check-whitelist query '#{.toUpper <}) '#{max}))
      (is (= (check-whitelist query '#{max .toUpper <}) '#{}))
      (is (false? (whitelist-safe? query '#{})))
      (is (false? (whitelist-safe? query '#{.toUpper <})))
      (is (true? (whitelist-safe? query '#{max .toUpper <})))
      (is (false? (whitelist-safe? [query rules] '#{})))
      (is (true? (whitelist-safe? [query rules] '#{max .toUpper < eval foo}))))))
