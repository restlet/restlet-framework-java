package checker;

import dataLoader.data.LibraryPackage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CheckUtils {
    /**
     * Returns true if the package is provided by the maven repository.
     *
     * @param url
     *            The maven repository url.
     * @param p
     *            The package.
     * @return True if the package is provided by the maven repository.
     */
    static boolean checkMavenRepository(String url, LibraryPackage p) {
        try {
            // check that the file exists
            final URI uri = URI.create(url
                    + p.getMavenGroupId().replace(".", "/") + "/"
                    + p.getMavenArtifactId() + "/" + p.getMavenVersion() + "/"
                    + p.getMavenArtifactId() + "-" + p.getMavenVersion() + ".jar");

            System.out.println("Checking maven artifact " + uri.toString() + " ...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            return HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.discarding())
                    .statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
