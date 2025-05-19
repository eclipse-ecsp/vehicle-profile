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

import org.eclipse.ecsp.dao.utils.EmbeddedMongoDB;
import org.eclipse.ecsp.testutils.EmbeddedRedisServer;
import org.eclipse.ecsp.vehicleprofile.dao.VehicleDao;
import org.eclipse.ecsp.vehicleprofile.domain.Ecu;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.exception.ApiResourceNotFoundException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiValidationFailedException;
import org.eclipse.ecsp.vehicleprofile.service.VehicleAssociationService;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManager;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManagerV2;
import org.eclipse.ecsp.vehicleprofile.test.utils.VehicleProfileTestUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

/**
 * VehicleManagerV2Test class.
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test-base.properties")
@ActiveProfiles("test1")
@ContextConfiguration(classes = { TheApplication.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class VehicleManagerV2Test {

    @ClassRule
    public static final EmbeddedMongoDB MONGO_SERVER = new EmbeddedMongoDB();

    @ClassRule
    public static final EmbeddedRedisServer REDIS = new EmbeddedRedisServer();

    @Autowired
    private VehicleManagerV2 vehicleMgrV2;

    @Autowired
    private VehicleManager vehicleMgr;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 400;
    
    private String validVehicleId;

    @Autowired
    private VehicleAssociationService vehicleAssociationService;

    @Autowired
    private TestRestTemplate restTemplate;

    // Added as part of US 295583.
    @Value("${disable.dev.assoc.check}")
    private String disableDevAssocCheck;

    /**
     * This is for Mocking TestTemplate being used in code somewhere.
     */
    @Rule
    public final MockWebServer server = new MockWebServer();

    @InjectMocks
    private VehicleManagerV2 vehicleManagerV2;

    @Mock
    VehicleDao vehicleDao;

    /**
     * setUp.
     */
    @Before
    public void setUp() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        this.validVehicleId = vehicleMgrV2.createVehicle(vehicleProfile);
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
    }

    @Test
    public void testCreateVehicle() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        String id = vehicleMgrV2.createVehicle(vehicleProfile);
        assertEquals(vehicleProfile.getVin(), id);
    }

    @Test(expected = ApiValidationFailedException.class)
    public void testCreateVehicleEmptyVin() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleProfile.setVin(null);
        vehicleMgrV2.createVehicle(vehicleProfile);
    }

    @Test(expected = ApiValidationFailedException.class)
    public void testCreateVehicleExistingVin() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleMgrV2.createVehicle(vehicleProfile);
        vehicleMgrV2.createVehicle(vehicleProfile);
    }

    @Test(expected = ApiValidationFailedException.class)
    public void testCreateVehicleInvalidEcus() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        Ecu ecu = new Ecu();
        ecu.setClientId("testDeviceId");

        Map<String, Ecu> ecuMap = new HashMap<>();
        ecuMap.put("testEcus", ecu);
        vehicleProfile.setEcus(ecuMap);
        vehicleMgrV2.createVehicle(vehicleProfile);
    }

    @Test
    public void testUpdateVehicle() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setSoldRegion("USA");

        // Updated as part of US 295583.
        if (!Boolean.valueOf(disableDevAssocCheck)) {
            server.enqueue(new MockResponse().setBody("{\"userId\": \"apitest43\"}").setResponseCode(SUCCESS_CODE));
            vehicleAssociationService.setAssociationBaseUrl("http://localhost:" + server.getPort());
        }
        boolean id = vehicleMgrV2.update(vehicleProfile, "apitest43");
        assertEquals(true, id);
    }

    @Test(expected = ApiResourceNotFoundException.class)
    public void testUpdateVehicleEmptyProfile() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleProfile.setVehicleId("testVehicleId");
        vehicleProfile.setSoldRegion("USA");

        server.enqueue(new MockResponse().setBody("{\"userId\": \"apitest43\"}").setResponseCode(SUCCESS_CODE));
        vehicleAssociationService.setAssociationBaseUrl("http://localhost:" + server.getPort());
        vehicleMgrV2.update(vehicleProfile, "apitest43");
    }

    @Test
    public void testUpdateVehicleNullEcu() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setSoldRegion("USA");
        Map<String, ? extends Ecu> ecu = vehicleProfile.getEcus();
        Iterator<Entry<String, Ecu>> ecuItr = ((Map<String, Ecu>) ecu).entrySet().iterator();
        while (ecuItr.hasNext()) {
            Entry<String, Ecu> ecuEntry = ecuItr.next();
            ecuEntry.getValue().setEcuType("");
        }
        vehicleProfile.setEcus(ecu);

        // Updated as part of US 295583.
        if (!Boolean.valueOf(disableDevAssocCheck)) {
            server.enqueue(new MockResponse().setBody("{\"userId\": \"apitest43\"}").setResponseCode(SUCCESS_CODE));
            vehicleAssociationService.setAssociationBaseUrl("http://localhost:" + server.getPort());
        }
        boolean id = vehicleMgrV2.update(vehicleProfile, "apitest43");
        assertTrue(id);
    }

    @Test(expected = ApiValidationFailedException.class)
    public void testUpdateVehicleInvalidEcus() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setVin("testVin");
        vehicleProfile.setSoldRegion("USA");
        Ecu ecu = new Ecu();
        ecu.setClientId("testDeviceId");

        Map<String, Ecu> ecuMap = new HashMap<>();
        ecuMap.put("testEcus", ecu);
        vehicleProfile.setEcus(ecuMap);
        server.enqueue(new MockResponse().setBody("{\"userId\": \"apitest43\"}").setResponseCode(SUCCESS_CODE));
        vehicleAssociationService.setAssociationBaseUrl("http://localhost:" + server.getPort());
        boolean id = vehicleMgrV2.update(vehicleProfile, "apitest43");
    }

    @Test(expected = Exception.class)
    public void testUpdateVehicleException() {
        // Updated as part of US 295583.
        assumeFalse(Boolean.valueOf(disableDevAssocCheck));
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setSoldRegion("USA");

        server.enqueue(new MockResponse().setBody("Failure").setResponseCode(ERROR_CODE));
        vehicleAssociationService.setAssociationBaseUrl("http://localhost:" + server.getPort());
        vehicleMgrV2.update(vehicleProfile, "apitest43");
    }

    @Test
    public void testPutVehicle() {
        VehicleProfile vehicleProfile = vehicleMgr.findVehicleById(validVehicleId);
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setSoldRegion("USA");
        boolean id = vehicleMgrV2.put(vehicleProfile);
        assertEquals(true, id);
    }

    @Test(expected = ApiResourceNotFoundException.class)
    public void testPutVehicleEmptyProfile() {
        VehicleProfile vehicleProfile = vehicleMgr.findVehicleById(validVehicleId);
        vehicleProfile.setVehicleId("invalidVehicleId");
        vehicleProfile.setSoldRegion("USA");
        vehicleMgrV2.put(vehicleProfile);
    }

    @Test(expected = ApiValidationFailedException.class)
    public void testPutVehicleNonExistingVin() {
        VehicleProfile vehicleProfile = vehicleMgr.findVehicleById(validVehicleId);
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setVin("testVin");
        vehicleProfile.setSoldRegion("USA");
        vehicleMgrV2.put(vehicleProfile);
    }

    @Test(expected = ApiValidationFailedException.class)
    public void testPutVehicleInvalidEcus() {
        VehicleProfile vehicleProfile = vehicleMgr.findVehicleById(validVehicleId);
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setSoldRegion("USA");
        Ecu ecu = new Ecu();
        ecu.setClientId("testDeviceId");

        Map<String, Ecu> ecuMap = new HashMap<>();
        ecuMap.put("testEcus", ecu);
        vehicleProfile.setEcus(ecuMap);
        vehicleMgrV2.put(vehicleProfile);
    }

    @Test
    public void testGetById() {
        Object object = vehicleMgrV2.get(validVehicleId,"InvalidPath");
        assertNotNull(object);
    }

    @Test(expected = ApiResourceNotFoundException.class)
    public void testGetByInvalidId() {
        vehicleMgrV2.get("I_AM_INVALID_VEHICLE_ID", "InvalidPath");
    }

}
