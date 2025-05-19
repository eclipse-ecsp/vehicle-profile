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

package org.eclipse.ecsp.vehicleprofile.commons.service.vin.internal;

import org.eclipse.ecsp.entities.vin.CodeValue;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.commons.dao.CodeValueDao;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.Specification;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinDto;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeException;
import org.eclipse.ecsp.vehicleprofile.commons.service.vin.AbstractInternalVinDecoder;
import org.eclipse.ecsp.vehicleprofile.commons.utils.JsonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.eclipse.ecsp.vehicleprofile.commons.service.vin.internal.InternalDecoderUtils.getCountryCode;
import static org.eclipse.ecsp.vehicleprofile.commons.service.vin.internal.InternalDecoderUtils.getModelCode;
import static org.eclipse.ecsp.vehicleprofile.commons.service.vin.internal.InternalDecoderUtils.validateVin;

/**
 * This class decode vin based on following specification 1) Extract Model,
 * Country and Manufacturer code using VIN based on position of character(s) 2)
 * Fetch Model, Country and Manufacturer value from DB 3) Return Manufacturer,
 * Model and Country value as part of decode response.
 *
 * @author aagrahari
 * @since 2.13
 */
@Service
public class CodeValueVinDecoder extends AbstractInternalVinDecoder<VinDto> {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(CodeValueVinDecoder.class);

    private final CodeValueDao codeValueDao;

    @Autowired
    public CodeValueVinDecoder(CodeValueDao codeValueDao) {
        this.codeValueDao = codeValueDao;
    }

    /**
     * Decode vin.
     *
     * @param vinDto vin details
     * @return Object of VinDecodeResult of String type
     * @throws VinDecodeException If any error has occurred while decoding vin
     */
    @Override
    public String decode(VinDto vinDto) throws VinDecodeException {
        LOGGER.info("## CodeValueVinDecoder service - decode - START, vinDto: {}", vinDto);
        try {
            final String vin = vinDto.getVin().toUpperCase();
            validateVin(vin);
            Specification specification;
            String modelCode = getModelCode(vin);
            String countryCode = getCountryCode(vin);
            LOGGER.info("modelCode: {}, countryCode: {}", modelCode, countryCode);
            CodeValue modelCv = codeValueDao.findById(modelCode);
            CodeValue countryCv = codeValueDao.findById(countryCode);
            LOGGER.info("modelCodeValue: {}, countryCodeValue: {}", modelCv, countryCv);
            if (modelCv == null) {
                throw new IllegalArgumentException("Unable to find model value for ModelCode: " + modelCode + " of VIN "
                        + CommonUtils.maskContent(vin));
            } else if (countryCv == null) {
                throw new IllegalArgumentException("Unable to find country value for CountryCode: " + countryCode
                        + " of VIN: " + CommonUtils.maskContent(vin));
            } else {
                specification = new Specification(countryCv.getValue(), modelCv.getCode(), modelCv.getValue(), "");
                return JsonUtils.createJsonFromObject(specification);
            }
        } catch (Exception e) {
            throw new VinDecodeException("Error has occurred while decoding VIN: " + vinDto.getVin(), e);
        }
    }
}
