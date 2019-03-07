(ns hackathon-graph.load
  (:require [clojure.string :as str]))

(def org-lines
  (-> (slurp "/data/orgs.dump")
      (str/split #"\n")))

(def partner-lines
  (-> (slurp "/data/partners.dump")
      (str/split #"\n")))

(defn split-rows
  [coll]
  (map #(str/split % #",") coll))

(defn orgs
  []
  (let [coll (split-rows org-lines)
        columns [:id :name :crunchbase_url :url :onboarded_since :since]]
    (->> coll
         (map #(apply assoc {} (interleave columns %))))))

(defn org-nodes
  []
  (map #(dissoc % :id) (orgs)))

(defn partnerships
  []
  (let [coll (split-rows partner-lines)
        columns [:since :updated_at :id :org_id :partner_org_id]]
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

