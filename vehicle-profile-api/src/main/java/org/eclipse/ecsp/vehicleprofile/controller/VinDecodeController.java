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
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinDecoderEnums;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinRegionEnums;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeClientException;
import org.eclipse.ecsp.vehicleprofile.enums.ApiMessageEnum;
import org.eclipse.ecsp.vehicleprofile.service.VinDecodeService;
import org.eclipse.ecsp.vehicleprofile.utils.ApiResponse;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

/**
 * Vin decode controller to decode vin using default decoder or provide decoder
 * type from configuration.
 *
 * @author aagrahari
 * @since 2.13
 */
@RestController
@RequestMapping(value = "/v1")
public class VinDecodeController {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VinDecodeController.class);

    /**
     * Vin decoder service.
     */
    private final VinDecodeService vinDecodeService;

    @Autowired
    public VinDecodeController(VinDecodeService vinDecodeService) {
        this.vinDecodeService = vinDecodeService;
    }

    /**
     * Decode given vin of respective device. It has optional param to support
     * explicit decoder type. Possible decoder type value is "codeValue" Similarly
     * region is optional
     *
     * @param vinId vin
     * @param type  decoder type based on which we choose decoder like codeValue if
     *              it is null then default decoder (NHTSA) type used
     * @return decoded result response
     */
    @PostMapping(value = "/vins/{vinId}/decode")
    @Operation(summary = "POST /v1/vins/{vinId}/decode", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(description = "Success",
          responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) })
    @SecurityRequirement(name = "JwtAuthValidator", scopes = { "IgniteSystem" })
    public ResponseEntity<ApiResponse<String>> decodeVin(@PathVariable(value = "vinId") String vinId,
            @RequestParam(value = "type", required = false) VinDecoderEnums type,
            @RequestParam(value = "region", required = false) VinRegionEnums region) {
        LOGGER.info("VinDecodeController - decodeVin - START vinId: {}, type: {}, region: {}",
                CommonUtils.maskContent(vinId), type, region);
        ApiResponse<String> apiResponse;
        try {
            if (!vinId.matches("[a-zA-Z0-9]+")) {
                apiResponse = new ApiResponse.Builder<String>(ApiMessageEnum.VIN_MUST_BE_VALID.getCode(),
                        ApiMessageEnum.VIN_MUST_BE_VALID.getMessage(), BAD_REQUEST).build();
            }
            boolean vinDecoderEnabled = vinDecodeService.isVinDecoderEnabled();
            LOGGER.info("vinDecoderEnabled: {}", vinDecoderEnabled);
            if (vinDecoderEnabled) {
                String result = vinDecodeService.decodeVin(vinId, type != null ? type.getDecoderType() : null,
                        region != null ? region.getRegionType() : null);
                apiResponse = new ApiResponse.Builder<String>(ApiMessageEnum.VP_SUCCESS.getCode(),
                        ApiMessageEnum.VP_SUCCESS.getMessage(), OK).withData(result).build();
            } else {
                apiResponse = new ApiResponse.Builder<String>(ApiMessageEnum.VP_NOT_FOUND.getCode(),
                        ApiMessageEnum.VP_NOT_FOUND.getMessage(), NOT_FOUND).build();
            }
        } catch (VinDecodeClientException e) {
            LOGGER.error("Error has occurred while decoding vin, vin :{}, errMsg: {}, causeMsg: {}, errTrace: ",
                    CommonUtils.maskContent(vinId), e.getMessage(), ExceptionUtils.getRootCauseMessage(e), e);
            if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
                apiResponse = new ApiResponse.Builder<String>(ApiMessageEnum.VP_BAD_REQUEST_VIN_DECODE.getCode(),
                        ApiMessageEnum.VP_BAD_REQUEST_VIN_DECODE.getMessage(), BAD_REQUEST).build();
            } else if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                apiResponse = new ApiResponse.Builder<String>(ApiMessageEnum.VP_NOT_FOUND.getCode(),
                        ApiMessageEnum.VP_NOT_FOUND.getMessage(), NOT_FOUND).build();
            } else {
                apiResponse = new ApiResponse.Builder<String>(ApiMessageEnum.VP_INTERNAL_SERVER_ERROR.getCode(),
                        ApiMessageEnum.VP_INTERNAL_SERVER_ERROR.getMessage(), INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            LOGGER.error("## Error has occurred while decoding vin, Vin :{}, errMsg: {}, causeMsg: {}, errTrace: ",
                    CommonUtils.maskContent(vinId), e.getMessage(), ExceptionUtils.getRootCauseMessage(e), e);
            apiResponse = new ApiResponse.Builder<String>(ApiMessageEnum.VP_INTERNAL_SERVER_ERROR.getCode(),
                    ApiMessageEnum.VP_INTERNAL_SERVER_ERROR.getMessage(), INTERNAL_SERVER_ERROR).build();
        }
        return WebUtils.getResponseEntity(apiResponse);
    }
}
