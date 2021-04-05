(ns cartagena.core)

(def colors [:red :yellow :green :blue :brown])
(def symbols [:sword :parrot :hook :skull :treasure :rum])
(def board-segments 6)
(def pirates 6)
(def starting-cards 6)
(def turn-actions 3)

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

(defn player-has-card?
  "Returns true if player has the card."
  [hand symbol]
  (pos? (get hand symbol)))

(defn player-on-space?
  "Returns true if player has a piece on space."
  [space player]
  (pos? (get space player)))

(defn player-wins?
  "Returns true if player has all their pieces on sloop space."
  [sloop player]
  (= pirates (get sloop player)))

(defn piece-count
  "Returns the count of the number of pieces on a space."
  [space]
  (reduce + (vals space)))

(defn space-occupied?
  "Returns true if space is occupied by at least one piece."
  [space]
  (pos? (piece-count space)))

(defn space-unoccupied?
  "Returns true if space is unoccupied by any pieces."
  [space]
  (not (space-occupied? space)))

(defn prev-space
  "Returns the space index player can move back to.
   From starting space, the first space backwards
   occupied by 1 or 2 pieces, or the start space."
  [board from]
  (or (->> (range (dec from) 0 -1)
           (filter #(< 0 (piece-count (get-in board [% :pieces])) 3))
           first)
      0))

(defn next-space
  "Returns the space index player can move forward to.
   From starting space, the first unoccupied space
   forwards with the symbol, or the sloop space."
  [board from symbol]
  (or (->> (range (inc from) (count board))
           (filter #(and (= symbol (get-in board [% :symbol]))
                         (space-unoccupied? (get-in board [% :pieces]))))
           first)
      (dec (count board))))

(defn active-player
  "Returns the active player's color."
  [players]
  (->> (filter #(pos? (:actions (val %)))
               players)
       first
       key))

(defn start-turn
  "Starts a players turn."
  [game player]
  (assoc-in game [:players player :actions] turn-actions))

(defn use-action
  "Use a player's action and when none left, next player."
  [game]
  (let [active-player (active-player (:players game))]
    (-> game
        (update-in [:players active-player :actions] dec)
        (#(if (zero? (get-in % [:players active-player :actions]))
            (start-turn % (get-in % [:next-turn active-player]))
            %)))))