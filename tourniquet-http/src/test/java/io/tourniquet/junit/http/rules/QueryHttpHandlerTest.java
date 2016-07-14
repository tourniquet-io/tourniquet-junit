package io.tourniquet.junit.http.rules;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.undertow.server.HttpHandler;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryHttpHandlerTest {

    @Mock
    private HttpHandler defaultHandler;

    @Rule
    public HttpServerExchangeMock exchange = new HttpServerExchangeMock();

    /**
     * The class under test
     */
    @InjectMocks
    private QueryHttpHandler subject;

    @Test
    public void testHandleRequest_default() throws Exception {
        //arrange

        //act
        subject.handleRequest(exchange.getExchange());

        //assert
        verify(defaultHandler).handleRequest(exchange.getExchange());
    }

    @Test
    public void testRegisterQueryHandler_emptyRequestQuery_useDefault() throws Exception {

        //arrange
        HttpHandler queryHandler = mock(HttpHandler.class);

        //act
        subject.registerQueryHandler("param=value", queryHandler);
        subject.handleRequest(exchange.getExchange());

        //assert
        verify(defaultHandler).handleRequest(exchange.getExchange());
    }

    @Test
    public void testRegisterQueryHandler_matchingRequestQuery_useQueryHandler() throws Exception {

        //arrange
        HttpHandler queryHandler = mock(HttpHandler.class);
        exchange.getExchange().setQueryString("param=value");

        //act
        subject.registerQueryHandler("param=value", queryHandler);

        subject.handleRequest(exchange.getExchange());

        //assert
        verify(queryHandler).handleRequest(exchange.getExchange());
        verify(defaultHandler, times(0)).handleRequest(exchange.getExchange());
    }

    @Test
    public void testRegisterQueryHandler_notMatchingRequestQuery_useNoHandler() throws Exception {

        //arrange
        HttpHandler queryHandler = mock(HttpHandler.class);
        exchange.getExchange().setQueryString("param=value");

        //act
        subject.registerQueryHandler("other=value", queryHandler);

        subject.handleRequest(exchange.getExchange());

        //assert
        verify(queryHandler, times(0)).handleRequest(exchange.getExchange());
        verify(defaultHandler, times(0)).handleRequest(exchange.getExchange());
    }


}
