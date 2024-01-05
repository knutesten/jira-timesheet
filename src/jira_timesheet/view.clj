(ns jira-timesheet.view
  (:require
   [hiccup.page :refer [html5 include-js]]
   [java-time.api :as jt]))

(defn layout [& children]
  (html5
   (include-js "https://cdn.tailwindcss.com")
   [:div.p-4.relative
    children]))

(defn cell [& children]
  (vec
   (concat [:span.w-12.p-1.shrink-0.text-center.border-r.last:border-r-0.border-t.border-slate-300] children)))

(defn timesheet-page [issues date]
  (layout
   [:div.flex
    [:div.flex.flex-col
     [:div.shrink-0.w-64.border-r.border-l.border-t.border-slate-300.text-center.p-1.font-bold "Dag"]
     (for [{:keys [key href summary]} issues]
       [:div.shrink-0.w-64.border-r.border-l.border-t.border-slate-300.text-ellipsis.whitespace-nowrap.overflow-hidden.p-1.hover:underline
        [:a {:href href :title summary} key " - " summary]])
     [:div.shrink-0.w-64.border-r.border-l.border-b.border-t.border-slate-300.text-center.p-1.font-bold "Sum"]]
    [:div.border-b.border-r.border-slate-300.overflow-auto
     [:div.flex
      (for [day (range 1 (inc (.lengthOfMonth date)))]
        (cell {:class
               (str "font-bold "
                    (when (jt/weekend? (.withDayOfMonth date day))
                      "bg-slate-100"))}
              day))]

     (for [{worklogs :worklogs} issues]
       [:div.flex.w-fit.hover:bg-green-50
        (for [day (range 1 (inc (.lengthOfMonth date)))]
          (let [d (.withDayOfMonth date day)]
            (cell {:class
                   (str "hover:bg-green-100 bg-opacity-50 "
                        (when (jt/weekend? d)
                          "bg-slate-200"))}
                  (get worklogs d))))])
     [:div.flex
      (for [day (range 1 (inc (.lengthOfMonth date)))]

        (let [d (.withDayOfMonth date day)]
          (cell {:class
                 (str "font-bold hover:bg-green-100 bg-opacity-50 "
                      (when (jt/weekend? d)
                        "bg-slate-200"))}
                (reduce + (map #(or (get-in % [:worklogs d]) 0) issues)))))]]

    [:div.flex.flex-col.items-end
     [:div.w-14.p-1.shrink-0.border-t.border-r.border-slate-300 "&nbsp;"]
     (for [issue issues]
       [:div.w-14.p-1.shrink-0.text-center.border-t.border-r.border-slate-300.font-bold
        (reduce + (vals (:worklogs issue)))])

     [:div.w-14.p-1.shrink-0.text-center.border-t.border-r.border-b.border-slate-300.font-bold
      (reduce + (flatten (map #(vals (:worklogs %)) issues)))]]]))
