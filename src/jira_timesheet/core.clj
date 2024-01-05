(ns jira-timesheet.core
  (:require
   [ring.adapter.jetty :refer [run-jetty]]
   [java-time.api :as jt]
   [compojure.core :refer [GET defroutes]]
   [compojure.route :as route]
   [jira-timesheet.view :as view]
   [jira-timesheet.jira :as jira]))

(declare server)

(defn index [_]
  (let [date (jt/local-date)
        issues (jira/get-timesheet date)]
    (view/timesheet-page issues date)))

(defroutes app
  (GET "/" [] index)
  (route/not-found "404 Not found"))

(when (bound? #'server)
  (.stop server))

(def server (run-jetty app
                       {:port 3030 :join? false}))

(defn -main [])

