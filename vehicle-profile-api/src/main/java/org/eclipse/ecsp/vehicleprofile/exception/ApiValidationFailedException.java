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

/**
 * ApiPreConditionNotFoundException.
 */
public class ApiValidationFailedException extends RuntimeException {
    private String code;
    private String message;
    private String errorMessage;
    private Throwable throwable;

    /**
     * ApiPreConditionNotFoundException constructor.
     *
     * @param message string
     */
    public ApiValidationFailedException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * ApiPreConditionNotFoundException .
     *
     * @param code string
     * @param message string
     */
    public ApiValidationFailedException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * ApiPreConditionNotFoundException constructor.
     *
     * @param code string
     * @param message string
     * @param errorMessage string
     */
    public ApiValidationFailedException(String code, String message, String errorMessage) {
        super(message);
        this.code = code;
        this.message = message;
        this.errorMessage = errorMessage;
    }

    /**
     * ApiPreConditionNotFoundException constructor.
     *
     * @param code string
     * @param message string
     * @param errorMessage string
     * @param e Throwable
     */
    public ApiValidationFailedException(String code, String message, String errorMessage, Throwable e) {
        super(message, e);
        this.code = code;
        this.message = message;
        this.errorMessage = errorMessage;
        this.throwable = e;
    }

    /**
     * get the code.
     *
     * @return string
     */
    public String getCode() {
        return code;
    }

    /**
     * set the code.
     *
     * @param code string
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * get the message.
     *
     * @return string
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * set the message.
     *
     * @param message string
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * get the error message.
     *
     * @return string
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * set the error message.
     *
     * @param errorMessage string
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * get the throwable.
     *
     * @return Throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * set the throwable.
     *
     * @param throwable Throwable
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
