(defproject org.openvoxproject/trapperkeeper-metrics "2.0.5-SNAPSHOT"
  :description "Trapperkeeper Metrics Service"
  :url "http://github.com/openvoxproject/trapperkeeper-metrics"
  :license {:name "Apache License, Version 2.0"
              :url "http://www.apache.org/licenses/LICENSE-2.0.html"}

  :min-lein-version "2.9.1"

  :pedantic? :abort

  :parent-project {:coords [org.openvoxproject/clj-parent "7.5.0"]
                   :inherit [:managed-dependencies]}

  :dependencies [[org.clojure/clojure]

                 [prismatic/schema]

                 [org.openvoxproject/kitchensink]
                 [org.openvoxproject/trapperkeeper]
                 [org.openvoxproject/trapperkeeper-authorization]
                 [org.openvoxproject/ring-middleware]

                 [cheshire]
                 [org.clojure/java.jmx]

                 [org.clojure/tools.logging]
                 [io.dropwizard.metrics/metrics-core]
                 [io.dropwizard.metrics/metrics-graphite]
                 [org.jolokia/jolokia-core "1.7.0"]
                 [org.openvoxproject/comidi]
                 [org.openvoxproject/i18n]]

  :plugins [[org.openvoxproject/i18n "0.9.3"]
            [lein-parent "0.3.9"]]

  :source-paths  ["src/clj"]
  :java-source-paths  ["src/java"]

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/CLOJARS_USERNAME
                                     :password :env/CLOJARS_PASSWORD
                                     :sign-releases false}]]

  :classifiers  [["test" :testutils]]

  :profiles {:defaults {:dependencies [[org.openvoxproject/http-client]
                                       [org.openvoxproject/trapperkeeper :classifier "test"]
                                       [org.openvoxproject/trapperkeeper-webserver-jetty10]
                                       [org.openvoxproject/kitchensink :classifier "test"]]}
             :dev-dependencies {:dependencies [[org.bouncycastle/bcpkix-jdk18on]]}
             :dev [:defaults :dev-dependencies]
             :fips-dependencies {:dependencies [[org.bouncycastle/bcpkix-fips]
                                                [org.bouncycastle/bc-fips]
                                                [org.bouncycastle/bctls-fips]]
                                 :jvm-opts ~(let [version (System/getProperty "java.specification.version")
                                                  [major minor _] (clojure.string/split version #"\.")
                                                  unsupported-ex (ex-info "Unsupported major Java version. Expects 17 or 21."
                                                                               {:major major
                                                                                :minor minor})]
                                                 (condp = (java.lang.Integer/parseInt major)
                                                   17 ["-Djava.security.properties==./dev-resources/java.security.jdk17-fips"]
                                                   21 ["-Djava.security.properties==./dev-resources/java.security.jdk21-fips"]
                                                   (throw unsupported-ex)))}
             :fips [:defaults :fips-dependencies]


             ;; per https://github.com/technomancy/leiningen/issues/1907
             ;; the provided profile is necessary for lein jar / lein install
             :provided {:dependencies [[org.bouncycastle/bcpkix-jdk18on]]}

             :testutils {:source-paths ^:replace ["test"]
                         :java-source-paths ^:replace []}}

  :repl-options {:init-ns examples.ring-app.repl}

  :main puppetlabs.trapperkeeper.main)
