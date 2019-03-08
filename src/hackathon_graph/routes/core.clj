(ns hackathon-graph.routes.core
  (:require [compojure.core :refer [defroutes GET POST OPTIONS]]
            [compojure.route :as route]
            [ring.util.response :refer [response]]
            [hackathon-graph.graph :as graph]))

(defn- inject-edge-ids
  [{:keys [a b c p1 p2] :as row}]
  (let [p1-new (assoc p1 :sid (:id a) :tid (:id b))
        p2-new (assoc p2 :sid (:id b) :tid (:id c))]
    (assoc row :p1 p1-new :p2 p2-new)))

(defn get-all
  []
  (let [nodes (map :customer (graph/query graph/get-all-xbeam-customers))
        edges (graph/query graph/get-all-xbeam-customer-partnerships)]
    (response {:nodes nodes :edges edges})))

(defn get-node
  [node-id]
  (let [coll (graph/query graph/get-xbeam-customer {:id node-id})
        temp (map inject-edge-ids coll)
        nodes (->> temp
                   (map #(select-keys % [:a :b :c]))
                   (map vals)
                   flatten
                   set)
        edges (->> temp
                   (map #(select-keys % [:p1 :p2]))
                   (map vals)
                   flatten
                   set)]
    (if (seq nodes)
      (response {:nodes nodes :edges edges})
      ; node has no partners - return set
      (response {:nodes (->> {:id node-id}
                             (graph/query graph/get-xbeam-customer-node)
                             (map :customer))
                 :edges edges}))))

(defroutes app
  (GET "/graph/" [] (get-all))
  (GET "/graph/:node_id" [node_id] (get-node (Integer/parseInt node_id)))
  (POST "/reseed" []
    (graph/seed-db)
    (response {:message "success!"}))
  (route/not-found (response {:error "route not found"})))
