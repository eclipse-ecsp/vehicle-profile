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

import jakarta.annotation.PostConstruct;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.NhtsaResponse;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinDto;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinRegionDto;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinUrlDto;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.vehiclespecs.VehicleSpecificationVinDto;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinDecoderEnums;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinRegionEnums;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeClientException;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeException;
import org.eclipse.ecsp.vehicleprofile.commons.service.vin.VinDecoder;
import org.eclipse.ecsp.vehicleprofile.commons.utils.JsonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.eclipse.ecsp.vehicleprofile.constants.Constants.CODE_VALUE_DECODER;
import static org.eclipse.ecsp.vehicleprofile.constants.Constants.DEFAULT_DECODER;
import static org.eclipse.ecsp.vehicleprofile.constants.Constants.POSITION_MATCHER_DECODER;
import static org.eclipse.ecsp.vehicleprofile.constants.Constants.SKIP_UNKOWN_PROPERTIES;
import static org.eclipse.ecsp.vehicleprofile.constants.Constants.VEHICLE_SPECIFICATION;

/**
 * Decoder service to decode vin using default or provided decoder.
 */
@Service
public class VinDecodeService {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VinDecodeService.class);

    @Value("${vin.decode.enable}")
    private String vinDecodeEnabled;

    @Value("${vin.decoder}")
    private String vinDecoder;

    @Value("${default.vin.decode.url}")
    private String defaultDecodeUrl;

    @Value("${vehicle.specification.auth.base.url}")
    private String vehicleSpecificationAuthBaseUrl;

    @Value("${vehicle.specification.auth.token.url}")
    private String vehicleSpecificationAuthTokenUrl;

    @Value("${vehicle.specification.auth.token.audience}")
    private String vehicleSpecificationAuthTokenAudience;

    @Value("${vehicle.specification.auth.token.grant.type}")
    private String vehicleSpecificationAuthTokenGrantType;

    @Value("${vehicle.specification.vin.decode.base.url}")
    private String vehicleSpecificationVinDecodeBaseUrl;

    @Value("${vehicle.specification.vin.decode.url}")
    private String vehicleSpecificationVinDecodeUrl;

    @Value("${vehicle.specification.client.id}")
    private String vehicleSpecificationClientId;

    @Value("${vehicle.specification.client.secret}")
    private String vehicleSpecificationClientSecret;

    private final VinDecoder<VinDto> externalVinDecoder;
    private final VinDecoder<VinDto> codeValueVinDecoder;
    private final VinDecoder<String> positionMatcherVinDecoder;
    private final VinDecoder<VinDto> vehicleSpecificationVinDecoder;

    /**
     * VinDecodeService.
     */
    @Autowired
    public VinDecodeService(@Qualifier(value = "defaultVinDecoder") VinDecoder<VinDto> externalVinDecoder,
            @Qualifier(value = "codeValueVinDecoder") VinDecoder<VinDto> codeValueVinDecoder,
            @Qualifier(value = "positionMatcherVinDecoder") VinDecoder<String> positionMatcherVinDecoder,
            @Qualifier(value = "vehicleSpecificationVinDecoder") VinDecoder<VinDto> vehicleSpecificationVinDecoder) {
        this.externalVinDecoder = externalVinDecoder;
        this.codeValueVinDecoder = codeValueVinDecoder;
        this.positionMatcherVinDecoder = positionMatcherVinDecoder;
        this.vehicleSpecificationVinDecoder = vehicleSpecificationVinDecoder;
    }

    /**
     * Decode vin using default decoder.
     */
    public String decodeVin(String vin) throws VinDecodeException, VinDecodeClientException {
        return decodeVin(vin, VinDecoderEnums.DEFAULT.getDecoderType(), VinRegionEnums.USA.getRegionType());
    }

    /**
     * Decode vin using provided decoder type.
     *
     * @param vin         vin (Vehicle Identification Number)
     * @param decoderType VinDecoder Type
     * @return decoded vin result in json format
     * @throws VinDecodeException if any error has occurred using provided decoder
     *                            type and region
     */
    public String decodeVin(String vin, String decoderType) throws VinDecodeException, VinDecodeClientException {
        return decode(vin, decoderType, VinRegionEnums.USA.getRegionType());
    }

    /**
     * Decode vin using provided decoder type and region.
     *
     * @param vin         vin (Vehicle Identification Number)
     * @param decoderType VinDecoder Type
     * @param regionEnums VinDecoder Region
     * @return decoded vin result in json format
     * @throws VinDecodeException if any error has occurred using provided decoder
     *                            type and region
     */
    public String decodeVin(String vin, String decoderType, String regionEnums)
            throws VinDecodeException, VinDecodeClientException {
        return decode(vin, decoderType, regionEnums);
    }

    /**
     * Decode vin using provided decoder type and region.
     *
     * @param vin           vin (Vehicle Identification Number)
     * @param decoderType   VinDecoder Type
     * @param decoderRegion VinDecoder Region
     * @return decoded vin result in json format
     * @throws VinDecodeException if any error has occurred using provided decoder
     *                            type and region
     */
    private String decode(String vin, String decoderType, String decoderRegion)
            throws VinDecodeException, VinDecodeClientException {
        LOGGER.info("## VinDecodeService - decodeVin - START vin: {}, decoderType: {}, decoderRegion: {}",
                CommonUtils.maskContent(vin), decoderType, decoderRegion);
        decoderRegion = (decoderRegion) == null ? VinRegionEnums.USA.getRegionType() : decoderRegion;
        decoderType = (decoderType != null) ? decoderType.trim().toLowerCase() : vinDecoder;

        LOGGER.info("## decoderType: {}, decoderRegion", decoderType, decoderRegion);
        String decodedResult = null;
        try {
            if (isVinDecoderEnabled()) {
                switch (decoderType) {
                    case CODE_VALUE_DECODER:
                        VinRegionDto vinRegionDto = new VinRegionDto();
                        vinRegionDto.setVin(vin);
                        vinRegionDto.setRegion(decoderRegion);
                        decodedResult = codeValueVinDecoder.decode(vinRegionDto);
                        break;
                    case POSITION_MATCHER_DECODER:
                        decodedResult = positionMatcherVinDecoder.decode(vin);
                        break;
                    case VEHICLE_SPECIFICATION:
                        VehicleSpecificationVinDto vehicleSpecificationVinDto = new VehicleSpecificationVinDto();
                        vehicleSpecificationVinDto.setVehicleSpecificationAuthBaseUrl(vehicleSpecificationAuthBaseUrl);
                        vehicleSpecificationVinDto
                        .setVehicleSpecificationAuthTokenUrl(vehicleSpecificationAuthTokenUrl);
                        vehicleSpecificationVinDto
                                .setVehicleSpecificationAuthTokenAudience(vehicleSpecificationAuthTokenAudience);
                        vehicleSpecificationVinDto
                                .setVehicleSpecificationAuthTokenGrantType(vehicleSpecificationAuthTokenGrantType);
                        vehicleSpecificationVinDto
                                .setVehicleSpecificationVinDecodeBaseUrl(vehicleSpecificationVinDecodeBaseUrl);
                        vehicleSpecificationVinDto
                        .setVehicleSpecificationVinDecodeUrl(vehicleSpecificationVinDecodeUrl);
                        vehicleSpecificationVinDto.setVehicleSpecificationClientId(vehicleSpecificationClientId);
                        vehicleSpecificationVinDto
                        .setVehicleSpecificationClientSecret(vehicleSpecificationClientSecret);
                        vehicleSpecificationVinDto.setVin(vin);
                        decodedResult = vehicleSpecificationVinDecoder.decode(vehicleSpecificationVinDto);
                        break;
                    case DEFAULT_DECODER:
                    default:
                        VinUrlDto vinUrlDto = new VinUrlDto();
                        vinUrlDto.setVin(vin);
                        vinUrlDto.setUrl(defaultDecodeUrl);
                        decodedResult = externalVinDecoder.decode(vinUrlDto);
                        NhtsaResponse nhtsaResponse = (NhtsaResponse) JsonUtils.createObjectFromJson(decodedResult,
                                NhtsaResponse.class, SKIP_UNKOWN_PROPERTIES);
                        decodedResult = JsonUtils.createJsonFromObject(nhtsaResponse);
                        break;
                }
            } else {
                LOGGER.info("## Cannot decode vin. This feature is disabled");
            }
        } catch (VinDecodeClientException e) {
            throw e;
        } catch (Exception e) {
            throw new VinDecodeException(e);
        }
        return decodedResult;
    }

    /**
     * Check whether VinDecode is enabled or not from system.
     *
     * @return true if VinDecode is enabled otherwise false
     */
    public boolean isVinDecoderEnabled() {
        return "true".equals(vinDecodeEnabled);
    }

    public void setVinDecodeEnabled(String vinDecodeEnabled) {
        this.vinDecodeEnabled = vinDecodeEnabled;
    }

    @PostConstruct
    private void initialize() throws Exception {
        LOGGER.info("## PostConstruct - START Initializing ");
        LOGGER.info("## vinDecodeEnabled {}", isVinDecoderEnabled());
        if (isVinDecoderEnabled()) {
            LOGGER.debug("## vehicleSpecificationClientId: {} ", vehicleSpecificationClientId);
            LOGGER.debug("## vehicleSpecificationClientSecret: {} ", vehicleSpecificationClientSecret);           
        }
        LOGGER.info("## PostConstruct - END");
    }
}
