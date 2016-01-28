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

package com.tomtom.speedtools.akka.tokenbucket;

import akka.dispatch.Futures;
import com.tomtom.speedtools.akka.TypedActorContext;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Note: this implementation could be made more efficient, by only scheduling a timer when there are not enough tokens
 * to acquire, and compute the number of tokens based on time.
 */
public class TokenBucketImpl implements TokenBucket {

    public static final double ONE_TOKEN = 1.0;
    @Nonnull
    private final TypedActorContext<TokenBucket> context;
    @Nonnull
    private final FiniteDuration interval;
    private final int bucketSize;
    private final double minimumIncrement;

    private double increment;
    private double nrTokens;
    private final List<TokenAcceptor> waitingAcceptors = new ArrayList<>();

    @Inject
    public TokenBucketImpl(
            @Nonnull final TypedActorContext<TokenBucket> context,
            @Nonnull final FiniteDuration interval,
            @Nonnull final Double initialIncrement,
            @Nonnull final Integer bucketSize,
            @Nonnull final Double minimumIncrement) {
        assert context != null;
        assert interval != null;
        assert initialIncrement != null;
        assert bucketSize != null;
        assert minimumIncrement != null;
        assert initialIncrement >= minimumIncrement;
        assert minimumIncrement > 0;
        assert bucketSize > minimumIncrement;
        assert interval.gt(FiniteDuration.Zero());

        this.context = context;
        this.interval = interval;
        increment = initialIncrement;
        this.bucketSize = bucketSize;
        this.minimumIncrement = minimumIncrement;

        // Allow sending an increment straight away.
        this.nrTokens = initialIncrement;

        // Schedule increment.
        scheduleNextIncrement();
    }

    @Override
    public void acquireToken(@Nonnull final TokenAcceptor tokenAcceptor) {
        assert tokenAcceptor != null;

        // If we have enough tokens, use immediately, otherwise, make acceptor wait.
        if (nrTokens >= ONE_TOKEN) {
            giveToken(tokenAcceptor);
        } else {

            // Wait.
            waitingAcceptors.add(tokenAcceptor);
        }
    }

    @Override
    public void flushTokens() {
        nrTokens = 0.0;
    }

    @Override
    public void multiplyTokenRate(final double factor) {
        assert factor > 0.0;

        // Compute new increment.
        increment *= factor;

        // Check increment range.
        if (increment < minimumIncrement) {
            increment = minimumIncrement;
        } else if (increment > bucketSize) {
            // Limit increment to bucketSize, since we can never add more than bucketSize.
            increment = bucketSize;
        } else {
            // Increment need not be changed.
        }
    }

    @Override
    @Nonnull
    public Future<Boolean> isEmpty() {
        return Futures.successful(nrTokens < ONE_TOKEN);
    }

    @Override
    public void addNextIncrement() {

        // Add more tokens.
        nrTokens += increment;

        if (nrTokens > bucketSize) {
            nrTokens = bucketSize;
        }

        // Give tokens to acceptors.
        while ((nrTokens >= ONE_TOKEN) && !waitingAcceptors.isEmpty()) {
            giveToken(waitingAcceptors.remove(0));
        }

        // Next increment.
        scheduleNextIncrement();
    }

    private void scheduleNextIncrement() {
        context.scheduleOnce(interval, () -> context.self().addNextIncrement());
    }

    /**
     * Give 1 token to the acceptor.
     *
     * @param tokenAcceptor The acceptor to give token to.
     */
    private void giveToken(@Nonnull final TokenAcceptor tokenAcceptor) {
        assert tokenAcceptor != null;
        assert nrTokens >= ONE_TOKEN;

        nrTokens -= ONE_TOKEN;
        tokenAcceptor.tokenAcquired();
    }
}
