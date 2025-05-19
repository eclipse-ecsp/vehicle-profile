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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.testutils.EmbeddedRedisServer;
import org.eclipse.ecsp.vehicleprofile.dao.VehicleDao;
import org.eclipse.ecsp.vehicleprofile.domain.LotusVehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertFalse;

/**
 * VehicleDaoDefaultOemTest class.
 */
@TestPropertySource("classpath:application-test-base.properties")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class VehicleDaoDefaultOemTest {

    @ClassRule
    public static final EmbeddedMongoDB MONGO_SERVER = new EmbeddedMongoDB();

    @ClassRule
    public static final EmbeddedRedisServer REDIS = new EmbeddedRedisServer();

    @Autowired
    private VehicleDao vehicleDao;

    @Test
    public void testSave() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        VehicleProfile vehicleProfile = objectMapper.readValue(
                "{\"vin\":\"VDDOTtestSave1\",\"ecus\":{\"hu\":{\"swVersion\":\"\"},\"telematics\":"
                + "{\"swVersion\":\"1.0\"}},\"vehicleAttributes\":{\"make\":\"make type\"}}",
                VehicleProfile.class);
        assertFalse(vehicleProfile instanceof LotusVehicleProfile);
        String vehicleId = vehicleDao.save(vehicleProfile).getVehicleId();
        vehicleDao.save(vehicleProfile);
        VehicleProfile dbVehicleProfile = vehicleDao.findById(vehicleId);
        assertFalse(dbVehicleProfile instanceof LotusVehicleProfile);
    }
}