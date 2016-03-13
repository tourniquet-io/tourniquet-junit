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

package io.tourniquet.junit.matchers;

import static org.slf4j.LoggerFactory.getLogger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.slf4j.Logger;

/**
 * Created by Gerald Muecke on 03.12.2015.
 */
public class DateFormatMatcher extends BaseMatcher<String>{

    private static final Logger LOG = getLogger(DateFormatMatcher.class);

    private final DateFormat dateFormat;
    private final String strDateFormat;

    public DateFormatMatcher(final String dateFormatPattern) {
        try {
            this.strDateFormat = dateFormatPattern;
            this.dateFormat = new SimpleDateFormat(dateFormatPattern, Locale.getDefault());
        } catch (IllegalArgumentException e){
            throw new AssertionError("Invalid Date Format " + dateFormatPattern,e);
        }
    }

    public static DateFormatMatcher matchesDateFormat(String dateFormatPattern){
        return new DateFormatMatcher(dateFormatPattern);
    }

    @Override
    public boolean matches(final Object object) {
        if(object instanceof String){
            return matchesDateString((String)object);
        }
        return false;
    }

    private boolean matchesDateString(final String dateString) {

        try {
            dateFormat.parse(dateString);
            return true;
        } catch (ParseException e) {
            LOG.warn("{}", e);
            return false;
        }
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("matches format " + strDateFormat);
    }
}
