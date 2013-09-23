(ns io.exo.collmann.pool
  (:import  java.util.concurrent.ScheduledThreadPoolExecutor)
  (:require [clojure.tools.logging :refer [info error]]))

(def stpe
  (delay
   (ScheduledThreadPoolExecutor. 5)))

(defmacro with-pool
  [& [action & body :as full-body]]
  (if (string? action)
    `(.submit 
      @stpe
      (fn []
        (try
          (info "trying to " ~action)
          (do ~@body)
          (catch Exception e#
            (error e# (str "could not " ~action))))))
    `(.submit 
      @stpe
      (fn []
        (try
          (do ~@full-body)
          (catch Exception e#
            (error e# "pool error")))))))

(defmacro with-sync-pool
  [action & body]
  `(deref (with-pool ~action ~@body)))
