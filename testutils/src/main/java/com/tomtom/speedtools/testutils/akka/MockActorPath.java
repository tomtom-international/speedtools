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

package com.tomtom.speedtools.testutils.akka;

import akka.actor.ActorPath;
import akka.actor.Address;
import akka.actor.RootActorPath;

import javax.annotation.Nullable;

/**
 * Mock implementation of ActorPath.
 */
@SuppressWarnings({"DollarSignInName", "ComparableImplementedButEqualsNotOverridden"})
public class MockActorPath implements ActorPath {
    @Override
    @Nullable
    public Address address() {
        return null;
    }

    @Override
    @Nullable
    public String name() {
        return null;
    }

    @Override
    @Nullable
    public ActorPath parent() {
        return null;
    }

    @Override
    @Nullable
    public ActorPath $div(@Nullable final String child) {
        return null;
    }

    @Override
    @Nullable
    public ActorPath child(@Nullable final String child) {
        return null;
    }

    @Override
    @Nullable
    public ActorPath $div(@Nullable final scala.collection.Iterable<String> child) {
        return null;
    }

    @Override
    @Nullable
    public ActorPath descendant(@Nullable final Iterable<String> names) {
        return null;
    }

    @Override
    @Nullable
    public scala.collection.immutable.Iterable<String> elements() {
        return null;
    }

    @Override
    @Nullable
    public Iterable<String> getElements() {
        return null;
    }

    @Override
    @Nullable
    public RootActorPath root() {
        return null;
    }

    @Nullable
    @Override
    public String toStringWithoutAddress() {
        return null;
    }

    @Override
    @Nullable
    public String toStringWithAddress(@Nullable final Address address) {
        return null;
    }

    @Override
    @Nullable
    public String toSerializationFormat() {
        return null;
    }

    @Override
    @Nullable
    public String toSerializationFormatWithAddress(final Address address) {
        return null;
    }

    @Override
    public int uid() {
        return 0;
    }

    @Override
    @Nullable
    public ActorPath withUid(final int uid) {
        return null;
    }

    @Override
    public int compareTo(@Nullable final ActorPath o) {
        return 0;
    }
}
