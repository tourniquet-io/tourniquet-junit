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

package io.tourniquet.junit.pdf;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.preflight.utils.ByteArrayDataSource;

/**
 * An abstract handle of a PDF file to be used with matchers.
 */
public class PDF {

    private final Object source;

    private PDF(Path path){
        this.source = path;
    }

    private PDF(final byte[] bytes) { //NOSONAR
        this.source = new  byte[bytes.length];
        System.arraycopy(bytes, 0, this.source, 0, bytes.length);
    }

    private PDF(final URL url) {
        this.source = url;
    }

    public PDF(final InputStream inStream) {
        this.source = inStream;
    }

    /**
     * Creates a PDF handle from a file in the file system.
     * @param file
     *  the file to read PDF data from
     * @return
     *  the PDF handle for the file
     */
    public static PDF of(File file){ //NOSONAR
        return new PDF(file.toPath());
    }

    /**
     * Creates a PDF handle form a file in the file system.
     * @param path
     *  the path to the file
     * @return
     *  the PDF handle for the file.
     */
    public static PDF of(Path path){ //NOSONAR
        return new PDF(path);
    }

    /**
     * Creates a PDF handle from a file in the filesystem.
     * @param path
     *  a path to the filesystem.
     * @return
     *  the PDF handle for the file
     */
    public static PDF of(String path){ //NOSONAR
        return new PDF(Paths.get(path));
    }

    /**
     * Creates a PDF handle from an InputStream.
     * @param inStream
     *  the input stream to read the pdf from
     * @return
     *  the PDF handle for the data
     * @throws IOException
     */
    public static PDF of(InputStream inStream) throws IOException { //NOSONAR
        return new PDF(inStream);
    }

    /**
     * Creates a PDF handle from binary data
     * @param data
     *  the PDF data
     * @return
     *  the PDF handle for the data
     */
    public static PDF of(byte[] data){ //NOSONAR
        return new PDF(data);
    }

    /**
     * Creates a PDF handle for the URL source.
     * @param url
     *  the url to read the pdf data from
     * @return
     *  the PDF handle for the data
     */
    public static PDF of(URL url){ //NOSONAR
        return new PDF(url);
    }

    /**
     * Opens an input stream to read the PDF data.
     * @return
     *  an InputStream on the PDF data. The input stream can be of any type, it is not guaranteed it is actual PDF
     *  data.
     * @throws IOException
     */
    public InputStream openStream() throws IOException {
        final InputStream result;
        if(this.source instanceof byte[]) {
            result = new ByteArrayInputStream((byte[])this.source);
        } else
        if(this.source instanceof Path) {
            result = Files.newInputStream((Path)this.source);
        } else
        if(this.source instanceof URL) {
            result = ((URL)this.source).openStream();
        } else
        if(this.source instanceof InputStream){
            result = (InputStream)this.source;
        } else {
            throw new IllegalArgumentException("PDF source is null");
        }
        return result;
    }

    /**
     * Creates a datasource on the PDF source so that it's input stream can be obtained. Not all sources support to open
     * an output stream.
     *
     * @return a datasource for the PDF document.
     *
     * @throws IOException
     */
    public DataSource toDataSource() throws IOException {
        final DataSource result;
        if (this.source instanceof byte[] || this.source instanceof InputStream) {
            result = new ByteArrayDataSource(openStream());
        } else
        if (this.source instanceof Path) {
            result = new FileDataSource(((Path) this.source).toFile());
        } else
        if (this.source instanceof URL) {
            result = new URLDataSource((URL) this.source);
        } else {
            throw new IllegalArgumentException("PDF source is null");
        }
        return result;
    }

    @Override
    public String toString() {
        return this.source.toString();
    }
}
