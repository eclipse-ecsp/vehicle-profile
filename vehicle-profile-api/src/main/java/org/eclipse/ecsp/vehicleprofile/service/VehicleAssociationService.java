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

package org.eclipse.ecsp.vehicleprofile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.exception.BadRequestException;
import org.eclipse.ecsp.vehicleprofile.exception.FailureReason;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * VehicleAssociationService.
 */
@Service
public class VehicleAssociationService {

    @Value("${vehicle.association.base.url}")
    private String associationBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    private static final String USER_ID = "userId";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String APPLICATION_JSON = "application/json";

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleAssociationService.class);

    /**
     * replaceVinForDevice.
     */
    public void replaceVinForDevice(String deviceId, String newVin) throws Exception {

        String url = associationBaseUrl + "/v1/device/" + deviceId + "/vin/" + newVin + "/replace";
        LOGGER.debug("Calling replaceVin api of vehicle-association-service.");

        try {
            HttpEntity httpEntity = HttpEntity.EMPTY;
            ResponseEntity<String> response;

            response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
            LOGGER.debug("Response: {}", response);
            if (Constants.API_SUCCESS.equals(response.getBody())) {
                LOGGER.info("Successfully replaced vin for device in vin_details table");
            } else {
                LOGGER.error("Update failed with code :{} and reason: {} ....", response.getStatusCode(),
                        response.getBody());
                throw new BadRequestException(FailureReason.INTERNAL_DATA_INCONSITENT_ERROR);
            }

        } catch (Exception e) {
            LOGGER.error("Exception occured while calling vehicle-association-service for vin replace for device ", e);
            throw e;
        }
    }

    public void setAssociationBaseUrl(String associationBaseUrl) {
        this.associationBaseUrl = associationBaseUrl;
    }

    /**
     * Get User Id from Harman Id.
     */
    public String getAssociatedUserIdfromHarmanId(String harmanId) {

        String userId = null;
        if (StringUtils.isNotBlank(harmanId)) {

            HttpHeaders headers = new HttpHeaders();
            headers.add(CONTENT_TYPE, APPLICATION_JSON);

            String url = associationBaseUrl + "/v1/users/association/details?deviceid=" + harmanId;
            LOGGER.info(" Device Associate URL: {}",
                    associationBaseUrl + "/v1/users/association/details?deviceid=" + CommonUtils.maskContent(harmanId));
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
                    String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode responseNode = null;

                try {
                    responseNode = mapper.readValue(response.getBody(), ObjectNode.class);
                } catch (IOException e) {
                    LOGGER.error("Error has occurred while finding associated userId for given HarmanId: {}",
                            CommonUtils.maskContent(harmanId));
                }
                if (responseNode != null && responseNode.has(USER_ID)) {
                    userId = responseNode.get(USER_ID).asText();
                }
            }
        }

        return userId;
    }
}
