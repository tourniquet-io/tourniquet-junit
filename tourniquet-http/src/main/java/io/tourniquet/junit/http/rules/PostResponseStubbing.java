package io.tourniquet.junit.http.rules;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class for fluently create http POST responses.
 */
public class PostResponseStubbing {

    private final HttpServer server;
    private Map<String, String> params;
    private byte[] refPayload;
    private String path;

    PostResponseStubbing(HttpServer server) {

        this.server = server;
    }

    /**
     * Defines on which request parameters the handler should respond. If no parameters are defined, the handler
     * responds to all requests. The specified parameters must be contained in the request, if the request contains more
     * parameters, they are ignored.
     *
     * @param params
     *         the parameters that activate the handler.
     *
     * @return this stubbing.
     */
    public PostResponseStubbing withParams(Map<String, String> params) {

        this.params = params;
        return this;
    }

    /**
     * Defines the payload that triggers the response. If not specified, the handler will respond to all payloads
     *
     * @param data
     *         the data to be used as matching
     *
     * @return this stubbing
     */
    public PostResponseStubbing withPayload(byte[] data) {

        this.refPayload = data;
        return this;
    }

    /**
     * Static content to be returned to the client.
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
     * Sets a handler for processing the request. If defining an execution consumer, the
     *
     * @param exchangeHandler
     *
     * @return this stubbing
     */
    public void execute(Consumer<HttpExchange> exchangeHandler) {

        this.server.addAction(this.path, getPredicate(), exchangeHandler);

    }

    /**
     * Creates the predicate to for the current configuration of the stubbing to match incoming requests.
     *
     * @return the predicate to associate with the content handler.
     */
    private Predicate<HttpExchange> getPredicate() {

        Predicate<HttpExchange> predicate = x -> true;
        if (this.params != null) {
            predicate = predicate.and(x -> params.entrySet().stream().allMatch(matchesParam(getFormParams(x))));
        }
        if (this.refPayload != null) {
            predicate = predicate.and(x -> Arrays.equals(refPayload, x.getPayload()));
        }
        return predicate;
    }

    private Map<String, String> getFormParams(final HttpExchange x) {

        return Stream.of(new String(x.getPayload()).split("&"))
                     .map(nameValuePair -> nameValuePair.split("="))
                     .collect(Collectors.toMap(s -> s[0], s -> s[1]));
    }

    /**
     * Sets the resource that should be requested via GET, as in <pre>
     *     GET /pathToResource HTTP/1.1
     * </pre>
     *
     * @param resource
     *         the path to the resource
     *
     * @return this stubbing
     */
    PostResponseStubbing resource(final String resource) {

        this.path = resource;
        return this;
    }

    private static Predicate<Map.Entry<String, String>> matchesParam(Map<String, String> requestParams) {

        return e -> requestParams.containsKey(e.getKey()) && requestParams.get(e.getKey()).equals(e.getValue());
    }

}
