(defproject hackathon-graph "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.codec "0.1.1"]
                 [nrepl "0.6.0"]
                 [gorillalabs/neo4j-clj "2.0.1"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-json "0.4.0"]
                 [jumblerg/ring-cors "2.0.0"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [org.clojure/tools.cli "0.4.1"]
                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]
                 [clj-time "0.15.1"]
                 [com.cemerick/url "0.1.1"]]
  :main ^:skip-aot hackathon-graph.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[cljfmt "0.6.4"]]
                   :plugins [[cider/cider-nrepl "0.20.0"]
                             [lein-ancient "0.6.15"]
                             [refactor-nrepl "2.4.0"]]}})

