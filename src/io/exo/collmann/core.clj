(ns io.exo.collmann.core
  (:gen-class :init init)
  (:import [org.collectd.api
            Collectd
            CollectdConfigInterface
            CollectdWriteInterface
            CollectdReadInterface])
  (:require [clojure.string          :as s]
            [clojure.tools.logging   :as log]
            [io.exo.collmann.interop :as io]
            [io.exo.collmann.index   :as index]
            [io.exo.collmann.config  :refer []]
            [io.exo.collmann.pool    :refer [with-sync-pool with-pool]]
            [riemann.time            :as time]
            [riemann.core            :as riemann]
            [riemann.config          :as config]
            [riemann.streams         :as streams]))

(defn find-config
  [ci]
  (-> (for [node (.getChildren ci)
            :when (= "loadconfig" (s/lower-case (.getKey node)))]
        (for [value (.getValues node) :let [value (.getString value)]]
          value))
      flatten
      first))

(defn get-handler
  []
  (reify 
    CollectdConfigInterface
    (config [this ci]
      (log/info "called with configuration")
      (when-let [path (find-config ci)]
        (with-sync-pool "load configuration"
          (time/start!)
          (log/info "reading riemann plugin configuration from: " path)
          (binding [*ns*                 (find-ns 'io.exo.collmann.config)
                    config/*config-file* path]
            (load-file path)
            (log/info "successfully loaded riemann configuration"))
          (config/apply!)))
      0)
    CollectdReadInterface
    (read [this]
      (log/info "dispatching values")
      (let [events @index/event-queue]
        (swap! index/event-queue empty)
        (doseq [event (reverse events)
                :let [vl (io/event->vl event)]]
          (log/info "found collectd metric: " vl)
          (log/info "ds size: " (count (.getDataSource vl)))
          (Collectd/dispatchValues (io/event->vl event))))
      0)
    CollectdWriteInterface
    (write [this vl]
      (with-sync-pool
        (doseq [event (io/vl->events vl)]
          (riemann/stream! @riemann.config/core event)))
      0)))

(defn -init
  []
  (with-pool "initialize" (log/info "configuration thread-pool init"))
  (let [handler (get-handler)]
    (Collectd/registerConfig "riemann" handler)
    (Collectd/registerWrite "riemann" handler)
    (Collectd/registerRead "riemann" handler)
    [[] handler]))
