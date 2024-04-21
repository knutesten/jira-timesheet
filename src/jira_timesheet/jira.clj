(ns jira-timesheet.jira
  (:require
   [clojure.edn :as edn]
   [clj-http.client :as client]
   [cheshire.core :as json]
   [java-time.api :as jt]))

(defn first-day [date]
  (jt/adjust date :first-day-of-month))

(defn last-day [date]
  (jt/adjust date :last-day-of-month))

(def config (edn/read-string (slurp "config.edn")))

(def basic-auth [(:email config) (:api-key config)])

(defn get-issues [date]
  (-> (client/post
       (str (:host config) "/rest/api/2/search")
       {:basic-auth basic-auth
        :body (json/generate-string
               {:jql (str "worklogAuthor=currentUser() AND worklogDate>= "
                          (first-day date)
                          " AND worklogDate <= "
                          (last-day date))
                :fields ["key" "summary" "worklog"]
                :maxResults 100})
        :content-type :json
        :as :json})
      :body
      :issues))

(defn get-worklogs [issue-id]
  (-> (client/get
       (str (:host config) "/rest/api/2/issue/" issue-id "/worklog")
       {:basic-auth basic-auth
        :as :json})
      :body
      :worklogs))

(defn is-valid-worklog? [date worklog]
  (let [email (-> worklog :author :emailAddress)
        worklog-date (-> worklog :started (subs 0 10) jt/local-date)]
    (and (= email (:email config))
         (= (jt/year-month worklog-date) (jt/year-month date)))))

(defn seconds->hours [seconds]
  (double (/ seconds 3600)))

(defn fetch-missing-worklogs [issue]
  (assoc issue :worklogs
         (if (> (-> issue :fields :worklog :total)
                (-> issue :fields :worklog :maxResults))
           (get-worklogs (:id issue))
           (-> issue :fields :worklog :worklogs))))

(defn issue->domain [date issue]
  {:key (:key issue)
   :summary (-> issue :fields :summary)
   :href (str (:host config) "/browse/" (:key issue))
   :worklogs
   (->> (:worklogs issue)
        (filter (partial is-valid-worklog? date))
        (reduce #(update
                  %1
                  (jt/local-date (subs (:started %2) 0 10))
                  (fnil + 0)
                  (seconds->hours (:timeSpentSeconds %2)))
                {}))})

(defn get-worklogs-for-issues [date issues]
  (->> issues
       (map fetch-missing-worklogs)
       (map (partial issue->domain date))
       (filter #(-> % :worklogs not-empty))))

(defn get-timesheet [date]
  (->> (get-issues date)
       (get-worklogs-for-issues date)
       (sort-by :key)))


