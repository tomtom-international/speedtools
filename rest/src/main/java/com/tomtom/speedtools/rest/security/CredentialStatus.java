/*
 * Copyright (C) 2012-2017. TomTom International BV (http://tomtom.com).
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
package com.tomtom.speedtools.rest.security;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Credential Statuses for Credential.
 *
 * Valid transitions are:
 * <pre>
 *     {@link #TRY_1} -- {@link #TRY_1}
 *     {@link #TRY_1} -- {@link #TRY_2}
 *     {@link #TRY_1} -- {@link #SUSPENDED}
 *     {@link #TRY_1} -- {@link #BLOCKED}
 *
 *     {@link #TRY_2} -- {@link #TRY_3}
 *     {@link #TRY_2} -- {@link #TRY_1}
 *     {@link #TRY_2} -- {@link #SUSPENDED}
 *     {@link #TRY_2} -- {@link #BLOCKED}
 *
 *     {@link #TRY_3} -- {@link #SUSPENDED}
 *     {@link #TRY_3} -- {@link #TRY_1}
 *     {@link #TRY_3} -- {@link #BLOCKED}
 *
 *     {@link #SUSPENDED} -- {@link #SUSPENDED}
 *     {@link #SUSPENDED} -- {@link #BLOCKED}
 *     {@link #SUSPENDED} -- {@link #TRY_1}
 *
 *     {@link #BLOCKED} -- {@link #SUSPENDED}
 *     {@link #BLOCKED} -- {@link #BLOCKED}
 *     {@link #BLOCKED} -- {@link #TRY_1}
 * </pre>
 */
@SuppressWarnings("Duplicates")
public enum CredentialStatus {
    TRY_1,
    TRY_2,
    TRY_3,
    SUSPENDED,
    BLOCKED;

    // List of all valid transitions.
    static private final Map<CredentialStatus, List<CredentialStatus>> validPairs;

    // Static initialization of the validPairs map.
    static {
        // Initialize validPairs map.
        validPairs = new EnumMap<>(CredentialStatus.class);
        validPairs.put(TRY_1, new ArrayList<>());
        validPairs.put(TRY_2, new ArrayList<>());
        validPairs.put(TRY_3, new ArrayList<>());
        validPairs.put(SUSPENDED, new ArrayList<>());
        validPairs.put(BLOCKED, new ArrayList<>());

        // From TRY_1:
        validPairs.get(TRY_1).add(TRY_1);
        validPairs.get(TRY_1).add(TRY_2);
        validPairs.get(TRY_1).add(SUSPENDED);
        validPairs.get(TRY_1).add(BLOCKED);

        // From TRY_2:
        validPairs.get(TRY_2).add(TRY_3);
        validPairs.get(TRY_2).add(TRY_1);
        validPairs.get(TRY_2).add(SUSPENDED);
        validPairs.get(TRY_2).add(BLOCKED);

        // From TRY_3:
        validPairs.get(TRY_3).add(SUSPENDED);
        validPairs.get(TRY_3).add(TRY_1);
        validPairs.get(TRY_3).add(BLOCKED);

        // From SUSPENDED:
        validPairs.get(SUSPENDED).add(SUSPENDED);
        validPairs.get(SUSPENDED).add(BLOCKED);
        validPairs.get(SUSPENDED).add(TRY_1);

        // From BLOCKER:
        validPairs.get(BLOCKED).add(SUSPENDED);
        validPairs.get(BLOCKED).add(BLOCKED);
        validPairs.get(BLOCKED).add(TRY_1);
    }

    @Nonnull
    public static CredentialStatus nextSuccessStatus(@Nonnull final CredentialStatus fromStatus) {
        assert fromStatus != null;
        final CredentialStatus toStatus;
        switch (fromStatus) {

            case TRY_1:
                toStatus = TRY_1;
                break;

            case TRY_2:
                toStatus = TRY_1;
                break;

            case TRY_3:
                toStatus = TRY_1;
                break;

            case BLOCKED:
                toStatus = BLOCKED;
                break;

            case SUSPENDED:
                toStatus = SUSPENDED;
                break;

            default:
                toStatus = SUSPENDED;
                break;
        }
        assert isValidTransition(fromStatus, toStatus);
        return toStatus;
    }

    @Nonnull
    public static CredentialStatus nextFailureStatus(@Nonnull final CredentialStatus fromStatus) {
        assert fromStatus != null;

        final CredentialStatus nextStatus;
        switch (fromStatus) {

            case TRY_1:
                nextStatus = TRY_2;
                break;

            case TRY_2:
                nextStatus = TRY_3;
                break;

            case TRY_3:
                nextStatus = SUSPENDED;
                break;

            case BLOCKED:
                nextStatus = BLOCKED;
                break;

            case SUSPENDED:
                nextStatus = SUSPENDED;
                break;

            default:
                nextStatus = BLOCKED;
                break;
        }
        assert isValidTransition(fromStatus, nextStatus);
        return nextStatus;
    }

    /**
     * This function checks if a transition for TripStatus is valid.
     *
     * @param fromStatus (in) The origin.
     * @param toStatus   (in) The destination.
     * @return If true, this is a valid transition, false if not.
     */
    static boolean isValidTransition(@Nonnull final CredentialStatus fromStatus,
                                     @Nonnull final CredentialStatus toStatus) {
        assert fromStatus != null;
        assert toStatus != null;
        final List<CredentialStatus> validDestinations = validPairs.get(fromStatus);
        if (validDestinations == null) {
            return false;
        }
        return validDestinations.contains(toStatus);
    }
}
