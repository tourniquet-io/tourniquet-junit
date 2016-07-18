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

package io.tourniquet.selenium;

import static org.junit.Assert.assertEquals;

import io.tourniquet.selenium.User;
import org.junit.Test;

/**
 *
 */
public class UserTest {

    /**
     * The class under test
     */
    private User subject = new User("user", "password");


    @Test
    public void testGetUsername() throws Exception {
        assertEquals("user", subject.getUsername());
    }

    @Test
    public void testGetPassword() throws Exception {
        assertEquals("password", subject.getPassword());

    }
}
