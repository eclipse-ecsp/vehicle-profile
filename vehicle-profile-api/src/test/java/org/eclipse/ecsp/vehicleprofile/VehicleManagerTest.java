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
import org.eclipse.ecsp.entities.vin.CodeValue;
import org.eclipse.ecsp.testutils.EmbeddedRedisServer;
import org.eclipse.ecsp.vehicleprofile.commons.dao.CodeValueDao;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.dao.VehicleDao;
import org.eclipse.ecsp.vehicleprofile.domain.Count;
import org.eclipse.ecsp.vehicleprofile.domain.Ecu;
import org.eclipse.ecsp.vehicleprofile.domain.FilterDto;
import org.eclipse.ecsp.vehicleprofile.domain.Filters;
import org.eclipse.ecsp.vehicleprofile.domain.Inventory;
import org.eclipse.ecsp.vehicleprofile.domain.InventoryEcu;
import org.eclipse.ecsp.vehicleprofile.domain.InventoryEcuHardware;
import org.eclipse.ecsp.vehicleprofile.domain.InventoryScomo;
import org.eclipse.ecsp.vehicleprofile.domain.PageResponse;
import org.eclipse.ecsp.vehicleprofile.domain.TermsAndConditions;
import org.eclipse.ecsp.vehicleprofile.domain.User;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileEcuFilterRequest;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileFilterRequest;
import org.eclipse.ecsp.vehicleprofile.exception.BadRequestException;
import org.eclipse.ecsp.vehicleprofile.exception.NotFoundException;
import org.eclipse.ecsp.vehicleprofile.rest.mapping.EcuClient;
import org.eclipse.ecsp.vehicleprofile.service.VehicleAssociationService;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManager;
import org.eclipse.ecsp.vehicleprofile.test.utils.VehicleProfileTestUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

