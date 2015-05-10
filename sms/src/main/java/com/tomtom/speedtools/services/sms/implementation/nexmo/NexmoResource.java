/*
 * Copyright (C) 2015. TomTom International BV (http://tomtom.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomtom.speedtools.services.sms.implementation.nexmo;

import com.tomtom.speedtools.services.sms.implementation.nexmo.dto.NexmoMessageResponse;
import org.jboss.resteasy.annotations.ClientResponseType;
import org.jboss.resteasy.client.ClientResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Resteasy resource to call the Nexmo REST api.  Currently only supports sending simple text messages.  Parameters for
 * vcards, vcals, etc, have been omitted (they are optional).
 *
 * The baseUrl can be obtained using the property SMSProvider.Nexmo.baseUrl.
 */
@Path("/sms/json")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public interface NexmoResource {

    /**
     * This operation sends an SMS text message using Nexmo.
     *
     * @param userName             Your API Key. Ex: username=n3xm0rocks.
     * @param password             Your API Secret. Ex: password=12ab34cd.
     * @param from                 Sender address could be alphanumeric (Ex: from=MyCompany20), restrictions may apply
     *                             depending on the destination.
     * @param to                   Mobile number in international format and one recipient per request. Ex:
     *                             to=447525856424 or to=00447525856424 when sending to UK.
     * @param type                 This can be omitted text (default), unless sending a Binary (binary), WAP Push
     *                             (wappush), Unicode message (unicode), vcal (vcal) or vcard (vcard). This should be of
     *                             an enum type, however, resteasy seems to ignore the annotations, and uses the java
     *                             name as string (in capitals in this case), which is not correct.
     * @param text                 Body of the text message (with a maximum length of 3200 characters), UTF-8 encoded.
     *                             Ex: text=Hello+World!
     * @param statusReportRequired Set to 1 if you want to receive a delivery report (DLR) for this request. Make sure
     * @param clientReference      Client reference number.
     * @return Response.
     */
    @GET
    @ClientResponseType(entityType = NexmoMessageResponse.class)
    ClientResponse<NexmoMessageResponse> sendMessage(
            // NotNulls allowed because resource is used as client!
            @Nonnull @QueryParam("username") String userName,
            @Nonnull @QueryParam("password") String password,
            @Nonnull @QueryParam("from") String from,
            @Nonnull @QueryParam("to") String to,
            @Nullable @QueryParam("type") String type,
            @Nullable @QueryParam("text") String text,
            @Nullable @QueryParam("status-report-req") String statusReportRequired,
            @Nullable @QueryParam("client-ref") Long clientReference
    );
}
