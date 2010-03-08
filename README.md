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
    user=> (use 'redirectly-realtime.listener :reload)
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
    user=> (setup-channel "clicks-exchange" "clicks-queue" "routing-key")
    #<ChannelN AMQChannel(amqp://guest@localhost:5672/,1)>
    user=> (listen-loop "clicks-queue")

This will block whilst waiting for messages to be delivered. To send a message, you'll need to start another REPL with `lein repl` and run:

    Clojure 1.1.0
    user=> (use 'redirectly-realtime.rabbit :reload)
    user=> (send-message channel "clicks-exchange" "routing-key" "Hello World!")

If you switch back to the first window you should see a message:

    Mar 8, 2010 5:17:37 PM clojure.contrib.logging$eval__100$impl_write_BANG___111 invoke
    INFO: Received Hello World!
