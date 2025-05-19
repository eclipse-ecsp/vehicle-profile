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

package org.eclipse.ecsp.vehicleprofile.commons.service.vin.external;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.ecsp.cache.PutStringRequest;
import org.eclipse.ecsp.cache.redis.IgniteCacheRedisImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinDto;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.vehiclespecs.VehicleSpecificationAuthTokenRequest;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.vehiclespecs.VehicleSpecificationVinDto;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeClientException;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeException;
import org.eclipse.ecsp.vehicleprofile.commons.service.vin.AbstractExternalVinDecoder;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * VehicleSpecificationVinDecoder.
 */
@Service
public class VehicleSpecificationVinDecoder extends AbstractExternalVinDecoder<VinDto> {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleSpecificationVinDecoder.class);
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    public static final String AUTHORIZATION = "Authorization";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EXPIRATION_TIME_IN_SEC = "expires_in";
    public static final String VEHICLE_CLASS_CODE = "vehicleClassCode";
    public static final String MODEL = "model";
    public static final String MODEL_YEAR = "modelYear";
    public static final String SPECIFICATION_AUTH_TOKEN = "vehicleSpecificationAuthToken";
    public static final long TIMESEC = 86400;
    public static final String ERROR_RESP = "VIN is either invalid or not found";

    public static final int VIN_DECODE_SUBSTRING = 3;
    public static final int VIN_DECODE_MIN = 300;
    public static final int VIN_DECODE_MAX = 1000;
    public static final int VIN_DECODE_LENGTH = 4;
    
    @Autowired
    private IgniteCacheRedisImpl vehicleProfileRedisDaoImpl;

    @Override
    public String decode(VinDto input)
            throws VinDecodeException,
            VinDecodeClientException {
        LOGGER.info("VehicleSpecificationVinDecoder - decode - START");
        String vinDecoderResponseJson = null;
        VehicleSpecificationVinDto vehicleSpecificationVinDto = (VehicleSpecificationVinDto) input;
        String vinDecodeBaseUrl = vehicleSpecificationVinDto.getVehicleSpecificationVinDecodeBaseUrl();
        String vinDecodeUrl = vehicleSpecificationVinDto.getVehicleSpecificationVinDecodeUrl();
        String vinDecoderUrl = vinDecodeBaseUrl + vinDecodeUrl + "?vin=" + vehicleSpecificationVinDto.getVin();

        // Extract token
        String bearerToken = vehicleProfileRedisDaoImpl.getString(SPECIFICATION_AUTH_TOKEN);
        String cachedVin = vehicleProfileRedisDaoImpl.getString(vehicleSpecificationVinDto.getVin());
        try {
            vinDecoderResponseJson =
                    getVinDecoderResponseJson(bearerToken,
                            vehicleSpecificationVinDto,
                            cachedVin,
                            vinDecoderResponseJson,
                            vinDecoderUrl);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                LOGGER.info("VIN decode failed for vin: {}",
                        CommonUtils.maskContent(vehicleSpecificationVinDto.getVin()));
                if (StringUtils.isBlank(cachedVin)) {
                    updateInfoInCache(vehicleSpecificationVinDto.getVin(), e.getMessage(),
                            (Long.valueOf(TIMESEC) - VIN_DECODE_MIN) * VIN_DECODE_MAX);
                }

            }
            throw new VinDecodeClientException(e.getStatusCode(), e);
        } catch (Exception e) {
            throw new VinDecodeException(e);
        }
        LOGGER.info("VehicleSpecificationVinDecoder - decode - END");
        return vinDecoderResponseJson;
    }

    private String getVinDecoderResponseJson(String bearerToken,
                                             VehicleSpecificationVinDto vehicleSpecificationVinDto,
                                             String cachedVin,
                                             String vinDecoderResponseJson,
                                             String vinDecoderUrl) throws VinDecodeException, IOException {
        if (StringUtils.isBlank(bearerToken) && !vehicleSpecificationVinDto.getVin().equalsIgnoreCase("Unknown")
                && StringUtils.isBlank(cachedVin)) {

            String vehicleSpecificationAuthBaseUrl = vehicleSpecificationVinDto
                    .getVehicleSpecificationAuthBaseUrl();
            String vehicleSpecificationAuthTokenUrl = vehicleSpecificationVinDto
                    .getVehicleSpecificationAuthTokenUrl();
            String vehicleSpecificationAuthTokenAudience = vehicleSpecificationVinDto
                    .getVehicleSpecificationAuthTokenAudience();
            String vehicleSpecificationAuthTokenGrantType = vehicleSpecificationVinDto
                    .getVehicleSpecificationAuthTokenGrantType();

            LOGGER.debug("## vehicleSpecificationAuthBaseUrl: {}", vehicleSpecificationAuthBaseUrl);
            LOGGER.debug("## vehicleSpecificationAuthTokenUrl: {}", vehicleSpecificationAuthTokenUrl);
            LOGGER.debug("## vehicleSpecificationAuthTokenAudience: {}", vehicleSpecificationAuthTokenAudience);
            LOGGER.debug("## vehicleSpecificationAuthTokenGrantType: {}", vehicleSpecificationAuthTokenGrantType);

            VehicleSpecificationAuthTokenRequest authTokenRequest = new VehicleSpecificationAuthTokenRequest();
            String vehicleSpecificationClientId = vehicleSpecificationVinDto.getVehicleSpecificationClientId();
            String vehicleSpecificationClientSecret = vehicleSpecificationVinDto
                    .getVehicleSpecificationClientSecret();
            authTokenRequest.setClientId(vehicleSpecificationClientId);
            authTokenRequest.setClientSecret(vehicleSpecificationClientSecret);
            authTokenRequest.setAudience(vehicleSpecificationAuthTokenAudience);
            authTokenRequest.setGrantType(vehicleSpecificationAuthTokenGrantType);

            String authTokenUrl = vehicleSpecificationAuthBaseUrl + vehicleSpecificationAuthTokenUrl;

            bearerToken = generateAuthToken(authTokenRequest, authTokenUrl);
        } else {
            LOGGER.info("Auth token extracted from cache");
        }

        // Call decoder
        if (!StringUtils.isBlank(vehicleSpecificationVinDto.getVin())
                && !vehicleSpecificationVinDto.getVin().equalsIgnoreCase("Unknown")) {
            if (StringUtils.isBlank(cachedVin)) {
                vinDecoderResponseJson = callVinDecoder(vinDecoderUrl, bearerToken,
                        vehicleSpecificationVinDto.getVin());
            } else {
                vinDecoderResponseJson = cachedVin;
                int i = 0;
                try {
                    i = Integer.parseInt(vinDecoderResponseJson.substring(0, VIN_DECODE_SUBSTRING));
                    throw new HttpClientErrorException(HttpStatus.valueOf(i), cachedVin);
                } catch (NumberFormatException e) {
                    LOGGER.debug("Retrieved actual vin response");
                }
            }
        }
        return vinDecoderResponseJson;
    }

    private String generateAuthToken(VehicleSpecificationAuthTokenRequest authTokenRequest, String authTokenUrl)
            throws VinDecodeException {
        String bearerToken;
        HttpHeaders authTokenHeader = new HttpHeaders();

        authTokenHeader.add(CONTENT_TYPE, APPLICATION_JSON);
        HttpEntity<VehicleSpecificationAuthTokenRequest> entity = new HttpEntity<>(authTokenRequest, authTokenHeader);
        LOGGER.info("## authTokenUrl: {}", authTokenUrl);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> authTokenResponse = restTemplate.exchange(authTokenUrl, HttpMethod.POST, entity,
                String.class);
        if (authTokenResponse.getStatusCode() == HttpStatus.OK) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            LOGGER.info("## Auth token generated successfully.");
            String authTokenResponseJson = authTokenResponse.getBody();
            try {
                String accessToken = getJsonAttributeValue(authTokenResponseJson, ACCESS_TOKEN);
                String expirationTimeInSec = getJsonAttributeValue(authTokenResponseJson, EXPIRATION_TIME_IN_SEC);
                bearerToken = "Bearer " + accessToken;
                updateInfoInCache(SPECIFICATION_AUTH_TOKEN, bearerToken,
                        (Long.valueOf(expirationTimeInSec) - VIN_DECODE_MIN) * VIN_DECODE_MAX);
            } catch (IOException e) {
                throw new VinDecodeException("Error has occurred while parsing auth token", e);
            }
        } else {
            throw new VinDecodeException("Unable to generate vin decoder auth token");
        }
        return bearerToken;
    }

    private void updateInfoInCache(String key, String accessToken, long expirationTimeInMs) {
        vehicleProfileRedisDaoImpl
                .putString(new PutStringRequest().withKey(key).withValue(accessToken).withTtlMs(expirationTimeInMs));
        LOGGER.info("Saved Info to cache");
    }

    private String callVinDecoder(String vinDecoderUrl, String bearerToken, String vin)
            throws IOException, VinDecodeException {
        HttpHeaders vinDecoderHeader = new HttpHeaders();
        vinDecoderHeader.add(CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        vinDecoderHeader.add(AUTHORIZATION, bearerToken);

        LOGGER.info("## vinDecoderUrl: {}", vinDecoderUrl.substring(0, 
                vinDecoderUrl.length() - VIN_DECODE_LENGTH) + "****");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> vinDecoderResponse;
        vinDecoderResponse = restTemplate.exchange(vinDecoderUrl, HttpMethod.GET,
                new HttpEntity<String>(vinDecoderHeader), String.class);
        String vinDecoderResponseJson = null;
        if (vinDecoderResponse.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("## Vin decoder executed successfully.");
            vinDecoderResponseJson = vinDecoderResponse.getBody();
            updateInfoInCache(vin, vinDecoderResponseJson, (Long.valueOf(TIMESEC) - VIN_DECODE_MIN) * VIN_DECODE_MAX);
            // print for testing
            String vehicleClassCode = getJsonAttributeValue(vinDecoderResponseJson, VEHICLE_CLASS_CODE);
            String model = getJsonAttributeValue(vinDecoderResponseJson, MODEL);
            String modelYear = getJsonAttributeValue(vinDecoderResponseJson, MODEL_YEAR);
            LOGGER.debug("## vehicleClassCode: {}", vehicleClassCode);
            LOGGER.debug("## make: {}", vehicleClassCode);
            LOGGER.debug("## model: {}", model);
            LOGGER.debug("## modelYear: {}", modelYear);
        } else {
            LOGGER.debug("## Vin decoder response code.", vinDecoderResponse.getStatusCode());
            if (vinDecoderResponse.getStatusCode() != HttpStatus.SERVICE_UNAVAILABLE
                    && vinDecoderResponse.getStatusCode() != HttpStatus.INTERNAL_SERVER_ERROR
                    && vinDecoderResponse.getStatusCode() != HttpStatus.UNAUTHORIZED
                    && vinDecoderResponse.getStatusCode() != HttpStatus.FORBIDDEN) {
                updateInfoInCache(vin, ERROR_RESP, (Long.valueOf(TIMESEC) - VIN_DECODE_MIN) * VIN_DECODE_MAX);
            }
            throw new VinDecodeException("Vin decoder failed");
        }
        return vinDecoderResponseJson;
    }
}
