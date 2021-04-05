(ns cartagena.core)

(def colors [:red :yellow :green :blue :brown])
(def symbols [:sword :parrot :hook :skull :treasure :rum])
(def board-segments 6)
(def pirates 6)
(def starting-cards 6)

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
  (deal (empty-hand) (draw starting-cards)))

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
  (draw starting-cards)
  (empty-hand)
  (-> (empty-hand)
      (deal [:sword :parrot]))
  (-> (empty-hand)
      (deal (draw starting-cards)))
  (initial-hand)
  (init-players ["Peter" "John"]))

(defn board-seq
  "Create board sequence of spaces."
  []
  (flatten [:start (repeatedly board-segments #(shuffle symbols)) :sloop]))

(defn init-space
  "Create a board space with n pieces for each player color."
  [playing-colors n]
  {:pieces (zipmap playing-colors (repeat n))})

(defn init-board
  "Initialise board and player pieces."
  [playing-colors]
  (mapv (fn [s]
          (let [n (if (= :start s) pirates 0)]
            (-> (init-space playing-colors n)
                (assoc :symbol s))))
        (board-seq)))

(comment
  (board-seq)
  (init-space [:red :yellow] pirates)
  (init-board [:red :yellow])
  )

(defn turn-seq
  "Create a turn sequence map.
   Mapping current player to next player"
  [playing-colors]
  (zipmap playing-colors (rest (cycle playing-colors))))

(defn init-game
  "Initialise game state.
   players should be a seq of string names."
  [players]
  (let [p (init-players players)]
    {:players p
     :next-turn (turn-seq (keys p))
     :board (init-board (keys p))}))

(comment
  (init-game ["Peter" "John"])
  )