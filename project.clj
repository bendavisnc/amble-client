(defproject amble-client "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [;;[cljsjs/react "17.0.1-0"]
                 ;;[cljsjs/react-dom "17.0.1-0"]
            ;;      [cljsjs/react-transition-group "4.3.0-0"]
                 [haslett "0.1.6"]
                 [hiccup "1.0.5"]
                 [integrant "0.8.0"]
                 [martian "0.1.15"]
                 [martian-cljs-http "0.1.12"]
                 [metosin/jsonista "0.2.6"]
                 [metosin/reitit "0.5.1"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [org.clojure/core.async "1.2.603"]
                 [pez/clerk "1.0.0"]
                 [reagent "1.1.1" :exclude [cljsjs/react
                                            cljsjs.react/dom]]
                 [re-frame "0.9.4"]
                 [ring "1.8.1"]
                 [ring-server "0.5.0"]
                 [ring/ring-defaults "0.3.2"]
                 [venantius/accountant "0.2.5" :exclusions [org.clojure/tools.reader]]
                 [yogthos/config "1.1.7" :scope "provided"]]


  :plugins [[lein-environ "1.1.0"]
            [lein-cljsbuild "1.1.7"]
            [lein-asset-minifier "0.4.6"
             :exclusions [org.clojure/clojure]]
            [lein-cljfmt "0.7.0"]]

  :ring {:handler amble-client.handler/app
         :uberwar-name "amble-client.war"}

  :cljfmt {:remove-multiple-non-indenting-spaces? true
           :sort-ns-references true}

  :min-lein-version "2.5.0"
  :uberjar-name "amble-client.jar"
  :main amble-client.server
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  [[:css {:source "resources/public/css/site.css"
          :target "resources/public/css/site.min.css"}]]

  :cljsbuild
  {:builds {:min {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
                  :compiler {:output-to        "target/cljsbuild/public/js/app.js"
                             :output-dir       "target/cljsbuild/public/js"
                             :source-map       "target/cljsbuild/public/js/app.js.map"
                             :optimizations :advanced
                             :infer-externs true
                             :pretty-print  false
                             :language-out :es5
                             :npm-deps {:react-settings-pane "0.1.5"}
                             :install-deps true}} 
            :app {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                  :figwheel {:on-jsload "amble-client.core/mount-root"}
                  :compiler {:main "amble-client.dev"
                             :asset-path "/js/out"
                             :output-to "target/cljsbuild/public/js/app.js"
                             :output-dir "target/cljsbuild/public/js/out"
                             :source-map true
                             :optimizations :none
                        ;;   :optimizations :simple
                             :language-out :es6
                             :pretty-print  true
                        ;;      :npm-deps {:react-settings-pane "0.1.5"}
                             :install-deps true}}}} 






  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl]

   :css-dirs ["resources/public/css"]
   :ring-handler amble-client.handler/app}



  :profiles {:dev {:repl-options {:init-ns amble-client.repl}
                   :dependencies [[cider/piggieback "0.5.0"]
                                  [binaryage/devtools "1.0.0"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.8.1"]
                                  [prone "2020-01-17"]
                                  [figwheel-sidecar "0.5.20"]
                                  [nrepl "0.7.0"]
                                  [pjstadig/humane-test-output "0.10.0"]]



                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.20"]]


                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true
                         :port 3001
                         :host "localhost"}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
