(ns io.exo.collmann.config
  (:import  java.util.concurrent.ScheduledThreadPoolExecutor)
  (:require [riemann.core :as core]
            [riemann.service :as service]
            [riemann.transport.tcp        :as tcp]
            [riemann.transport.udp        :as udp]
            [riemann.transport.websockets :as websockets]
            [riemann.transport.graphite   :as graphite]
            [riemann.repl]
            [riemann.index]
            [riemann.logging :as logging]
            [riemann.folds :as folds]
            [riemann.pubsub :as pubsub]
            [riemann.graphite :as graphite-client]
            [clojure.tools.nrepl.server :as repl]
            [riemann.time :refer [unix-time linear-time once! every!]]
            [riemann.pagerduty :refer [pagerduty]]
            [riemann.campfire :refer [campfire]]
            [riemann.librato :refer [librato-metrics]]
            [clojure.java.io :refer [file]]
            [clojure.tools.logging :refer :all]
            [riemann.client :refer :all]
            [riemann.email :refer :all]
            [riemann.sns :refer :all]
            [riemann.streams :refer :all]
            [riemann.config :refer :all]
            [io.exo.collmann.index :refer [collectd-index]]))


(def collectd?
  (fn [event] (tagged-all "collectd") event))

(defn plugin-instance
  [pi]
  (fn [event]
    (= (:plugin-instance event) pi)))

(defn plugin
  [p]
  (fn [event]
    (= (:plugin event) p)))

(defn type-instance
  [ti]
  (fn [event]
    (= (:type-instance event) ti)))

(defn collectd-type
  [t]
  (fn [event]
    (= (:type event) t)))
