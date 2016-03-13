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

package io.tourniquet.junit.net;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URL;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.slf4j.Logger;

/**
 * Matcher for verifying a a resource is available. Supported resources are <ul> <li>local TCP ports</li> <li>URLs</li>
 * </ul>
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class ResourceAvailabilityMatcher<RESOURCE> extends BaseMatcher<RESOURCE> {

    private static final Logger LOG = getLogger(ResourceAvailabilityMatcher.class);

    @Override
    public boolean matches(final Object item) {

        if (item == null) {
            return false;
        }

        final boolean result;

        if (item instanceof NetworkPort) {
            result = this.isAvailable((NetworkPort) item);
        } else if (item instanceof URL) {
            result = this.isAvailable((URL) item);
        } else {
            result = false;
        }
        return result;

    }

    /**
     * Checks the availability of a tcpPort by checking if a {@link ServerSocket} is already bound.
     *
     * @param port
     *         the port to check
     *
     * @return <code>true</code> if the port is available
     */
    protected boolean isAvailable(final NetworkPort port) {

        int portNumber = port.getPortNumber();

        try {
            try (ServerSocket tcp = new ServerSocket(portNumber);
                 DatagramSocket udp = new DatagramSocket(portNumber)) {
                return tcp.isBound() && udp.isBound();
            }
        } catch (IOException e) { //NOSONAR
            LOG.debug("Port {} not available", port.getSocketAddress(), e);
            return false;
        }
    }

    /**
     * Checks the availability of a URL by openening a reading stream on it.
     *
     * @param url
     *         the url to check
     *
     * @return <code>true</code> if the URL can be read
     */
    protected boolean isAvailable(URL url) {

        try (InputStream inputStream = url.openStream()) {
            return true;
        } catch (IOException e) { //NOSONAR
            LOG.debug("URL {} not available", url, e);
            return false;
        }
    }

    @Override
    public void describeTo(final Description description) {

        description.appendText("resource is available");
    }
}
