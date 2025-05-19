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

package org.eclipse.ecsp.vehicleprofile.config;

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.notifier.KeysTree;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * VehicleProfileNotifierConfig class.
 */
@Component
public class VehicleProfileNotifierConfig {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileNotifierConfig.class);
    @Value("${vehicleprofile.property.changes.tolisten}")
    private String propertyChnagesToListenStr;
    private static final String TOPIC_NAME_SEPERATOR = "#";
    private KeysTree keysTree = new KeysTree();

    /**
     * sample configuraiton: ecus.hu.provisionedServices.services,
     * ecus.hu.provisionedServices.applications,
     * ecus.telematics.provisionedServices.services,
     * ecus.telematics.provisionedServices.applications modemInfo.iccidm, modemInfo.imsi,
     * modemInfo.iccid, modemInfo.msisdn ecus.hu.hwVersion, ecus.hu.swVersion,
     * ecus.telematics.hwVersion, ecus.telematics.swVersion.
     * 
     */

    @PostConstruct
    public void init() {
        if (propertyChnagesToListenStr == null || propertyChnagesToListenStr.isEmpty()) {
            LOGGER.warn("No vehicleProfile changes notifiers is configured in the system");
            return;
        }
        List<String> propertyChnagesToListen = Arrays.asList(this.propertyChnagesToListenStr.split(","));

        String topicName = "";
        if (!propertyChnagesToListen.get(propertyChnagesToListen.size() - 1).contains(TOPIC_NAME_SEPERATOR)) {
            throw new RuntimeException(
                    "Invalid vehicleProfileNotification lisnters configuration,"
                    + " It should be key1,key2,key1.key3.key5#topicName,key5.childKey1#topicName2");
        }

        for (int i = propertyChnagesToListen.size() - 1; i >= 0; i--) {
            String property = propertyChnagesToListen.get(i);
            if (property.contains(TOPIC_NAME_SEPERATOR)) {
                String[] split = property.split(TOPIC_NAME_SEPERATOR);
                property = split[0];
                topicName = split[1];
                LOGGER.info("The topic {} is configured for the following property changes {}", topicName, property);
            }
            keysTree.populate(property.trim(), topicName.trim());
        }

    }

    public KeysTree getConfigTree() {
        return keysTree;
    }

}
