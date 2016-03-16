package io.tourniquet.junit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/**
 *
 */
public class TestClassLoaderTest {

    private URL testJar = new ResourceResolver(true).resolve("test1.zip");

    @Test
    public void testLoadClass_testClass() throws Exception {

        //prepare
        TestClassLoader cl = new TestClassLoader(Arrays.asList(testJar));

        //act
        Class cls1 = cl.loadClass("io.devcon5.classutils.ClassStreams");
        Class cls2 = cl.loadClass("io.devcon5.classutils.ClassStreams");

        //assert
        assertEquals(cl, cls1.getClassLoader());
        assertEquals(cls1.getClassLoader(), cls2.getClassLoader());
        assertSame(cls1, cls2);
    }

    @Test
    public void testLoadClass_nonTestClass() throws Exception {

        //prepare
        TestClassLoader cl = new TestClassLoader(Collections.singletonList(testJar));

        //act
        Class cls1 = cl.loadClass(TestClassLoaderTest.class.getName());
        Class cls2 = cl.loadClass(TestClassLoaderTest.class.getName());

        //assert
        assertEquals(cls1.getClassLoader(), cl);
        assertSame(cls1, cls2);
    }
}
