# Debug the Netty connectors

The HTTP connectors of the core are based on Netty. Netty logs through `java.util.logging` when no
other logging framework (SLF4J, Log4j 2) is found, under the `io.netty` logger names.

To trace the connectors in a test:

```java
Engine.setLogLevel(Level.FINE);
Logger.getLogger("io.netty").setLevel(Level.FINE);
```

To dump the HTTP traffic, add a Netty `LoggingHandler` at the start of the connection pipeline, in
`NettyServerHelper.start()` (server) or `NettyClientHelper.initConnection(...)` (client):

```java
p.addFirst("wire", new io.netty.handler.logging.LoggingHandler(io.netty.handler.logging.LogLevel.INFO));
```

Server calls run on virtual threads by default. A thread dump that includes them can be taken with:

```
jcmd <pid> Thread.dump_to_file -format=text threads.txt
```

Set the "threadPool.virtual" server parameter to `false` to run calls on platform threads instead,
which `jstack` also shows.
