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

package io.tourniquet.junit.jcr.rules;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoteContentRepositoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    /**
     * The class under test
     */
    @InjectMocks
    private RemoteContentRepository subject;

    @Test(expected = AssertionError.class)
    public void testBefore_invalidContainerQualifier() throws Throwable {
        //prepare
        System.setProperty("arquillian.launch", "& '1'='1'");
        URL arquillianContainerConfiguration = new URL("file:///notexisting");
        subject.onArquillianHost(arquillianContainerConfiguration);

        //act
        subject.before();

    }

    @Test
    public void testBefore_validContainerQualifier() throws Throwable {
        //prepare
        System.setProperty("arquillian.launch", "testContainer");
        URL arquillianContainerConfiguration = getClass().getResource("RemoteContentRepositoryTest_arquillian.xml");
        subject.onArquillianHost(arquillianContainerConfiguration);
        subject.setupManually();

        //act
        subject.before();

        //assert
        assertEquals("test.example.com", subject.getRemoteHost());
    }

    /**
     * This tests implements an exploit to the XML External Entity Attack {@see http://www.ws-attacks.org/index.php/XML_Entity_Reference_Attack}.
     * The attack targets a file in the filesystem containing a secret, i.e. a password, which is not untypical for
     * build servers containing passwords for artifact repositories. The attacking file defines an entity that resolves
     * to the file containing the secret. The entity (&amp;xxx;) is used in the xml file and can be read using xpath.
     * The RemoteContentRepository rule uses an arquillian.xml file to resolve the hostname of the target server using
     * an xpath expression. A test may use a specially prepared xml file and (optionally) an xpath injection to resolve
     * the contents of a system file. Of course this is more a hypothetical attack in a test framework as the attacker
     * may freely access the file directly with the same privileges as the user that started the JVM.
     *
     * @throws Throwable
     */
    @Test(expected = AssertionError.class)
    public void test_ExternalitEntityAttack_notVulnerable() throws Throwable {
        //prepare
        //the attacked file containing the secret
        final File attackedFile = folder.newFile("attackedFile.txt");
        try (FileOutputStream fos = new FileOutputStream(attackedFile)) {
            IOUtils.write("secretContent", fos);
        }
        //as attacker file we use a template and replacing a %s placeholder with the url of the attacked file
        //in a real-world attack we would use a valuable target such as /etc/passwd
        final File attackerFile = folder.newFile("attackerFile.xml");

        //load the template file from the classpath
        try (InputStream is = getClass().getResourceAsStream("RemoteContentRepositoryTest_attacker.xml");
             FileOutputStream fos = new FileOutputStream(attackerFile)) {

            final String attackerContent = prepareAttackerContent(is, attackedFile);
            IOUtils.write(attackerContent, fos);
        }

        System.setProperty("arquillian.launch", "testContainer");
        subject.onArquillianHost(attackerFile.toURI().toURL());
        subject.setupManually();

        //act
        subject.before();

        //assert
        //the content from the attacked file is put into the attacking xml where it resolves to the hostname
        assertEquals("secretContent", subject.getRemoteHost());
    }

    private String prepareAttackerContent(final InputStream templateInputStream, final File attackedFile)
            throws IOException {

        final StringWriter writer = new StringWriter();
        IOUtils.copy(templateInputStream, writer, Charset.defaultCharset());
        return String.format(writer.toString(), attackedFile.toURI().toURL());
    }
}
