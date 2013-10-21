(ns gitgraph.core
  (:require [clj-oauth2.client :as oauth2]
            [clojure.pprint :refer [pprint]]
            [cheshire.core :refer [parse-string]]))

(def access-token
  {:token-type 'oauth2
   :code       "access-token"})

(defn get-uri
  [uri]
  (oauth2/get (format "https://api.github.com%s"
                      uri)
              {:oauth2 access-token}))

(defn get-github-data
  [uri]
  (-> (get-uri uri)
      :body
      (parse-string true)))

(defn get-github-repos-data [login]
  (get-github-data (format "/users/%s/repos"
                           login)))

(defn repo-to-stats [repo]
  (hash-map (:full_name repo)
            (:watchers repo)))

(defn login-stats [login]
  (reduce merge
          (map repo-to-stats
               (get-github-repos-data login))))

(def projects
  (let [state (atom {})]
    (add-watch state
               :wlhn
               (fn [_ new _ _]
                 (pprint 
                  (take 5 (sort-by second > @new)))))
    state))

(defn do-it [user]
  (doseq [{:keys [login]
           :as follower} (get-github-data (format "/users/%s/followers"
                                                  user))]
    (println login)
    (swap! projects merge (login-stats login))
  (println "DONE!")))

#_(do-it "krisajenkins")
