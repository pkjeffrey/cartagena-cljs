(defproject cartagena-cljs "0.1.0"
  
  :source-paths ["src/cljc"]
  :target-path "target/%s"
  
  :profiles
  {:dev {:dependencies [[com.bhauman/figwheel-main "0.2.12"]
                        [com.bhauman/rebel-readline-cljs "0.1.4"]
                        [org.clojure/clojurescript "1.10.773"]]}}
   
  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]})