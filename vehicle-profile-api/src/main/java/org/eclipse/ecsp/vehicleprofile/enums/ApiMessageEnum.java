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

package org.eclipse.ecsp.vehicleprofile.enums;

import java.text.MessageFormat;

/**
 * ApiMessageEnum class.
 */
public enum ApiMessageEnum {
    /**
     * Success.
     */
    VP_SUCCESS("vp-200xxx", "Success", "200 OK"),

    /**
     * System Created.
     */
    VEHICLE_ID_IS_SYSTEM_CREATED("vp-002", "Validation failed", "VehcileID is system generated, must not be sent"),

    /**
     * vin must be provided.
     */
    VIN_MUST_BE_PROVIDED("vp-003", "Validation failed", "VIN must be provided"),

    /**
     * Vehicle with provided VIN already exists in the system.
     */
    VEHICLE_WITH_VIN_ALREADY_EXIST("vp-004", "Validation failed",
            "Vehicle with provided VIN already exists in the system"),

    /**
     * Does not have valid ECUs.
     */
    DOES_NOT_HAVE_VALID_ECUS("vp-005", "Validation failed", "Does not have valid ECUs"),

    /**
     * Creation failed.
     */
    CREATION_FAILED("vp-006", "Internal server error", "Creation failed"),

    /**
     * No vehicle record found.
     */
    NO_VEHICLE_FOUND("vp-007", "Resource not found", "No vehicle record found"),

    /**
     * VehicleId and VIN cannot be modified.
     */
    VEHICLE_ID_VIN_CANNOT_BE_MODIFIED("vp-008", "Validation failed", "VehicleId and VIN cannot be modified"),

    /**
     * VIN does not match with existing record.
     */
    VIN_DOES_NOT_MATCH_WITH_EXISTING_RECORD("vp-009", "Validation failed",
            "Provided VIN does not match with existing record in db"),

    /**
     * Vehicle profile does not belong to user.
     */
    VEHICLE_PROFILE_DOES_NOT_BELONG_TO_USER("vp-010", "Validation failed", "Vehicle profile does not belong to user"),


    /**
     * Update failed.
     */
    UPDATE_FAILED("vp-011", "Internal server error", "Update failed"),

    /**
     * DeviceId or VIN is empty in request.
     */
    DEVICE_AND_VIN_MUST_BE_PRESENT("vp-012", "Validation failed", "DeviceId or VIN is empty in request"),

    /**
     * Internal data error.
     */
    INTERNAL_DATA_INCONSITENT_ERROR("vp-013", "Internal server error", "Internal data error"),

    /**
     * Search should have min one query parameter.
     */
    SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER("vp-014", "Validation failed",
            "Search must be performed with atleast one query parameter"),

    /**
     * deletion failed.
     */
    DELETION_FAILED("vp-015", "Resource not found", "Vehicle Profile does not exist for deletion."),

    /**
     * General error.
     */
    GENERAL_ERROR("vp-016", "Internal server error", "Not successful. Something went wrong. Please contact admin."),

    /**
     * VP not found.
     */
    VP_NOT_FOUND("vp-404", "Resource not found", "Not found"),

    /**
     * VIN decode failed.
     */
    VP_BAD_REQUEST_VIN_DECODE("vp-017", "Bad Request", "Bad Request for vin decode."),

    /**
     * VP internal server error.
     */
    VP_INTERNAL_SERVER_ERROR("vp-500", "Internal server error", "Internal server error"),

    /**
     * VP count by filter success.
     */
    VP_COUNT_BY_FILTER_SUCCESS("vp-018", "Success", "Count retrieved successfully"),

    /**
     * Vin must be valid.
     */
    VIN_MUST_BE_VALID("vp-020", "Bad Request", "VIN must be valid");

    private String code;
    private String message;
    private String generalMessage;

    ApiMessageEnum(String code, String message, String generalMessage) {
        this.code = code;
        this.message = message;
        this.generalMessage = generalMessage;
    }

    /**
     * Get the code.
     *
     * @return string
     */
    public String getCode() {
        return code;
    }

    /**
     * Set the code.
     *
     * @param code string
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Get the message.
     *
     * @return string
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the message.
     *
     * @param message string
     * @param value   object array
     * @return string
     */
    public String getMessage(String message, Object[] value) {
        return new MessageFormat(message).format(value);
    }

    /**
     * Set the message.
     *
     * @param message string
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the general message.
     *
     * @return string
     */
    public String getGeneralMessage() {
        return generalMessage;
    }

    /**
     * Set the general message.
     *
     * @param generalMessage string
     */
    public void setGeneralMessage(String generalMessage) {
        this.generalMessage = generalMessage;
    }
}
