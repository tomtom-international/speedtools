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

package com.tomtom.speedtools.apivalidation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tomtom.speedtools.apivalidation.exceptions.ApiInternalException;
import com.tomtom.speedtools.json.JsonBased;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

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
 *    public class MyDTO extends ApiDTO {
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
 *
 * The method call {@link ApiValidator#start()} initializes the validation process and
 * subsequent checkXXX() calls will check the value and ranges of parameters. If errors
 * are found, they are accumulated. Finally, {@link ApiValidator#done()} will throw an
 * {@link com.tomtom.speedtools.apivalidation.exceptions.ApiBadRequestException} if errors
 * were found. The exception contains all errors in a structured format, which will normally
 * be passed back to the caller as a 400 (Bad Request) status, with a JSON body containing
 * the error list.
 *
 * The advantage of this type of validation over other frameworks is that it accumulates
 * ALL API parameter validations before an exception is thrown.
 *
 * This allows the caller to mark ALL offending fields at once. As the result message contains
 * the allowed values, or value ranges as well, in a structured format, these can be easily
 * shown as feedback to the user.
 *
 * The framework was originally designed to work in a WEB and mobile based environment, where
 * a user enters all sorts of details and a single validation call allows the client to mark
 * all offending fields.
 */
public abstract class ApiDTO extends JsonBased {

    @JsonIgnore
    private static final Logger LOG = LoggerFactory.getLogger(ApiDTO.class);

    /**
     * Will get set once validated. May only be set once.
     */
    @JsonIgnore
    private boolean validated = false;

    @JsonIgnore
    private final ApiValidator validator = new ApiValidator();

    @JsonIgnore
    private final Object lockThisObjectInstance = new Object();

    /**
     * Indicates whether the object is to be considered immutable or not.
     * Immutable objects are auto-validated whenever a getter is called.
     */
    @JsonIgnore
    private final boolean immutable;

    /**
     * The default constructor creates an immutable object.
     */
    public ApiDTO() {
        this(true);
    }

    /**
     * Construct a DTO object.
     *
     * @param immutable If true, the framework automatically executes validate() when a getter is
     *                  executed (using beforeGet()). Also, no setters are allowed after any getter
     *                  has been executed. For mutable objects (immutable is false), no such checks
     *                  are performed and the caller is responsible for executing validate() at the
     *                  appropriate time.
     */
    public ApiDTO(final boolean immutable) {
        this.immutable = immutable;
    }

    /**
     * This method must be called by all getters.
     */
    public void beforeGet() {

        // No checks if the objects is considered mutable.
        if (!immutable) {
            return;
        }

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
     * This method must be called by all setters. If you need to set an attribute after
     * the validator finished, you can call ApiValidator.reset(), but you should call
     * validate() manually in that case.
     */
    public void beforeSet() {

        // No checks if the objects is considered mutable.
        if (!immutable) {
            return;
        }

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
    public abstract void validate();

    /**
     * Check contains the parameter checks in derived classes. Useful for subclasses calling its super validate method
     * without finishing the validation.
     *
     * @param callDone Tells method to call validator().done().
     */
    public void validate(final boolean callDone) {
        final String name = this.getClass().getName();
        LOG.error("Validate method need to be implemented by subclass: {}", name);
        throw new AssertionError("Subclass " + name + " needs to provide an implementation of 'validate'");
    }
}
