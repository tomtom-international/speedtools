/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.services.sms.implementation.nexmo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tomtom.speedtools.apivalidation.ApiDTO;
import com.tomtom.speedtools.objects.Immutables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Response from the Nexmo REST API.
 */

// Ignore unexpected properties because interface specification is not under our control.
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class NexmoMessageResponse extends ApiDTO {

    @Nullable
    private Integer messageCount;
    @Nullable
    private List<NexmoMessage> messages;

    public NexmoMessageResponse(@Nonnull final Collection<NexmoMessage> messages) {
        super();
        assert messages != null;

        this.messageCount = messages.size();
        this.messages = Immutables.listOf(messages);
    }

    // Normally unused (required for mock and JAX-B).
    public NexmoMessageResponse() {
        super();
    }

    @Override
    public void validate() {
        validator().start();
        validator().checkNotNull(true, "messageCount", messageCount);
        validator().checkNotNullAndValidateAll(true, "messages", messages);
        validator().done();
    }

    @XmlElement(name = "message-count", required = true)
    public int getMessageCount() {
        beforeGet();
        assert messageCount != null;
        return messageCount;
    }

    public void setMessageCount(@Nullable final Integer messageCount) {
        beforeSet();
        this.messageCount = messageCount;
    }

    @XmlElement(name = "messages", required = true)
    @Nonnull
    public List<NexmoMessage> getMessages() {
        beforeGet();
        assert messages != null;
        return messages;
    }

    public void setMessages(@Nullable final List<NexmoMessage> messages) {
        beforeSet();
        this.messages = (messages == null) ? Collections.<NexmoMessage>emptyList() : messages;
    }
}
