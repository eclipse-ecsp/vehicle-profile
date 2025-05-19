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
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileEcuFilterRequest;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileToVehicleAdapter;
import org.eclipse.ecsp.vehicleprofile.enums.ApiMessageEnum;
import org.eclipse.ecsp.vehicleprofile.exception.ApiException;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManager;
import org.eclipse.ecsp.vehicleprofile.utils.ApiResponse;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.Utils;
import org.eclipse.ecsp.vehicleprofile.utils.VehicleProfileToVehicleAdapterProvider;
import org.eclipse.ecsp.vehicleprofile.utils.WebUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

/**
 * VehiclesControllerV2 class.
 */
@RestController
@RequestMapping("/v2/vehicles")
public class VehiclesControllerV2 {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehiclesControllerV2.class);

    @Autowired
    private VehicleProfileToVehicleAdapterProvider vehicleAdapterProvidor;

    @Autowired
    private VehicleManager vehicleManager;

    @Autowired
    private VehicleProfileControllerV2 vehicleProfileControllerV2;

    /**
     * get vehicleProfile.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "GET /v2/vehicles", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success", 
          responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "AssociateMyselfToVehicle,SelfManage" })
    public ResponseEntity<ApiResponse<List<VehicleProfileToVehicleAdapter>>> search(
            @RequestParam Map<String, String> searchParams) {
        LOGGER.info("v2 search keys {} values {}", searchParams.keySet(),
                searchParams.values().stream().map(x -> x = CommonUtils.maskContent(x)).toArray());
        try {

            ApiResponse<List<VehicleProfileToVehicleAdapter>> apiResponse;
            List<VehicleProfile> vehicleProfiles = vehicleManager.search(searchParams);
            List<VehicleProfileToVehicleAdapter> vehicles = Utils.getVehicleAdapters(vehicleAdapterProvidor,
                    vehicleProfiles);
            apiResponse = new ApiResponse.Builder<List<VehicleProfileToVehicleAdapter>>(
                    ApiMessageEnum.VP_SUCCESS.getCode(), ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(vehicles)
                            .build();
            LOGGER.info("search keys {} values {}, returning", searchParams.keySet(),
                    searchParams.values().stream().map(x -> x = CommonUtils.maskContent(x)).toArray());
            return WebUtils.getResponseEntity(apiResponse);

        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * get vehicle details by clientId.
     */
    @Hidden
    @GetMapping(value = "/getEcuByClientId", produces = "application/json")
    public ResponseEntity<ApiResponse<VehicleProfileEcuFilterRequest>> getEcuByClientId(
            @RequestParam(Constants.DB_FIELD_NAME_CLIENTID) String clientId) {
        LOGGER.info("v2 getEcuByClientId entered {} ", CommonUtils.maskContent(clientId));
        try {
            ApiResponse<VehicleProfileEcuFilterRequest> apiResponse;
            VehicleProfileEcuFilterRequest vehicleDetails = vehicleManager.getEcuByClientId(clientId);
            apiResponse = new ApiResponse.Builder<VehicleProfileEcuFilterRequest>(ApiMessageEnum.VP_SUCCESS.getCode(),
                    ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(vehicleDetails).build();
            LOGGER.info("v2 getEcuByClientId returning {} ", CommonUtils.maskContent(clientId));
            return WebUtils.getResponseEntity(apiResponse);
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * update vehicleProfile.
     */
    @PatchMapping(consumes = "application/json", value = "/{vehicleId}")
    @Operation(summary = "PATCH /v2/vehicles/{vehicleId}", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success", 
           responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "AssociateMyselfToVehicle,SelfManage" })
    public ResponseEntity<ApiResponse<Boolean>> update(@RequestBody VehicleProfile vehicleProfile,
            @PathVariable("vehicleId") String vehicleId,
            @RequestHeader(required = false) final Map<String, String> headerMap) {
        LOGGER.info("v2 PATCH {} {}", CommonUtils.maskContent(vehicleId), vehicleProfile);
        ResponseEntity<ApiResponse<Boolean>> response = vehicleProfileControllerV2.update(vehicleProfile, vehicleId,
                headerMap);
        LOGGER.info("v2 PATCH {} {}, returning {}", CommonUtils.maskContent(vehicleId), vehicleProfile,
                response);
        return response;
    }

}
