/*
 *  *******************************************************************************
// *  Copyright (c) 2023-24 Harman International
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// *
// *  SPDX-License-Identifier: Apache-2.0
// *  *******************************************************************************
// */
//
//package com.harman.ignite.vehicleprofile;
//
//import com.fasterxml.jackson.core.JsonParseException;
//import com.fasterxml.jackson.databind.JsonMappingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.harman.ignite.cache.redis.EmbeddedRedisServer;
////import com.harman.ignite.security.EncryptDecryptInterface;
//import com.harman.dao.vehicleprofile.ecsp.VehicleDao;
//import com.harman.domain.vehicleprofile.ecsp.VehicleProfile;
//import com.harman.utils.test.vehicleprofile.ecsp.MongoServer;
//import org.junit.Before;
//import org.junit.ClassRule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.io.IOException;
//
//import static org.junit.Assert.assertTrue;
//
///**
// * VehicleDaoOemTest class.
// */
//@RunWith(SpringRunner.class)
//@TestPropertySource("classpath:application-test-base.properties")
//@ActiveProfiles("test1")
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//public class VehicleDaoOemTest {
//
//    @ClassRule
//    public static MongoServer MongoServer = new MongoServer();
//
//    @ClassRule
//    public static final EmbeddedRedisServer REDIS = new EmbeddedRedisServer();
//
//    @Autowired
//    private VehicleDao vehicleDao;
//    //@MockBean
//    //private EncryptDecryptInterface encryptDecryptInterface;
//
//    @Before
//    public void setup() throws Exception {
//    // Mockito.when(encryptDecryptInterface.encrypt(Mockito.anyString())).thenReturn(" ");
//    }
//
//    @Test
//    public void testMerge() throws JsonParseException, JsonMappingException, IOException {
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        VehicleProfile vehicleProfile = objectMapper.readValue(
//                "{\"vin\":\"VDOTtestMerge\",\"ecus\":{\"hu\":{\"swVersion\":\"\"},\"telematics\":"
//                + "{\"swVersion\":\"1.0\"}},\"vehicleAttributes\":{\"make\":\"make type\"}}",
//                VehicleProfile.class);
//        vehicleProfile.setVin(null);
//        vehicleProfile.setEcus(null);
//        vehicleProfile.getVehicleAttributes().setBaseColor("Red");
//        vehicleDao.merge(vehicleProfile);
//        String vehicleId = vehicleDao.save(vehicleProfile).getVehicleId();
//        VehicleProfile dbVehicleProfile = vehicleDao.findById(vehicleId);
//        assertTrue(dbVehicleProfile.getVehicleAttributes().getBaseColor()
//                .equals(vehicleProfile.getVehicleAttributes().getBaseColor()));
//
//    }
//
//    @Test
//    public void testUpdate() throws JsonParseException, JsonMappingException, IOException {
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        VehicleProfile vehicleProfile = objectMapper.readValue(
//                "{\"vin\":\"VDOTtestUpdate\",\"ecus\":{\"hu\":{\"swVersion\":\"\"},"
//                + "\"telematics\":{\"swVersion\":\"1.0\"}},\"vehicleAttributes\":{\"make\":\"make type\"}}",
//                VehicleProfile.class);
//        vehicleProfile.setVin(null);
//        vehicleProfile.setEcus(null);
//        vehicleProfile.getVehicleAttributes().setBaseColor("Red");
//        vehicleDao.update(vehicleProfile);
//        String vehicleId = vehicleDao.save(vehicleProfile).getVehicleId();
//        VehicleProfile dbVehicleProfile = vehicleDao.findById(vehicleId);
//        assertTrue(dbVehicleProfile.getVehicleAttributes().getBaseColor()
//                .equals(vehicleProfile.getVehicleAttributes().getBaseColor()));
//
//    }
//
//}