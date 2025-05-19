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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1;
import org.eclipse.ecsp.testutils.EmbeddedRedisServer;
import org.eclipse.ecsp.vehicleprofile.domain.Application;
import org.eclipse.ecsp.vehicleprofile.domain.ModemInfo;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.notifier.VehicleProfileChangedNotifier;
import org.eclipse.ecsp.vehicleprofile.test.utils.MongoServer;
import org.eclipse.ecsp.vehicleprofile.test.utils.VehicleProfileTestUtil;
import org.apache.commons.collections.CollectionUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1.ChangeDescription;

/**
 * VehicleProfileChangedNotifierTest class.
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test-base.properties")
@ActiveProfiles("test1")
@ContextConfiguration(classes = { TheApplication.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class VehicleProfileChangedNotifierTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleProfileChangedNotifierTest.class);
    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private VehicleProfileChangedNotifier notifier;
    @ClassRule
    public static MongoServer MongoServer = new MongoServer();

    @ClassRule
    public static final EmbeddedRedisServer REDIS = new EmbeddedRedisServer();

    public static final int DESCRIPTION_SIZE = 2;

    @Test
    public void testModemChanged() throws JsonProcessingException {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        changedVp.getModemInfo().setIccid("changedICCID");
        changedVp.getModemInfo().setImei("changedIMEI");
        changedVp.getModemInfo().setImsi("changedIMSI");
        changedVp.getModemInfo().setMsisdn("changedMsisdn");

        Map<String, List<VehicleProfileNotificationEventDataV1_1.ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        LOGGER.debug("topicToChanges {}", topicToChanges);
        List<VehicleProfileNotificationEventDataV1_1.ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-modemInfo");
        LOGGER.debug("Change description JSON {}", objectMapper.writeValueAsString(changeDescriptions));
        assertFalse(CollectionUtils.sizeIsEmpty(changeDescriptions));
        assertEquals(1, changeDescriptions.size());
        ModemInfo notificationChnagedModemInfo = (ModemInfo) changeDescriptions.get(0).getChanged();
        assertEquals(changedVp.getModemInfo().getIccid(), notificationChnagedModemInfo.getIccid());

        ModemInfo notificationOldModemInfo = (ModemInfo) changeDescriptions.get(0).getOld();
        assertEquals(oldVp.getModemInfo().getIccid(), notificationOldModemInfo.getIccid());
    }

    @Test
    public void testUnChangedModem() {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        Map<String, List<VehicleProfileNotificationEventDataV1_1.ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<VehicleProfileNotificationEventDataV1_1.ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-modemInfo");
        assertNull(changeDescriptions);
    }

    @Test
    public void testModemPartiallyChanged() {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        changedVp.getModemInfo().setIccid("changedICCID");
        changedVp.getModemInfo().setImei("changedIMEI");
        // changedVp.getModemInfo().setImsi(null);
        changedVp.getModemInfo().setMsisdn(null);

        Map<String, List<VehicleProfileNotificationEventDataV1_1.ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<VehicleProfileNotificationEventDataV1_1.ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-modemInfo");

        ModemInfo notificationChnagedModemInfo = (ModemInfo) changeDescriptions.get(0).getChanged();
        assertEquals(changedVp.getModemInfo().getIccid(), notificationChnagedModemInfo.getIccid());
        assertEquals(oldVp.getModemInfo().getImsi(), notificationChnagedModemInfo.getImsi());
        assertNull(notificationChnagedModemInfo.getMsisdn());
    }

    @Test
    public void testModemDeleted() {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        changedVp.setModemInfo(null);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-modemInfo");
        assertNull(changeDescriptions.get(0).getChanged());
        assertEquals(oldVp.getModemInfo().getIccid(), ((ModemInfo) changeDescriptions.get(0).getOld()).getIccid());
    }

    @Test
    public void testModemAdded() throws JsonProcessingException {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        oldVp.setModemInfo(null);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-modemInfo");
        LOGGER.debug("testModemAdded {}", objectMapper.writeValueAsString(changeDescriptions));
        assertNull(changeDescriptions.get(0).getOld());
        assertEquals(changedVp.getModemInfo().getIccid(),
                ((ModemInfo) changeDescriptions.get(0).getChanged()).getIccid());
    }

    @Test
    public void testHwswChanged() {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        changedVp.getEcus().get("hu").setHwVersion("ChangedhwVersion");
        changedVp.getEcus().get("hu").setSwVersion("ChangedswVersion");
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-ecuswhwversion");
        LOGGER.debug("testHWSWChanged {}", changeDescriptions);
        assertNotNull(changeDescriptions);
        assertEquals(DESCRIPTION_SIZE, changeDescriptions.size());

        for (ChangeDescription changeDescription : changeDescriptions) {
            if (changeDescription.getPath().equals("ecus.hu.hwVersion")) {
                assertEquals(changedVp.getEcus().get("hu").getHwVersion(), changeDescription.getChanged());
            } else if (changeDescription.getPath().equals("ecus.hu.swVersion")) {
                assertEquals(changedVp.getEcus().get("hu").getSwVersion(), changeDescription.getChanged());
            }
        }

    }

    @Test
    public void testHwSwParentDeleted() {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        changedVp.getEcus().put("hu", null);

        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-ecuswhwversion");
        LOGGER.debug("testHwSwParentDeleted {}", changeDescriptions);
        assertNotNull(changeDescriptions);
        assertEquals(DESCRIPTION_SIZE, changeDescriptions.size());

        for (ChangeDescription changeDescription : changeDescriptions) {
            if (changeDescription.getPath().equals("ecus.hu.hwVersion")) {
                assertNull(changeDescription.getChanged());
                assertEquals(oldVp.getEcus().get("hu").getHwVersion(), changeDescription.getOld());
            } else if (changeDescription.getPath().equals("ecus.hu.swVersion")) {
                assertNull(changeDescription.getChanged());
                assertEquals(oldVp.getEcus().get("hu").getSwVersion(), changeDescription.getOld());
            }
        }

    }

    @Test
    public void testHwSwParentAdded() {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        oldVp.getEcus().put("hu", null);

        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-ecuswhwversion");
        LOGGER.debug("testHwSwParentAdded {}", changeDescriptions);
        assertNotNull(changeDescriptions);
        assertEquals(DESCRIPTION_SIZE, changeDescriptions.size());

        for (ChangeDescription changeDescription : changeDescriptions) {
            if (changeDescription.getPath().equals("ecus.hu.hwVersion")) {
                assertNull(changeDescription.getOld());
                assertEquals(changedVp.getEcus().get("hu").getHwVersion(), changeDescription.getChanged());
            } else if (changeDescription.getPath().equals("ecus.hu.swVersion")) {
                assertNull(changeDescription.getOld());
                assertEquals(changedVp.getEcus().get("hu").getSwVersion(), changeDescription.getChanged());
            }
        }

    }

    @Test
    public void testHwswAdded() {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        oldVp.getEcus().get("hu").setSwVersion(null);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-ecuswhwversion");
        assertEquals(1, changeDescriptions.size());
        assertNull(changeDescriptions.get(0).getOld());
        assertEquals(changedVp.getEcus().get("hu").getSwVersion(), changeDescriptions.get(0).getChanged());

    }

    @Test
    public void testHwswDeleted() {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        changedVp.getEcus().get("hu").setSwVersion(null);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-ecuswhwversion");
        assertEquals(1, changeDescriptions.size());
        assertNull(changeDescriptions.get(0).getChanged());
        assertEquals(oldVp.getEcus().get("hu").getSwVersion(), changeDescriptions.get(0).getOld());
    }

    // @Test
    public void testVehicleCreated() throws JsonProcessingException {

    }

    @Test
    public void testServiceProvisioningAdded() throws JsonProcessingException {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        oldVp.getEcus().get("hu").getProvisionedServices().setServices(null);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-serviceProvisioning");
        LOGGER.debug("testServiceProvisioningAdded {}", objectMapper.writeValueAsString(changeDescriptions));
        assertEquals(1, changeDescriptions.size());
        assertNull(changeDescriptions.get(0).getOld());
        assertEquals(changedVp.getEcus().get("hu").getProvisionedServices().getServices(),
                changeDescriptions.get(0).getChanged());
    }

    @Test
    public void testServiceProvisioningParentAdded() throws JsonProcessingException {

        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        oldVp.getEcus().get("hu").setProvisionedServices(null);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-serviceProvisioning");
        LOGGER.debug("testServiceProvisioningAdded {}", objectMapper.writeValueAsString(changeDescriptions));
        assertEquals(1, changeDescriptions.size());
        assertNull(changeDescriptions.get(0).getOld());
        assertEquals(changedVp.getEcus().get("hu").getProvisionedServices().getServices(),
                changeDescriptions.get(0).getChanged());

    }

    @Test
    public void testServiceProvisioningParentDeleted() throws JsonProcessingException {

        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        changedVp.getEcus().get("hu").setProvisionedServices(null);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-serviceProvisioning");
        LOGGER.debug("testServiceProvisioningAdded {}", objectMapper.writeValueAsString(changeDescriptions));
        assertEquals(1, changeDescriptions.size());
        assertNull(changeDescriptions.get(0).getChanged());
        assertEquals(oldVp.getEcus().get("hu").getProvisionedServices().getServices(),
                changeDescriptions.get(0).getOld());

    }

    @Test
    public void testServiceProvisioningDeleted() throws JsonProcessingException {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        changedVp.getEcus().get("hu").getProvisionedServices().setServices(null);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-serviceProvisioning");
        LOGGER.debug("testServiceProvisioningAdded {}", objectMapper.writeValueAsString(changeDescriptions));
        assertEquals(1, changeDescriptions.size());
        assertNull(changeDescriptions.get(0).getChanged());
        assertEquals(oldVp.getEcus().get("hu").getProvisionedServices().getServices(),
                changeDescriptions.get(0).getOld());
    }

    @Test
    public void testServiceProvisioningAddedService() throws JsonProcessingException {
        Application applicationRo = new Application();
        applicationRo.setApplicationId("RO");
        applicationRo.setVersion("1.1");
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        ((List<Application>) changedVp.getEcus().get("hu").getProvisionedServices().getServices()).add(applicationRo);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-serviceProvisioning");
        LOGGER.debug("testServiceProvisioningAdded {}", objectMapper.writeValueAsString(changeDescriptions));

        assertEquals(1, changeDescriptions.size());
        assertEquals(changedVp.getEcus().get("hu").getProvisionedServices().getServices(),
                changeDescriptions.get(0).getChanged());
        assertEquals(oldVp.getEcus().get("hu").getProvisionedServices().getServices(),
                changeDescriptions.get(0).getOld());

    }

    @Test
    public void testServiceProvisioningDeletedService() throws JsonProcessingException {
        VehicleProfile oldVp = VehicleProfileTestUtil.generateVehicleProfile();
        VehicleProfile changedVp = VehicleProfileTestUtil.generateVehicleProfile();
        changedVp.getEcus().get("hu").getProvisionedServices().getServices().remove(0);
        Map<String, List<ChangeDescription>> topicToChanges = notifier.compuateChanges(oldVp, changedVp);
        List<ChangeDescription> changeDescriptions = topicToChanges.get("vehicleprofile-modified-serviceProvisioning");
        LOGGER.debug("testServiceProvisioningAdded {}", objectMapper.writeValueAsString(changeDescriptions));

        assertEquals(1, changeDescriptions.size());
        assertEquals(changedVp.getEcus().get("hu").getProvisionedServices().getServices(),
                changeDescriptions.get(0).getChanged());
        assertEquals(oldVp.getEcus().get("hu").getProvisionedServices().getServices(),
                changeDescriptions.get(0).getOld());
    }

}
