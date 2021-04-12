(ns cartagena.app
  (:require [cartagena.events]
            [clojure.string :as str]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]))

(defn player-input [a ph]
  [:input {:type "text"
           :placeholder ph
           :value @a
           :on-change #(reset! a (-> % .-target .-value))}])

(defn new-game []
  (let [player1 (r/atom nil)
        player2 (r/atom nil)
        player3 (r/atom nil)
        player4 (r/atom nil)
        player5 (r/atom nil)]
    (fn []
      [:div.player-input
       [:div
        [:h1 "Cartagena-cljs"]
        [:p "Enter player names:"]
        [player-input player1 "Player one (required)"]
        [player-input player2 "Player two (required)"]
        [player-input player3 "Player three"]
        [player-input player4 "Player four"]
        [player-input player5 "Player five"]
        [:input.btn {:type     "button"
                     :value    "Start Game"
                     :disabled (or (str/blank? @player1)
                                   (str/blank? @player2))
                     :on-click #(rf/dispatch [:new-game (->> [@player1 @player2 @player3 @player4 @player5]
                                                             (filter (comp not str/blank?)))])}]]])))

(defn page []
  [:div
   [new-game]])

(rdom/render [#'page]
             (.getElementById js/document "app"))