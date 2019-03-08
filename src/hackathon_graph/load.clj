(ns hackathon-graph.load
  (:require [clojure.string :as str]))

(def org-lines
  (-> (slurp "/data/orgs.dump")
      (str/split #"\n")))

(def partner-lines
  (-> (slurp "/data/partners.dump")
      (str/split #"\n")))

(def hubspot-lines
  (-> (slurp "/data/hubspot.dump")
      (str/split #"\n")))

(def hubspot-edges-lines
  (-> (slurp "/data/hubspot_edges.dump")
      (str/split #"\n")))

(defn split-rows
  [coll]
  (map #(str/split % #",") coll))

(defn orgs
  []
  (let [coll (split-rows org-lines)
        columns [:_color :id :name :crunchbase_url :url :onboarded_since :since]]
    (map #(apply assoc {} (interleave columns %)) coll)))

(defn org-nodes
  []
  (map #(assoc % :id (-> % :id Integer/parseInt)) (orgs)))

(defn hubspot
  []
  (let [coll (split-rows hubspot-lines)
        columns (map keyword (first coll))
        data (rest coll)]
    (map #(apply assoc {} (interleave columns %)) data)))

(defn hubspot-nodes
  []
  (map #(assoc %2 :id %1) (range 100 10000 1) (hubspot)))

(defn hubspot-edges
  []
  (split-rows hubspot-edges-lines))

(defn partnerships
  []
  (let [coll (split-rows partner-lines)
        columns [:_color :since :updated_at :id :org_id :partner_org_id]]
    (->> coll
         (map #(apply assoc {} (interleave columns %)))
         (map #(dissoc % :updated_at :id)))))

(defn partner-rels
  []
  (let [org-lookup (group-by :id (orgs))]
    (->> (partnerships)
         (map (fn [p]
                (let [lhs (->> p :org_id (get org-lookup) first :name)
                      rhs (->> p :partner_org_id (get org-lookup) first :name)]
                  [lhs rhs p])))
         (map (fn [[l r props]] [l r (dissoc props :org_id :partner_org_id)])))))

