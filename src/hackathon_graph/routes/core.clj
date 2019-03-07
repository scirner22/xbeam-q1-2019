(ns hackathon-graph.routes.core
  (:require [compojure.core :refer [defroutes GET POST OPTIONS]]
            [compojure.route :as route]
            [ring.util.response :refer [response]]
            [hackathon-graph.graph :as graph]))

(defn- inject-edge-ids
  [{:keys [a b p]}]
  (def a a)
  (def b b)
  (def p p)
  (assoc (first p) :sid (:id a) :tid (:id b)))

(defn get-all
  []
  (let [nodes (map :customer (graph/query graph/get-all-xbeam-customers))
        edges (graph/query graph/get-all-xbeam-customer-partnerships)]
    (response {:nodes nodes :edges edges})))

(defn get-node
  [node-id]
  (let [coll (graph/query graph/get-xbeam-customer {:id node-id})
        edges (map inject-edge-ids coll)])
  (response {:message node-id}))

(defroutes app
  (GET "/graph/" [] (get-all))
  (OPTIONS "/graph/" [] (response {}))
  (GET "/graph/:node_id" [node_id] (get-node (Integer/parseInt node_id)))
  (POST "/reseed" []
    (graph/seed-db)
    (response {:message "success!"}))
  (route/not-found (response {:error "route not found"})))
