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

import org.eclipse.ecsp.events.vehicleprofile.constants.Constants;

import org.eclipse.ecsp.security.Security;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.User;
import org.eclipse.ecsp.vehicleprofile.domain.Vehicle;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileToVehicleAdapter;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManager;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.Utils;
import org.eclipse.ecsp.vehicleprofile.utils.VehicleProfileToVehicleAdapterProvider;
import org.eclipse.ecsp.vehicleprofile.wrappers.ResponsePayload;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * VehiclesController class.
 */
@RestController
@RequestMapping("/v1.0/vehicles")
public class VehiclesController {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehiclesController.class);
    @Autowired
    private VehicleProfileToVehicleAdapterProvider vehicleAdapterProvidor;
    @Autowired
    private VehicleManager vehicleManager;

    @Autowired
    private VehicleProfileController vehicleProfileController;

    /**
     * find vehicle profile by id.
     */
    @Hidden
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success", 
          responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    public ResponsePayload find(@PathVariable("id") String id) {
        LOGGER.info("find {}", CommonUtils.maskContent(id));
        VehicleProfile vehicleProfile = vehicleManager.findVehicleById(id);
        Vehicle response = vehicleAdapterProvidor.getAdapter(vehicleProfile);
        LOGGER.info("find {}, returning ", CommonUtils.maskContent(id));
        return new ResponsePayload(response);
    }

    /**
     * search vehicle profile.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "GET /v1.0/vehicles", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success", 
          responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "AssociateMyselfToVehicle,SelfManage" })
    public ResponsePayload search(@RequestParam Map<String, String> searchParams) {
        LOGGER.info("search keys {} values {}", searchParams.keySet(),
                searchParams.values().stream().map(x -> x = CommonUtils.maskContent(x)).toArray());
        List<VehicleProfile> vehicleProfiles = vehicleManager.search(searchParams);
        List<VehicleProfileToVehicleAdapter> vehicles = Utils.getVehicleAdapters(vehicleAdapterProvidor,
                vehicleProfiles);
        LOGGER.info("search keys {} values {}, returning", searchParams.keySet(),
                searchParams.values().stream().map(x -> x = CommonUtils.maskContent(x)).toArray());
        return new ResponsePayload(vehicles);
    }

    /**
     * associate.
     */
    @PostMapping(value = "/{vehicleId}/associate", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "POST /v1.0/vehicles/{vehicleId}/associate", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success", 
          responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    public ResponsePayload associate(@PathVariable("vehicleId") String vehicleId, @RequestBody User user) {
        LOGGER.info("associate vheicleId {} user {} ", CommonUtils.maskContent(vehicleId), user);
        boolean response = vehicleManager.associate(vehicleId, user);
        LOGGER.info("associate vheicleId {} user {}, returning {} ", CommonUtils.maskContent(vehicleId), user,
                response);
        return new ResponsePayload(response);
    }

    /**
     * disassociate.
     */
    @Hidden
    @PostMapping(value = "/{vehicleId}/disassociate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponsePayload disassociate(@PathVariable("vehicleId") String vehicleId,
            @RequestBody Map<String, String> body,
            @RequestHeader(required = false, name = "clientIdOrigin") String clientIdOrigin) {
        String userId = body.get(Constants.REQUEST_PARAMETER_USERID);
        String reason = body.get(Constants.REQUEST_PARAMETER_REASON);
        LOGGER.info("disassociate vheicleId {} user {} reason {} clientIdOrigin {}", CommonUtils.maskContent(vehicleId),
                CommonUtils.maskContent(userId), reason, clientIdOrigin);
        boolean response = vehicleManager.disassociate(vehicleId, userId, reason, clientIdOrigin);
        LOGGER.info("disassociate vehicleId {} user {}, response {} ", CommonUtils.maskContent(vehicleId),
                CommonUtils.maskContent(userId), response);
        return new ResponsePayload(response);
    }

    /**
     * updateAssociation.
     */
    @Hidden
    @PatchMapping(value = "/{vehicleId}/associate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponsePayload updateAssociation(@PathVariable("vehicleId") String vehicleId, @RequestBody User user) {
        LOGGER.info("PATCH associate vheicleId {} user {} ", CommonUtils.maskContent(vehicleId), user);
        boolean response = vehicleManager.updateAssociation(vehicleId, user);
        LOGGER.info("PATCH associate vheicleId {} user {}, returning {} ", CommonUtils.maskContent(vehicleId), user,
                response);
        return new ResponsePayload(response);
    }

    /**
     * update vehicleProfile.
     */
    @PatchMapping(consumes = "application/json", value = "/{vehicleId}")
    @Operation(summary = "PATCH /v1.0/vehicles/{vehicleId}", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success", 
          responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "AssociateMyselfToVehicle,SelfManage" })
    public ResponsePayload update(@RequestBody VehicleProfile vehicleProfile,
            @PathVariable("vehicleId") String vehicleId,
            @RequestHeader(required = false) final Map<String, String> headerMap) {
        LOGGER.info("PATCH id {}, request {}", CommonUtils.maskContent(vehicleId), vehicleProfile);
        ResponsePayload response = vehicleProfileController.update(vehicleProfile, vehicleId, headerMap);
        LOGGER.info("PATCH id {}, request {}, returning {}", CommonUtils.maskContent(vehicleId),
                vehicleProfile, response);
        return response;
    }
}