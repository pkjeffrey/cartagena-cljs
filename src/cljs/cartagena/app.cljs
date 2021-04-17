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

(defn space [i]
  (let [{:keys [symbol pieces]} @(rf/subscribe [:game/board-space i])
        colors @(rf/subscribe [:game/player-colors])
        tokens (reduce (fn [t c]
                         (concat t (repeat (get pieces c) c)))
                       [] colors)]
    (fn []
      [:div.space {:id (str "space-" i)}
       (name symbol) " " (for [t tokens]
                           [:div.token {:class (name t)}])])))

(defn board []
  [:div.board
   (for [i (range @(rf/subscribe [:game/board-space-count]))]
     ^{:key i}[space i])])

(defn game []
  [:div
   [:p "playing"]
   (for [[key player] @(rf/subscribe [:game/players])]
     ^{:key key}[:p (:name player)])
   [board]])

(defn page []
  [:div
   (if (= :playing @(rf/subscribe [:game-state]))
     [game]
     [new-game])])

(rdom/render [page] (.getElementById js/document "app"))