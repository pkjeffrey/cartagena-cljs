(ns cartagena.app
  (:require [cartagena.core :as c]
            [cartagena.events]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]))

(defn inp []
  (let [name @(rf/subscribe [:name])]
    [:div
     "Name: " [:input {:type "text"
                       :value name
                       :on-change #(rf/dispatch [:name (-> % .-target .-value)])}]]))

(defn out []
  (let [name @(rf/subscribe [:name])]
    [:p "Greeting: Hello " name]))

(defn page []
  [:div
   [inp]
   [out]])

(rdom/render [#'page]
             (.getElementById js/document "app"))