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

package io.tourniquet.junit.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.security.auth.Subject;
import java.security.Principal;
import org.junit.Test;

public class SecurityTestHelperTest {

    @Test
    public void testToPrincipal() throws Exception {
        //prepare
        final String userName = "user1";

        //act
        Principal principal = SecurityTestHelper.toPrincipal(userName);

        //assert
        assertNotNull(principal);
        assertEquals(userName, principal.getName());

    }

    @Test
    public void testSubjectForUser_byName() throws Exception {

        //prepare
        String userName = "user1";

        //act
        Subject subject = SecurityTestHelper.subjectForUser(userName);

        //assert
        assertNotNull(subject);
        assertFalse(subject.isReadOnly());
        assertTrue(subject.getPrivateCredentials().isEmpty());
        assertTrue(subject.getPublicCredentials().isEmpty());
        assertEquals(userName, subject.getPrincipals().iterator().next().getName());

    }

    @Test
    public void testSubjectForUser_byPrincipal() throws Exception {

        //prepare
        Principal user = new SimpleUserPrincipal("user1");

        //act
        Subject subject = SecurityTestHelper.subjectForUser(user);

        //assert
        assertNotNull(subject);
        assertFalse(subject.isReadOnly());
        assertTrue(subject.getPrivateCredentials().isEmpty());
        assertTrue(subject.getPublicCredentials().isEmpty());
        assertEquals("user1", subject.getPrincipals(SimpleUserPrincipal.class).iterator().next().getName());

    }
}
