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

package io.tourniquet.junit.http.rules.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 */
public final class HttpClientHelper {

    private HttpClientHelper(){}

    public static BasicNameValuePair param(String name, String value) {

        return new BasicNameValuePair(name, value);
    }

    public static HttpPost post(String url, BasicNameValuePair... params) throws UnsupportedEncodingException {

        HttpPost post = new HttpPost(url);
        if(params.length > 0){
            post.setEntity(new UrlEncodedFormEntity(Arrays.asList(params)));
        }
        return post;
    }

    public static HttpPost post(String url, String content) throws UnsupportedEncodingException {

        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(content));
        return post;
    }

    public static HttpPost post(String url, byte[] data) throws UnsupportedEncodingException {

        HttpPost post = new HttpPost(url);
        post.setEntity(new ByteArrayEntity(data));
        return post;
    }

    public static HttpPost post(String url, InputStream data) throws UnsupportedEncodingException {

        HttpPost post = new HttpPost(url);
        post.setEntity(new InputStreamEntity(data));
        return post;
    }

    public static HttpGet get(String url){
        return new HttpGet(url);
    }

    public static String getString(final HttpEntity entity) throws IOException {

        return IOUtils.toString(entity.getContent(), Charset.defaultCharset());
    }
}
