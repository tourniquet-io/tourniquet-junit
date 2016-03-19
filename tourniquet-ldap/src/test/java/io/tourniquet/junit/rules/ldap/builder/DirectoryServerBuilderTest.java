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

package io.tourniquet.junit.rules.ldap.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import io.tourniquet.junit.net.NetworkMatchers;
import io.tourniquet.junit.net.NetworkUtils;
import io.tourniquet.junit.rules.ldap.DirectoryServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.tourniquet.junit.rules.ldap.Directory;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryServerBuilderTest {

    public TemporaryFolder folder = new TemporaryFolder();

    public Directory dir = new Directory(folder);

    @Rule
    public RuleChain chain = RuleChain.outerRule(folder).around(dir);

    @Mock
    private Description description;


    private DirectoryServerBuilder subject;

    @Before
    public void setUp() throws Exception {

        subject = new DirectoryServerBuilder(dir);
    }

    @Test
    public void testBuild() throws Exception {
        //prepare

        //act
        DirectoryServer ds = subject.build();
        //assert
        assertNotNull(ds);
        //default values
        assertEquals(10389, ds.getTcpPort());
        assertNull(ds.getListenAddress());
    }

    @Test
    public void onAvailablePort() throws Throwable {
        //prepare

        //act
        DirectoryServerBuilder builder = subject.onAvailablePort();

        //assert
        assertSame(builder, subject);
        final DirectoryServer ds = subject.build();

        final AtomicInteger port1 = new AtomicInteger();
        final AtomicInteger port2 = new AtomicInteger();

        //apply the rule twice
        ds.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                port1.set(ds.getTcpPort());
                assertThat(NetworkMatchers.remotePort("localhost", port1.get()), NetworkMatchers.isReachable());

            }
        }, description).evaluate();

        ds.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                port2.set(ds.getTcpPort());
                assertThat(NetworkMatchers.remotePort("localhost", port2.get()), NetworkMatchers.isReachable());

            }
        }, description).evaluate();

        //on each application a new port should have been used
        assertNotNull(port1.get());
        assertNotNull(port2.get());
        assertNotEquals(port1.get(), port2.get());
    }

    @Test
    public void testOnPort() throws Throwable {
        //prepare

        final int port = NetworkUtils.findAvailablePort();

        //act
        DirectoryServerBuilder builder = subject.onPort(port);

        //assert
        assertSame(builder, subject);
        DirectoryServer ds = subject.build();
        assertEquals(port, ds.getTcpPort());
        ds.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                assertThat(NetworkMatchers.remotePort("localhost", port), NetworkMatchers.isReachable());

            }
        }, description).evaluate();

    }

    @Test
    public void testOnListenAddress() throws Throwable {
        //prepare
        String address = "myhost";
        final int port = NetworkUtils.findAvailablePort();
        subject.onPort(port);

        //act
        DirectoryServerBuilder builder = subject.onListenAddress(address);

        //assert
        assertSame(builder, subject);
        DirectoryServer ds = subject.build();
        assertEquals("myhost", ds.getListenAddress());

        ds.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                assertThat(NetworkMatchers.remotePort("localhost", port), NetworkMatchers.isReachable());

            }
        }, description).evaluate();
    }

}
