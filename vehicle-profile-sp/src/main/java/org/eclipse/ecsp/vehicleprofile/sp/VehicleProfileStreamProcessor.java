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

package org.eclipse.ecsp.vehicleprofile.sp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.analytics.stream.base.IgniteEventStreamProcessor;
import org.eclipse.ecsp.analytics.stream.base.StreamProcessingContext;
import org.eclipse.ecsp.analytics.stream.base.stores.HarmanPersistentKVStore;
import org.eclipse.ecsp.analytics.stream.base.utils.JsonUtils;
import org.eclipse.ecsp.domain.EventID;
import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.EventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.vehicleprofile.DeviceMessageEventImpl;
import org.eclipse.ecsp.events.vehicleprofile.DeviceVinEventDataV1_0;
import org.eclipse.ecsp.events.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.key.IgniteKey;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.DeviceMessageData;
import org.eclipse.ecsp.vehicleprofile.domain.Ecu;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.sp.service.DeviceMessageService;
import org.eclipse.ecsp.vehicleprofile.sp.service.EncryptSensitiveDataService;
import org.eclipse.ecsp.vehicleprofile.sp.service.VehicleProfileApiCallService;
import org.eclipse.ecsp.vehicleprofile.sp.service.vin.DeviceVinEventChain;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.eclipse.ecsp.vehicleprofile.sp.utils.VehicleProfileProcessorException;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * VehicleProfileStreamProcessor.
 *
 */
public class VehicleProfileStreamProcessor implements IgniteEventStreamProcessor, InitializingBean {

