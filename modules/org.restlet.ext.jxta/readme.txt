
abstract
--------

[preliminary] jxta/restlet integration

environment
-----------

intellij idea ... standalone binaries and ide agnostic processes will be provided in time.

build
-----

open "jxta" module and press the "make project" button

run
---

add a server run configuration:

  run -> edit configurations
    + -> application
      name: server
      main class: org.restlet.ext.jxta.prototype.Main
      program parameters: -server
      working directory: .../restlet/modules/org.restlet.ext.jxta_2.5
      use classpath and jdk of module: jxta

add a client run configuration:

  same as server with the following exception:
      program parameters: -client

run server
run client

todo
----

incode todo
restlet integration
socket factory
  based on uri scheme
socket support
  multicastsocket (async)
  serversocket (stream) [wip]
configuration
  profile.adhoc not propogated PlatformConfig (work around change RdvConfig.client->adhoc by hand)
  main/properties
  guice
  spring