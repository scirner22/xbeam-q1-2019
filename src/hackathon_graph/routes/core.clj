(ns hackathon-graph.routes.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :refer [response]]
            [hackathon-graph.graph :as graph]))

(defn get-all
  []
  (let [nodes (map :customer (graph/query graph/get-all-xbeam-customers))
        edges (graph/query graph/get-all-xbeam-customer-partnerships)]
    (response {:nodes nodes
               :edges edges})))

(defroutes app
  (GET "/" [] (get-all))
  (POST "/reseed" []
    (graph/seed-db)
    (response {:message "success!"}))
  (route/not-found (response {:error "route not found"})))
