/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tourniquet.junit.http.rules;

import static java.util.stream.Collectors.groupingBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Predicates for matching incoming requests
 */
final class HttpPredicates {

    private HttpPredicates(){}

    /**
     * Predicate to match the incoming request's method
     * @param method
     *  the method an incoming request has to have
     * @return
     *  the predicate
     */
    public static Predicate<HttpExchange> matchesMethod(final HttpMethod method) {
        return x -> x.getRequestMethod().equals(method.toString());
    }

    /**
     * Predicate to match the query string of an incoming request with the specified query. As query are optional
     * elements of a query, the expected query may be optional as well. If it is empty, the predicate always
     * evaluates to true.
     * @param query
     *  the expected query of the request uri
     * @return
     *  the predicate
     */
    public static Predicate<HttpExchange> matchesQuery(final Optional<String> query) {
        return x -> query.map(q -> q.equals(x.getQueryString())).orElse(true);
    }

    /**
     * Predicate to match the parameters of a request. If the request is a GET request, the query parameters
     * are checked. If it is a POST request, the content of the request is parsed to form parameters and compared
     * with the expected parameters. All expected parameters defined has to be contained in the request. If the
     * request contains more parameters they are ignored.
     * @param params
     *  the expected parameters
     * @param method
     *  the expected http method
     * @return
     *  the predicate
     */
    public static Predicate<HttpExchange> matchesParams(Map<String,List<String>> params, HttpMethod method) {

        return x -> params.entrySet().stream().allMatch(matchesParam(() -> {
            if (method == HttpMethod.POST) {
                return getFormParams(x);
            } else {
                return getQueryParams(x);
            }
        }));
    }

    private static Predicate<Map.Entry<String, List<String>>> matchesParam(Supplier<Map<String, List<String>>> requestParams) {

        final Map<String, List<String>> params = requestParams.get();
        return e -> params.containsKey(e.getKey()) && params.get(e.getKey()).stream().anyMatch(s -> e.getValue().contains(s));
    }

    private static Map<String,List<String>> getQueryParams(final HttpExchange x) {
        String query = x.getQueryString();
        if (query.matches("[^\\s&=]+=[^&=]*((&amp;|&)[^\\s&=]+=[^&=]*)*")) {
            return Stream.of(query.split("&amp;|&"))
                         .map(nameValuePair -> nameValuePair.split("="))
                         .collect(groupingBy(s -> s[0], Collectors.mapping(s -> s[1], Collectors.toList())));
        }
        return Collections.emptyMap();
    }

    private static Map<String, List<String>> getFormParams(final HttpExchange x) {
        String payload = new String(x.getPayload());
        if (payload.matches("[^\\s&=:]+(:|=).*((&amp;|&|\\n)[^\\s&=:]+(:|=).*)*")) {
            return Stream.of(payload.split("&amp;|&|\\n"))
                         .map(nameValuePair -> nameValuePair.split(":|="))
                         .collect(groupingBy(s -> s[0], Collectors.mapping(s -> s[1], Collectors.toList())));
        }
        return Collections.emptyMap();
    }

    /**
     * Predicate to match the payload of a request. The content of the request is byte-wise compared agains the
     * expected payload. As payloads are optional, the expected payload is optional, too, and can be empty. If the
     * optional is empty, all requests match.
     * @param payload
     *  the expected payload
     * @return
     *  the predicate
     */
    public static Predicate<HttpExchange> matchedPayload(Optional<byte[]> payload) {

        return x -> payload.map(pl -> Arrays.equals(pl, x.getPayload())).orElse(true);
    }

    /**
     * Predicate to match the resource path of a request. The path should not contain a query part.
     * @param path
     *  the expected path to match the query
     * @return
     *  the predicate
     */
    public static Predicate<HttpExchange> matchesPath(final String path) {

        return x -> x.getRequestPath().equals(path);
    }
}
