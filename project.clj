(def kitchensink-version "3.5.3")
(def trapperkeeper-version "4.3.0")
(def i18n-version "1.0.2")
(def dropwizard-metrics-version "3.2.2")

(defproject org.openvoxproject/trapperkeeper-metrics "2.1.1-SNAPSHOT"
  :description "Trapperkeeper Metrics Service"
  :url "http://github.com/openvoxproject/trapperkeeper-metrics"
  :license {:name "Apache License, Version 2.0"
              :url "http://www.apache.org/licenses/LICENSE-2.0.html"}

  :min-lein-version "2.9.1"

  :pedantic? :abort

  ;; These are to enforce consistent versions across dependencies of dependencies,
  ;; and to avoid having to define versions in multiple places. If a component
  ;; defined under :dependencies ends up causing an error due to :pedantic? :abort,
  ;; because it is a dep of a dep with a different version, move it here.
  :managed-dependencies [[org.clojure/clojure "1.12.4"]

                         [ring/ring-core "1.8.2"]
                         [ring/ring-codec "1.3.0"]

                         [org.bouncycastle/bcpkix-jdk18on "1.83"]
                         [org.bouncycastle/bcpkix-fips "1.0.8"]
                         [org.bouncycastle/bc-fips "1.0.2.6"]
                         [org.bouncycastle/bctls-fips "1.0.19"]
  
                         [org.openvoxproject/kitchensink ~kitchensink-version]
                         [org.openvoxproject/kitchensink ~kitchensink-version :classifier "test"]
                         [org.openvoxproject/trapperkeeper ~trapperkeeper-version]
                         [org.openvoxproject/trapperkeeper ~trapperkeeper-version :classifier "test"]]

  :dependencies [[org.clojure/clojure]

                 [prismatic/schema "1.1.12"]

                 [org.openvoxproject/kitchensink]
                 [org.openvoxproject/trapperkeeper]
                 [org.openvoxproject/trapperkeeper-authorization "2.1.0"]
                 [org.openvoxproject/ring-middleware "2.1.0"]

                 [cheshire "5.10.2"]
                 [org.clojure/java.jmx "1.0.0"]

                 [org.clojure/tools.logging "1.2.4"]
                 [io.dropwizard.metrics/metrics-core ~dropwizard-metrics-version]
                 [io.dropwizard.metrics/metrics-graphite ~dropwizard-metrics-version]
                 [org.jolokia/jolokia-core "1.7.0"]
                 [org.openvoxproject/comidi "1.1.1"]
                 [org.openvoxproject/i18n ~i18n-version]]

  :plugins [[org.openvoxproject/i18n ~i18n-version]]

  :source-paths  ["src/clj"]
  :java-source-paths  ["src/java"]

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/CLOJARS_USERNAME
                                     :password :env/CLOJARS_PASSWORD
                                     :sign-releases false}]]

  :classifiers  [["test" :testutils]]

  :profiles {:defaults {:dependencies [[org.openvoxproject/http-client "2.2.0"]
                                       [org.openvoxproject/trapperkeeper :classifier "test"]
                                       [org.openvoxproject/trapperkeeper-webserver-jetty10 "1.1.0"]
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
