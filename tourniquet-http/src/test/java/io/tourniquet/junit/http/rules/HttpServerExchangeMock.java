package io.tourniquet.junit.http.rules;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.undertow.connector.ByteBufferPool;
import io.undertow.connector.PooledByteBuffer;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;

/**
 * Mock for initializing a HttpServerExchange
 */
public class HttpServerExchangeMock implements TestRule {

    private ServerConnection serverConnection;

    private ByteBufferPool byteBufferPool;

    private PooledByteBuffer pooledByteBuffer;

    private HttpServerExchange exchange;

    private ByteBuffer buffer;

    @Override
    public Statement apply(Statement base, Description description) {

        this.serverConnection = mock(ServerConnection.class);
        this.byteBufferPool = mock(ByteBufferPool.class);
        this.pooledByteBuffer = mock(PooledByteBuffer.class);

        this.buffer = ByteBuffer.allocate(8);
        this.exchange = new HttpServerExchange(serverConnection);
        when(serverConnection.getByteBufferPool()).thenReturn(byteBufferPool);
        when(byteBufferPool.allocate()).thenReturn(pooledByteBuffer);
        when(pooledByteBuffer.getBuffer()).thenReturn(buffer);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
            }
        };
    }

    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public ByteBufferPool getByteBufferPool() {
        return byteBufferPool;
    }

    public PooledByteBuffer getPooledByteBuffer() {
        return pooledByteBuffer;
    }

    public HttpServerExchange getExchange() {
        return exchange;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
