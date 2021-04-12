(ns cartagena.events
  (:require
   [cartagena.core :as c]
   [re-frame.core :as rf]))

(rf/reg-event-db
 :new-game
 (fn [db [_ players]]
   (-> db
       (assoc :game-state :playing
              :winner nil
              :game (c/init-game players)))))

(rf/reg-event-db
 :name
 (fn [db [_ name]]
   (assoc db :name name)))

(rf/reg-sub
 :name
 (fn [db _]
   (:name db)))