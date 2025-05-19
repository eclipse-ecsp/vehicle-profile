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
import org.eclipse.ecsp.vehicleprofile.domain.Count;
import org.eclipse.ecsp.vehicleprofile.domain.FilterDto;
import org.eclipse.ecsp.vehicleprofile.domain.Inventory;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VinReplaceRequest;
import org.eclipse.ecsp.vehicleprofile.exception.BadRequestException;
import org.eclipse.ecsp.vehicleprofile.exception.FailureReason;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManager;
import org.eclipse.ecsp.vehicleprofile.utils.ApiResponse;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.WebUtils;
import org.eclipse.ecsp.vehicleprofile.wrappers.ResponsePayload;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.eclipse.ecsp.vehicleprofile.enums.ApiMessageEnum.VP_COUNT_BY_FILTER_SUCCESS;
import static org.springframework.http.HttpStatus.OK;

/**
 * VehicleProfileController class.
 */
@RestController
@RequestMapping(path = "/v1.0/vehicleProfiles")
public class VehicleProfileController {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileController.class);
    @Autowired
    private VehicleManager vehicleMgr;

    /**
     * create vehicle profile.
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(summary = "POST /v1.0/vehicleProfiles", responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success",
                    responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    @ResponseBody
    public ResponsePayload post(@RequestBody VehicleProfile req) {
        LOGGER.info("POST req: {}", req);
        String id = vehicleMgr.createVehicle(req);
        LOGGER.info("POST req - return req {}, response {}", req, CommonUtils.maskContent(id));
        return new ResponsePayload(id);
    }

    /**
     * get vehicle profile.
     */
    @Hidden
    @GetMapping(value = "/{id}/**", produces = "application/json")
    @ResponseBody
    public ResponsePayload get(@PathVariable("id") String id, HttpServletRequest req) {
        LOGGER.info("GET request id {}, req {}", CommonUtils.maskContent(id), req);
        Object object = vehicleMgr.get(id, req.getServletPath());
        LOGGER.info("GET request id {}, req {}, return response", CommonUtils.maskContent(id), req);
        return new ResponsePayload(object);

    }

    /**
     * update vehicle profile.
     */
    @Hidden
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    @Operation(responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success",
                    responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "SelfManage" })
    public ResponsePayload put(@PathVariable("id") String id, @RequestBody VehicleProfile vehicleProfile) {
        LOGGER.info("PUT id {}, profile {}", CommonUtils.maskContent(id), vehicleProfile);

        if (vehicleProfile.getVehicleId() != null && !vehicleProfile.getVehicleId().equals(id)) {
            throw new BadRequestException(FailureReason.VEHICLE_ID_VIN_CANNOT_BE_MODIFIED);
        }
        vehicleProfile.setVehicleId(id);
        boolean response = vehicleMgr.put(vehicleProfile);
        LOGGER.info("PUT id {}, profile {}, returning {}", CommonUtils.maskContent(id), vehicleProfile,
                response);
        return new ResponsePayload(response);
    }

    /**
     * update vehicle profile.
     */
    @PatchMapping(consumes = "application/json", value = "/{id}")
    @Operation(summary = "PATCH /v1.0/vehicleProfiles/{vehicleId}", responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success",
                    responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    public ResponsePayload update(@RequestBody VehicleProfile vehicleProfile, @PathVariable("id") String id,
                                  @RequestHeader(required = false) final Map<String, String> headerMap) {
        LOGGER.info("PATCH id {}, request {}", CommonUtils.maskContent(id), vehicleProfile);
        if (vehicleProfile.getVehicleId() != null || vehicleProfile.getVin() != null) {
            throw new BadRequestException(FailureReason.VEHICLE_ID_VIN_CANNOT_BE_MODIFIED);
        }
        vehicleProfile.setVehicleId(id);
        String userId = headerMap.get(Constants.USER_ID);
        boolean response = vehicleMgr.update(vehicleProfile, userId);
        LOGGER.info("PATCH id {}, request {}, returning {}", CommonUtils.maskContent(id),
                vehicleProfile, response);
        return new ResponsePayload(response);
    }

    /**
     * replaceVin.
     */
    @PutMapping(consumes = "application/json", value = "/vin/replace")
    @Operation(summary = "PUT /v1.0/vehicleProfiles/vin/replace", responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success",
                    responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    public ResponsePayload replaceVin(@RequestBody VinReplaceRequest req) throws Exception {
        LOGGER.info("Replace Vin - req: {}", req == null ? "" : req);

        if (req == null || StringUtils.isEmpty(req.getDeviceId()) || StringUtils.isEmpty(req.getVin())) {
            throw new BadRequestException(FailureReason.DEVICE_AND_VIN_MUST_BE_PRESENT);
        }
        ResponsePayload responsePayload = new ResponsePayload();

        String response = vehicleMgr.replaceVin(req.getDeviceId().trim(), req.getVin().trim());
        LOGGER.info("Replace Vin - returning: {}", response);
        responsePayload.setData(response);

        return responsePayload;
    }

    /**
     * This end points servers search operations.
     */
    @GetMapping(produces = "application/json")
    @Operation(summary = "GET /v1.0/vehicleProfiles/", responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success",
                    responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "SelfManage" })
    public ResponsePayload search(@RequestParam Map<String, String> searchParams) {
        LOGGER.info("search keys {} values {}", searchParams.keySet(),
                searchParams.values().stream().map(x -> x = CommonUtils.maskContent(x)).toArray());

        if (null == searchParams || searchParams.isEmpty()
                || (searchParams.size() == 1 && searchParams.containsKey(Constants.LOGICAL_OPERATOR_KEY))) {
            throw new BadRequestException(FailureReason.SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER);
        }
        List<VehicleProfile> profiles = vehicleMgr.search(searchParams);
        LOGGER.info("search - return params, returning");
        return new ResponsePayload(profiles);
    }

    /**
     * search clientIds by vehicleId,and serviceId.
     */
    @Hidden
    @GetMapping(value = "/clientId", produces = "application/json")
    public ResponsePayload search(@RequestParam(Constants.DB_FIELD_NAME_VEHICLEID) String vehicleId,
                                  @RequestParam(Constants.SEARCH_QUERY_PARAMETER_NAME_SERVICEID) String serviceId) {
        LOGGER.info("SEARCH vehicle id {} service id {}", CommonUtils.maskContent(vehicleId), serviceId);
        if (StringUtils.isBlank(vehicleId) || StringUtils.isBlank(serviceId)) {

            throw new BadRequestException(FailureReason.SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER);
        }
        return new ResponsePayload(vehicleMgr.searchClientIds(vehicleId, serviceId));
    }

    /**
     * delete by id.
     */
    @Hidden
    @DeleteMapping(value = "/{id}", produces = "application/json")
    public ResponsePayload delete(@PathVariable("id") String id) {
        LOGGER.info("DELETE id {}", CommonUtils.maskContent(id));
        boolean response = vehicleMgr.delete(id);
        LOGGER.info("DELETE id {}, returning {}", CommonUtils.maskContent(id), response);
        return new ResponsePayload(response);
    }

    /**
     * terminate device.
     */
    @Hidden
    @DeleteMapping(value = "/terminate/device", produces = "application/json")
    public ResponsePayload terminateDevice(@RequestParam(Constants.DB_FIELD_NAME_CLIENTID) String clientId) {
        LOGGER.info("DELETE by deviceId {}", CommonUtils.maskContent(clientId));
        boolean response = vehicleMgr.terminateDevice(clientId);
        LOGGER.info("DELETE by deviceId {}, returning {}", CommonUtils.maskContent(clientId), response);
        return new ResponsePayload(response);
    }

    /**
     * Get vehicle profile count based on filter criteria. If filter criteria is
     * empty then returns all vehicle profiles count.
     */
    @Hidden
    @PostMapping(value = "/filters/count", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse<Count>> getCountByFilter(@RequestBody FilterDto filterDto) {
        LOGGER.info("getCountByFilter - START");
        Count countByFilter = vehicleMgr.getCountByFilter(filterDto);
        return WebUtils.getResponseEntity(new ApiResponse.Builder<Count>(VP_COUNT_BY_FILTER_SUCCESS.getCode(),
                VP_COUNT_BY_FILTER_SUCCESS.getMessage(), OK).withData(countByFilter).build());
    }

    /**
     * streamByFilter.
     */
    @Hidden
    @PostMapping(value = "/filter", consumes = "application/json", produces = "text/event-stream")
    @Operation(responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success",
                    responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    public Flux<VehicleProfile> streamByFilter(@RequestBody FilterDto filterDto) {
        LOGGER.info("streamByFilter - START");
        return vehicleMgr.streamByFilter(filterDto);
    }

    /**
     * updateInventory.
     */
    @PostMapping(value = "/{id}/inventory", consumes = "application/json")
    @Operation(summary = "POST /v1.0/vehicleProfiles/{id}/inventory", responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success",
                    responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    public ResponsePayload updateInventory(@PathVariable("id") String id, @RequestBody Inventory inventory)
            throws Exception {
        LOGGER.info("POST id {} , inventory {}", CommonUtils.maskContent(id), inventory);
        boolean response = vehicleMgr.updateInventory(id, inventory);
        LOGGER.info("POST returning {}", response);
        return new ResponsePayload(response);
    }
}
