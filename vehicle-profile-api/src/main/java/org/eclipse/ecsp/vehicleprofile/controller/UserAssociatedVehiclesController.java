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


import org.eclipse.ecsp.security.HeaderContext;
import org.eclipse.ecsp.security.UserDetails;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.wrappers.ResponsePayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserAssociatedVehiclesController class.
 */
@RestController
@RequestMapping(value = "/v1.0/associatedVehicles")
public class UserAssociatedVehiclesController {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(UserAssociatedVehiclesController.class);

    @Autowired
    private UserController userController;

    /**
     * getAssociatedVehicles.
     */
    @GetMapping
    @Operation(summary = "GET /v1.0/associatedVehicles", responses = {
      @ApiResponse(description = "Success", responseCode = "200", 
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "SelfManage" })
    public ResponsePayload getAssociatedVehicles() {
        String userId = StringUtils.EMPTY;
        UserDetails userDetails = HeaderContext.getUserDetails();
        if (userDetails != null) {
            userId = userDetails.getUserId();
        }
        LOGGER.info("Get associatedVehicles called {}", CommonUtils.maskContent(userId));
        ResponsePayload response = userController.getAssociatedVehicles(userId);
        LOGGER.info("associatedVehicles response: {}", response);
        return response;
    }
}
