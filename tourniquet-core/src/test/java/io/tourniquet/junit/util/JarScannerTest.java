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

package io.tourniquet.junit.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

/**
 *
 */
public class JarScannerTest {

    /**
     * The class under test
     */
    private JarScanner subject = new JarScanner();

    @Test
    public void testIgnore_list() throws Exception {
        //prepare
        URL jar1Url = JarScannerTest.class.getResource("test1.zip");
        URL jar2Url = JarScannerTest.class.getResource("test2.zip");
        subject.addJar(Arrays.asList(jar1Url, jar2Url));

        //act
        subject.ignore(Arrays.asList("META-INF", "io.devcon5.mixin"));

        //assert
        Collection<String> pkgs = subject.scanPackages();
        assertTrue(pkgs.contains("io.devcon5.classutils"));
        assertFalse(pkgs.contains("io.devcon5.mixin"));
        assertFalse(pkgs.contains("META-INF"));
    }

    @Test
    public void testIgnore_varargs() throws Exception {
        //prepare
        URL jar1Url = JarScannerTest.class.getResource("test1.zip");
        URL jar2Url = JarScannerTest.class.getResource("test2.zip");
        subject.addJar(Arrays.asList(jar1Url, jar2Url));

        //act
        subject.ignore("META-INF", "io.devcon5.mixin");

        //assert
        Collection<String> pkgs = subject.scanPackages();
        System.out.println(pkgs);
        assertTrue(pkgs.contains("io.devcon5.classutils"));
        assertFalse(pkgs.contains("io.devcon5.mixin"));
        assertFalse(pkgs.contains("META-INF"));
    }

    @Test
    public void testAddJar() throws Exception {
        //prepare
        URL jar1Url = JarScannerTest.class.getResource("test1.zip");
        URL jar2Url = JarScannerTest.class.getResource("test2.zip");

        //act
        subject.addJar(Arrays.asList(jar1Url, jar2Url));

        //assert
        Collection<String> pkgs = subject.scanPackages();
        assertTrue(pkgs.contains("io.devcon5.classutils"));
        assertTrue(pkgs.contains("io.devcon5.mixin"));
        assertFalse(pkgs.contains("META-INF"));
    }

    @Test
    public void testAddJar_varargs() throws Exception {
        //prepare
        URL jar1Url = JarScannerTest.class.getResource("test1.zip");
        URL jar2Url = JarScannerTest.class.getResource("test2.zip");

        //act
        subject.addJar(jar1Url, jar2Url);

        //assert
        Collection<String> pkgs = subject.scanPackages();
        assertTrue(pkgs.contains("io.devcon5.classutils"));
        assertTrue(pkgs.contains("io.devcon5.mixin"));
        assertFalse(pkgs.contains("META-INF"));
    }

    @Test
    public void testScanPackages() throws Exception {
        //prepare
        URL jarUrl = JarScannerTest.class.getResource("test1.zip");
        subject.addJar(jarUrl);

        //act
        Collection<String> pkgs = subject.scanPackages();

        //assert
        assertFalse(pkgs.contains("io"));
        assertFalse(pkgs.contains("io.devcon5"));
        assertTrue(pkgs.contains("io.devcon5.classutils"));
    }

    @Test
    public void testScanClasses() throws Exception {
        //prepare
        URL jarUrl = JarScannerTest.class.getResource("test1.zip");
        subject.addJar(jarUrl);

        //act
        Collection<String> classes = subject.scanClasses();

        //assert
        assertTrue(classes.contains("io.devcon5.classutils.ClassStreams"));
        assertFalse(classes.contains("io"));
        assertFalse(classes.contains("io.devcon5"));
        assertFalse(classes.contains("io.devcon5.classutils"));
    }
}
