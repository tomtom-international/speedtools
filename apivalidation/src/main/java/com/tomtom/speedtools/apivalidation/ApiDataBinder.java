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

package com.tomtom.speedtools.apivalidation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomtom.speedtools.apivalidation.exceptions.ApiInternalException;
import com.tomtom.speedtools.json.JsonBased;

/**
 * This class provides an abstract base class for JAX-B data binder classes.
 *
 * The idea is that the JAX-B classes provide a validate() function, which is called for the 1st getters that is being
 * used by the class.
 *
 * The setters should not be called after any getters has been called.
 *
 * The user of this class should make sure the JAX-B classes look something like this:
 *
 * <pre>
 *    public class MyDataBinder extends ApiDataBinder {
 *
 *       public void validate() {
 *           validator().start();
 *           validator.checkString(true, "name", name, minLen, maxLen);
 *           validator.done();
 *       }
 *
 *       public String getName() {
 *           beforeGet();
 *           return name;
 *       }
 *
 *       public void setName() {
 *           beforeSet();
 *           this.name = name;
 *       }
 *    }
 * </pre>
 */
public abstract class ApiDataBinder extends JsonBased {

    @JsonIgnore
    private static final Logger LOG = LoggerFactory.getLogger(ApiDataBinder.class);

    /**
     * Will get set once validated. May only be set once.
     */
    @JsonIgnore
    private boolean validated = false;

    @JsonIgnore
    private final ApiValidator validator = new ApiValidator();

    private final Object lockThisObjectInstance = new Object();

    /**
     * This method must be called by all getters.
     */
    public void beforeGet() {

        /**
         * Do not allow simultaneous invocations of validate() on same object without
         * fully finishing the validation first. The lock is acquired before the
         * check of 'validated' to make sure we don't validate twice if two calls
         * to getters are performed at the same time.
         */
        synchronized (lockThisObjectInstance) {

            // Check if already validated. If so, return immediately.
            if (validated) {
                return;
            }
            validate();

            // Validator must be ended now.
            validator.assertFinished();
            validated = true;
        }
    }

    /**
     * This method must be called by all setters.
     */
    public void beforeSet() {

        /**
         * This method needs to acquire the lock as well, because the value of 'validated' may be
         * changed by another thread.
         */
        final boolean isAlreadyValidated;
        synchronized (lockThisObjectInstance) {

            // Validator should not be called yet.
            validator.assertNotFinished();
            isAlreadyValidated = validated;
        }
        if (isAlreadyValidated) {
            final String msg = "Class cannot be modified after validation: " + this.getClass().toString();
            LOG.error("beforeSet: {}", msg);
            throw new ApiInternalException();
        }
    }

    @Nonnull
    public ApiValidator validator() {
        return validator;
    }

    /**
     * Check contains the parameter checks in derived classes.
     */
    protected abstract void validate();

    /**
     * Check contains the parameter checks in derived classes. Useful for subclasses calling its super validate method
     * without finishing the validation.
     *
     * @param callDone Tells method to call validator().done().
     */
    protected void validate(final boolean callDone) {
        final String name = this.getClass().getName();
        LOG.error("Validate method need to be implemented by subclass: {}", name);
        throw new AssertionError("Subclass " + name + " needs to provide an implementation of 'validate'");
    }
}
