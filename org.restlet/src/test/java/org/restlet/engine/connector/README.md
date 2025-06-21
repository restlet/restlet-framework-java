# Debug Jetty

## Add full logs

[Jetty's documentation](https://jetty.org/docs/jetty/12/programming-guide/troubleshooting/logging.html) (the `org.eclipse.jetty:jetty-slf4j-impl`is already added to the `pom.xml`).

Programmatically:
```
System.setProperty("org.eclipse.jetty.LEVEL", "TRACE");
```

Or add a `jetty-logging.properties`:
```
org.eclipse.jetty.LEVEL=TRACE
org.eclipse.jetty.client.LEVEL=TRACE
```

## Debug using JMX
You need to update the current implementation by hand.

- [activate JMX](https://jetty.org/docs/jetty/12/programming-guide/arch/jmx.html)
The Jetty server is created in class `JettyServerHelper`.

```
        // Create an MBeanContainer with the platform MBeanServer.
        MBeanContainer mbeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        // Add MBeanContainer to the root component.
        jettyServer.addBean(mbeanContainer);
```

- to [state tracking](https://jetty.org/docs/jetty/12/programming-guide/troubleshooting/state-tracking.html)
  You can use [jconsole](https://docs.oracle.com/javase/8/docs/technotes/guides/management/jconsole.html) to check the state of MBean or just run operations on them.
