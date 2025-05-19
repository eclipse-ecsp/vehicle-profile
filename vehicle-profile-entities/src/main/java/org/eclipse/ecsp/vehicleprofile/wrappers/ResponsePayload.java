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

package org.eclipse.ecsp.vehicleprofile.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * ResponsePayload.
 */
@JsonInclude(value = Include.NON_NULL)
public class ResponsePayload {
    
    /**
     * MSG.
     */
    public enum Msg {
        /**
         * Success.
         */
        SUCCESS,
        /**
         * Failure.
         */
        FAILURE
    }

    ResponsePayload.Msg message = Msg.SUCCESS;
    Integer failureReasonCode = null;
    String failureReason;
    Object data;

    /**
     * Response payload.
     *
     */
    public ResponsePayload() {
        this(null);
    }

    /**
     * Set response payload data.
     *
     * @param data object
     */
    public ResponsePayload(Object data) {
        this.data = data;
    }

    /**
     * Set failure reason.
     *
     * @param failureReason string
     */
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    /**
     * Get failure reason.
     *
     * @return string
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Set failure reason code.
     *
     * @param failureReasonCode int
     */
    public void setFailureReasonCode(int failureReasonCode) {
        this.failureReasonCode = failureReasonCode;
    }

    /**
     * Get failure reason code.
     *
     * @return int
     */
    public Integer getFailureReasonCode() {
        return failureReasonCode;
    }

    /**
     * Set message.
     *
     * @param message string
     */
    public void setMessage(ResponsePayload.Msg message) {
        this.message = message;
    }

    /**
     * Get message.
     *
     * @return string
     */
    public ResponsePayload.Msg getMessage() {
        return message;
    }

    /**
     * Set data.
     *
     * @param data object
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * Get data.
     *
     * @return object
     */
    public Object getData() {
        return data;
    }
}