/*
 * Copyright (C) 2012-2019, TomTom NV (http://tomtom.com).
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

package com.tomtom.speedtools.services.sms.implementation.messagebird;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Resteasy resource to call the MessageBird REST api. Currently only supports sending simple text messages. timestamp
 * and inbox have been omitted from this api (they are optional).
 *
 * The baseUrl can be obtained using the property SMSProvider.MessageBird.baseUrl.
 */
@Path("/api/sms")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public interface MessageBirdResource {

    @XmlEnum
    public enum ResponseType {
        @XmlEnumValue("XML")
        XML     // XML.
    }

    /**
     * This operation sends an SMS text message using MessageBird.
     *
     * @param userName     Username of messageBird account.
     * @param password     Password of MessageBird account.
     * @param reference    Unique reference between 1 and 20 digits.
     * @param sender       The number of the sender. Since inbox is omitted, this field is compulsory.
     * @param destination  Recipient as MSISDN encoded number (i.e., without +, and 00).
     * @param body         Body of the text message (with a maximum length of 670 characters), UTF-8 encoded.
     * @param responseType Response type.
     * @param replaceChars Replace characters with GSM7 equivalents.  Optional, default <b>true</b>.
     * @return Response.
     */
    @GET
    Response sendMessage(
            // NotNuls allowed because resource is used as client!
            @Nonnull @QueryParam("username") String userName,
            @Nonnull @QueryParam("password") String password,
            @Nullable @QueryParam("reference") Long reference,
            @Nonnull @QueryParam("sender") String sender,
            @Nonnull @QueryParam("destination") String destination,
            @Nonnull @QueryParam("body") String body,
            @Nonnull @QueryParam("responsetype") ResponseType responseType,
            @Nullable @QueryParam("replaceChars") String replaceChars
    );
}
