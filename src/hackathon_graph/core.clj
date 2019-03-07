(ns hackathon-graph.core
  (:require [nrepl.server :as nrepl]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.json :refer [wrap-json-response]]
            [jumblerg.middleware.cors :refer [wrap-cors]]
            [hackathon-graph.routes.core :refer [app]])
  (:gen-class))

(defonce http-server (atom nil))
(defonce nrepl-server (atom nil))
(def nrepl-port-file ".nrepl-port")

(defn start-http-server
  []
  (reset! http-server (run-server (wrap-cors (wrap-json-response #'app) #".*") {:port 3000})))

(defn stop-http-server
  []
  (when-not (nil? @http-server)
    (@http-server :timeout 100)
    (reset! http-server nil)))

(defn run
  []
  (reset! nrepl-server (nrepl/start-server :bind "0.0.0.0" :port 3001))
  (println "Started nrepl server at 3001")
  (start-http-server)
  (println "Started http server at 3000"))

(defn -main
  [& args]
  (run))
