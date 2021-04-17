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

(rf/reg-sub
 :game-state
 (fn [db _]
   (:game-state db)))

(rf/reg-sub
 :game
 (fn [db _]
   (:game db)))

(rf/reg-sub
 :game/board
 :<- [:game]
 (fn [game _]
   (:board game)))

(rf/reg-sub
 :game/board-space-count
 :<- [:game/board]
 (fn [board _]
   (count board)))

(rf/reg-sub
 :game/board-space
 :<- [:game/board]
 (fn [board [_ i]]
   (nth board i)))

(rf/reg-sub
 :game/players
 :<- [:game]
 (fn [game _]
   (:players game)))

(rf/reg-sub
 :game/player-colors
 :<- [:game/players]
 (fn [players _]
   (take (count players) c/colors)))