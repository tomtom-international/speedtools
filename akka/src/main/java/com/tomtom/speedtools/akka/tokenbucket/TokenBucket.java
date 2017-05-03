/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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

package com.tomtom.speedtools.akka.tokenbucket;

import scala.concurrent.Future;

import javax.annotation.Nonnull;

/**
 * Implementation of the Token Bucket algorithm for traffic shaping. See Computer Networks 4ed by
 * Andy Tanenbaum, p. 306-307.
 *
 * This implementation is an Akka actor and uses the scheduler to increment the
 * number of tokens at a set interval. The party that wishes to have its traffic shaped then calls acquire() to
 * obtain the next available token, passing in a callback to invoke when a token becomes available. The callback
 * allows an implementation without blocking any threads.
 */
public interface TokenBucket {

    /**
     * Callback interface, called when a token becomes available.
     */
    public interface TokenAcceptor {
        void tokenAcquired();
    }

    /**
     * Acquire a token from the bucket. When a token becomes available, TokenAcceptor.tokenAcquired() will be called.
     *
     * @param tokenAcceptor the callback to use.
     */
    void acquireToken(@Nonnull TokenAcceptor tokenAcceptor);

    /**
     * Flush any remaining tokens in the bucket (useful after, e.g., token rate changes).
     */
    void flushTokens();

    /**
     * Change the rate of generating new tokens by multiplying with factor. Note that the rate will be limited (both
     * upper and lower bound) in the implementation, specified when the actor is instantiated.
     *
     * @param factor The factor to multiply the rate with. Must be &gt; 0.
     */
    void multiplyTokenRate(double factor);

    /**
     * Returns whether there are any tokens left in this bucket. Note that this does not guarantee that a token can
     * be acquired, since someone else could be first to acquire the token.
     *
     * @return Whether there are any tokens left.
     */
    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    @Nonnull
    Future<Boolean> isEmpty();

    /**
     * Internal method called from scheduler to increment the number of tokens in the bucket.
     */
    void addNextIncrement();
}
