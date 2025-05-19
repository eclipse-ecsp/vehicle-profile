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

import org.eclipse.ecsp.testutils.EmbeddedRedisServer;
import org.eclipse.ecsp.vehicleprofile.TheApplication;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.VinDto;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinDecoderEnums;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinRegionEnums;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeClientException;
import org.eclipse.ecsp.vehicleprofile.commons.exception.VinDecodeException;
import org.eclipse.ecsp.vehicleprofile.commons.service.rest.HttpClientService;
import org.eclipse.ecsp.vehicleprofile.commons.service.vin.internal.InternalDecoderUtils;
import org.eclipse.ecsp.vehicleprofile.commons.utils.JsonUtils;
import org.eclipse.ecsp.vehicleprofile.controller.VinDecodeController;
import org.eclipse.ecsp.vehicleprofile.service.VinDecodeService;
import org.eclipse.ecsp.vehicleprofile.test.utils.MongoServer;
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
 * DefaultVinDecoderTest.
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test-base.properties")
@ActiveProfiles("test1")
@ContextConfiguration(classes = { TheApplication.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultVinDecoderTest {

    private static final String DEFAULT_VIN = "2cf3cf35-bff3-4d68-be9a-49a03bf3fbac";

    @ClassRule
    public static MongoServer MongoServer = new MongoServer();

    @ClassRule
    public static final EmbeddedRedisServer REDIS = new EmbeddedRedisServer();

    @Autowired
    private VinDecodeController vinDecodeController;

    @Autowired
    private VinDecodeService vinDecodeService;

    @Autowired
    private HttpClientService httpClientService;

    @Test
    public void test_VinDecoderControllerApi() {
        Assert.assertNotNull(vinDecodeController.decodeVin(DEFAULT_VIN, VinDecoderEnums.DEFAULT, VinRegionEnums.USA));
        vinDecodeService.setVinDecodeEnabled("false");
        Assert.assertNotNull(vinDecodeController.decodeVin(DEFAULT_VIN, VinDecoderEnums.DEFAULT, VinRegionEnums.USA));
    }

    @Test
    public void test_idVinDecoderEnabled() {
        vinDecodeController.decodeVin(DEFAULT_VIN, VinDecoderEnums.DEFAULT, VinRegionEnums.USA);
        vinDecodeService.setVinDecodeEnabled("false");
        vinDecodeController.decodeVin(DEFAULT_VIN, VinDecoderEnums.DEFAULT, VinRegionEnums.USA);

        vinDecodeService.setVinDecodeEnabled("true");
        boolean vinDecoderEnabled = vinDecodeService.isVinDecoderEnabled();
        Assert.assertTrue(vinDecoderEnabled);
    }

    @Test
    public void test_decodeVin_with_vin() throws VinDecodeException, VinDecodeClientException {
        vinDecodeService.setVinDecodeEnabled("true");
        Assert.assertNotNull(vinDecodeService.decodeVin(DEFAULT_VIN));
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
    public void test_HttpClientService() throws IOException {
        String result = httpClientService.executeGetMethod(
                "https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVin/2cf3cf35-bff3-4d68-be9a-49a03bf3fbac?format=json");
        Assert.assertNotNull(result);
    }
}