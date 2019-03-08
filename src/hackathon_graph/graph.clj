(ns hackathon-graph.graph
  (:require [neo4j-clj.core :as db]
            [hackathon-graph.load :as seed]))

(def local-db
  (db/connect "bolt://localgraph:7687" "neo4j" "password"))

(db/defquery create-xbeam-customer
  "CREATE (c:customer $customer)")

(db/defquery get-all-xbeam-customers
  "MATCH (c:customer) RETURN c as customer, labels(c) as labels")

(db/defquery get-xbeam-customer-node
  "MATCH (a:customer{id: $id})
     RETURN a as customer")

(db/defquery get-xbeam-customer
  "MATCH (a{id: $id})-[p1]-(b)
     OPTIONAL MATCH (b)-[p2]-(c)
      RETURN a, b, c, p1, p2")

(db/defquery get-all-xbeam-customer-partnerships
  "MATCH (a)-[l]->(b)
     RETURN a.id as sid, b.id as tid, l as link, type(l) as type")

(db/defquery create-xbeam-customer-partnership
  "MATCH (a:customer),(b:customer)
     WHERE a.name = $a_name AND b.name = $b_name
   CREATE (a)-[r:PARTNER $partner]->(b)
     RETURN type(r)")

(db/defquery create-xbeam-customer-prospect-partnership
  "MATCH (a:customer),(b:prospect)
     WHERE a.name = $a_name AND b.name = $b_name
   CREATE (a)-[r:OPPORTUNITY]->(b)
     RETURN type(r)")

(db/defquery create-xbeam-prospect-partnership
  "MATCH (a:prospect),(b:prospect)
     WHERE a.name = $a_name AND b.name = $b_name
   CREATE (a)-[r:OPPORTUNITY]->(b)
     RETURN type(r)")

(db/defquery create-xbeam-opportunity
  "MATCH (a),(b)
     WHERE a.name = $a_name AND b.name = $b_name
   CREATE (a)-[r:OPPORTUNITY $opp]->(b)
     RETURN type(r)")

(db/defquery get-all-xbeam-prospects
  "MATCH (p:prospect) RETURN p as prospect")

(db/defquery create-xbeam-prospects
  "CREATE (p:prospect $prospect)")

(db/defquery create-xbeam-lead
  "CREATE (l:lead $lead)")

(db/defquery get-all-nodes
  "MATCH (n) RETURN n as node")

(db/defquery delete-all
  "MATCH (n)
     DETACH DELETE n")

(db/defquery connected-non-customers
  "MATCH (n:prospect)-[r]-()
     RETURN n as node, labels(n) as labels
   UNION MATCH (n:lead)-[r]-()
     RETURN n as node, labels(n) as labels")

(defn query
  ([func]
   (with-open [session (db/get-session local-db)]
     (func session)))
  ([func opts]
   (with-open [session (db/get-session local-db)]
     (func session opts))))

(defn seed-db
  []
  (with-open [session (db/get-session local-db)]
    (delete-all session)
    (doseq [node (seed/org-nodes)]
      (create-xbeam-customer session {:customer node}))
    (doseq [[l r props] (seed/partner-rels)]
      (create-xbeam-customer-partnership session
                                         {:partner props
                                          :a_name l
                                          :b_name r})))
    (let [all-customers (map :customer (query get-all-nodes))
          all-customers-lookup (group-by :name all-customers)
          all-prospects (map :prospect (query get-all-xbeam-prospects))
          all-prospects-lookup (group-by :name all-prospects)
          counter (atom 5000)]
    (doseq [node (seed/hubspot-nodes)]
      (when (not (contains? all-customers-lookup (:name node)))
      (with-open [session (db/get-session local-db)]
      (create-xbeam-prospects session {:prospect node}))))
    (doseq [[l r] (seed/hubspot-edges)]
      (if (and (or (contains? all-customers-lookup l) (contains? all-prospects-lookup l))
          (or (contains? all-customers-lookup r) (contains? all-prospects-lookup r)))
        (with-open [session (db/get-session local-db)]
          (create-xbeam-opportunity session {:a_name l :b_name r :opp {:_color "#fcb827"}}))
        (do
          (with-open [session (db/get-session local-db)]
            (when (not (contains? (set (mapv #(get-in % [:node :name]) (get-all-nodes session))) l))
              (create-xbeam-lead session {:lead {:_color "#b5b5b5" :name l :id (swap! counter inc)}}))
            (when (not (contains? (set (mapv #(get-in % [:node :name]) (get-all-nodes session))) r))
              (create-xbeam-lead session {:lead {:_color "#b5b5b5" :name r :id (swap! counter inc)}}))
            (create-xbeam-opportunity session {:a_name l :b_name r :opp {:_color "#b5b5b5"}})))))))

(comment
  (query get-all-xbeam-customer-partnerships)

  (seed-db)

  (with-open [session (db/get-session local-db)]
    (doseq [[l r] (seed/hubspot-edges)]
      (println (not (contains? (mapv #(get-in % [:lead :name]) (get-all-nodes session)) l)))))

    (with-open [session (db/get-session local-db)]
      (contains? (mapv #(get-in % [:lead :name]) (get-all-nodes session)) l))

    (with-open [session (db/get-session local-db)]
      (delete-all session))

    (doseq [node (seed/org-nodes)]
      (with-open [session (db/get-session local-db)]
        (create-xbeam-customer session {:customer node})))

    (doseq [[l r props] (seed/partner-rels)]
      (with-open [session (db/get-session local-db)]
        (create-xbeam-customer-partnership session
                                           {:partner props
                                            :a_name l
                                            :b_name r})))
    (doseq [node (seed/hubspot-nodes)]
      (with-open [session (db/get-session local-db)]
        (create-xbeam-prospects session {:prospect node}))
      )

  (doseq [[l r] (seed/hubspot-edges)]
    (with-open [session (db/get-session local-db)]
    (if (and (or (contains? all-customers-lookup l) (contains? all-prospects-lookup l))
        (or (contains? all-customers-lookup r) (contains? all-prospects-lookup r)))
      (create-xbeam-opportunity session {:a_name l :b_name r :opp {:_color "#fcb827"}})
      (do
        (create-xbeam-lead session {:lead {:_color "#b5b5b5" :name l}})
        (create-xbeam-lead session {:lead {:_color "#b5b5b5" :name r}})
        (create-xbeam-opportunity session {:a_name l :b_name r :opp {:_color "#b5b5b5"}})))))
)