/**
 * VehicleManagerTest class.
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test-base.properties")
@ActiveProfiles("test1")
@ContextConfiguration(classes = { TheApplication.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class VehicleManagerTest {
    @ClassRule
    public static final EmbeddedMongoDB MONGO_SERVER = new EmbeddedMongoDB();

    @ClassRule
    public static final EmbeddedRedisServer REDIS = new EmbeddedRedisServer();

    @Autowired
    private VehicleManager vehicleMgr;

    @Autowired
    private CodeValueDao codeValueDao;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 400;
    
    private String validVehicleId;

    private String vin;

    @Autowired
    private VehicleAssociationService vehicleAssociationService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${disable.dev.assoc.check}")
    private String disableDevAssocCheck;

    private static ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * This is for Mocking TestTemplate being used in code somewhere.
     */
    @Rule
    public final MockWebServer server = new MockWebServer();
    VehicleProfile vehicleProfile;

    /**
     * setUp.
     */
    @Before
    public void setUp() {
        vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        this.validVehicleId = vehicleMgr.createVehicle(vehicleProfile);
        this.vin = vehicleProfile.getVin();
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
    }

    @After
    public void tearDown() {
        vehicleMgr.delete(vehicleProfile.getVehicleId());
        vehicleProfile = null;
    }

    @Test(expected = BadRequestException.class)
    public void testCreateVehicleEmptyVin() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleProfile.setVin(null);
        vehicleMgr.createVehicle(vehicleProfile);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateVehicleExistingVin() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vehicleMgr.createVehicle(vehicleProfile);
        vehicleMgr.createVehicle(vehicleProfile);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateVehicleInvalidEcus() {
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        Ecu ecu = new Ecu();
        ecu.setClientId("testDeviceId");

        Map<String, Ecu> ecuMap = new HashMap<>();
        ecuMap.put("testEcus", ecu);
        vehicleProfile.setEcus(ecuMap);
        vehicleMgr.createVehicle(vehicleProfile);
    }

    @Test
    public void testUpdateVehicle() {
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setSoldRegion("USA");

        // Updated as part of US 295583.
        if (!Boolean.valueOf(disableDevAssocCheck)) {
            server.enqueue(new MockResponse().setBody("{\"userId\": \"apitest43\"}").setResponseCode(SUCCESS_CODE));
            vehicleAssociationService.setAssociationBaseUrl("http://localhost:" + server.getPort());
        }
        boolean id = vehicleMgr.update(vehicleProfile, "apitest43");
        assertTrue(id);
    }

    @Test
    public void testUpdateVehicleNullEcu() {
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
        boolean id = vehicleMgr.update(vehicleProfile, "apitest43");
        assertTrue(id);
    }

    @Test(expected = Exception.class)
    public void testUpdateVehicleException() {
        // Updated as part of US 295583.
        assumeFalse(Boolean.valueOf(disableDevAssocCheck));
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setSoldRegion("USA");

        server.enqueue(new MockResponse().setBody("Failure").setResponseCode(ERROR_CODE));
        vehicleAssociationService.setAssociationBaseUrl("http://localhost:" + server.getPort());
        vehicleMgr.update(vehicleProfile, "apitest43");
    }

    @Test
    public void testPutVehicle() {
        VehicleProfile vehicleProfile = vehicleMgr.findVehicleById(validVehicleId);
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setSoldRegion("USA");
        boolean id = vehicleMgr.put(vehicleProfile);
        assertTrue(id);
    }

    @Test(expected = NotFoundException.class)
    public void testPutVehicleEmptyProfile() {
        VehicleProfile vehicleProfile = vehicleMgr.findVehicleById(validVehicleId);
        vehicleProfile.setVehicleId("invalidVehicleId");
        vehicleProfile.setSoldRegion("USA");
        vehicleMgr.put(vehicleProfile);
    }

    @Test(expected = BadRequestException.class)
    public void testPutVehicleNonExistingVin() {
        VehicleProfile vehicleProfile = vehicleMgr.findVehicleById(validVehicleId);
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setVin("testVin");
        vehicleProfile.setSoldRegion("USA");
        vehicleMgr.put(vehicleProfile);
    }

    @Test(expected = BadRequestException.class)
    public void testPutVehicleInvalidEcus() {
        VehicleProfile vehicleProfile = vehicleMgr.findVehicleById(validVehicleId);
        vehicleProfile.setVehicleId(validVehicleId);
        vehicleProfile.setSoldRegion("USA");
        Ecu ecu = new Ecu();
        ecu.setClientId("testDeviceId");

        Map<String, Ecu> ecuMap = new HashMap<>();
        ecuMap.put("testEcus", ecu);
        vehicleProfile.setEcus(ecuMap);
        vehicleMgr.put(vehicleProfile);
    }

    @Test
    public void testFindVehicleById() {
        VehicleProfile profile = vehicleMgr.findVehicleById(validVehicleId);
        assertNotNull(profile);
    }

    @Test(expected = NotFoundException.class)
    public void testFindVehicleByInvalidId() {
        vehicleMgr.findVehicleById("I_AM_INVALID_VEHICLE_ID");
    }

    @Test
    public void testSearchVehicle() {
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("vin", "4100ec30e42ac107");
        searchParams.put(Constants.LOGICAL_OPERATOR_KEY, Constants.OR_OPERATOR);
        List<VehicleProfile> profileList = vehicleMgr.search(searchParams);
        assertEquals(0, profileList.size());
    }

    @Test
    public void testGetById() {
        Object object = vehicleMgr.get(validVehicleId, "InvalidPath");
        assertNotNull(object);
    }

    @Test(expected = NotFoundException.class)
    public void testGetByInvalidId() {
        vehicleMgr.get("I_AM_INVALID_VEHICLE_ID", "InvalidPath");
    }

    @Test
    public void testSearchVehicleMsisdn() {
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("msisdn", "12484798091");
        searchParams.put(Constants.LOGICAL_OPERATOR_KEY, Constants.OR_OPERATOR);
        List<VehicleProfile> profileList = vehicleMgr.search(searchParams);
        assertTrue(profileList.size() > 0);
    }

    @Test
    public void testFilterVehicle() {
        PageResponse<VehicleProfile> vehicleProfileList = vehicleMgr.filter(null, null);
        assertNotNull(vehicleProfileList);

        // Case 2: requestBody is not null, filterParams is not null
        VehicleProfileFilterRequest vehicleProfileFilterRequest = new VehicleProfileFilterRequest();
        vehicleProfileFilterRequest.setVin("10025981790400");
        Map<String, String> filterParams = new HashMap<>();
        filterParams.put("pageSize", "10");
        filterParams.put("pageNumber", "2");
        filterParams.put("sortBy", "vin:asc");
        vehicleProfileList = vehicleMgr.filter(vehicleProfileFilterRequest, filterParams);
        assertNotNull(vehicleProfileList);
    }

    @Test
    public void testReplaceVin() throws Exception {
        server.enqueue(new MockResponse().setBody("Success").setResponseCode(SUCCESS_CODE));
        vehicleAssociationService.setAssociationBaseUrl("http://localhost:" + server.getPort());

        insertDummyCodeValue();
        String responseMessage = vehicleMgr.replaceVin("4100ec30e42ac107", "JN1TAAT32A0XXXXXX");
        assertEquals(Constants.API_SUCCESS, responseMessage);
    }

    @Test(expected = Exception.class)
    public void testReplaceVinException() throws Exception {
        server.enqueue(new MockResponse().setBody("Failure").setResponseCode(ERROR_CODE));
        vehicleAssociationService.setAssociationBaseUrl("http://localhost:" + server.getPort());

        insertDummyCodeValue();
        vehicleMgr.replaceVin("4100ec30e42ac107", "JN1TAAT32A0XXXXXX");
    }

    @Test
    public void testAssociate() {
        assertTrue(vehicleMgr.associate(validVehicleId, generateUser()));
    }

    @Test
    public void testUpdateAssociate() {
        User user = generateUser();
        vehicleMgr.associate(validVehicleId, user);
        boolean result = vehicleMgr.updateAssociation(validVehicleId, user);
        assertTrue(result);
    }

    @Test(expected = NotFoundException.class)
    public void testAssociateForNonExistingVehicle() {
        vehicleMgr.associate("non_existing_vehicleId", generateUser());
    }

    @Test
    public void testSearchClientIds() {
        List<EcuClient> ecuList = vehicleMgr.searchClientIds(validVehicleId, "LOCATION");
        assertEquals(2, ecuList.size());
    }

    @Test(expected = BadRequestException.class)
    public void testAssociateForExistingAssociation() {
        User user = generateUser();
        vehicleMgr.associate(validVehicleId, user);
        vehicleMgr.associate(validVehicleId, user);
    }

    @Test(expected = BadRequestException.class)
    public void testAssociateForInvalidStatus() {
        User user = generateUser();
        user.setStatus("INVALID");
        vehicleMgr.associate(validVehicleId, user);
    }

    @Test(expected = BadRequestException.class)
    public void testAssociateForNullStatus() {
        User user = generateUser();
        user.setStatus(null);
        vehicleMgr.associate(validVehicleId, user);
    }

    @Test
    public void testDisassociate() {
        User user = generateUser();
        vehicleMgr.associate(validVehicleId, user);
        boolean result = vehicleMgr.disassociate(validVehicleId, user.getUserId(), "reason", null);
        assertTrue(result);
    }

    @Test(expected = NotFoundException.class)
    public void testDisassociateForNonExistingAssociation() {
        User user = generateUser();
        vehicleMgr.disassociate(validVehicleId, user.getUserId(), null, null);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteVehicleException() {
        assertTrue(vehicleMgr.delete("I_AM_INVALID_VEHICLE_ID"));
    }

    @Test
    public void testGetCountByFilterWithFilter() {
        Filters filters = new Filters();
        filters.setMakes(Collections.singletonList("BrandName"));
        filters.setModels(Collections.singletonList("ModelName"));
        filters.setYears(Collections.singletonList("2019"));
        filters.setSoldRegions(Collections.singletonList("NorthAmerica"));
        filters.setVins(Collections.singletonList(this.vin));
        filters.setImeis(Collections.singletonList("35453"));
        List<Filters> filterList = new ArrayList<>();
        filterList.add(filters);
        FilterDto filterDto = new FilterDto();
        filterDto.setFilters(filterList);

        Count countByFilter = vehicleMgr.getCountByFilter(filterDto);
        assertNotNull(countByFilter);
        assertEquals(1, countByFilter.getCount());
    }

    @Test
    public void testGetCountByFilterWithoutFilter() {
        FilterDto filterDto = new FilterDto();
        List<Filters> filterList = new ArrayList<>();
        filterDto.setFilters(filterList);

        Count countByFilter = vehicleMgr.getCountByFilter(filterDto);
        assertNotNull(countByFilter);
    }

    @Test
    public void testStreamByFilterWithFilter() {
        Filters filters = new Filters();
        filters.setMakes(Collections.singletonList("BrandName"));
        filters.setModels(Collections.singletonList("ModelName"));
        filters.setYears(Collections.singletonList("2019"));
        filters.setSoldRegions(Collections.singletonList("NorthAmerica"));
        filters.setVins(Collections.singletonList(this.vin));
        filters.setImeis(Collections.singletonList("35453"));
        List<Filters> filterList = new ArrayList<>();
        filterList.add(filters);
        FilterDto filterDto = new FilterDto();
        filterDto.setFilters(filterList);
        Flux<VehicleProfile> actual = vehicleMgr.streamByFilter(filterDto);
        assertNotNull(actual);
        assertEquals(1, actual.toStream().count());
    }

    @Test
    public void testStreamByFilterWithoutFilter() {
        FilterDto filterDto = new FilterDto();
        List<Filters> filterList = new ArrayList<>();
        filterDto.setFilters(filterList);
        Flux<VehicleProfile> actual = vehicleMgr.streamByFilter(filterDto);
        assertNotNull(actual);
        assertEquals(1, actual.toStream().count());
    }

    @Test
    public void testUpdateInventoryDetail() {
        Inventory inventory = generateInventory();
        assertTrue(vehicleMgr.updateInventory(validVehicleId, inventory));
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateInventoryNoVehicleFoundException() {
        Inventory inventory = generateInventory();
        vehicleMgr.updateInventory("InvalidId", inventory);
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateInventoryVinMismatchException() {
        Inventory inventory = generateInventory();
        inventory.setVin("InvalidVin");
        vehicleMgr.updateInventory(validVehicleId, inventory);
    }

    @Test
    public void testTerminateDeviceWhenDeviceNotFound() {
        // Arrange
        String invalidDeviceId = "NON_EXISTENT_DEVICE_ID";

        // Act
        boolean result = vehicleMgr.terminateDevice(invalidDeviceId);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testTerminateDeviceDeletesVinAndHcpVinDetails() {
        // Arrange
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        String deviceId = vehicleProfile.getEcus().values().iterator().next().getClientId();
        vehicleMgr.createVehicle(vehicleProfile);

        // Insert a profile with VIN and HCP keys for deletion
        VehicleProfile vinProfile = VehicleProfileTestUtil.generateVehicleProfile();
        vinProfile.setVin("VIN" + deviceId);
        vehicleMgr.createVehicle(vinProfile);

        VehicleProfile hcpProfile = VehicleProfileTestUtil.generateVehicleProfile();
        hcpProfile.setVin("HCP" + deviceId);
        vehicleMgr.createVehicle(hcpProfile);

        // Act
        boolean result = vehicleMgr.terminateDevice(deviceId);

        // Assert
        assertTrue(result);
        // Both VIN and HCP profiles should be deleted
        Map<String, String> vinParams = new HashMap<>();
        vinParams.put("vin", "VIN" + deviceId);
        List<VehicleProfile> vinProfiles = vehicleMgr.search(vinParams);
        assertTrue(vinProfiles == null || vinProfiles.isEmpty());

        Map<String, String> hcpParams = new HashMap<>();
        hcpParams.put("vin", "HCP" + deviceId);
        List<VehicleProfile> hcpProfiles = vehicleMgr.search(hcpParams);
        assertTrue(hcpProfiles == null || hcpProfiles.isEmpty());
    }
    private Inventory generateInventory() {
        Map<String, InventoryScomo> scomoMapValue = new LinkedHashMap<>();
        InventoryScomo inventoryScomo = new InventoryScomo();

        inventoryScomo.setScomoId("M23000002");
        inventoryScomo.setVersion("9695479580");
        scomoMapValue.put("software1", inventoryScomo);
        
        Map<String, Map<String, InventoryScomo>> inventoryScomoMap = new LinkedHashMap<>();
        inventoryScomoMap.put("software", scomoMapValue);
        InventoryEcuHardware inventoryEcuHardware = new InventoryEcuHardware();
        inventoryEcuHardware.setSerialNumber("02100007600A70A50B72");
        inventoryEcuHardware.setVersion("9845291880");
        InventoryEcu ecu = new InventoryEcu();
        ecu.setPartNumber("187469231");
        ecu.setInventoryEcuHardware(inventoryEcuHardware);
        ecu.setInventoryScomoMap(inventoryScomoMap);
        Map<String, InventoryEcu> inventoryEcuMap = new LinkedHashMap<>();
        inventoryEcuMap.put("ecu2", ecu);
        InventoryEcu ecu1 = new InventoryEcu();
        ecu1.setPartNumber("MismatchedPartNumber");
        inventoryEcuMap.put("ecu1", ecu1);
        
        Inventory inventory = new Inventory();
        inventory.setInventoryversion("1.0");
        inventory.setVin(vin);
        inventory.setTimestamp(new Date());
        inventory.setCampaignid("68288490274495066275225960801970052724");
        inventory.setInventoryEcuMap(inventoryEcuMap);

        return inventory;
    }

    private User generateUser() {
        User user = new User();
        user.setUserId(String.valueOf(System.currentTimeMillis()));
        user.setRole("VEHICLE_OWNER");
        user.setStatus("ASSOCIATED");
        TermsAndConditions tcUs = new TermsAndConditions();
        tcUs.setCountryCode("US");
        tcUs.setLastAcceptedOn(new Date());
        tcUs.setVersion("11a");

        TermsAndConditions tcCa = new TermsAndConditions();
        tcCa.setCountryCode("CA");
        tcCa.setLastAcceptedOn(new Date());
        tcCa.setVersion("11b");

        Map<String, TermsAndConditions> tcs = new HashMap<>();
        tcs.put("activation", tcUs);
        user.setTc(tcs);
        return user;
    }
    
    @Test
    public void testDeleteVinDetailsbyVinIdDeletesProfileWhenExists() {
        // Arrange
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        String deviceId = vehicleProfile.getEcus().values().iterator().next().getClientId();
        String vinKey = "VIN";
        String vinId = vinKey + deviceId;
        vehicleProfile.setVin(vinId);
        vehicleMgr.createVehicle(vehicleProfile);

        // Act
        boolean result = vehicleMgr.deleteVinDetailsbyVinId(vinKey, deviceId);

        // Assert
        assertTrue(result);
        Map<String, String> params = new HashMap<>();
        params.put("vin", vinId);
        List<VehicleProfile> profiles = vehicleMgr.search(params);
        assertTrue(profiles == null || profiles.isEmpty());
    }

    @Test
    public void testDeleteVinDetailsbyVinIdReturnsFalseWhenProfileDoesNotExist() {
        // Arrange
        String vinKey = "VIN";
        String deviceId = "NON_EXISTENT_DEVICE_ID";

        // Act
        boolean result = vehicleMgr.deleteVinDetailsbyVinId(vinKey, deviceId);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testDeleteVinDetailsbyVinIdReturnsFalseWhenDeleteFails() {
        // Arrange
        VehicleProfile vehicleProfile = VehicleProfileTestUtil.generateVehicleProfile();
        String deviceId = vehicleProfile.getEcus().values().iterator().next().getClientId();
        String vinKey = "VIN";
        String vinId = vinKey + deviceId;
        vehicleProfile.setVin(vinId);
        vehicleMgr.createVehicle(vehicleProfile);

        // Simulate failure by deleting the profile first
        Map<String, String> params = new HashMap<>();
        params.put("vin", vinId);
        List<VehicleProfile> profiles = vehicleMgr.search(params);
        if (profiles != null && !profiles.isEmpty()) {
            vehicleMgr.deleteVehicleProfile(profiles.get(0));
        }

        // Act
        boolean result = vehicleMgr.deleteVinDetailsbyVinId(vinKey, deviceId);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testGetEcuByClientIdReturnsNullWhenNotFound() {
        // Act
        VehicleProfileEcuFilterRequest result = vehicleMgr.getEcuByClientId("NON_EXISTENT_CLIENT_ID");

        // Assert
        assertNull(result);
    }
    
    
    @Test
    public void testGetEcuByClientIdReturnsCorrectEcu() {
        // Arrange
        VehicleProfile vehicleProfile1 = VehicleProfileTestUtil.generateVehicleProfile();
        String clientId = vehicleProfile1.getEcus().values().iterator().next().getClientId();
        vehicleMgr.createVehicle(vehicleProfile1);

        // Act
        VehicleProfileEcuFilterRequest result = vehicleMgr.getEcuByClientId(clientId);

        // Assert
        assertNotNull(result);
    }
    
    
    @Test
    public void testGetEcuByClientId() throws IOException {
        String clientId = "4100ec30e42ac109";
        VehicleProfile mockedVehicleProfile = objectMapper.readValue(new URL("classpath:vehicle-profileV2.json"), 
                VehicleProfile.class);
        String vehicleId = vehicleMgr.createVehicle(mockedVehicleProfile);
        
        VehicleProfileEcuFilterRequest response = vehicleMgr.getEcuByClientId(clientId);
        assertEquals("TestPlatform", response.getConnectedPlatform());
        assertEquals("tbm", response.getDeviceType());
        assertEquals(vehicleId, response.getVehicleId());
        
        vehicleMgr.delete(vehicleId);
    }
    @Test
    public void testGetAssociatedVehiclesReturnsEmptyListWhenNoAssociations() {
        // Arrange
        VehicleProfile vehicleProfile1 = VehicleProfileTestUtil.generateVehicleProfile();
        String vehicleId = vehicleMgr.createVehicle(vehicleProfile1);

        // Act
        List<org.eclipse.ecsp.vehicleprofile.rest.mapping.AssociatedVehicle> associatedVehicles =
                vehicleMgr.getAssociatedVehicles("non_existing_user");

        // Assert
        assertNotNull(associatedVehicles);
        assertTrue(associatedVehicles.isEmpty());

        vehicleMgr.delete(vehicleId);
    }

    @Test
    public void testGetAssociatedVehiclesReturnsAssociatedVehicle() {
        // Arrange
        VehicleProfile vehicleProfile1 = VehicleProfileTestUtil.generateVehicleProfile();
        String vehicleId = vehicleMgr.createVehicle(vehicleProfile1);
        User user = generateUser();
        vehicleMgr.associate(vehicleId, user);

        // Act
        List<org.eclipse.ecsp.vehicleprofile.rest.mapping.AssociatedVehicle> associatedVehicles =
                vehicleMgr.getAssociatedVehicles(user.getUserId());

        // Assert
        assertNotNull(associatedVehicles);
        assertFalse(associatedVehicles.isEmpty());
        boolean found = false;
        for (org.eclipse.ecsp.vehicleprofile.rest.mapping.AssociatedVehicle av : associatedVehicles) {
            if (vehicleId.equals(av.getVehicleId()) && user.getRole().equals(av.getRole())) {
                found = true;
                break;
            }
        }
        assertTrue(found);

        vehicleMgr.delete(vehicleId);
    }

    @Test
    public void testGetAssociatedVehiclesWithMultipleAssociations() {
        // Arrange
        VehicleProfile vehicleProfile1 = VehicleProfileTestUtil.generateVehicleProfile();
        String vehicleId1 = vehicleMgr.createVehicle(vehicleProfile1);
        User user1 = generateUser();
        vehicleMgr.associate(vehicleId1, user1);

        VehicleProfile vehicleProfile2 = VehicleProfileTestUtil.generateVehicleProfile();
        String vehicleId2 = vehicleMgr.createVehicle(vehicleProfile2);
        User user2 = new User();
        user2.setUserId(user1.getUserId());
        user2.setRole("SECONDARY_OWNER");
        user2.setStatus("ASSOCIATED");
        vehicleMgr.associate(vehicleId2, user2);

        // Act
        List<org.eclipse.ecsp.vehicleprofile.rest.mapping.AssociatedVehicle> associatedVehicles =
                vehicleMgr.getAssociatedVehicles(user1.getUserId());

        // Assert
        assertNotNull(associatedVehicles);
        assertTrue(associatedVehicles.size() >= 2);

        boolean found1 = false, found2 = false;
        for (org.eclipse.ecsp.vehicleprofile.rest.mapping.AssociatedVehicle av : associatedVehicles) {
            if (vehicleId1.equals(av.getVehicleId()) && user1.getRole().equals(av.getRole())) {
                found1 = true;
            }
            if (vehicleId2.equals(av.getVehicleId()) && user2.getRole().equals(av.getRole())) {
                found2 = true;
            }
        }
        assertTrue(found1 && found2);

        vehicleMgr.delete(vehicleId1);
        vehicleMgr.delete(vehicleId2);
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
    
    @Test
    public void testDeleteDeviceReturnsFalseWhenEcusIsNull() throws Exception{
        // Arrange
        VehicleProfile mockedVehicleProfile = objectMapper.readValue(new URL("classpath:vehicle-profileV2.json"), 
                VehicleProfile.class);
        mockedVehicleProfile.setEcus(null);
        String vehicleId = vehicleMgr.createVehicle(mockedVehicleProfile);

        // Act
        boolean result = vehicleMgr.terminateDevice(vehicleId);

        // Assert
        assertFalse(result);
        vehicleMgr.delete(vehicleId);
    }
    
    @Test
    public void testDeleteDeviceDeletesVehicleProfileWhenSingleEcu() throws Exception{
        // Arrange
        VehicleProfile mockedVehicleProfile = objectMapper.readValue(new URL("classpath:vehicle-profileV3.json"), 
                VehicleProfile.class);
        String vehicleId = vehicleMgr.createVehicle(mockedVehicleProfile);

        // Act
        boolean result = vehicleMgr.terminateDevice(vehicleId);

        // Assert
        assertTrue(result);
    }
}
