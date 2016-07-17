package io.tourniquet.junit.http.rules;

import static io.tourniquet.junit.http.rules.HttpPredicates.matchedPayload;
import static io.tourniquet.junit.http.rules.HttpPredicates.matchesMethod;
import static io.tourniquet.junit.http.rules.HttpPredicates.matchesParams;
import static io.tourniquet.junit.http.rules.HttpPredicates.matchesPath;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Class for fluently create http responses.
 */
public class ResponseStubbing {

    private final HttpServer server;
    private HttpMethod method = HttpMethod.GET;
    private final Map<String, String> params = new HashMap<>();
    private Optional<String> path = Optional.empty();
    private Optional<byte[]> payload = Optional.empty();

    ResponseStubbing(HttpServer server) {

        this.server = server;
    }

    ResponseStubbing method(HttpMethod httpMethod) {

        this.method = httpMethod;
        return this;
    }

    /**
     * Sets the resource that should be requested via GET, as in <pre>
     *     GET /pathToResource HTTP/1.1
     * </pre>
     * In case you mix resource paths with and without queries, that the paths without query should be defined after
     * paths with queries as these will also match requests with queries defined. For example:<br>
     * <pre>on(GET).resource(&quot;/path?query=example&quot;).respond(&quot;Answer1&quot;);
     * on(GET).resource(&quot;/path&quot;).respond(&quot;DefaultAnswer&quot;);
     * </pre>
     *
     * @param resource
     *         the path to the resource
     *
     * @return this stubbing
     */
    public ResponseStubbing resource(final String resource) {

        this.path = Optional.ofNullable(resource);
        return this;
    }

    /**
     * Defines on which request parameters the handler should respond. If no parameters are defined, the handler
     * responds to all requests. The specified parameters must be contained in the request, if the request contains more
     * parameters, they are ignored. Depending on the type of request, the parameters are handled differently. <ul>
     * <li>on GET requests, they are appended to the query part of the resource</li> <li>on POST requests, they are
     * added as multiform content, unless a payload is defined</li> </ul>
     *
     * @param params
     *         the parameters that activate the handler.
     *
     * @return this stubbing.
     */
    public ResponseStubbing withParams(Map<String, String> params) {

        this.params.putAll(params);
        return this;
    }

    /**
     * Defines on which request parameter the handler should respond. If no parameters are defined, the handler responds
     * to all requests. The specified parameter must be contained in the request, if the request contains more
     * parameters, they are ignored. Depending on the type of request, the parameters are handled differently. <ul>
     * <li>on GET requests, they are appended to the query part of the resource</li> <li>on POST requests, they are
     * added as multiform content, unless a payload is defined</li> </ul>
     *
     * @param name
     *         the name of the parameter
     * @param value
     *         the value of the parameter
     *
     * @return this stubbing.
     */
    public ResponseStubbing withParam(String name, String value) {

        this.params.put(name, value);
        return this;
    }

    /**
     * Defines the payload that triggers the response. If not specified, the handler will respond to all payloads. The
     * payload is compared using array-compare, so it has to be exact. If a payload is specified for a POST request, it
     * is only evaluated if the parameter match done before succeeded. So before comparing the payload it is first
     * attempted to parse the payload as parameter body.
     *
     * @param data
     *         the data to be used as matching
     *
     * @return this stubbing
     */
    public ResponseStubbing withPayload(byte[] data) {

        if (method == HttpMethod.GET) {
            throw new IllegalArgumentException("payload is not supported for get");
        }
        this.payload = Optional.ofNullable(data);
        return this;
    }

    /**
     * Defines static content to be served upon the request.
     *
     * @param someContent
     *         the content to be sent as response
     */
    public void respond(String someContent) {

        execute(x -> {
            try {
                x.getOutputStream().write(someContent.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Sets a handler for processing the request. The {@link HttpExchange} provides access to request and response
     * handles. This method can be used to define the reaction on the response and may produce dynamic output.
     *
     * @param exchangeHandler
     *
     * @return this stubbing
     */
    public void execute(Consumer<HttpExchange> exchangeHandler) {
        //TODO don't mix query params with non query params
        withParams(getQueryParams());
        this.server.addAction(getPath(), getPredicate(), exchangeHandler);

    }

    /**
     * Extracts the path without query
     *
     * @return
     */
    private String getPath() {

        return this.path.map(p -> p.split("\\?")[0]).orElse("/");
    }

    /**
     * Extract the query paramters from the path. If no path is set or no query parameters set, the map is empty.
     *
     * @return a map of key-value pairs resembling the query parameters
     */
    private Map<String, String> getQueryParams() {

        return this.path.map(p -> {
            int idx = p.indexOf('?');
            if (idx != -1) {
                return (Map<String, String>) Arrays.stream(p.substring(idx + 1).split("(&amp;|&)"))
                                                   .map(kv -> kv.split("="))
                                                   .collect(toMap(kv -> kv[0], kv -> kv[1]));
            }
            return null;
        }).orElse(Collections.emptyMap());
    }

    /**
     * Creates the predicate to for the current configuration of the stubbing to match incoming requests.
     *
     * @return the predicate to associate with the content handler.
     */
    private Predicate<HttpExchange> getPredicate() {
        return matchesMethod(method).and(matchesPath(getPath()))
                                    .and(matchesParams(params, method))
                                    .and(matchedPayload(payload));
    }

}
