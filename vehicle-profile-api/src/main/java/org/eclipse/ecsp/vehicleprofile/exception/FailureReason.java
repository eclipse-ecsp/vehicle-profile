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
 * FailureReason enum.
 */
public enum FailureReason {
    NO_VEHICLE_FOUND(4120, "No vehicle record found"),
    VEHICLE_ID_VIN_CANNOT_BE_MODIFIED(4121, "VehicleId and VIN cannot be modified"),

    SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER(4122, "search must be performed with atleast one query parameter"),
    CLIENTID_SEARCH_SHOULD_HAVE_VEHICLEID_AND_SERVICEID(4123, "ClientId search should have vehicleId and serviceId"),

    VIN_DOES_NOT_MATCH_WITH_EXISTING_RECORD(4133, "Provided VIN does not match with existing record in db"),
    UPDATE_FAILED(4144, "Update failed"),
    VEHICLE_ID_IS_SYSTEM_CREATED(4145, "VehcileID is system generated, must not be sent"),

    VIN_MUST_BE_PROVIDED(4146, "VIN must be provided"),
    VEHICLE_WITH_VIN_ALREADY_EXIST(4147, "Vehicle with provided VIN already exists in the system"),
    DOES_NOT_HAVE_VALID_ECUS(4148, "Does not have valid ECUs"), CREATION_FAILED(4149, "Creation failed"),
    DELETION_FAILED(4150, "Vehicle Profile does not exist for deletion."),

    ASSOCIATION_USER_ID_MUST_BE_PRESENT(4160, "userId must be present"),
    ASSOCIATION_USER_VEHICLE_ASSOCIATION_ALREADY_EXIST(4161, "Vehicle to User association already exist"),
    ASSOCIATION_USER_VEHICLE_ASSOCIATION_DOES_NOT_EXIST(4162, "Vehicle to User association does not exist"),
    ASSOCIATION_USER_INVALID_ASSOCIATION_STATUS(4163, "status must not be null, and should contain valid status"),
    ASSOCIATION_MAX_NUMBER_OF_ASSOCIATIONS_REACHED(4164, "Max number of associations reached for the given vehicle"),
    VEHICLE_PROFILE_DOES_NOT_BELONG_TO_USER(4163, "Vehicle profile does not belong to user"),
    VEHICLE_PROFILE_ENROLLMENT_NOT_ALLOWED(4164, "Enrollment to this vehicle profile is not allowed"),

    INTERNAL_DATA_INCONSITENT_ERROR(5010, "Internal data error"),
    DEVICE_AND_VIN_MUST_BE_PRESENT(400, "DeviceId or VIN is empty in request");

    private int code;
    private String reason;

    /**
     * FailureReason constructor.
     *
     * @param code   int
     * @param reason string
     */
    FailureReason(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    /**
     * Get code.
     *
     * @return int
     */
    public int getCode() {
        return code;
    }

    /**
     * Set code.
     *
     * @param code int
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Get reason.
     *
     * @return string
     */
    public String getReason() {
        return reason;
    }

    /**
     * Set reason.
     *
     * @param reason string
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

}
