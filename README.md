Realtime
========

Description
-----------
A small play around with processing aggregate click data using Esper and RabbitMQ. It's a work-in-progress so we'll see how it goes. Here's my notes so far on what needs to be done to get up and running.

Requirements
------------
* [Leiningen](http://github.com/technomancy/leiningen). Follow the instructions within the section "Installation".
* [RabbitMQ](http://www.rabbitmq.com/). This can be installed by installing [Home Brew](http://github.com/mxcl/homebrew) and running `brew install rabbitmq`.

Running from the REPL
=====================

Esper Listener
--------------
Start a REPL by running `lein repl` in the project root. Then you can run the following commands:

    Clojure 1.1.0
    user=> (use 'redirectly-realtime.esper :reload)
    user=> (attach-listener count-statement print-listener)
    user=> (send-event {"keyword" "this is the keyword"} "ClickEvent")

This will produce a response of:

    Mar 8, 2010 4:18:49 PM clojure.contrib.logging$eval__100$impl_write_BANG___111 invoke
    INFO: (last 30 seconds): keyword= this is the keyword, sum= 1
    nil


RabbitMQ Publisher + Subscriber
-------------------------------
Start a REPL by running `lein repl` in the project root. Then, to start the client, run the following:

    Clojure 1.1.0
    user=> (use 'redirectly-realtime.rabbit :reload)                                    
    user=> (with-open [channel (create-channel)]
    (setup-channel channel "clicks-exchange" "click-queue-1" "some-routing-key")
    (listen-loop channel "click-queue-1" handle-delivery))

This will block whilst waiting for messages to be delivered. To send a message, you'll need to start another REPL with `lein repl` and run:

    Clojure 1.1.0
    user=> (use 'redirectly-realtime.rabbit :reload)
    user=> (with-open [channel (create-channel)] (send-message channel "clicks-exchange" "some-routing-key" "some message here again"))

If you switch back to the first window you should see a message:

    Mar 8, 2010 5:17:37 PM clojure.contrib.logging$eval__100$impl_write_BANG___111 invoke
    INFO: Received Hello World!

Multiple listeners can be connected by binding their own queue to the exchange. In a new REPL window, run the following:

    Clojure 1.1.0
    user=> (use 'redirectly-realtime.rabbit :reload)                                    
    user=> (with-open [channel (create-channel)]
    (setup-channel channel "clicks-exchange" "click-queue-2" "some-routing-key")
    (listen-loop channel "click-queue-2" handle-delivery))

This will bind the `click-queue-2` to the `clicks-exchange` exchange. Send a message (as before) and it should appear in both sessions.


Sample App Micro-Benchmark
--------------------------
The `redirectly-realtime.sample` namespace includes some things to make it easier to test a publisher and consumer, as well as time the overall throughput.

Having built the application with `lein uberjar`, run `java -server -cp redirectly-realtime-standalone.jar redirectly_realtime.sample --run client` to start the client process. Run `java -server -cp redirectly-realtime-standalone.jar redirectly_realtime.sample --run publisher` to start the publisher.

The client should report

    INFO: Starting Esper/RabbitMQ sample. Logging with org.apache.commons.logging

Upon starting the publisher you should also see log messages showing the output from the Esper listener:

    INFO: Sample Keyword Here (1)
    INFO: Sample Keyword Here (2)
    ...
    INFO: Sample Keyword Here (10000)
    
The publisher will also print it's elapsed time:

    Elapsed time: 11708.645 msecs
