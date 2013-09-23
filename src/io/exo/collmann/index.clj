(ns io.exo.collmann.index
  (:require [io.exo.collmann.interop :as io]))

(def event-queue (atom (list)))

(defn collectd-index
  [event]
  (swap! event-queue conj event))
