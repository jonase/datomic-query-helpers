(defproject datomic-query-helpers "0.1.0"
  :description "A few helper functions for working with Datomic queries"
  :url "https://github.com/jonase/datomic-query-helpers"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.cemerick/clojurescript.test "0.0.4"]]
  :plugins [[com.keminglabs/cljx "0.3.0"]]
  :source-paths ["src/clojure" "src/cljs"]
  :test-paths ["test/clojure" "test/cljs"]
  :cljsbuild {:builds [{:source-paths ["src/cljs" "test/cljs"]
                        :compiler {:output-to "target/cljs/testable.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "src/clojure"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "src/cljs"
                   :rules :cljs}
                  {:source-paths ["test/cljx"]
                   :output-path "test/clojure"
                   :rules :clj}
                  {:source-paths ["test/cljx"]
                   :output-path "test/cljs"
                   :rules :cljs}]})
