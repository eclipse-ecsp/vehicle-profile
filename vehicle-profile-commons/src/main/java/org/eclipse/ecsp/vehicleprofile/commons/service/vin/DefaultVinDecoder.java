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

package org.eclipse.ecsp.vehicleprofile.commons.service.vin;

import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinDto;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinUrlDto;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeException;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * VIN Decoder service class to decode vin using any third party vendor api.
 *
 * @author aagrahari
 * @since 2.13
 */
@Service
public class DefaultVinDecoder extends AbstractExternalVinDecoder<VinDto> {

    /**
     * Decode VIN using any external Vendor API.
     *
     * @param vinDto object represents VIN and any external vin decoder url
     * @return String which hold actual vin decoded result string
     * @throws VinDecodeException If any error has occurred while calling NHTSA
     *                            vendor api
     */
    @Override
    public String decode(VinDto vinDto) throws VinDecodeException {
        VinUrlDto vinUrlDto = (VinUrlDto) vinDto;
        String vin = vinUrlDto.getVin();
        String url = vinUrlDto.getUrl();
        String thirdPartVinDecoderUrl = prepareThirdPartyVinDecoderApiUrl(url, vin);
        try {
            return httpClientService.executeGetMethod(thirdPartVinDecoderUrl);
        } catch (IOException e) {
            throw new VinDecodeException("Error has occurred while decoding vin using external decoder", e);
        }
    }

    private String prepareThirdPartyVinDecoderApiUrl(String thirdPartyVinDecodeUrl, String vin) {
        return thirdPartyVinDecodeUrl + "/" + vin + "?format=json";
    }
}
