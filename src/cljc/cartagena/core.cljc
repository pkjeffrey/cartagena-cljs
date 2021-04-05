(ns cartagena.core)

(def colors [:red :yellow :green :blue :brown])
(def symbols [:sword :parrot :hook :skull :treasure :rum])
(def pirates 6)

(defn draw
  "Draw n cards from the deck."
  [n]
  (repeatedly n #(rand-nth symbols)))

(defn empty-hand
  "Create an empty hand."
  []
  (zipmap symbols (repeat 0)))

(defn deal
  "Deal cards into the hand."
  [hand cards]
  (reduce #(update %1 %2 inc)
          hand cards))

(defn initial-hand
  "Create an intial hand of cards for a player."
  []
  (deal (empty-hand) (draw 6)))

(defn init-players
  "Initialise players and their hands.
   players should be a seq of string names."
  [players]
  (zipmap (take (count players) colors)
          (map (fn [p]
                 {:name p
                  :cards (initial-hand)
                  :actions 0})
               players)))

(comment
  (draw 1)
  (draw 6)
  (empty-hand)
  (-> (empty-hand)
      (deal [:sword :parrot]))
  (-> (empty-hand)
      (deal (draw 6)))
  (initial-hand)
  (init-players ["Peter" "Ralph"])
  )