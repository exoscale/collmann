(tcp-server :host "0.0.0.0")

(let [index (update-index (index))]
  (streams
   ;; write out incoming metrics to both the riemann index
   ;; and back to collectd
   (where collectd?
          index
          collectd-index)))



