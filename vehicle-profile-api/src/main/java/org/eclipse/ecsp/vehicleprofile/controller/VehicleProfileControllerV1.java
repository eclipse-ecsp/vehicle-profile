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
import org.eclipse.ecsp.vehicleprofile.domain.PageResponse;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileFilterRequest;
import org.eclipse.ecsp.vehicleprofile.enums.ApiMessageEnum;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManager;
import org.eclipse.ecsp.vehicleprofile.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

/**
 * VehicleProfileControllerV1 class.
 */
@RestController
@RequestMapping(path = "/v1/vehicleProfiles")
public class VehicleProfileControllerV1 {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileController.class);
    @Autowired
    private VehicleManager vehicleMgr;

    /**
     * Filter vehicle profiles by query parameters.
     */
    @PostMapping(value = "/filter", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Operation(summary = "POST /v1/vehicleProfiles/filter", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success", 
          responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    public ApiResponse<PageResponse<VehicleProfile>> filter(
            @RequestBody @Nullable VehicleProfileFilterRequest vehicleProfileFilterRequest,
            @RequestParam Map<String, String> requestParams) {
        LOGGER.info("Filter vehicle profiles with query params {} and body {} ", requestParams,
                vehicleProfileFilterRequest != null ? vehicleProfileFilterRequest : "");

        PageResponse<VehicleProfile> responseData = vehicleMgr.filter(vehicleProfileFilterRequest, requestParams);
        return new ApiResponse.Builder<PageResponse<VehicleProfile>>(ApiMessageEnum.VP_SUCCESS.getCode(),
                ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(responseData).build();
    }

}
