Test-only certificate chain for DefaultSslContextFactoryTestCase (#1397).

  test-ca-root.pem          CN=Test Root CA          (self-signed CA)
  test-ca-intermediate.pem  CN=Test Intermediate CA  (CA, signed by the root)
  test-ca-leaf.pem          CN=localhost, SAN dns:localhost (signed by the intermediate)
  test-ca-leaf-key.pem      private key of the leaf, PKCS#8, TEST ONLY, protects nothing

All certificates are valid for 36500 days from 2026-11-23 (RSA 2048). Generated with keytool:

  keytool -genkeypair -alias root  -dname "CN=Test Root CA" -keyalg RSA -keysize 2048 -validity 36500 -ext bc:c ...
  keytool -genkeypair -alias inter -dname "CN=Test Intermediate CA" ... -ext bc:c ...
  keytool -genkeypair -alias leaf  -dname "CN=localhost" ... -ext san=dns:localhost ...
  keytool -certreq / keytool -gencert: intermediate signed by root, leaf signed by intermediate.

The test serves the chain out of order ([leaf, root, intermediate]), which a PKCS12 key store refuses to
store, hence the JKS store built in memory by the test.
