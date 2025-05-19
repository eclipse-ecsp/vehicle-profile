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

package org.eclipse.ecsp.vehicleprofile.controller;

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.enums.ApiMessageEnum;
import org.eclipse.ecsp.vehicleprofile.exception.ApiException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiPreConditionFailedException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiResourceNotFoundException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiTechnicalException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiValidationFailedException;
import org.eclipse.ecsp.vehicleprofile.exception.RequestException;
import org.eclipse.ecsp.vehicleprofile.utils.ApiResponse;
import org.eclipse.ecsp.vehicleprofile.utils.ErrorUtils;
import org.eclipse.ecsp.vehicleprofile.utils.WebUtils;
import org.eclipse.ecsp.vehicleprofile.wrappers.ResponsePayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

/**
 * VehicleProfileControllerAdvice class.
 */
@RestControllerAdvice
public class VehicleProfileControllerAdvice {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileControllerAdvice.class);


    /**
     * FAILURE_CODE.
     */

    /**
     * FAILURE_CODE.
     */
    public static final int FAILURE_CODE = 5000;
    
    /**
     * handleException.
     */
    @ExceptionHandler({ RequestException.class })
    public ResponseEntity<ResponsePayload> handleException(RequestException ex) {
        LOGGER.error("The expection {} thrown by application", ex);
        ResponsePayload resp = new ResponsePayload();
        resp.setMessage(ResponsePayload.Msg.FAILURE);
        resp.setFailureReasonCode(ex.getFailureReason().getCode());
        resp.setFailureReason(ex.getMessage());
        return new ResponseEntity<>(resp, ex.getStatus());
    }

    /**
     * handleException.
     */
    @ExceptionHandler({ Exception.class })
    public ResponseEntity<ResponsePayload> handleException(Exception ex) {
        LOGGER.error("The expection {} thrown by application", ex);
        ResponsePayload resp = new ResponsePayload();
        resp.setMessage(ResponsePayload.Msg.FAILURE);
        resp.setFailureReasonCode(FAILURE_CODE);
        resp.setFailureReason(ErrorUtils.extractErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * handleException.
     */
    @ExceptionHandler({ ApiValidationFailedException.class })
    public ResponseEntity<ApiResponse<Object>> handleExceptions(ApiValidationFailedException e) {
        LOGGER.error("Error has occurred while calling api, Error message: {} ", e.getErrorMessage());
        return WebUtils.getResponseEntity(
                new ApiResponse.Builder<>(e.getCode(), e.getMessage(), HttpStatus.BAD_REQUEST).build());

    }

    /**
     * handleException.
     */
    @ExceptionHandler({ ApiResourceNotFoundException.class })
    public ResponseEntity<ApiResponse<Object>> handleExceptions(ApiResourceNotFoundException e) {
        LOGGER.error("Error has occurred while calling api, Error message: {}", e.getErrorMessage());
        return WebUtils.getResponseEntity(
                new ApiResponse.Builder<>(e.getCode(), e.getMessage(), HttpStatus.NOT_FOUND).build());
    }

    /**
     * handleException.
     */
    @ExceptionHandler({ ApiPreConditionFailedException.class })
    public ResponseEntity<ApiResponse<Object>> handleExceptions(ApiPreConditionFailedException e) {
        LOGGER.error("Error has occurred while calling api, Error message: {}", e.getErrorMessage());
        return WebUtils.getResponseEntity(
                new ApiResponse.Builder<>(e.getCode(), e.getMessage(), HttpStatus.PRECONDITION_FAILED).build());
    }

    /**
     * handleException.
     */
    @ExceptionHandler({ ApiTechnicalException.class })
    public ResponseEntity<ApiResponse<Object>> handleExceptions(ApiTechnicalException e) {
        LOGGER.error("{}",
                ErrorUtils.buildError("## Error has occurred while performing api ", e, Collections.emptyMap()));
        return WebUtils.getResponseEntity(
                new ApiResponse.Builder<>(e.getCode(), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * handleException.
     */
    @ExceptionHandler({ ApiException.class })
    public ResponseEntity<ApiResponse<Object>> handleExceptions(ApiException e) {
        LOGGER.error("{}",
                ErrorUtils.buildError("## Error has occurred while performing api", e, Collections.emptyMap()));
        ApiResponse<Object> apiResponse = new ApiResponse.Builder<>(ApiMessageEnum.GENERAL_ERROR.getCode(),
                ApiMessageEnum.GENERAL_ERROR.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR).build();
        return WebUtils.getResponseEntity(apiResponse);
    }

}