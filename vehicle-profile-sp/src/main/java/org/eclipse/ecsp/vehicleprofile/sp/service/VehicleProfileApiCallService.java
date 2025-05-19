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

package org.eclipse.ecsp.vehicleprofile.sp.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinDecoderEnums;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinRegionEnums;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.wrappers.ApiResponse;
import org.eclipse.ecsp.vehicleprofile.wrappers.ResponsePayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class to provide REST call methods to query Vehicle Profile APIs.
 *
 *
 */
@Service
public class VehicleProfileApiCallService {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileApiCallService.class);

    /**
     * The vehicle profile REST base url.
     */
    @Value("${vehicle.profile.base.url}")
    private String vehicleProfileBaseUrl;

    /**
     * The vehicle profile REST controller url.
     */
    @Value("${vehicle.profile.controller.url}")
    private String vehicleProfileControllerUrl;

    /**
     * The vin decode REST controller url.
     */
    @Value("${vin.decoder.base.url}")
    private String vinDecoderBaseUrl;

    /**
     * Method to get vehicle profile.
     *
     * @param searchParams string
     * @return vehicle profile
     * @throws URISyntaxException - exception
     */
    public VehicleProfile getVehicleProfile(final String searchParams) throws URISyntaxException {
        try {
            LOGGER.info("Starting Vehicle Profile GET call for inputs {}",
                    searchParams.split("=")[0] + "=" + CommonUtils.maskContent(searchParams.split("=")[1]));
            String url = vehicleProfileBaseUrl + SpCommonConstants.URL_SEPARATOR + vehicleProfileControllerUrl + "?"
                    + searchParams;
            HttpHeaders headers = new HttpHeaders();
            headers.add(SpCommonConstants.CONTENT_TYPE, SpCommonConstants.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ResponsePayload> response = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), ResponsePayload.class);

            if (response.getBody().getMessage().equals(ResponsePayload.Msg.SUCCESS)) {
                LOGGER.info("Successfully Obtained response ...");

                ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
                List vpLst = mapper.convertValue(response.getBody().getData(), List.class);
                if (!vpLst.isEmpty()) {
                    VehicleProfile profile = mapper.convertValue(vpLst.get(0), VehicleProfile.class);
                    LOGGER.debug("Vehicle Profile data returned :{}", profile);
                    return profile;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Caught exception in GET Vehicle Profile API call with msg: {}", ex.getMessage());
        }
        return null;
    }

    /**
     * Method to update vehicle profile.
     *
     * @param vehicleProfile string
     * @param vehicleId string
     * @return boolean
     * @throws URISyntaxException - exception
     */
    public boolean updateVehicleProfile(final String vehicleId, VehicleProfile vehicleProfile)
            throws URISyntaxException {
        LOGGER.info("Starting Vehicle Profile PUT call for vehicle id: {} , vehicle profile :{}",
                CommonUtils.maskContent(vehicleId), vehicleProfile);
        try {
            vehicleProfile.setVehicleId(null);

            String url = vehicleProfileBaseUrl + SpCommonConstants.URL_SEPARATOR + vehicleProfileControllerUrl
                    + SpCommonConstants.URL_SEPARATOR + vehicleId;
            LOGGER.debug("PUT url used :{}",
                    vehicleProfileBaseUrl + SpCommonConstants.URL_SEPARATOR + vehicleProfileControllerUrl
                            + SpCommonConstants.URL_SEPARATOR + CommonUtils.maskContent(vehicleId));

            HttpEntity<VehicleProfile> httpEntity = new HttpEntity<>(vehicleProfile);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ResponsePayload> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity,
                    ResponsePayload.class);

            if (response.getBody().getMessage().equals(ResponsePayload.Msg.SUCCESS)) {
                LOGGER.debug("Successfully updated Vehicle Profile data ....response returned :{} ....",
                        response.getBody().getData());
                return response.getBody().getData().equals(Boolean.TRUE);
            } else {
                LOGGER.info("Update failed with code :{} and reason: {} ....", response.getBody().getFailureReason(),
                        response.getBody().getFailureReasonCode());
            }

        } catch (Exception ex) {
            LOGGER.error("Caught exception in PATCH Vehicle Profile API call with msg : {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Method to call POST API of vehicle profile.
     *
     * @param vehicleProfile string
     * @throws URISyntaxException - exception
     */
    public void createVehicleProfile(VehicleProfile vehicleProfile) throws URISyntaxException {
        LOGGER.info("Vehicle Profile POST call for vehicle profile :{}", vehicleProfile);
        try {
            String url = vehicleProfileBaseUrl + SpCommonConstants.URL_SEPARATOR + vehicleProfileControllerUrl;
            LOGGER.debug("POST url used :{}", url);

            if (null != vehicleProfile && Pattern.matches("[a-zA-Z0-9]+", vehicleProfile.getVin())) {
                HttpEntity<VehicleProfile> entity = new HttpEntity<>(vehicleProfile);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<ResponsePayload> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                        ResponsePayload.class);

                if (response.getBody() != null) {
                    if (ResponsePayload.Msg.SUCCESS.name().equalsIgnoreCase(response.getBody().getMessage().name())) {
                        if (response.getBody().getData() != null) {
                            LOGGER.debug("Successfully created Vehicle profile. Response obtained from API {}...",
                                    CommonUtils.maskContent(response.getBody().getData().toString()));
                            vehicleProfile.setVehicleId((String) response.getBody().getData());
                        } else {
                            LOGGER.error("Create Vehicle Profile failed with code :{} and reason: {} ....",
                                    response.getBody().getFailureReason(), response.getBody().getFailureReasonCode());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Caught exception in CREATE Vehicle Profile API call with msg : {}", ex.getMessage());
        }
    }

    /**
     * Method to call DELETE API of vehicle profile.
     *
     * @param vehicleProfile string
     * @return boolean
     * @throws URISyntaxException - exception
     */
    public boolean deleteVehicleProfile(VehicleProfile vehicleProfile) throws URISyntaxException {
        LOGGER.info("Vehicle Profile DELETE call");
        try {
            String url = vehicleProfileBaseUrl + SpCommonConstants.URL_SEPARATOR + vehicleProfileControllerUrl
                    + SpCommonConstants.URL_SEPARATOR + vehicleProfile.getVehicleId();
            LOGGER.debug("DELETE url used :{}",
                    vehicleProfileBaseUrl + SpCommonConstants.URL_SEPARATOR + vehicleProfileControllerUrl
                            + SpCommonConstants.URL_SEPARATOR + CommonUtils.maskContent(vehicleProfile.getVehicleId()));

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ResponsePayload> response = restTemplate.exchange(new URI(url), HttpMethod.DELETE,
                    requestEntity, ResponsePayload.class);

            if (response.getBody().getMessage().equals(ResponsePayload.Msg.SUCCESS)
                    && (Boolean) response.getBody().getData()) {
                LOGGER.debug("Successfully deleted Vehicle profile...");
                return true;
            } else {
                LOGGER.info("Deletion of Vehicle Profile failed with code :{} and reason: {} ....",
                        response.getBody().getFailureReason(), response.getBody().getFailureReasonCode());
            }

        } catch (Exception ex) {
            LOGGER.error("Caught exception in DELETE Vehicle Profile API call with msg : {}", ex.getMessage());
        }
        return false;
    }


    /**
     * Method to decode VIN.
     *
     * @param vin string
     * @param type string
     * @param region string
     * @return string
     */
    public String decodeVin(String vin, VinDecoderEnums type, VinRegionEnums region) {
        LOGGER.info("decodeVin - START vin: {}, type: {}, region: {}", CommonUtils.maskContent(vin), type, region);
        String url = prepareVinDecodeUrl(vin, type, region);
        String vinDecodedResult = null;
        HttpEntity httpEntity = HttpEntity.EMPTY;
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<ApiResponse> response = restTemplate.exchange(new URI(url), HttpMethod.POST, httpEntity,
                    ApiResponse.class);

            if (response.getBody() != null
                    && SpCommonConstants.SUCCESS_MESSAGE.equals(response.getBody().getMessage())) {
                vinDecodedResult = response.getBody().getData();
                LOGGER.info("decodeVin - Success");
            } else {
                LOGGER.info("VIN decode failed with code :{} and reason: {} ....",
                        (HttpStatus) response.getBody().getStatusCode(), response.getBody().getMessage());
            }

        } catch (Exception ex) {
            LOGGER.error("Exception occurred while calling VIN decode API ", ex);
        }
        return vinDecodedResult;
    }

    private String prepareVinDecodeUrl(String vin, VinDecoderEnums type, VinRegionEnums region) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(vehicleProfileBaseUrl).append(SpCommonConstants.URL_SEPARATOR).append(vinDecoderBaseUrl)
                .append(SpCommonConstants.URL_SEPARATOR).append("vins/").append(vin).append("/decode?type=")
                .append(type);
        if (region != null) {
            urlBuilder.append("&region=").append(region);
        }
        return urlBuilder.toString();
    }

}
