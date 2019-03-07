(ns hackathon-graph.graph
  (:require [neo4j-clj.core :as db]
            [hackathon-graph.load :as seed]))

(def local-db
  (db/connect "bolt://localgraph:7687" "neo4j" "password"))

(db/defquery create-xbeam-customer
  "CREATE (c:customer $customer)")

(db/defquery get-all-xbeam-customers
  "MATCH (c:customer) RETURN c as customer")

(db/defquery get-xbeam-customer
  "MATCH (a:customer{id: $id})-[p:PARTNER*1..2]-(b:customer) return a, b, p")

(db/defquery get-all-xbeam-customer-partnerships
  "MATCH (a:customer)-[p:PARTNER]->(b:customer)
     RETURN a.id as sid, b.id as tid, p as partnership")

(db/defquery create-xbeam-customer-partnership
  "MATCH (a:customer),(b:customer)
     WHERE a.name = $a_name AND b.name = $b_name
   CREATE (a)-[r:PARTNER $partner]->(b)
     RETURN type(r)")

(db/defquery delete-all
  "MATCH (n)
     DETACH DELETE n")

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
                                          :b_name r}))))

(comment
  (query get-all-xbeam-customers)
  (query get-all-xbeam-customer-partnerships)

  (seed-db)
)
