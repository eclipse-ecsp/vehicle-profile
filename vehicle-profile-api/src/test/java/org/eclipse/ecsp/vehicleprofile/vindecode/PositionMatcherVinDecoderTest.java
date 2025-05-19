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
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.PositionMatcherResult;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinDecoderEnums;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinRegionEnums;
import org.eclipse.ecsp.vehicleprofile.commons.service.vin.internal.PositionMatcherVinDecoder;
import org.eclipse.ecsp.vehicleprofile.commons.utils.JsonUtils;
import org.eclipse.ecsp.vehicleprofile.controller.VinDecodeController;
import org.eclipse.ecsp.vehicleprofile.service.VinDecodeService;
import org.eclipse.ecsp.vehicleprofile.test.utils.MongoServer;
import org.eclipse.ecsp.vehicleprofile.utils.ApiResponse;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * PositionMatcherVinDecoderTest.
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test-base.properties")
@ActiveProfiles("test1")
@ContextConfiguration(classes = { TheApplication.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PositionMatcherVinDecoderTest {
    private static final String SAMPLE_VIN_NUMBER = "56KTCDAA0K3378818";
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
    private PositionMatcherVinDecoder positionMatcherVinDecoder;

    @Test
    public void test_PositionMatcherVinDecoder() throws Exception {
        String decode = positionMatcherVinDecoder.decode(SAMPLE_VIN_NUMBER);
        Assert.assertNotNull(decode);
        PositionMatcherResult positionMatcherResult = (PositionMatcherResult) JsonUtils.createObjectFromJson(decode,
                PositionMatcherResult.class, false);
        Assert.assertNotNull(positionMatcherResult);

        String make = positionMatcherResult.getMake();
        String model = positionMatcherResult.getModel();
        String year = positionMatcherResult.getYear();

        Assert.assertEquals("Indian", make);
        Assert.assertEquals("Chieftain", model);
        Assert.assertEquals("2019", year);
    }

    @Test
    public void test_PositionMatcherVinDecoder_using_api() throws Exception {
        ResponseEntity<ApiResponse<String>> apiResponseResponseEntity = vinDecodeController.decodeVin(SAMPLE_VIN_NUMBER,
                VinDecoderEnums.POSITION_MATCHER, VinRegionEnums.USA);
        Assert.assertNotNull(apiResponseResponseEntity);
        ApiResponse<String> apiResponse = apiResponseResponseEntity.getBody();
        Assert.assertNotNull(apiResponse);
        HttpStatus statusCode = apiResponse.getStatusCode();
        String data = apiResponse.getData();
        PositionMatcherResult positionMatcherResult = (PositionMatcherResult) JsonUtils.createObjectFromJson(data,
                PositionMatcherResult.class, false);
        Assert.assertNotNull(positionMatcherResult);

        String make = positionMatcherResult.getMake();
        String model = positionMatcherResult.getModel();
        String year = positionMatcherResult.getYear();

        Assert.assertEquals("Indian", make);
        Assert.assertEquals("Chieftain", model);
        Assert.assertEquals("2019", year);
        Assert.assertNotNull(apiResponseResponseEntity);

        vinDecodeService.setVinDecodeEnabled("false");
        Assert.assertNull(vinDecodeService.decodeVin(DEFAULT_VIN, VinDecoderEnums.DEFAULT.getDecoderType()));
    }

    @Test
    public void test_PositionMatcherVinDecoder_using_api_when_vinDecodeEnabled_False() throws Exception {
        vinDecodeService.setVinDecodeEnabled("false");
        Assert.assertNull(vinDecodeService.decodeVin(DEFAULT_VIN, VinDecoderEnums.DEFAULT.getDecoderType()));
    }
}