(ns cartagena.events
  (:require
   [re-frame.core :as rf]))

(rf/reg-event-db
 :name
 (fn [db [_ name]]
   (assoc db :name name)))

(rf/reg-sub
 :name
 (fn [db _]
   (:name db)))