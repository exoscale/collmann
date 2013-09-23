(defproject io.exo/collmann "0.1.0"
  :description "riemann events for collectd"
  :url "https://github.com/exoscale/collmann"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :aot :all
  :dependencies [[org.clojure/clojure       "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.collectd/plugin-api   "5.4.0"]
                 [riemann                   "0.2.2"]
                 [log4j/log4j "1.2.16" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]])
