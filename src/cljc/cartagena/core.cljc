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
  (init-players ["Peter" "Ralph"]))

(defn board-seq
  "Create board sequence of spaces."
  []
  (flatten [:start (repeatedly 6 #(shuffle symbols)) :sloop]))

(defn init-space
  "Create a board space with n pieces for each player color."
  [playing-colors n]
  {:pieces (zipmap playing-colors (repeat n))})

(defn init-board
  "Initialise board and player pieces."
  [playing-colors]
  (mapv (fn [s]
          (let [n (if (= :start s) 6 0)]
            (-> (init-space playing-colors n)
                (assoc :symbol s))))
        (board-seq)))

(comment
  (board-seq)
  (init-space [:red :yellow] 6)
  (init-board [:red :yellow])
  )