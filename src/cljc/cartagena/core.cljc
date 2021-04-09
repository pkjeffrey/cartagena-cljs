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
  [player-names]
  (zipmap (take (count player-names) colors)
          (map (fn [name]
                 {:name name
                  :cards (initial-hand)
                  :actions 0})
               player-names)))

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
  (init-board [:red :yellow]))

(defn turn-seq
  "Create a turn sequence map.
   Mapping current player to next player"
  [playing-colors]
  (zipmap playing-colors (rest (cycle playing-colors))))

(defn player-has-card?
  "Returns true if player has the card."
  [player symbol]
  (pos? (get-in player [:cards symbol])))

(defn player-on-space?
  "Returns true if player has a piece on space."
  [space player-color]
  (pos? (get-in space [:pieces player-color])))

(defn player-wins?
  "Returns true if player has all their pieces on sloop space."
  [sloop player-color]
  (= pirates (get-in sloop [:pieces player-color])))

(defn piece-count
  "Returns the count of the number of pieces on a space."
  [space]
  (reduce + (-> space :pieces vals)))

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
  [board from-idx]
  (or (->> (range (dec from-idx) 0 -1)
           (filter #(< 0 (piece-count (get board %)) 3))
           first)
      0))

(defn next-space
  "Returns the space index player can move forward to.
   From starting space, the first unoccupied space
   forwards with the symbol, or the sloop space."
  [board from-idx symbol]
  (or (->> (range (inc from-idx) (count board))
           (filter (fn [i]
                     (let [space (get board i)]
                       (and (= symbol (:symbol space))
                            (space-unoccupied? space)))))
           first)
      (dec (count board))))

(defn move-player
  "Move a players piece from one space to another."
  [board player-color from-idx to-idx]
  (-> board
      (update-in [from-idx :pieces player-color] dec)
      (update-in [to-idx :pieces player-color] inc)))

(defn active-player
  "Returns the active player's color."
  [players]
  (->> players
       (filter #(pos? (:actions (val %))))
       first
       key))

(defn start-turn
  "Starts a players turn."
  [game player-color]
  (assoc-in game [:players player-color :actions] turn-actions))

(defn init-game
  "Initialise game state.
   players should be a seq of string names."
  [player-names]
  (let [p (init-players player-names)]
    (-> {:players p
         :next-turn (turn-seq (keys p))
         :board (init-board (keys p))}
        (start-turn (first colors)))))

(comment
  (init-game ["Peter" "John"]))

(defn use-action
  "Use a player's action and when none left, next player."
  [game]
  (let [active-player (active-player (:players game))]
    (-> game
        (update-in [:players active-player :actions] dec)
        (#(if (zero? (get-in % [:players active-player :actions]))
            (start-turn % (get-in % [:next-turn active-player]))
            %)))))

(defn declare-winner
  "Declare the active player the winner if all their
   pieces are at the sloop space."
  [game]
  (let [active-player (active-player (:players game))]
    (if (player-wins? (last (:board game)) active-player)
      (assoc game :winner active-player)
      game)))

(defn action-pass
  "Make a 'pass' turn by the active player."
  [game]
  (let [active-player (active-player (:players game))]
    (-> game
        (assoc-in [:players active-player :actions] 1)
        use-action)))

(defn action-back
  "Make a 'back' turn by the active player."
  [game from-idx]
  (let [active-player (active-player (:players game))
        to-idx (prev-space (:board game) from-idx)
        cards (if (< 0 to-idx) (piece-count (get-in game [:board to-idx])) 0)]
    (when (player-on-space? (get-in game [:board from-idx]) active-player)
      (-> game
          (update :board move-player active-player from-idx to-idx)
          (update-in [:players active-player :cards] deal (draw cards))
          use-action))))

(defn action-card
  "Make a 'play card' turn by the active player."
  [game from-idx card]
  (let [active-player (active-player (:players game))
        to-idx (next-space (:board game) from-idx card)]
    (when (and (player-on-space? (get-in game [:board from-idx]) active-player)
               (player-has-card? (get-in game [:players active-player]) card))
      (-> game
          (update :board move-player active-player from-idx to-idx)
          (update-in [:players active-player :cards card] dec)
          use-action
          declare-winner))))