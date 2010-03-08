Realtime
========

Description
-----------
Provides a realtime aggregate feed of click stream data.

Requirements
------------
* [Leiningen](http://github.com/technomancy/leiningen). Follow the instructions within the section "Installation".
* [RabbitMQ](http://www.rabbitmq.com/). This can be installed by installing [Home Brew](http://github.com/mxcl/homebrew) and running `brew install rabbitmq`.


Running
-------
Start a REPL by running `lein repl` in the project root. Then you can run the following commands:

    Clojure 1.1.0
    user=> (use 'redirectly-realtime.listener :reload)
    user=> (attach-listener count-statement print-listener)
    user=> (send-event {"keyword" "this is the keyword"} "ClickEvent")

This will produce a response of:

    Mar 8, 2010 4:18:49 PM clojure.contrib.logging$eval__100$impl_write_BANG___111 invoke
    INFO: (last 30 seconds): keyword= this is the keyword, sum= 1
    nil
