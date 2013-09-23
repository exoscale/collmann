(ns io.exo.collmann.interop
  (:import  [org.collectd.api ValueList PluginData DataSet DataSource])
  (:require [clojure.tools.logging :refer [info]]
            [clojure.string        :refer [join]]
            [riemann.codec         :refer [map->Event]]))

(defn format-service
  [host plugin plugin-instance type type-instance ds-name]
  (format "%s/%s/%s"
          host
          (join "-" (remove empty? [plugin plugin-instance]))
          (join "-" (remove empty? [type type-instance ds-name]))))

(defn vl->events
  [vl]
  (let [plugin          (.getPlugin vl)
        plugin-instance (.getPluginInstance vl)
        type            (.getType vl)
        type-instance   (.getTypeInstance vl)
        host            (.getHost vl)
        timestamp       (.getTime vl)
        source          (.getSource vl)]
    (for [[val ds] (map vector
                        (.getValues vl)
                        (-> (.getDataSet vl) (.getDataSources)))]
      (map->Event
       {:plugin          plugin
        :plugin-instance plugin-instance
        :type            type
        :type-instance   type-instance
        :host            host
        :time            timestamp
        :source          source
        :dataset         (-> (.getDataSet vl) .getType)
        :service         (format-service host
                                         plugin
                                         plugin-instance
                                         type
                                         type-instance
                                         (.getName ds))
        :tags            #{"collectd"}
        :ds-name         (.getName ds)
        :metric          val}))))

(defn event->vl
  [event]
  (let [pd   (doto (PluginData.)
               (.setHost (:host event))
               (.setPlugin (or (:plugin event) (:service event)))
               (.setPluginInstance (:plugin-instance event))
               (.setType (or (:type event) "gauge"))
               (.setTypeInstance (:type-instance event))
               (.setTime (:time event)))
        dsrc (DataSource/parseDataSource 
              (str (or (:ds-name event) "value") ":GAUGE:0:U"))
        dset (DataSet. (:dataset event) (list dsrc))]
    (doto (ValueList. pd)
      (.setValues [(:metric event)])
      (.setDataSet dset))))
