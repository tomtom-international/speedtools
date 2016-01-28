/*
 * Copyright (C) 2012-2016. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.testutils.akka;

import akka.actor.ActorPath;
import akka.actor.ActorRef;

import javax.annotation.Nonnull;

/**
 * Mock implementation of ActorRef.
 */
public class MockActorRef extends ActorRef {
    private final ActorPath actorPath;

    public MockActorRef(@Nonnull final ActorPath actorPath) {
        super();
        assert actorPath != null;
        this.actorPath = actorPath;
    }

    public MockActorRef() {
        this(new MockActorPath());
    }

    @Override
    @Nonnull
    public ActorPath path() {
        return actorPath;
    }

    @Deprecated
    @Override
    public boolean isTerminated() {
        return false;
    }
}