    private static final String STREAM_PROCESSOR_NAME = "vehicleProfilePostStreamProcessor";
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileStreamProcessor.class);
    private static final String KEY_APPLICATIONID = "applicationId";
    private static final String KEY_SERVICEID = "serviceId";
    private static final String KEY_VERSION = "version";

    @Value("#{'${vehicleprofile.sericeprovisioned.notificaiton.enabled}'.split(',')}")
    private Set<String> notificationEnabledServices;

    @Value("${vehicleprofile.sericeprovisioned.destination.topic.pattern}")
    private String destinationTopicPattern;

    @Value("${source.topic.name}")
    private String[] sources;

    @Value("${sink.topic.name}")
    private String[] sink;

    @Value("${notification.sink.topic.name}")
    private String notificationSinkTopic;

    @Value("${vin.events.notification.sink.topic.name}")
    private String vinEventNotificationSinkTopic;

    @Value("${capability.sink.topic.name}")
    private String capabilitySinkTopic;

    @Value("${enable.pid.request.event}")
    private String enablePiddbRequestEvent;

    @Value("${vin.event.process.enable}")
    private String vinEventProcessEnable;

    @Value("${vehicleprofile.sericeprovisioned.topic.name}")
    private String serviceProvisioningTopicName;

    @Autowired
    @Qualifier(value = "DeviceInitializationNode")
    private DeviceVinEventChain vinProcessNode;

    /**
     * VehicleProfileApiCallService instance.
     */
    @Autowired
    private VehicleProfileApiCallService vpApiCallService;

    @Autowired
    private EncryptSensitiveDataService encryptSensitiveDataService;

    @Autowired
    private DeviceMessageService deviceMessageService;

    private Map<String, String> serviceToTopicNameMap = new java.util.HashMap<String, String>();
    private StreamProcessingContext ctxt;

    /**
     * initialize.
     */
    public void init(StreamProcessingContext spc) {
        LOGGER.info("init called with SPC {}", spc);
        this.ctxt = spc;
    }

    /**
     * Get name.
     */
    @Override
    public String name() {
        return STREAM_PROCESSOR_NAME;
    }

    /**
     * Get sources.
     */
    @Override
    public String[] sources() {
        LOGGER.info("Source topics: {}", this.sources);
        return this.sources;
    }

    /**
     * Get topic sinks.
     */
    @Override
    public String[] sinks() {
        LOGGER.info("Sink topics: {}", this.sink);
        return this.sink;
    }

    /**
     * Processs record.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void process(Record<IgniteKey<?>, IgniteEvent> record) {

        IgniteKey key = record.key();
        IgniteEvent value = record.value();

        LOGGER.info("Started process in VehicleProfileProcessor for Ignite Key : {} Ignite Event : {}",
                CommonUtils.maskContent(record.key().toString()),
                encryptSensitiveDataService.encryptLog(Objects.toString(record.value(), "")));
        try {
            if (!Optional.ofNullable(key).isPresent()) {
                throw new VehicleProfileProcessorException(SpCommonConstants.KEY_NULL_EXCEPTION_MSG);
            }

            if (!Optional.ofNullable(value).isPresent()) {
                throw new VehicleProfileProcessorException(SpCommonConstants.VALUE_NULL_EXCEPTION_MSG);
            }

            EventData eventData = value.getEventData();
            String currentTopic = this.ctxt.streamName();
            LOGGER.info("Current topic :{}", currentTopic);

            if (eventData instanceof VehicleProfileNotificationEventDataV1_1) {

                processEventNotification(currentTopic, (VehicleProfileNotificationEventDataV1_1) eventData, value);

            } else if (Boolean.valueOf(vinEventProcessEnable) && value.getEventId().equals(Constants.VIN_EVENT_ID)) {
                // Processing of VIN events
                LOGGER.info("Start of VIN event processing ...");

                String deviceId = key.getKey().toString();
                final ObjectMapper mapper = new ObjectMapper();
                DeviceVinEventDataV1_0 vinEventData = null;
                vinEventData = mapper.readValue(mapper.writeValueAsString(value.getEventData()),
                        DeviceVinEventDataV1_0.class);
                LOGGER.info("Vin Event Data: {} , Device id: {}", vinEventData, CommonUtils.maskContent(deviceId));

                if (vinEventData != null && StringUtils.isNotBlank(vinEventData.getValue())) {
                    processRecords(deviceId, vinEventData, value, key);
                } else {
                    LOGGER.warn("Skipped processing of VIN event due to blank/empty vin with value :{} for deviceId:{}",
                            vinEventData.getValue(), deviceId);
                }

            } else if (value.getEventId().equals(Constants.DEVICE_MESSAGE_FAILURE_ID)) {
                LOGGER.error("Device Message failure event for key {}: {}",
                        CommonUtils.maskContent(key.getKey().toString()),
                        encryptSensitiveDataService.encryptLog(Objects.toString(value, "")));
            } else {
                LOGGER.info(
                        "Unknown event with key :{}, eventId {} and event value :{}"
                                + " obtained in the Vehicle Profile SP component topics ..",
                        CommonUtils.maskContent(key.getKey().toString()), value.getEventId(),
                        encryptSensitiveDataService.encryptLog(Objects.toString(value, "")));
                LOGGER.info("Ignoring the event......");
            }
        } catch (Exception ex) {
            LOGGER.error("Processing failed for key : {} , value : {} with msg : {}",
                    CommonUtils.maskContent(key.getKey().toString()),
                    encryptSensitiveDataService.encryptLog(Objects.toString(value, "")), ex.getMessage());
        }
        LOGGER.info("End of Vehicle profile processor ...");
    }

    private void processEventNotification(String currentTopic,
                                          VehicleProfileNotificationEventDataV1_1 eventData,
                                          IgniteEvent value) throws Exception {
        if (StringUtils.contains(currentTopic, serviceProvisioningTopicName)) {
            LOGGER.info("Reading events from topic: {} and starting service provisioning processing",
                    currentTopic);
            processAndProvisionChangeEvents(eventData, value);
        } else if (StringUtils.contains(currentTopic, SpCommonConstants.VEHICLE_ATTRIBUTES_TOPIC)) {

            LOGGER.info("Reading events from topic: {} and starting vehicle attributes processing",
                    currentTopic);
            VehicleProfile vehicleProfile = vinProcessNode.updateFuelTypeForVehicleAttributeMmychange(
                    eventData, value.getVehicleId());
            if (vehicleProfile != null && vehicleProfile.getVehicleAttributes() != null) {
                if (Optional.ofNullable(vehicleProfile.getEcus()).isPresent()
                        && !CollectionUtils.isEmpty(vehicleProfile.getEcus())) {

                    for (Map.Entry<String, ? extends Ecu> entry : vehicleProfile.getEcus().entrySet()) {

                        String deviceId = entry.getValue().getClientId();
                        if (!StringUtils.isBlank(deviceId)) {

                            DeviceMessageEventImpl event = formDeviceMessageEvent(value, vehicleProfile,
                                    deviceId);
                            LOGGER.info("Publishing vehicle details to device with ID:{} by MQTT, Event: {}",
                                    CommonUtils.maskContent(deviceId), event);
                            this.ctxt.forward(
                                    new Record(new IgniteStringKey(deviceId), event, System.currentTimeMillis()));
                            // this.ctxt.forward(new IgniteStringKey(deviceId), event);

                        }
                    }
                }
            }
        } else {
            LOGGER.info("Processing logic not available for events from topic :{}", currentTopic);
        }
    }

    private void processRecords(String deviceId,
                                DeviceVinEventDataV1_0 vinEventData,
                                IgniteEvent value,
                                IgniteKey key) throws Exception {
        // Start of VIN decoding and multiple checks.
        AtomicBoolean mmyAlertFlag = new AtomicBoolean(false);
        AtomicBoolean vinChangeAlertFlag = new AtomicBoolean(false);
        vinProcessNode.startVinProcess(deviceId, vinEventData, mmyAlertFlag, vinChangeAlertFlag);

        if (mmyAlertFlag.get()) {

            IgniteEventImpl igniteEvent = vinProcessNode.prepareUserNotificationEvent(value, vinEventData);
            this.ctxt.forwardDirectly(key, igniteEvent, notificationSinkTopic);

        }
        // Forwarding VIN event to vehicle capability topic only when a valid vin is
        // received from client
        if (!vinEventData.isDummy() && enablePiddbRequestEvent.equalsIgnoreCase("true")) {
            IgniteEventImpl igniteEvent = vinProcessNode.prepareCapabilityEvent(value, vinEventData);
            this.ctxt.forwardDirectly(key, igniteEvent, capabilitySinkTopic);
        }

        LOGGER.info("Vin Change Alert Flag status:  {}", vinChangeAlertFlag);
        // fetching current vin number mapped to received client Id before vin
        // processing
        VehicleProfile vpByDeviceIdBeforeProcessing = vpApiCallService
                .getVehicleProfile("clientId=" + deviceId);

        if (vinChangeAlertFlag.get()) {

            IgniteEventImpl igniteEvent = vinProcessNode.prepareVinChangeUserNotificationEvent(deviceId,
                    vpByDeviceIdBeforeProcessing, vinEventData.getValue());
            LOGGER.info("Publishing vin Change notification to user, Device id: {},  Event: {}",
                    CommonUtils.maskContent(deviceId), igniteEvent);
            this.ctxt.forwardDirectly(key, igniteEvent, vinEventNotificationSinkTopic);

        }

        // fetching latest vehicle profile for the client id
        VehicleProfile vpByDeviceId = vpApiCallService.getVehicleProfile("clientId=" + deviceId);

        // updating default provisioned service and capabilities from configured
        // property
        vinProcessNode.updateDefaultProvisionedServicesandCapabilties(deviceId, vinEventData, vpByDeviceId);
        // Device Message operation using stream-base DeviceMessageAgent
        // feature.
        DeviceMessageEventImpl event = formDeviceMessageEvent(value, vpByDeviceId, deviceId);
        LOGGER.info("Publishing vehicle details to device with ID:{} by MQTT, Event: {}",
                CommonUtils.maskContent(deviceId), event);
        this.ctxt.forward(new Record(key, event, System.currentTimeMillis()));
        LOGGER.info("End of VIN event processing ...");
    }

    private DeviceMessageEventImpl formDeviceMessageEvent(IgniteEvent value, VehicleProfile vpByDeviceId,
            String deviceId) {

        DeviceMessageEventImpl event = new DeviceMessageEventImpl();
        DeviceMessageData data = deviceMessageService.getDeviceMessageData(vpByDeviceId, deviceId);

        event.setEventId(value.getEventId());
        event.setDeviceRoutable(true);
        event.setCommand("put");
        event.setVehicleId(deviceId);
        event.setDomain("vehicleProfile");
        event.setVersion(Version.V3_1);
        event.setEventData(null);
        event.setData(data);
        event.setTimestamp(System.currentTimeMillis());
        return event;
    }

    private void processAndProvisionChangeEvents(VehicleProfileNotificationEventDataV1_1 profileNotificationEventData,
            IgniteEvent value) throws IOException {
        List<VehicleProfileNotificationEventDataV1_1.ChangeDescription> changeDescriptions = profileNotificationEventData.getChangeDescriptions();
        if (changeDescriptions == null) {
            return;
        }
        for (VehicleProfileNotificationEventDataV1_1.ChangeDescription changeDescription : changeDescriptions) {
            if (changeDescription.getChanged() == null && changeDescription.getOld() == null) {
                // won't happen, unless it is a bug.
                continue;
            }
            // don't care about keys here.
            Set<Application> changedObjects = convertToSet(changeDescription.getChanged());
            Set<Application> oldObjects = convertToSet(changeDescription.getOld());

            findAndPublishChanges(oldObjects, changedObjects, changeDescription.getKey(), changeDescription.getPath(),
                    value);
        }

    }

    @SuppressWarnings("unchecked")
    private Set<Application> convertToSet(Object object) {
        HashSet<Application> applicaitons = new HashSet<VehicleProfileStreamProcessor.Application>();
        try {
            // extra logic to convert jackson parsed HasMap to application
            // object
            if (object != null && object instanceof List) {
                List<Map<String, String>> applicaitonsObjs = (List<Map<String, String>>) object;
                for (Map<String, String> map : applicaitonsObjs) {
                    applicaitons.add(
                            new Application(map.get(KEY_APPLICATIONID), map.get(KEY_SERVICEID), map.get(KEY_VERSION)));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured while processing the message {}", object);
        }
        return applicaitons;

    }

    private void findAndPublishChanges(Set<Application> old, Set<Application> changed, String key, String path,
            IgniteEvent value) {
        // It is done by iterating through the elements, in order to support
        // publishing
        // the changes to all provisioning topics in future.
        Set<Application> addedApplications = null;
        Set<Application> removedApplications = null;

        if (old != null && changed != null) {
            addedApplications = new HashSet<VehicleProfileStreamProcessor.Application>(changed);
            removedApplications = new HashSet<VehicleProfileStreamProcessor.Application>();
            for (Application oldApplication : old) {
                if (changed.contains(oldApplication)) {
                    addedApplications.remove(oldApplication);
                } else {
                    removedApplications.add(oldApplication);
                }
            }
        }
        if (old == null) {
            // new services are provisioned
            addedApplications = changed;
        }
        if (changed == null) {
            // services are de-provisioned
            removedApplications = old;
        }

        if (addedApplications != null) {
            for (Application application : addedApplications) {
                VehicleProfileNotificationEventDataV1_1.ChangeDescription changeDescription = new VehicleProfileNotificationEventDataV1_1.ChangeDescription();
                changeDescription.setKey(key);
                changeDescription.setPath(path);
                changeDescription.setChanged(application);
                generateEventAndSend(changeDescription, serviceToTopicNameMap.get(application.getServiceId()), value);
            }
        }

        if (removedApplications != null) {
            for (Application application : removedApplications) {
                VehicleProfileNotificationEventDataV1_1.ChangeDescription changeDescription = new VehicleProfileNotificationEventDataV1_1.ChangeDescription();
                changeDescription.setKey(key);
                changeDescription.setPath(path);
                changeDescription.setOld(application);
                generateEventAndSend(changeDescription, serviceToTopicNameMap.get(application.getServiceId()), value);
            }
        }

    }

    private void generateEventAndSend(VehicleProfileNotificationEventDataV1_1.ChangeDescription changeDescription, String topicName, IgniteEvent value) {
        if (StringUtils.isEmpty(topicName)) {
            LOGGER.debug("Not publshing provision changed event {} beacuse it is not configured to",
                    encryptSensitiveDataService.encryptLog(changeDescription.toString()));
            return;
        }
        VehicleProfileNotificationEventDataV1_1 dataV1 = new VehicleProfileNotificationEventDataV1_1();
        dataV1.setChangeDescriptions(Arrays.asList(changeDescription));
        IgniteEventImpl igniteEventImpl = new IgniteEventImpl();
        igniteEventImpl.setEventId(EventID.VEHICLE_PROFILE_CHANGED_NOTIFICATION_EVENT);
        igniteEventImpl.setEventData(dataV1);
        igniteEventImpl.setVersion(Version.V1_1);
        igniteEventImpl.setTimestamp(value.getTimestamp());
        igniteEventImpl.setVehicleId(value.getVehicleId());
        LOGGER.info("Publishing event {} to topic {}",
                encryptSensitiveDataService.encryptLog(igniteEventImpl.toString()), topicName);
        ctxt.forwardDirectly(value.getVehicleId(), JsonUtils.getObjectValueAsString(igniteEventImpl), topicName);
    }

    @Override
    public void punctuate(long timestamp) {
        // Operation not required.
    }

    @Override
    public void close() {
        // Operation not required.
    }

    @Override
    public void configChanged(Properties props) {
        // Operation not required.
    }

    @SuppressWarnings("rawtypes")
    @Override
    public HarmanPersistentKVStore createStateStore() {
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class Application {

        public Application() {
            super();
        }

        public Application(String applicationId, String serviceId, String version) {
            super();
            this.applicationId = applicationId;
            this.serviceId = serviceId;
            this.version = version;
        }

        private String applicationId;
        private String serviceId;
        private String version;

        /**
         * Get serviceId.
         * 
         */
        public String getServiceId() {
            return serviceId;
        }

        /**
         * Set serviceId.
         * 
         */
        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        /**
         * Get applicationId.
         * 
         */
        public String getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(String applicationId) {
            this.applicationId = applicationId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Application other = (Application) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            // As of now, serviceId is the only one used to identify a unique service.
            if (serviceId == null) {
                if (other.serviceId != null) {
                    return false;
                }
            } else if (!serviceId.equals(other.serviceId)) {
                return false;
            }
            return true;
        }

        private VehicleProfileStreamProcessor getOuterType() {
            return VehicleProfileStreamProcessor.this;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Application [applicationId=" + applicationId + ", serviceId=" + serviceId + ", version=" + version
                    + "]";
        }

    }

    /**
     * Set properties.
     */
    public void afterPropertiesSet() throws Exception {
        ArrayList<String> topics = new ArrayList<String>(notificationEnabledServices.size());
        for (String serviceName : notificationEnabledServices) {
            String topicName = MessageFormat.format(destinationTopicPattern, serviceName).trim();
            topics.add(topicName);
            serviceToTopicNameMap.put(serviceName, topicName);
        }
        LOGGER.debug("The supported provisioned services are {}, and they will be published to topic {}",
                notificationEnabledServices, serviceToTopicNameMap);
    }

}
