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

package org.eclipse.ecsp.vehicleprofile.vindecode;

import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.entities.vin.CodeValue;
import org.eclipse.ecsp.testutils.EmbeddedRedisServer;
import org.eclipse.ecsp.vehicleprofile.TheApplication;
import org.eclipse.ecsp.vehicleprofile.commons.dao.CodeValueDao;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.Specification;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinDto;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinDecoderEnums;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinRegionEnums;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeClientException;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeException;
import org.eclipse.ecsp.vehicleprofile.commons.service.rest.HttpClientService;
import org.eclipse.ecsp.vehicleprofile.commons.service.vin.internal.CodeValueVinDecoder;
import org.eclipse.ecsp.vehicleprofile.commons.service.vin.internal.InternalDecoderUtils;
import org.eclipse.ecsp.vehicleprofile.commons.utils.JsonUtils;
import org.eclipse.ecsp.vehicleprofile.controller.VinDecodeController;
import org.eclipse.ecsp.vehicleprofile.service.VinDecodeService;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * CodeValueVinDecoderTest.
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test-base.properties")
@ActiveProfiles("test1")
@ContextConfiguration(classes = { TheApplication.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CodeValueVinDecoderTest {

    private static final String DEFAULT_VIN = "2cf3cf35-bff3-4d68-be9a-49a03bf3fbac";
    private static final String CODE_VALUE_VIN = "JN1TAAT32A0XXXXXX";

    @ClassRule
    public static final EmbeddedMongoDB MONGO_SERVER = new EmbeddedMongoDB();

    @ClassRule
    public static final EmbeddedRedisServer REDIS = new EmbeddedRedisServer();

    @Autowired
    private CodeValueDao codeValueDao;

    @Autowired
    private VinDecodeController vinDecodeController;

    @Autowired
    private VinDecodeService vinDecodeService;

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private CodeValueVinDecoder codeValueVinDecoder;

    @Test
    public void test_VinDecoderControllerApi() {
        Assert.assertNotNull(
                vinDecodeController.decodeVin(DEFAULT_VIN, VinDecoderEnums.CODE_VALUE, VinRegionEnums.USA));
    }

    @Test
    public void test_idVinDecoderEnabled() {
        vinDecodeController.decodeVin(DEFAULT_VIN, VinDecoderEnums.CODE_VALUE, VinRegionEnums.USA);

        vinDecodeService.setVinDecodeEnabled("true");
        boolean vinDecoderEnabled = vinDecodeService.isVinDecoderEnabled();
        Assert.assertTrue(vinDecoderEnabled);
    }

    @Test
    public void test_decodeVin_with_vin_and_decode_type() throws VinDecodeException, VinDecodeClientException {
        insertDummyCodeValue();
        vinDecodeService.setVinDecodeEnabled("true");
        Assert.assertNotNull(vinDecodeService.decodeVin(CODE_VALUE_VIN, VinDecoderEnums.CODE_VALUE.getDecoderType()));
    }

    @Test(expected = VinDecodeException.class)
    public void test_decodeVin_with_vin_and_decode_type_exception() throws Exception {
        insertDummyCodeValue();
        vinDecodeService.setVinDecodeEnabled("true");
        vinDecodeService.decodeVin(DEFAULT_VIN, VinDecoderEnums.CODE_VALUE.getDecoderType());
    }

    @Test
    public void test_decodeVin_with_decode_disable() throws VinDecodeException, VinDecodeClientException {
        insertDummyCodeValue();
        vinDecodeService.setVinDecodeEnabled("false");
        Assert.assertNull(vinDecodeService.decodeVin(CODE_VALUE_VIN, VinDecoderEnums.CODE_VALUE.getDecoderType()));
    }

    @Test
    public void test_decodeVin_with_vin_and_decode_type_null() throws VinDecodeException, VinDecodeClientException {
        insertDummyCodeValue();
        vinDecodeService.setVinDecodeEnabled("true");
        Assert.assertNotNull(vinDecodeService.decodeVin(CODE_VALUE_VIN, null, null));
    }

    @Test
    public void test_JsonUtils_createObject_FromJson() throws Exception {
        String vinJson = "{\"vin\":\"wine\"}";
        Object objectFromJson = JsonUtils.createObjectFromJson(vinJson, VinDto.class, false);
        Assert.assertNotNull(objectFromJson);

        objectFromJson = JsonUtils.createObjectFromJson(vinJson, VinDto.class, true);
        Assert.assertNotNull(objectFromJson);

        VinDto vinDto = new VinDto();
        vinDto.setVin("beer");
        String jsonFromObject = JsonUtils.createJsonFromObject(vinDto);
        Assert.assertNotNull(jsonFromObject);

        String vin = JsonUtils.getValue(vinJson, "vin");
        Assert.assertEquals("wine", vin);
    }

    @Test(expected = Exception.class)
    public void test_InternalDecoderUtils_invalidVin() {
        InternalDecoderUtils.validateVin("wonderful");
    }

    @Test
    public void test_InternalDecoderUtils_validVin() {
        InternalDecoderUtils.validateVin("JNTJCAD23Z0XXXXXX");
        String countryCode = InternalDecoderUtils.getCountryCode("JNTJCAD23Z0XXXXXX");
        Assert.assertNotNull(countryCode);
        String make = InternalDecoderUtils.getManufacturerCode("JNTJCAD23Z0XXXXXX");
        Assert.assertNotNull(make);
        String model = InternalDecoderUtils.getModelCode("JNTJCAD23Z0XXXXXX");
        Assert.assertNotNull(model);
    }

    @Test
    public void test_HttpClientService() throws IOException {
        String result = httpClientService.executeGetMethod(
                "https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVin/2cf3cf35-bff3-4d68-be9a-49a03bf3fbac?format=json");
        Assert.assertNotNull(result);
    }

    @Test
    public void test_CodeValueVinDecoder() throws Exception {
        insertDummyCodeValue();
        VinDto vinDto = new VinDto();
        vinDto.setVin("MNTJCAD23Z0XXXXXX");
        String decode = codeValueVinDecoder.decode(vinDto);
        Assert.assertNotNull(decode);
        Specification specification = (Specification) JsonUtils.createObjectFromJson(decode, Specification.class,
                false);
        Assert.assertEquals("WD23", specification.getModelCode());
        Assert.assertEquals("TERRA", specification.getModelName());
    }

    private void insertDummyCodeValue() {
        CodeValue codeValue = new CodeValue();
        codeValue.setCode("T32");
        codeValue.setValue("XTRAIL");
        codeValueDao.save(codeValue);

        codeValue.setCode("JN");
        codeValue.setValue("Japan");
        codeValueDao.save(codeValue);

        codeValue.setCode("JN1");
        codeValue.setValue("OEM_1");
        codeValueDao.save(codeValue);

        codeValue.setCode("WD23");
        codeValue.setValue("TERRA");
        codeValueDao.save(codeValue);

        codeValue.setCode("MN");
        codeValue.setValue("Thailand");
        codeValueDao.save(codeValue);

        codeValue.setCode("MNT");
        codeValue.setValue("OEM_2");
        codeValueDao.save(codeValue);

        codeValue.setCode("WD23");
        codeValue.setValue("TERRA");
        codeValueDao.save(codeValue);
    }
}