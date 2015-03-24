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

package com.tomtom.speedtools.akka.tokenbucket;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import com.tomtom.speedtools.testutils.akka.SimpleExecutionContext;
import com.tomtom.speedtools.akka.TypedActorContext;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TokenBucketImplTest {
    @Mock
    private TypedActorContext<TokenBucket> mockContext;
    @Mock
    private TokenBucket.TokenAcceptor mockAcceptor;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        when(mockContext.executionContext()).thenReturn(SimpleExecutionContext.getInstance());
    }

    @Test
    public void acquireWhenEnoughForOneToken() {
        final TokenBucketImpl tokenBucket =
                new TokenBucketImpl(mockContext, FiniteDuration.create(1, TimeUnit.SECONDS), 1.0, 20, 0.1);

        // Request token twice.
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);

        // Verify acceptor called once.
        verify(mockAcceptor).tokenAcquired();
    }

    @Test
    public void acquireWhenEnoughForTwoTokens() {
        final TokenBucketImpl tokenBucket =
                new TokenBucketImpl(mockContext, FiniteDuration.create(1, TimeUnit.SECONDS), 1.0, 20, 0.1);

        // Add 1 increments.
        tokenBucket.addNextIncrement();

        // Request token thrice.
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);

        // Verify acceptor called twice.
        verify(mockAcceptor, times(2)).tokenAcquired();
    }

    @Test
    public void acquireWhenEnoughForThreeTokensByMultiplying() {
        final TokenBucketImpl tokenBucket =
                new TokenBucketImpl(mockContext, FiniteDuration.create(1, TimeUnit.SECONDS), 1.0, 20, 0.1);

        tokenBucket.multiplyTokenRate(2.0);

        // Add 1 increments.
        tokenBucket.addNextIncrement();

        // Request token four times.
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);

        // Verify acceptor called three times.
        verify(mockAcceptor, times(3)).tokenAcquired();
    }

    @Test
    public void acquireWhenNotEnoughTokens() {
        final TokenBucketImpl tokenBucket =
                new TokenBucketImpl(mockContext, FiniteDuration.create(1, TimeUnit.SECONDS), 0.5, 20, 0.1);

        // Request token.
        tokenBucket.acquireToken(mockAcceptor);

        // Verify acceptor not called.
        verify(mockAcceptor, never()).tokenAcquired();

        // Add 1 increment.
        tokenBucket.addNextIncrement();

        // Verify acceptor called.
        verify(mockAcceptor).tokenAcquired();
    }

    @Test
    public void incrementPastBucketSize() {
        final TokenBucketImpl tokenBucket =
                new TokenBucketImpl(mockContext, FiniteDuration.create(1, TimeUnit.SECONDS), 1.0, 3, 0.1);

        // Add 4 increments.
        for (int i = 0; i < 4; i++) {
            tokenBucket.addNextIncrement();
        }

        // Request token four times.
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);

        // Verify acceptor should be called only 3 times (bucket limit).
        verify(mockAcceptor, times(3)).tokenAcquired();
    }

    @Test
    public void multiplyBelowMinimumSize() {
        final TokenBucketImpl tokenBucket =
                new TokenBucketImpl(mockContext, FiniteDuration.create(1, TimeUnit.SECONDS), 1.0, 3, 0.1);

        // Request one token to empty the bucket.
        tokenBucket.acquireToken(mockAcceptor);

        // This should set token rate to 0.1 (the minimum).
        tokenBucket.multiplyTokenRate(0.01);

        // Adding 11 increments should now do it (0.1 * 10 = 1, but because of rounding, do one more).
        for (int i = 0; i < 11; i++) {
            tokenBucket.addNextIncrement();
        }

        // Request token two times.
        tokenBucket.acquireToken(mockAcceptor);
        tokenBucket.acquireToken(mockAcceptor);

        // Verify acceptor should be called exactly 2 times (11 * minimum increment > 1.0).
        verify(mockAcceptor, times(2)).tokenAcquired();
    }
}
