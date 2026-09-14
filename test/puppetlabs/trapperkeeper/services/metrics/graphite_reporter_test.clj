(ns puppetlabs.trapperkeeper.services.metrics.graphite-reporter-test
  "Regression tests for the custom GraphiteReporter.
   In particular, verifies that the format(Object) method correctly dispatches
   to primitive overloads without infinite recursion (StackOverflowError)."
  (:require [clojure.test :refer :all]
            [clojure.set]
            [puppetlabs.trapperkeeper.services.metrics.metrics-testutils :as testutils])
  (:import (com.puppetlabs.trapperkeeper.metrics GraphiteReporter)
           (io.dropwizard.metrics5 MetricRegistry Gauge)
           (java.util.concurrent TimeUnit)))

(deftest graphite-reporter-format-does-not-stack-overflow
  (testing "GraphiteReporter.report() handles all boxed numeric gauge types without StackOverflow"
    ;; This test would have caught the bug where format(Object) called format(l)
    ;; with a boxed Long, which Java resolved back to format(Object) via widening,
    ;; causing infinite recursion.
    (let [registry (MetricRegistry.)
          reported-metrics (atom {})
          graphite-sender (testutils/make-graphite-sender reported-metrics :default)
          reporter (-> (GraphiteReporter/forRegistry registry)
                       (.convertRatesTo TimeUnit/SECONDS)
                       (.convertDurationsTo TimeUnit/MILLISECONDS)
                       (.build graphite-sender))]

      ;; Register gauges returning each boxed numeric type that format(Object) must handle.
      ;; The StackOverflow manifested specifically with Long and Double gauge values.
      (.registerGauge registry "test.gauge.long"
                      (reify Gauge (getValue [_] (Long/valueOf 42))))
      (.registerGauge registry "test.gauge.double"
                      (reify Gauge (getValue [_] (Double/valueOf 3.14))))
      (.registerGauge registry "test.gauge.integer"
                      (reify Gauge (getValue [_] (Integer/valueOf 7))))
      (.registerGauge registry "test.gauge.float"
                      (reify Gauge (getValue [_] (Float/valueOf 2.5))))
      (.registerGauge registry "test.gauge.short"
                      (reify Gauge (getValue [_] (Short/valueOf (short 1)))))
      (.registerGauge registry "test.gauge.byte"
                      (reify Gauge (getValue [_] (Byte/valueOf (byte 1)))))

      ;; This call would StackOverflow before the fix.
      ;; We run it in a thread with a timeout so a regression doesn't hang the suite.
      (let [result (deref (future (.report reporter)) 5000 :timeout)]
        (is (not= :timeout result)
            "GraphiteReporter.report() should complete without StackOverflow")
        (is (nil? result)
            "GraphiteReporter.report() should return normally"))

      ;; Verify all gauge types were actually reported to graphite
      (let [metrics @reported-metrics
            reported-names (get metrics :default)]
        (is (contains? reported-names "test.gauge.long"))
        (is (contains? reported-names "test.gauge.double"))
        (is (contains? reported-names "test.gauge.integer"))
        (is (contains? reported-names "test.gauge.float"))
        (is (contains? reported-names "test.gauge.short"))
        (is (contains? reported-names "test.gauge.byte")))

      (.close reporter))))

(deftest graphite-reporter-format-null-gauge-value
  (testing "GraphiteReporter handles nil/null gauge values gracefully"
    (let [registry (MetricRegistry.)
          reported-metrics (atom {})
          graphite-sender (testutils/make-graphite-sender reported-metrics :default)
          reporter (-> (GraphiteReporter/forRegistry registry)
                       (.convertRatesTo TimeUnit/SECONDS)
                       (.convertDurationsTo TimeUnit/MILLISECONDS)
                       (.build graphite-sender))]

      (.registerGauge registry "test.gauge.null"
                      (reify Gauge (getValue [_] nil)))

      ;; Should not throw
      (let [result (deref (future (.report reporter)) 5000 :timeout)]
        (is (not= :timeout result)))

      ;; Null gauge should NOT be reported (format returns null, send is skipped)
      (let [metrics @reported-metrics
            reported-names (get metrics :default #{})]
        (is (not (contains? reported-names "test.gauge.null"))))

      (.close reporter))))

