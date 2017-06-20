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

package com.tomtom.speedtools.rest;

import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

import javax.annotation.Nullable;

public class CheckFuturesTest {
    private static final Logger LOG = LoggerFactory.getLogger(CheckFuturesTest.class);

    // Create an Akka system.
    private ActorSystem system = null;

    @Before
    public void before() {
        system = ActorSystem.create();
    }

    @After
    public void after() {
        system.terminate();
    }

    public Future<Void> waitAndReturn() {
        LOG.info(("waitAndReturn"));
        return Futures.successful(null);
    }

    @Test
    public void testAssertInFuture() throws InterruptedException {
        LOG.info(("testAssertInFuture"));

        LOG.info(("testAssertInFuture: before call"));
        final Future<Void> future = waitAndReturn();
        LOG.info(("testAssertInFuture: after call"));

        future.onComplete(new OnComplete<Void>() {

            @Override
            public void onComplete(@Nullable final Throwable failure, @Nullable final Void ignored)
                    throws InterruptedException {
                //noinspection VariableNotUsedInsideIf
                if (failure != null) {
                    LOG.info(("testAssertInFuture: in onComplete - failure"));
                } else {
                    LOG.info(("testAssertInFuture: in onComplete - no failure, before wait"));

                    Thread.sleep(500);
                    LOG.info(("testAssertInFuture: in onComplete - no failure, after wait"));
                }
            }
        }, system.dispatcher());
        LOG.info(("testAssertInFuture: after onComplete"));
        Thread.sleep(1000);
        Assert.assertTrue(true);
    }
}
