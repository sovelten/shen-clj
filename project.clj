(defproject shen-port "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.analyzer.jvm "1.2.2"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [midje "1.10.5"]
                 [instaparse "1.4.8"]]
  :plugins [[lein-midje "3.2.1"]]
  :main ^:skip-aot shen-port.install
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :shen-clj {:main shen.functions}}
  :aliases {"shen-clj" ["with-profile" "shen-clj" "run"]
            "make"     ["with-profile" "shen-clj" "uberjar"]})
