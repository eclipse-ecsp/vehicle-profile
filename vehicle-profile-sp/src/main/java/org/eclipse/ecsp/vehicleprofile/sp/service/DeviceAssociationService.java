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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.ModemInfo;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * DeviceAssociationService.
 */
@Service
public class DeviceAssociationService {

    @Value("${device.association.base.url}")
    private String deviceAssociationBaseUrl;

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(DeviceAssociationService.class);

    /**
     * Get device details by device id.
     *
     * @param deviceId the device id
     * @return the modem info
     */
    public ModemInfo getDeviceDetailsByDeviceId(String deviceId) {

        ModemInfo modemInfo = null;
        if (StringUtils.isNotBlank(deviceId)) {

            HttpHeaders headers = new HttpHeaders();
            headers.add(SpCommonConstants.CONTENT_TYPE, SpCommonConstants.APPLICATION_JSON);

            String url = deviceAssociationBaseUrl + "/v1/users/association/details?deviceid=" + deviceId;
            LOGGER.info("Calling device association api to update device details : {}", deviceAssociationBaseUrl
                    + "/v1/users/association/details?deviceid=" + CommonUtils.maskContent(deviceId));
            ObjectNode responseNode = null;
            try {

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
                        String.class);

                if (null != response && response.getStatusCode().equals(HttpStatus.OK)) {
                    ObjectMapper mapper = new ObjectMapper();
                    responseNode = mapper.readValue(response.getBody(), ObjectNode.class);
                    LOGGER.info("Successfully fetched device details for device: {}.",
                            CommonUtils.maskContent(deviceId));
                } else {
                    LOGGER.error("Fetch device association details failed with code :{} and reason: {}",
                            response.getStatusCode(), response.getBody());
                }
            } catch (Exception e) {
                LOGGER.error("Error has occurred while fetching device association details using API call: {}",
                        deviceAssociationBaseUrl + "/v1/users/association/details?deviceid="
                                + CommonUtils.maskContent(deviceId));
            }

            if (responseNode != null && responseNode.hasNonNull(SpCommonConstants.DEVICE_DEAILS)) {

                JsonNode deviceNode = responseNode.get(SpCommonConstants.DEVICE_DEAILS);
                modemInfo = new ModemInfo();
                if (deviceNode.hasNonNull(SpCommonConstants.MSISDN)) {
                    modemInfo.setMsisdn(deviceNode.get(SpCommonConstants.MSISDN).asText());
                }
                if (deviceNode.hasNonNull(SpCommonConstants.IMSI)) {
                    modemInfo.setImsi(deviceNode.get(SpCommonConstants.IMSI).asText());
                }
                if (deviceNode.hasNonNull(SpCommonConstants.IMEI)) {
                    modemInfo.setImei(deviceNode.get(SpCommonConstants.IMEI).asText());
                }
                if (deviceNode.hasNonNull(SpCommonConstants.ICCID)) {
                    modemInfo.setIccid(deviceNode.get(SpCommonConstants.ICCID).asText());
                }
            }
        }
        return modemInfo;
    }
}
