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

package org.eclipse.ecsp.vehicleprofile.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

/**
 * ApiResponse.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<D> {
    private String code;
    private String message;
    private D data;
    private HttpStatus statusCode;

    private ApiResponse() {}
    
    private ApiResponse(Builder<D> builder) {
        this.code = builder.code;
        this.message = builder.message;
        this.data = builder.data;
        this.statusCode = builder.statusCode;
    }

    /**
     * buildError.
     */
    public static class Builder<D> {
        private String code;
        private String message;
        private D data;
        private HttpStatus statusCode;

        /**
         * Builder.
         *
         * @param code       String
         * @param message    String
         * @param statusCode HttpStatus
         */
        public Builder(String code, String message, HttpStatus statusCode) {
            this.code = code;
            this.message = message;
            this.statusCode = statusCode;
        }

        /**
         * Builder.
         *
         * @param data d
         * @return Builder
         */
        public Builder<D> withData(D data) {
            this.data = data;
            return this;
        }

        /**
         * build.
         *
         * @return api response
         */
        public ApiResponse<D> build() {
            return new ApiResponse<>(this);
        }
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
     * get the message.
     *
     * @return string
     */
    public String getMessage() {
        return message;
    }

    /**
     * get the data.
     *
     * @return object
     */
    public D getData() {
        return data;
    }

    /**
     * get the status code.
     *
     * @return HttpStatus
     */
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    /**
     * set the status code.
     *
     * @param statusCode HttpStatus
     */
    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }
}
