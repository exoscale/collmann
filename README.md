collmann: riemann event processing inside collectd
--------------------------------------------------

![xzibit](http://i.imgur.com/AbtD48O.jpg)

collmann is a collectd plugin relying on collectd's
java support which embeds a [riemann](http://riemann.io)
event processor within collectd.

The aim of collmann is to provide a flexible intermediate
layer between collectd input plugins and collectd output
plugins, by allowing event manipulation.

In addition to the very rich API exposed by riemann, a few
collectd specific functions are also available and documented
[below](#api).

### Building

Building collmann is done with the help of [leiningen](http://leiningen.org),
once installed, just run:

```bash
lein uberjar
```

which will produce an artifact in `<collman-dir>/target/collman-0.1.0-standalone.jar`

### Configuration

Configuration of the plugin happens in a standard riemann configuration file, which
you then point to from collectd's configuration.

#### Riemann configuration

A typical riemann configuration will look like this:

```clojure
(tcp-server :host "0.0.0.0")

(let [index (update-index (index))]
  (streams
     ;; write out incoming metrics to both the riemann index
	 ;; and back to collectd
	 (where collectd?
	        index
	        collectd-index)))
```

For additional documentation on the riemann configuration API,
please refer to the riemann [howto](http://riemann.io/howto.html)
and [api documentation](http://riemann.io/api.html).

The corresponding collectd configuration would look like this:

```
Hostname    "yourname"

LoadPlugin logfile
LoadPlugin cpu
LoadPlugin load
LoadPlugin java

<Plugin logfile>
        File STDOUT
        Timestamp true
        PrintSeverity false
</Plugin>
						
<Plugin java>
        JVMArg     "-Dlog4j.configuration=file:///path/to/collmann/resources/log4j.properties"
        JVMArg     "-Djava.class.path=/path/to/collmann/target/collmann-0.1.0-standalone.jar"
        LoadPlugin "io.exo.collmann.core"
       <Plugin "riemann">
             LoadConfig "/path/to/collmann/doc/collectd.clj"
       </Plugin>
</Plugin>
```

### Api

The following functions are available in addition to the [riemann api](http://riemann.io/api.html):

* `collectd?`: predicate which can be used in `where` to catch collectd events.
* `plugin`: predicate builder to catch events from specific plugins
* `plugin-instance`: predicate builder to catch events from specific plugin-instances
* `collectd-type`: predicate builder to catch events from specific collectd types
* `type-instance`: predicate builder to catch events from specific type instances


