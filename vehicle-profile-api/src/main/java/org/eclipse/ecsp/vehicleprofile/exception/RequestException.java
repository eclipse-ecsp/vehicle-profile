/*
 *  *******************************************************************************
 *  Copyright (c) 2023-24 Harman International
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  *******************************************************************************
 */

package org.eclipse.ecsp.vehicleprofile.exception;

import org.springframework.http.HttpStatus;

/**
 * RequestException.
 */
public abstract class RequestException extends RuntimeException {

    private static final long serialVersionUID = -694092955495136208L;
    private FailureReason failureReason;

    @SuppressWarnings("unused")
    private RequestException() {

    }


    /**
     * RequestException constructor.
     *
     * @param failureReason message
     */
    public RequestException(FailureReason failureReason) {
        super(failureReason.getCode() + " : " + failureReason.getReason());
        this.failureReason = failureReason;
    }

    /**
     * Get failure reason.
     *
     * @return FailureReason
     */
    public FailureReason getFailureReason() {
        return failureReason;
    }

    /**
     * Get status.
     *
     * @return HttpStatus
     */
    public abstract HttpStatus getStatus();
}