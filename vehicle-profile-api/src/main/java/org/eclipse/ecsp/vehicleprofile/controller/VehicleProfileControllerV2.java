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
import org.eclipse.ecsp.vehicleprofile.domain.VinReplaceRequest;
import org.eclipse.ecsp.vehicleprofile.enums.ApiMessageEnum;
import org.eclipse.ecsp.vehicleprofile.exception.ApiException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiResourceNotFoundException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiTechnicalException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiValidationFailedException;
import org.eclipse.ecsp.vehicleprofile.exception.BadRequestException;
import org.eclipse.ecsp.vehicleprofile.exception.InternalServerException;
import org.eclipse.ecsp.vehicleprofile.exception.NotFoundException;
import org.eclipse.ecsp.vehicleprofile.rest.mapping.EcuClient;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManager;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManagerV2;
import org.eclipse.ecsp.vehicleprofile.utils.ApiResponse;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.WebUtils;
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

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

/**
 * VehicleProfileControllerV2 class.
 */
@RestController
@RequestMapping(path = "/v2/vehicleProfiles")
public class VehicleProfileControllerV2 {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileController.class);
    @Autowired
    private VehicleManagerV2 vehicleMgrV2;

    @Autowired
    private VehicleManager vehicleMgr;

    /**
     * create vehicle profile.
     */
    @Hidden
    @PostMapping(consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> post(@RequestBody VehicleProfile req) {

        try {

            LOGGER.info("v2 POST req: {}", req);
            ApiResponse<String> apiResponse = null;
            if (null != req && req.getVin().matches("[a-zA-Z0-9]+")) {
                String id = vehicleMgrV2.createVehicle(req);
                apiResponse = new ApiResponse.Builder<String>(ApiMessageEnum.VP_SUCCESS.getCode(),
                        ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(id).build();
                LOGGER.info("v2 POST req - return req {}, response {}", req,
                        CommonUtils.maskContent(id));
            }
            return WebUtils.getResponseEntity(apiResponse);

        } catch (ApiValidationFailedException | ApiTechnicalException e) {

            throw e;

        } catch (Exception e) {

            throw new ApiException(e);
        }

    }

    /**
     * get vehicle profile by id.
     */
    @Hidden
    @GetMapping(value = "/{id}/**", produces = "application/json")
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> get(@PathVariable("id") String id, HttpServletRequest req) {
        try {

            LOGGER.info("v2 GET request id {}, req {}", CommonUtils.maskContent(id), req);
            ApiResponse<Object> apiResponse;
            Object object = vehicleMgrV2.get(id, req.getServletPath());
            apiResponse = new ApiResponse.Builder<Object>(ApiMessageEnum.VP_SUCCESS.getCode(),
                    ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(object).build();
            LOGGER.info("v2 GET request id {}, req {} - return, response ", CommonUtils.maskContent(id), req);
            return WebUtils.getResponseEntity(apiResponse);

        } catch (ApiResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * update vehicle profile by id.
     */
    @Hidden
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    //@Mdc(key = TracingFocusListConstant.TF_VIN, value = "#vehicleProfile.vin")
    public ResponseEntity<ApiResponse<Boolean>> put(@PathVariable("id") String id,
            @RequestBody VehicleProfile vehicleProfile) {
        try {
            LOGGER.info("v2 PUT id {} profile {}", CommonUtils.maskContent(id), vehicleProfile);
            if (vehicleProfile.getVehicleId() != null && !vehicleProfile.getVehicleId().equals(id)) {

                throw new ApiValidationFailedException(ApiMessageEnum.VEHICLE_ID_VIN_CANNOT_BE_MODIFIED.getCode(),
                        ApiMessageEnum.VEHICLE_ID_VIN_CANNOT_BE_MODIFIED.getMessage(),
                        ApiMessageEnum.VEHICLE_ID_VIN_CANNOT_BE_MODIFIED.getGeneralMessage());
            }
            vehicleProfile.setVehicleId(id);
            boolean response = vehicleMgrV2.put(vehicleProfile);
            ApiResponse<Boolean> apiResponse;
            apiResponse = new ApiResponse.Builder<Boolean>(ApiMessageEnum.VP_SUCCESS.getCode(),
                    ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(response).build();
            LOGGER.info("v2 PUT id {} profile {}, returning {}", CommonUtils.maskContent(id),
                    vehicleProfile, response);
            return WebUtils.getResponseEntity(apiResponse);

        } catch (ApiValidationFailedException | ApiResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * update vehicle profile.
     */
    @Hidden
    @PatchMapping(consumes = "application/json", value = "/{id}")
    //@Mdc(key = TracingFocusListConstant.TF_VIN, value = "#vehicleProfile.vin")
    public ResponseEntity<ApiResponse<Boolean>> update(@RequestBody VehicleProfile vehicleProfile,
            @PathVariable("id") String id, @RequestHeader(required = false) final Map<String, String> headerMap) {

        try {
            LOGGER.info("v2 PATCH {} {}", CommonUtils.maskContent(id), vehicleProfile);
            ApiResponse<Boolean> apiResponse;
            if (vehicleProfile.getVehicleId() != null || vehicleProfile.getVin() != null) {

                throw new ApiValidationFailedException(ApiMessageEnum.VEHICLE_ID_VIN_CANNOT_BE_MODIFIED.getCode(),
                        ApiMessageEnum.VEHICLE_ID_VIN_CANNOT_BE_MODIFIED.getMessage(),
                        ApiMessageEnum.VEHICLE_ID_VIN_CANNOT_BE_MODIFIED.getGeneralMessage());

            }
            vehicleProfile.setVehicleId(id);
            String userId = headerMap.get(Constants.USER_ID);
            boolean response = vehicleMgrV2.update(vehicleProfile, userId);
            apiResponse = new ApiResponse.Builder<Boolean>(ApiMessageEnum.VP_SUCCESS.getCode(),
                    ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(response).build();
            LOGGER.info("v2 PATCH {} {}, returning {}", CommonUtils.maskContent(id), vehicleProfile,
                    response);
            return WebUtils.getResponseEntity(apiResponse);

        } catch (ApiValidationFailedException | ApiResourceNotFoundException | ApiTechnicalException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * replaceVin.
     */
    @PutMapping(consumes = "application/json", value = "/vin/replace")
    //@Mdc(key = TracingFocusListConstant.TF_VIN, value = "#req.vin")
    @Operation(summary = "PUT /v2/vehicleProfiles/vin/replace", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success", 
          responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    public ResponseEntity<ApiResponse<String>> replaceVin(@RequestBody VinReplaceRequest req) {

        try {

            LOGGER.info("v2 Replace Vin - req: {}", req == null ? "" : req);
            ApiResponse<String> apiResponse = null;
            if (req == null || StringUtils.isEmpty(req.getDeviceId()) || StringUtils.isEmpty(req.getVin())) {

                throw new ApiValidationFailedException(ApiMessageEnum.DEVICE_AND_VIN_MUST_BE_PRESENT.getCode(),
                        ApiMessageEnum.DEVICE_AND_VIN_MUST_BE_PRESENT.getMessage(),
                        ApiMessageEnum.DEVICE_AND_VIN_MUST_BE_PRESENT.getGeneralMessage());

            }
            if (req.getDeviceId().matches("[a-zA-Z0-9]+") && req.getVin().matches("[a-zA-Z0-9]+")) {
                String response = vehicleMgr.replaceVin(req.getDeviceId(), req.getVin());
                apiResponse = new ApiResponse.Builder<String>(ApiMessageEnum.VP_SUCCESS.getCode(),
                        ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(response).build();
                LOGGER.info("v2 Replace Vin - returning: {}", response);
            }
            return WebUtils.getResponseEntity(apiResponse);

        } catch (NotFoundException e) {

            throw new ApiResourceNotFoundException(ApiMessageEnum.NO_VEHICLE_FOUND.getCode(),
                    ApiMessageEnum.NO_VEHICLE_FOUND.getMessage(), ApiMessageEnum.NO_VEHICLE_FOUND.getGeneralMessage());

        } catch (BadRequestException e) {

            throw new ApiTechnicalException(ApiMessageEnum.INTERNAL_DATA_INCONSITENT_ERROR.getCode(),
                    ApiMessageEnum.INTERNAL_DATA_INCONSITENT_ERROR.getMessage(),
                    ApiMessageEnum.INTERNAL_DATA_INCONSITENT_ERROR.getGeneralMessage());

        } catch (ApiValidationFailedException | ApiResourceNotFoundException | ApiTechnicalException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e);
        }

    }

    /**
     * This end points servers search operations.
     */
    @Hidden
    @GetMapping(produces = "application/json")
    //@Mdc(key = TracingFocusListConstant.TF_VIN, value = "#searchParams['vin']")
    public ResponseEntity<ApiResponse<List<VehicleProfile>>> search(@RequestParam Map<String, String> searchParams) {

        try {
            LOGGER.info("v2 search keys {} values {}", searchParams.keySet(),
                    searchParams.values().stream().map(x -> x = CommonUtils.maskContent(x)).toArray());
            ApiResponse<List<VehicleProfile>> apiResponse;
            if (null == searchParams || searchParams.isEmpty()
                    || (searchParams.size() == 1 && searchParams.containsKey(Constants.LOGICAL_OPERATOR_KEY))) {

                throw new ApiValidationFailedException(
                        ApiMessageEnum.SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER.getCode(),
                        ApiMessageEnum.SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER.getMessage(),
                        ApiMessageEnum.SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER.getGeneralMessage());

            }
            List<VehicleProfile> profiles = vehicleMgr.search(searchParams);
            apiResponse = new ApiResponse.Builder<List<VehicleProfile>>(ApiMessageEnum.VP_SUCCESS.getCode(),
                    ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(profiles).build();
            LOGGER.info("v2 search - return params, returning");
            return WebUtils.getResponseEntity(apiResponse);
        } catch (ApiValidationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e);
        }

    }

    /**
     * search clientIds by vehicleId,and serviceId.
     */
    @Hidden
    @GetMapping(value = "/clientId", produces = "application/json")
    public ResponseEntity<ApiResponse<List<EcuClient>>> search(
            @RequestParam(Constants.DB_FIELD_NAME_VEHICLEID) String vehicleId,
            @RequestParam(Constants.SEARCH_QUERY_PARAMETER_NAME_SERVICEID) String serviceId) {

        try {
            LOGGER.info("v2 SEARCH vehicle id {} service id {}", CommonUtils.maskContent(vehicleId), serviceId);
            ApiResponse<List<EcuClient>> apiResponse;
            if (StringUtils.isBlank(vehicleId) || StringUtils.isBlank(serviceId)) {

                throw new ApiValidationFailedException(
                        ApiMessageEnum.SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER.getCode(),
                        ApiMessageEnum.SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER.getMessage(),
                        ApiMessageEnum.SEARCH_SHOULD_HAVE_MIN_ONE_QUERY_PARAMETER.getGeneralMessage());

            }
            List<EcuClient> ecus = vehicleMgr.searchClientIds(vehicleId, serviceId);
            apiResponse = new ApiResponse.Builder<List<EcuClient>>(ApiMessageEnum.VP_SUCCESS.getCode(),
                    ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(ecus).build();
            return WebUtils.getResponseEntity(apiResponse);

        } catch (InternalServerException e) {

            throw new ApiTechnicalException(ApiMessageEnum.INTERNAL_DATA_INCONSITENT_ERROR.getCode(),
                    ApiMessageEnum.INTERNAL_DATA_INCONSITENT_ERROR.getMessage(),
                    ApiMessageEnum.INTERNAL_DATA_INCONSITENT_ERROR.getGeneralMessage());

        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    /**
     * delete vehicle profile by id.
     */
    @Hidden
    @DeleteMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable("id") String id) {
        try {
            LOGGER.info("v2 DELETE id {}", CommonUtils.maskContent(id));
            ApiResponse<Boolean> apiResponse;
            boolean response = vehicleMgr.delete(id);
            LOGGER.info("v2 DELETE id {}, returning {}", CommonUtils.maskContent(id), response);
            apiResponse = new ApiResponse.Builder<Boolean>(ApiMessageEnum.VP_SUCCESS.getCode(),
                    ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(response).build();
            return WebUtils.getResponseEntity(apiResponse);
        } catch (NotFoundException e) {

            throw new ApiResourceNotFoundException(ApiMessageEnum.DELETION_FAILED.getCode(),
                    ApiMessageEnum.DELETION_FAILED.getMessage(), ApiMessageEnum.DELETION_FAILED.getGeneralMessage());

        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

}
