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

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * A TCP port that describes a remote address and a port number.
 * Created by <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a> on 3/12/2015
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class RemoteNetworkPort extends NetworkPort {

    private final String hostname;

    public RemoteNetworkPort(final String hostname, final int port, final Type type) {
        super(port,type);
        this.hostname = hostname;

    }

    @Override
    public SocketAddress getSocketAddress() {

        return new InetSocketAddress(getHostname(), getPortNumber());
    }

    /**
     * The hostname for the remote port
     * @return
     *  the name of the remote host
     */
    public String getHostname() {

        return hostname;
    }

    @Override
    public String toString() {

        return getType().name().toLowerCase() + ":" + getHostname() + ":" + getPortNumber();
    }
}
