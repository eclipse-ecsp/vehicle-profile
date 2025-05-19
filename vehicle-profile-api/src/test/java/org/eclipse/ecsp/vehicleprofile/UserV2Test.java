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

package org.eclipse.ecsp.vehicleprofile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.eclipse.ecsp.nosqldao.Order;
import org.eclipse.ecsp.testutils.EmbeddedRedisServer;
import org.eclipse.ecsp.vehicleprofile.controller.UserControllerV2;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManagerV2;
import org.eclipse.ecsp.vehicleprofile.test.utils.MongoServer;
import org.eclipse.ecsp.vehicleprofile.test.utils.VehicleProfileTestUtil;
import org.junit.Assert;
import org.junit.Before;
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
 * UserV2Test class.
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test-base.properties")
@ActiveProfiles("test1")
@ContextConfiguration(classes = { TheApplication.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV2Test {

    @ClassRule
    public static MongoServer MongoServer = new MongoServer();

    @ClassRule
    public static final EmbeddedRedisServer REDIS = new EmbeddedRedisServer();

    @Autowired
    private UserControllerV2 userControllerV2;

    @Autowired
    private VehicleManagerV2 vehicleManagerV2;

    /**
     * setUp.
     */
    @Before
    public void setUp() throws JsonParseException, JsonMappingException, IOException {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleManagerV2.createVehicle(vehicleProfile);
    }

    @Test
    public void testSearchUser() throws JsonParseException, JsonMappingException, IOException {
        Assert.assertNotNull(
                userControllerV2.getAssociatedVehicles("schunchu", "1", "10", "vehicleId", Order.ASC.toString()));
        Assert.assertNotNull(
                userControllerV2.getAssociatedVehicles("schunchu", "1", "150", "vehicleId", Order.ASC.toString()));
    }

    @Test
    public void testSearchUserNotFound() throws JsonParseException, JsonMappingException, IOException {
        Assert.assertNotNull(
                userControllerV2.getAssociatedVehicles("admin", null, null, "vehicleId", Order.ASC.toString()));
        Assert.assertNotNull(
                userControllerV2.getAssociatedVehicles("admin", "0", "0", "vehicleId", Order.ASC.toString()));
        Assert.assertNotNull(
                userControllerV2.getAssociatedVehicles("admin", "", "", "vehicleId", Order.DESC.toString()));
    }

}
