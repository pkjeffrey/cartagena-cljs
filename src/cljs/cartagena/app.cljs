(ns cartagena.app
  (:require [cartagena.events]
            [clojure.string :as str]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]))

(defn player-input [players id]
  [:input {:type :text
           :value (get @players id)
           :on-change #(swap! players assoc id (-> % .-target .-value))}])

(defn new-game []
  (let [players (r/atom {})]
    (fn []
      [:div.player-input
       [:div
        [:h1 "Cartagena-cljs"]
        [:p "Enter player names:"]
        (for [i (range 1 6)]
          ^{:key i} [player-input players i])
        [:input.btn {:type     :button
                     :value    "Start Game"
                     :disabled (or (str/blank? (get @players 1))
                                   (str/blank? (get @players 2)))
                     :on-click (fn []
                                 (rf/dispatch [:new-game (->> (vals @players)
                                                              (filter (comp not str/blank?))
                                                              vec)]))}]]])))

(defn page []
  [:div
   [new-game]])

(rdom/render [page] (.getElementById js/document "app"))