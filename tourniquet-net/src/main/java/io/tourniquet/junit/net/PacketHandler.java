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

/**
 * A Handler that is invoked when a packet has been received.
 */
public interface PacketHandler {

    //TODO remove this class when migrating to Java8 and replace with consumer

    /**
     * Is invoked upon reception of a data packet.
     * @param data
     *  the received data. The array is a copy of the original data and may be kept as reference
     */
    void process(byte[] data);

}
