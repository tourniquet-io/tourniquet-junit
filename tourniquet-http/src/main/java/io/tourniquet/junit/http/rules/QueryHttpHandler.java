package io.tourniquet.junit.http.rules;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * A handler that dispatches requests to different handlers depending on a query string. The handler only checks
 * for opaque query strings and does not parse the queries. If no query is defined, the default handler of the
 * query handler handles the request. If a query is defined and the incoming request's query match, it is dispatched
 * otherwise nothing is responded.
 */
public class QueryHttpHandler implements HttpHandler {

    /**
     * Map of opaque query strings to dedicated query handlers
     */
    private final Map<String, HttpHandler> queryHandlers = new ConcurrentHashMap<>();

    /**
     * The default handler that processes the incoming query
     */
    private final HttpHandler defaultHandler;


    public QueryHttpHandler(HttpHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {

        final String queryString = httpServerExchange.getQueryString();
        if(queryString.isEmpty() || this.queryHandlers.isEmpty()){
            this.defaultHandler.handleRequest(httpServerExchange);
        } else if(this.queryHandlers.containsKey(queryString)){
            this.queryHandlers.get(queryString).handleRequest(httpServerExchange);
        }
    }

    /**
     * Advises the handler to consider request queries that match the specified query.
     * A handler implementation may ignore any request queries.
     * @param query
     *  the query to consider when processing incoming request with a request query
     *  @param handler
     *   a supplier of a handler that processes the request if the incoming request matches the query
     * @return
     *  this handler
     */
    public HttpHandler registerQueryHandler(String query, Supplier<? extends HttpHandler> handler) {
        this.queryHandlers.put(query, handler.get());
        return this;
    }
}
