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

package com.tomtom.speedtools.testutils.akka;


import scala.concurrent.ExecutionContext;

import javax.annotation.Nonnull;


/**
 * This class provides an extremely simple execution context for tasks, that can be used to pass to,
 * for example, Akka onComplete() methods.
 */
public class SimpleExecutionContext implements ExecutionContext {
    @Nonnull
    private static final SimpleExecutionContext INSTANCE = new SimpleExecutionContext();

    // Always use getInstance().
    private SimpleExecutionContext() {
        // Prevent use.
    }

    @Nonnull
    public static SimpleExecutionContext getInstance() {
        return INSTANCE;
    }

    @Override
    public ExecutionContext prepare() {
        return this;
    }

    @Override
    public void execute(@Nonnull final Runnable runnable) {
        assert runnable != null;
        runnable.run();
    }

    @Override
    public void reportFailure(@Nonnull final Throwable t) {
        assert t != null;
    }
}
