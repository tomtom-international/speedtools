/*
 * Copyright (C) 2012-2021, TomTom (http://tomtom.com).
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

package com.tomtom.speedtools.rest;


import akka.pattern.AskTimeoutException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.tomtom.speedtools.apivalidation.errors.ApiValidationError;
import com.tomtom.speedtools.apivalidation.exceptions.*;
import com.tomtom.speedtools.json.Json;
import com.tomtom.speedtools.objects.Tuple;
import com.tomtom.speedtools.time.UTCTime;
import com.tomtom.speedtools.xmladapters.DateTimeAdapter.XMLAdapterWithSecondsResolution;
import org.bson.BSONException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tomtom.speedtools.utils.StringUtils.nullToEmpty;
import static javax.ws.rs.core.Response.status;

@SuppressWarnings("UnnecessaryFullyQualifiedName")
@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOG = LoggerFactory.getLogger(GeneralExceptionMapper.class);
    private static final String LOG_MESSAGE_TEMPLATE_VERBOSE = "%s: reference=%s, %s(%s): %s, time=%s";
    private static final String LOG_MESSAGE_TEMPLATE_COMPACT = "%s(%s): %s";

    /**
     * This boolean indicates whether verbose or compact messages are used.
     */
    @SuppressWarnings("StaticNonFinalField")
    private static boolean verboseMode = false;

    private enum Level {
        WARN,
        ERROR
    }

    @Nonnull
    private final static Map<Class<? extends Exception>, Tuple<Boolean, Status>> customExceptionsMap =
            new HashMap<>();

    /**
     * Set or unset verbose mode for exceptions in log files.
     *
     * @param verboseMode If true, user verbose mode and add a unique reference to error messages.
     */
    public static void setVerboseMode(final boolean verboseMode) {
        GeneralExceptionMapper.verboseMode = verboseMode;
    }

    /**
     * Add a custom exception mapping. For example, when using MongoDB you might wish to add:
     *
     * addCustomException(MongoDBConnectionException.class, false, null); addCustomException(EntityNotFoundException.class,
     * true, Status.BAD_API_CALL);
     *
     * @param exception             Exception to add.
     * @param isInternalServerError If true, return "internal server error (501)"; if false return a specific status.
     * @param status                Status to return (must be non-null if isInternalServerError is false).
     */
    public static void addCustomException(
            @Nonnull final Class<? extends Exception> exception,
            final boolean isInternalServerError,
            @Nonnull final Status status) {
        assert exception != null;
        assert status != null;
        customExceptionsMap.put(exception, new Tuple<>(isInternalServerError, status));
    }

    public static void removeCustomException(@Nonnull final Class<? extends Exception> exception) {
        assert exception != null;
        customExceptionsMap.remove(exception);
    }

    @Nonnull
    @Override
    public Response toResponse(@Nullable final Throwable e) {
        if (e == null) {

            // Keep original media type output format.
            return status(Status.OK).build();
        } else {
            return toResponse(LOG, e);
        }
    }

    /**
     * Static function to map an exception to a proper (asynchronous) response.
     *
     * @param log       Logger to log information, warning or error message to.
     * @param exception Exception to be processed.
     * @return Status response.
     */
    @SuppressWarnings("deprecation")
    @Nonnull
    public static Response toResponse(
            @Nonnull final Logger log,
            @Nonnull final Throwable exception) {
        assert log != null;
        assert exception != null;

        //noinspection SuspiciousMethodCalls
        final Tuple<Boolean, Status> tuple = customExceptionsMap.get(exception.getClass());
        if (tuple != null) {
            if (tuple.getValue1()) {

                // Internal server error.
                return toResponseApiException(Level.ERROR, log, exception);
            } else {

                // Bad API call.
                return toResponseBadApiCall(log, tuple.getValue2(), exception);
            }
        }
        /**
         * Don't always throw an Error. This exception may be caused by asking for a wrong URL.
         * We need to catch those properly and log them as Informational, or Warnings, at most.
         *
         * Exceptions as a result of the way the call was issued (external cause, usually
         * a bad API call). These are never errors, just informational.
         */
        if (exception instanceof ApiBadRequestException) {
            return toResponseApiValidationError(log, (ApiBadRequestException) exception);
        }

        /**
         * Api exceptions other than bad request.
         */
        else if (exception instanceof ApiForbiddenException) {
            return toResponseBadApiCall(log, Status.FORBIDDEN, exception);
        } else if (exception instanceof ApiInternalException) {
            return toResponseBadApiCall(log, Status.INTERNAL_SERVER_ERROR, exception);
        } else if (exception instanceof ApiNotFoundException) {
            return toResponseBadApiCall(log, Status.NOT_FOUND, exception);
        } else if (exception instanceof ApiNotImplementedException) {
            return toResponseBadApiCall(log, HttpServletResponse.SC_NOT_IMPLEMENTED, exception);
        } else if (exception instanceof ApiConflictException) {
            return toResponseBadApiCall(log, Status.CONFLICT, exception);
        } else if (exception instanceof ApiUnauthorizedException) {
            return toResponseBadApiCall(log, Status.UNAUTHORIZED, exception);
        }

        /**
         * Rest-easy exceptions (deprecated).
         */
        else if (exception instanceof org.jboss.resteasy.spi.BadRequestException) {
            return toResponseBadApiCall(log, Status.BAD_REQUEST, exception);
        } else if (exception instanceof org.jboss.resteasy.spi.NotFoundException) {
            return toResponseBadApiCall(log, Status.NOT_FOUND, exception);
        } else if (exception instanceof org.jboss.resteasy.spi.NotAcceptableException) {
            return toResponseBadApiCall(log, Status.NOT_ACCEPTABLE, exception);
        } else if (exception instanceof org.jboss.resteasy.spi.MethodNotAllowedException) {
            return toResponseBadApiCall(log, Status.FORBIDDEN, exception);
        } else if (exception instanceof org.jboss.resteasy.spi.UnauthorizedException) {
            return toResponseBadApiCall(log, Status.UNAUTHORIZED, exception);
        } else if (exception instanceof org.jboss.resteasy.spi.UnsupportedMediaTypeException) {
            return toResponseBadApiCall(log, Status.UNSUPPORTED_MEDIA_TYPE, exception);
        }

        /**
         * Javax exceptions.
         */
        else if (exception instanceof javax.ws.rs.BadRequestException) {
            return toResponseBadApiCall(log, Status.BAD_REQUEST, exception);
        } else if (exception instanceof javax.ws.rs.NotFoundException) {
            return toResponseBadApiCall(log, Status.NOT_FOUND, exception);
        } else if (exception instanceof javax.ws.rs.NotAcceptableException) {
            return toResponseBadApiCall(log, Status.NOT_ACCEPTABLE, exception);
        } else if ((exception instanceof javax.ws.rs.NotAllowedException) ||
                (exception instanceof javax.ws.rs.ForbiddenException)) {
            return toResponseBadApiCall(log, Status.FORBIDDEN, exception);
        } else if (exception instanceof javax.ws.rs.NotAuthorizedException) {
            return toResponseBadApiCall(log, Status.UNAUTHORIZED, exception);
        } else if (exception instanceof javax.ws.rs.NotSupportedException) {
            return toResponseBadApiCall(log, Status.UNSUPPORTED_MEDIA_TYPE, exception);
        }

        /**
         * System specific exception, such as "entity not found". These are not
         * always errors, either, but some are. Inspect on case-by-case!
         */
        else if (exception instanceof AskTimeoutException) {
            return toResponseApiException(Level.WARN, log, exception);
        } else if (exception instanceof BSONException) {
            return toResponseApiException(Level.ERROR, log, exception);
        }

        /**
         * Jackson unmarshall exceptions typically thrown from a {@link XmlAdapter} wrap a more specific exception.
         */
        else //noinspection ObjectEquality
            if ((exception instanceof JsonMappingException) &&
                    (exception.getCause() != null) && (exception.getCause() != exception)) {

                /**
                 * Call toResponse again, with the cause of the exception.
                 */
                //noinspection TailRecursion
                return toResponse(log, exception.getCause());
            }

            /**
             * Some other system failure.
             */
            else {
                return toResponseApiException(Level.ERROR, log, exception);
            }
    }

    @Nonnull
    private static Response toResponseBadApiCall(
            @Nonnull final Logger log,
            @Nonnull final StatusType status,
            @Nonnull final Throwable exception) {
        assert log != null;
        assert status != null;
        assert exception != null;

        final ExceptionDTO exceptionDTO = new ExceptionDTO(exception, UTCTime.now());
        log.info(createLogMessage("toResponseBadApiCall: Bad API call", exception, exceptionDTO, status));

        // Explicitly set media type, to overwrite media content type.
        return status(status).entity(exceptionDTO).
                type(MediaType.APPLICATION_JSON_TYPE).
                build();
    }

    @Nonnull
    private static Response toResponseBadApiCall(
            @Nonnull final Logger log,
            final int statusCode,
            @Nonnull final Throwable exception) {
        assert log != null;
        assert exception != null;
        final ExceptionDTO exceptionDTO = new ExceptionDTO(exception, UTCTime.now());
        log.info(createLogMessage("toResponseBadApiCall: Bad API call", exception, exceptionDTO, statusCode));
        return status(statusCode).entity(exceptionDTO).
                type(MediaType.APPLICATION_JSON_TYPE).
                build();
    }

    @Nonnull
    private static Response toResponseApiValidationError(
            @Nonnull final Logger log,
            @Nonnull final ApiBadRequestException exception) {
        assert log != null;
        assert exception != null;
        final Status status = Status.BAD_REQUEST;
        final ExceptionDTO exceptionDTO = new ExceptionDTO(
                ExceptionDTO.API_ERROR_MESSAGE, UTCTime.now(), exception.getErrors());
        log.info(
                createLogMessage("toResponseApiValidationError: API validation error", exception, exceptionDTO, status));
        return status(status).entity(exceptionDTO).
                type(MediaType.APPLICATION_JSON_TYPE).
                build();
    }

    @Nonnull
    private static Response toResponseApiException(
            @Nonnull final Level level,
            @Nonnull final Logger log,
            @Nonnull final Throwable exception) {
        assert level != null;
        assert log != null;
        assert exception != null;
        final Status status = Status.INTERNAL_SERVER_ERROR;
        final ExceptionDTO exceptionDTO = new ExceptionDTO(
                ExceptionDTO.DEFAULT_MESSAGE, UTCTime.now());
        final String message =
                createLogMessage("toResponseApiException: API exception", exception, exceptionDTO, status);
        if (level == Level.WARN) {
            log.warn(message, exception);
        } else {
            log.error(message, exception);
        }
        return status(status).entity(exceptionDTO).
                type(MediaType.APPLICATION_JSON_TYPE).
                build();
    }


    @Nonnull
    private static String createLogMessage(
            @Nonnull final String prefix,
            @Nonnull final Throwable exception,
            @Nonnull final ExceptionDTO exceptionDTO,
            @Nonnull final StatusType status) {
        assert prefix != null;
        assert exception != null;
        assert exceptionDTO != null;
        assert status != null;
        if (verboseMode) {
            return String.format(LOG_MESSAGE_TEMPLATE_VERBOSE, prefix, exceptionDTO.getReference(), status,
                    status.getStatusCode(), exception.getMessage(), exceptionDTO.getTime());
        } else {
            return String.format(LOG_MESSAGE_TEMPLATE_COMPACT, status, status.getStatusCode(), exception.getMessage());
        }
    }

    @Nonnull
    private static String createLogMessage(
            @Nonnull final String prefix,
            @Nonnull final Throwable exception,
            @Nonnull final ExceptionDTO exceptionDTO,
            final int statusCode) {
        assert prefix != null;
        assert exception != null;
        assert exceptionDTO != null;
        if (verboseMode) {
            return String.format(LOG_MESSAGE_TEMPLATE_VERBOSE, prefix, exceptionDTO.getReference(), "status", statusCode,
                    exception.getMessage(), exceptionDTO.getTime());
        } else {
            return String.format(LOG_MESSAGE_TEMPLATE_COMPACT, "status", statusCode, exception.getMessage());
        }
    }

    @SuppressWarnings("CallToSimpleSetterFromWithinClass")
    @XmlRootElement(name = "exception")
    @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
    private static class ExceptionDTO {
        private static final String DEFAULT_MESSAGE = "Unexpected exception. " +
                "When contacting system administration, please use the 'reference' field.";

        private static final String API_ERROR_MESSAGE = "API message validation error. " +
                "When contacting system administration, please use the 'reference' field.";

        @Nullable
        private String message;
        @Nullable
        private String reference;
        @Nullable
        private DateTime time;
        @Nullable
        private List<ApiValidationError> errors;

        private ExceptionDTO(
                @Nonnull final String message,
                @Nonnull final DateTime time,
                @Nullable final List<ApiValidationError> errors) {
            super();
            assert message != null;
            assert time != null;
            setMessage(message);
            setTime(time);
            setErrors(errors);
            setReference(generateReference());
        }

        private ExceptionDTO(
                @Nonnull final String message,
                @Nonnull final DateTime time) {
            this(message, time, null);
        }

        private ExceptionDTO(
                @Nonnull final Throwable throwable,
                @Nonnull final DateTime time) {
            assert throwable != null;
            assert time != null;

            final String msg;
            if (throwable.getMessage() != null) {
                msg = throwable.getClass().getSimpleName() + "; " + throwable.getMessage();
            } else {
                msg = throwable.getClass().getSimpleName();
            }
            setMessage(msg);
            setTime(time);
            setErrors(null);
            setReference(generateReference());
        }

        private ExceptionDTO() {
            this(DEFAULT_MESSAGE, UTCTime.now(), null);
        }

        @XmlElement(name = "message", required = true)
        @Nonnull
        public String getMessage() {
            assert message != null;
            return message;
        }

        public void setMessage(@Nullable final String message) {
            this.message = nullToEmpty(message);
        }

        @XmlElement(name = "reference", required = true)
        @Nonnull
        public String getReference() {
            assert reference != null;
            return reference;
        }

        public void setReference(@Nullable final String reference) {
            this.reference = nullToEmpty(reference);
        }

        @XmlElement(name = "time", required = true)
        @XmlJavaTypeAdapter(type = DateTime.class, value = XMLAdapterWithSecondsResolution.class)
        @Nonnull
        public DateTime getTime() {
            assert time != null;
            return time;
        }

        public void setTime(@Nullable final DateTime time) {
            this.time = time;
        }

        @XmlElement(name = "errors", required = false)
        @Nullable
        public List<ApiValidationError> getErrors() {
            return errors;
        }

        public void setErrors(@Nullable final List<ApiValidationError> errors) {
            this.errors = errors;
        }

        @Nonnull
        public static String generateReference() {
            /**
             * Prefix and postfix with something so it's not a UUID anymore.
             */
            return "REF-" + UUID.randomUUID().toString().toUpperCase() + "-X";
        }

        @Override
        public String toString() {
            return Json.toStringJson(this);
        }
    }
}
