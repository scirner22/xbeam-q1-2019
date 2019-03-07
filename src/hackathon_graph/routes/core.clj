(ns hackathon-graph.routes.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :refer [response]]
            [hackathon-graph.graph :as graph]))

(defroutes app
  (GET "/" [] (response {:get "all"}))
  (POST "/reseed" []
    (graph/seed-db)
    (response {:message "success!"}))
  (route/not-found (response {:error "route not found"})))
