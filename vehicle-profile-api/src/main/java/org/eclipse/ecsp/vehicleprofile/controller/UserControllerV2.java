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


import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.exception.FailureReason;
import org.eclipse.ecsp.vehicleprofile.exception.NotFoundException;
import org.eclipse.ecsp.vehicleprofile.rest.mapping.AssociatedVehicle;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManagerV2;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.wrappers.ResponsePayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UserControllerV2 class.
 */
@RestController
@RequestMapping("/v2/users/")
public class UserControllerV2 {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(UserControllerV2.class);
    @Autowired
    private VehicleManagerV2 vehicleMgrV2;

    /**
     * getAssociatedVehicles.
     */
    @GetMapping(value = "{userId}/associatedVehicles")
    @Operation(summary = "GET /v2/users/{userId}/associatedVehicles", responses = {
      @ApiResponse(description = "Success", responseCode = "200", 
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "SelfManage,VehicleProfile" })
    public ResponsePayload getAssociatedVehicles(@PathVariable("userId") String userId,
            @RequestParam(value = "pageNumber", required = false) String pageNumber,
            @RequestParam(value = "pageSize", required = false) String pageSize,
            @RequestParam(value = "sortby", required = false) String sortby,
            @RequestParam(value = "orderby", required = false) String orderby) {
        ResponsePayload response = new ResponsePayload();
        LOGGER.info("Get associated vehicle data for userId {}, pageNumber {}, pageSize {}, sortBy {}, orderBy {} ",
                CommonUtils.maskContent(userId), pageNumber, pageSize, sortby, orderby);
        try {
            List<AssociatedVehicle> associatedVehicles = vehicleMgrV2.getAssociatedVehiclesData(userId, pageNumber,
                    pageSize, sortby, orderby);
            LOGGER.debug("associated vehicles data: {}", associatedVehicles);
            response.setData(associatedVehicles);
        } catch (NotFoundException exp) {
            LOGGER.error("No vehicle found exception {}", exp);
            response.setMessage(ResponsePayload.Msg.FAILURE);
            response.setFailureReasonCode(FailureReason.NO_VEHICLE_FOUND.getCode());
            response.setFailureReason(exp.getMessage());
        }
        LOGGER.info("associated vehicles response {}", response);
        return response;
    }
}
