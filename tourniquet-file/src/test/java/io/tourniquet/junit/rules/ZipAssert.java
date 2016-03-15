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

package io.tourniquet.junit.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

public final class ZipAssert {
    private ZipAssert(){}

    /**
     * Verifies if a folder with the specified path exists
     * @param zf
     *  the zip file to search for the entry
     * @param entryPath
     *  the path to the folder entry. Note that it is required that the path ends with '/' otherwise it can't
     *  be recognized as a directory, even though the entry may be found with the trailing slash
     */
    public static void assertZipFolderExists(final ZipFile zf, final String entryPath) {
        final String folderPath;
        if(entryPath.endsWith("/")){
            folderPath = entryPath;
        } else {
            folderPath = entryPath + '/';
        }
        final ZipEntry entry = zf.getEntry(folderPath);
        assertNotNull("Entry " + folderPath + " does not exist", entry);
        assertTrue("Entry "+folderPath+" is no folder", entry.isDirectory());
    }

    /**
     * Verifies the zip file contains an entry with the specified character content
     * @param zf
     *  the zip file to be searched
     * @param entryPath
     *  the path to the entry to be verified, the path must not start with a '/'
     * @param expectedContent
     *  the content as a string that is expected to be the content of the file.
     * @throws java.io.IOException
     */
    public static void assertZipContent(final ZipFile zf, final String entryPath, final String expectedContent)
            throws IOException {

        final ZipEntry entry = zf.getEntry(entryPath);
        assertNotNull("Entry " + entryPath + " does not exist", entry);
        assertFalse("Entry "+entryPath+" is a directory", entry.isDirectory());
        try(InputStream is = zf.getInputStream(entry)){
            assertNotNull("Entry " + entryPath + " has no content" , is);
            final String actualContent = IOUtils.toString(is);
            assertEquals(expectedContent, actualContent);
        }
    }

}
