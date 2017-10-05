These are self signed certificates that should only be used for test.
They are used so that the tests can be ran using HTTPS.
Primarilly the JSK file contains both the private and public certificates.

This can be used to set up the Server in your Component.

The .cer file can be used to import the public cert to a browser.

While the pem file contains the both the private and public cert.
It is intended to be used for tracing in Wireshark.
Check under Edit->Preferences->Protocols->SSL

In the RSA key list enter:
127.0.0.1,8443,http,/<path>/oauth2/src/test/resources/localhost.pem
Now wireshark will be able to decrypt all the HTTPS traffic.

Use the following filter:
tcp.port == 8443 && http
to mask out all the traffic to the test server.