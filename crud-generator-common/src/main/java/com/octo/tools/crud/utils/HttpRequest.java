package com.octo.tools.crud.utils;

import com.octo.tools.crud.web.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

@Data
@AllArgsConstructor
public class HttpRequest {

    /**
     * 'Authorization' header name
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    /**
     * 'Accept' header name
     */
    public static final String HEADER_ACCEPT = "Accept";


    public static class HttpRequestException extends RuntimeException {

        public HttpRequestException(final Exception cause) {
            super(cause);
        }

    }

    private java.net.http.HttpRequest request;
    private HttpResponse<String> response;

    public boolean ok() {
        return response.statusCode() == 200 || response.statusCode() == 201;
    }

    public boolean notFound() {
        return response.statusCode() == 404;
    }

    public String body() {
        return response.body();
    }

    public int code() {
        return response.statusCode();
    }

    /**
     * Configure HTTPS connection to trust all certificates
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     * @throws HttpRequestException
     */
    public HttpRequest trustAllCerts() throws HttpRequestException {
        request.
        final HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection)
            ((HttpsURLConnection) connection).setSSLSocketFactory(getTrustedFactory());
        return this;
    }

    /**
     * Configure HTTPS connection to trust all hosts using a custom
     * {@link HostnameVerifier} that always returns <code>true</code> for each host
     * verified
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     */
    public HttpRequest trustAllHosts() {
        final HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection)
            ((HttpsURLConnection) connection).setHostnameVerifier(getTrustedVerifier());
        return this;
    }

     /** Set header name to given value
	 *
             * @param name
	 * @param value
	 * @return this request
	 */
    private HttpRequest header(final String name, final String value) {
        request.headers().map().put(name, List.of(value));
        return this;
    }

    /**
     * Set the 'Accept' header to given value
     *
     * @param accept
     * @return this request
     */
    public HttpRequest accept(final String accept) {
        return header(HEADER_ACCEPT, accept);
    }

    /**
     * Set the 'Authorization' header to given value
     *
     * @param authorization
     * @return this request
     */
    public HttpRequest authorization(final String authorization) {
        return header(HEADER_AUTHORIZATION, authorization);
    }

    private HttpClient newHttpClient() {
        return HttpClient.newBuilder().sslContext(new SSLContext())
    }

    /*HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create(uri))
               .method("PATCH", HttpRequest.BodyPublishers.ofString(message))
               .header("Content-Type", "text/xml")
               .build();*/

    public static HttpRequest get(String uri) throws HttpRequestException {
        HttpClient client = HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .method("GET", java.net.http.HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        try {
            return new HttpRequest(request, client.send(request, HttpResponse.BodyHandlers.ofString()));
        } catch (IOException | InterruptedException e) {
            throw new HttpRequestException(e);
        }
    }



}
