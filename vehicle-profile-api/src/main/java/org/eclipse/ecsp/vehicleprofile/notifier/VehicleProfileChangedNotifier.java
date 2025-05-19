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

package org.eclipse.ecsp.vehicleprofile.notifier;

import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1;
import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1.ChangeDescription;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.config.VehicleProfileNotifierConfig;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.notifier.KeysTree.KeyNode;
import org.eclipse.ecsp.vehicleprofile.notifier.events.VehicleProfileCreatedNotificationEventDataV1;
import org.eclipse.ecsp.vehicleprofile.service.EncryptSensitiveDataService;
import org.eclipse.ecsp.vehicleprofile.utils.Utils;
import org.eclipse.ecsp.vehicleprofile.utils.VpKafkaService;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * VehicleProfileChangedNotifier.
 */
@Service
public class VehicleProfileChangedNotifier {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileChangedNotifier.class);
    @Autowired
    private VehicleProfileNotifierConfig vehicleProfileNotifierConfig;
    @Value("#{'${vehicleprofile.new.vehicle.publish.topicname}'.split(',')}")
    private List<String> vehicleProfileCreatedNotificationTopic;
    @Autowired
    @Lazy
    private VpKafkaService kafkaService;
    @Autowired
    private EncryptSensitiveDataService encryptSensitiveDataService;

    /**
     * changed.
     *
     * @param oldVehicleProfile VehicleProfile
     * @param changedVehicleProfile VehicleProfile
     * @param requestId String
     */
    @Async
    public void changed(VehicleProfile oldVehicleProfile, VehicleProfile changedVehicleProfile, String requestId) {
        // populate MDC in the async context (fix to avoid manipulating spring's default async executor
        
        Utils.mdc(requestId, null, null, (changedVehicleProfile != null) ? changedVehicleProfile.getVehicleId() : null,
                null);

        MDC.put(Constants.REQUEST_ID, requestId);

        long startTime = System.currentTimeMillis();
        try {
            KeysTree keysTree = vehicleProfileNotifierConfig.getConfigTree();
            if (keysTree.getRoot() == null || keysTree.getRoot().getChildren() == null
                    || keysTree.getRoot().getChildren().isEmpty()) {
                // not expected to publish any updates to anyone
                return;
            }

            if (oldVehicleProfile == null && CollectionUtils.isNotEmpty(vehicleProfileCreatedNotificationTopic)) {
                // new vehicle profile is created, publish notification
                VehicleProfileCreatedNotificationEventDataV1 notificationEventDataV1 = new
                        VehicleProfileCreatedNotificationEventDataV1();
                notificationEventDataV1.setVehicleProfile(changedVehicleProfile);
                for (String topic : vehicleProfileCreatedNotificationTopic) {
                    sendEvent(notificationEventDataV1, topic, changedVehicleProfile.getVehicleId(),
                            Constants.VEHICLE_PROFILE_CREATED_NOTIFICATION_EVENT);
                }
            } else {
                Map<String, List<ChangeDescription>> changes = compuateChanges(oldVehicleProfile,
                        changedVehicleProfile);
                publishChanges(changes, changedVehicleProfile);
            }
        } catch (Exception e) {
            LOGGER.error("execeptoin occured while processing/publishing the changes {}", e);
        }
        LOGGER.debug("TimeMills took to process & publish {}", (System.currentTimeMillis() - startTime));
    }

    /**
     * compuateChanges.
     *
     * @param oldVehicleProfile VehicleProfile
     * @param changedVehicleProfile VehicleProfile
     * @return map list
     */
    public Map<String, List<ChangeDescription>> compuateChanges(VehicleProfile oldVehicleProfile,
                                                                                                        VehicleProfile changedVehicleProfile) {
        KeysTree keysTree = vehicleProfileNotifierConfig.getConfigTree();
        Map<String, List<ChangeDescription>> topicToChangeDescription = new HashMap<>();
        try {
            identifyChnages(oldVehicleProfile, changedVehicleProfile, keysTree.getRoot(), topicToChangeDescription);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error("error ocurred while processing changes {}", e);
        }

        return topicToChangeDescription;
    }

    /**
     * identifyChnages.
     *
     * @param old Object
     * @param changed Object
     * @param node KeyNode
     * @param topicToChangeDescription Map<String, List<ChangeDescription>>
     */
    private void identifyChnages(Object old, Object changed, KeyNode node,
            Map<String, List<ChangeDescription>> topicToChangeDescription)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (node == null || node.getChildren() == null || node.getChildren().size() == 0) {
            // we have already processed it.
            return;
        }
        Iterator<KeysTree.KeyNode> iterator = node.getChildren().iterator();

        Map<String, Object> oldProperties = null;
        Map<String, Object> changedProperties = null;
        boolean isMap = false;

        if (old != null) {
            if (old instanceof Map) {
                isMap = true;
            } else {
                oldProperties = PropertyUtils.describe(old);
            }
        }

        if (changed != null) {
            if (changed instanceof Map) {
                isMap = true;
            } else {
                changedProperties = PropertyUtils.describe(changed);
            }
        }
        while (iterator.hasNext()) {
            identifyChanges(old, changed, topicToChangeDescription, iterator, isMap, oldProperties, changedProperties);
        }
    }

    private void identifyChanges(Object old,
                                                Object changed,
                                                Map<String,
                                                        List<ChangeDescription>> topicToChangeDescription,
                                                Iterator<KeyNode> iterator,
                                                boolean isMap, Map<String, Object> oldProperties,
                                                Map<String, Object> changedProperties)
            throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        KeyNode childNode = iterator.next();
        Object oldChild = null;
        Object changedChild = null;
        if (isMap) {
            oldChild = (old == null) ? null : ((Map) old).get(childNode.getKey());
            changedChild = (changed == null) ? null : ((Map) changed).get(childNode.getKey());
        } else {
            oldChild = (oldProperties == null) ? null : oldProperties.get(childNode.getKey());
            changedChild = (changedProperties == null) ? null : changedProperties.get(childNode.getKey());
        }

        // in case of both are null, or both are same objects, no changes
        // even down in
        // the hierarchy, continue to next one - good
        if (oldChild == changedChild) {
            return;
        }

        if (childNode.getTopicNames() == null || childNode.getTopicNames().isEmpty()) {
            // no further comparison needed, dig deep
            identifyChnages(oldChild, changedChild, childNode, topicToChangeDescription);
        } else {
            extractedChanges(topicToChangeDescription, oldChild, changedChild, childNode);
        }
    }

    private void extractedChanges(Map<String,
            List<ChangeDescription>> topicToChangeDescription,
                                  Object oldChild,
                                  Object changedChild,
                                  KeyNode childNode)
            throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        // look for changes, and publish them
        ChangeDescription changeDescription = null;
        if (oldChild == null) {
            // added data
            changeDescription = new ChangeDescription();
            changeDescription.setChanged(changedChild);
            LOGGER.debug("added data: new data {}, key {}, publishing to {}",
                    encryptSensitiveDataService.encryptLog(Objects.toString(changedChild, "")),
                    childNode.getPath(), childNode.getTopicNames());
        } else if (changedChild == null) {
            // deleted data
            LOGGER.debug("deleted data: old data {}, key {}, publishing to {}",
                    encryptSensitiveDataService.encryptLog(oldChild.toString()), childNode.getPath(),
                    childNode.getTopicNames());
            changeDescription = new ChangeDescription();
            changeDescription.setOld(oldChild);

        } else if (oldChild instanceof Collection) {
            // it can be any collection apart from Map, maps are already
            // handled as they
            // produce json objects
            if (!CollectionUtils.isEqualCollection((Collection) oldChild, (Collection) changedChild)) {
                LOGGER.debug("Chnaged data: old collection {}, changed collection {}, key {}, publishing to {}",
                        encryptSensitiveDataService.encryptLog(Objects.toString(oldChild, "")),
                        encryptSensitiveDataService.encryptLog(Objects.toString(changedChild, "")),
                        childNode.getPath(), childNode.getTopicNames());
                changeDescription = new ChangeDescription();
                changeDescription.setOld(oldChild);
                changeDescription.setChanged(changedChild);
            }

        } else if (!oldChild.equals(changedChild)) {
            LOGGER.debug("Chnaged data: old  {}, changed {}, key {}, publishing to {}",
                    encryptSensitiveDataService.encryptLog(Objects.toString(oldChild, "")),
                    encryptSensitiveDataService.encryptLog(Objects.toString(changedChild, "")),
                    childNode.getPath(), childNode.getTopicNames());
            changeDescription = new ChangeDescription();
            changeDescription.setOld(oldChild);
            changeDescription.setChanged(changedChild);
        }
        // once done,
        if (changeDescription != null) {
            changeDescription.setKey(childNode.getKey());
            changeDescription.setPath(childNode.getPath());
            addChangeDecription(childNode.getTopicNames(), topicToChangeDescription, changeDescription);
        }
        identifyChnages(oldChild, changedChild, childNode, topicToChangeDescription);
    }

    private void addChangeDecription(Set<String> topics, Map<String, List<ChangeDescription>> topicToChangeDescription,
            ChangeDescription changeDescription) {
        for (String topic : topics) {
            List<ChangeDescription> changeDescriptions = topicToChangeDescription.get(topic);
            if (changeDescriptions == null) {
                changeDescriptions = new ArrayList<>();
                topicToChangeDescription.put(topic, changeDescriptions);
            }
            changeDescriptions.add(changeDescription);
        }
    }

    private void publishChanges(Map<String, List<ChangeDescription>> changes, VehicleProfile changedVehicleProfile) {

        Iterator<Entry<String, List<ChangeDescription>>> changesItr = changes.entrySet().iterator();
        while (changesItr.hasNext()) {
            Entry<String, List<ChangeDescription>> changesEntry = changesItr.next();
            String topicName = changesEntry.getKey();
            List<ChangeDescription> changeDescriptions = changesEntry.getValue();
            VehicleProfileNotificationEventDataV1_1 notificationEventDataV1 = new
                    VehicleProfileNotificationEventDataV1_1();
            notificationEventDataV1.setChangeDescriptions(changeDescriptions);
            sendEvent(notificationEventDataV1, topicName, changedVehicleProfile.getVehicleId(),
                    EventID.VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT);
        }
    }

    private void sendEvent(EventData eventData, String topicName, String vehicleId, String eventId) {
        LOGGER.debug("Sending the event {} to topic {}", encryptSensitiveDataService.encryptLog(eventData.toString()),
                topicName);
        IgniteEventImpl igniteEvent = new IgniteEventImpl();
        igniteEvent.setEventData(eventData);
        igniteEvent.setEventId(eventId);
        igniteEvent.setVersion(Version.V1_1);
        igniteEvent.setTimestamp(System.currentTimeMillis());
        igniteEvent.setVehicleId(vehicleId);
        igniteEvent.setRequestId(MDC.get(Constants.REQUEST_ID));

        try {
            kafkaService.sendIgniteEvent(topicName, vehicleId, igniteEvent);
        } catch (ExecutionException e) {
            LOGGER.error("Exception occured while sending message", e);
        }

    }
}
