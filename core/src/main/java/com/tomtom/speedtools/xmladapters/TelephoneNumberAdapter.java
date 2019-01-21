/*
 * Copyright (C) 2012-2017. TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.xmladapters;


import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * This adapter implementation makes sure that the String representation of the telephone number is formatted properly.
 */
public class TelephoneNumberAdapter extends XmlAdapter<String, String> {

    @Override
    @Nullable
    public String unmarshal(@Nullable final String v) throws NumberParseException {
        if (v == null) {
            return null;
        }
        return format(v);
    }

    @Override
    @Nullable
    public String marshal(@Nullable final String v) throws NumberParseException {
        if (v == null) {
            return null;
        }
        return format(v);
    }

    @Nonnull
    public static String format(@Nonnull final String value) throws NumberParseException {
        assert value != null;

        final PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        final PhoneNumber phone; // Only international formatted numbers are allowed.
        phone = util.parse(value, null);
        return util.format(phone, PhoneNumberFormat.E164);
    }
}
