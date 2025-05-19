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

package org.eclipse.ecsp.vehicleprofile.sp.service.vin;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1;
import org.eclipse.ecsp.domain.VehicleProfileNotificationEventDataV1_1.ChangeDescription;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.events.vehicleprofile.DeviceVinEventDataV1_0;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.Specification;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinDecoderEnums;
import org.eclipse.ecsp.vehicleprofile.domain.Application;
import org.eclipse.ecsp.vehicleprofile.domain.Capabilities;
import org.eclipse.ecsp.vehicleprofile.domain.Ecu;
import org.eclipse.ecsp.vehicleprofile.domain.ProvisionedServices;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleAttributes;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleMasterInfo;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.sp.dao.VehicleMasterInfoDaoMongoImpl;
import org.eclipse.ecsp.vehicleprofile.sp.service.DeviceAssociationService;
import org.eclipse.ecsp.vehicleprofile.sp.service.EncryptSensitiveDataService;
import org.eclipse.ecsp.vehicleprofile.sp.service.VehicleProfileApiCallService;
import org.eclipse.ecsp.vehicleprofile.sp.utils.ChecksumGenerator;
import org.eclipse.ecsp.vehicleprofile.sp.utils.NhtsaResponse;
import org.eclipse.ecsp.vehicleprofile.sp.utils.NhtsaResult;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants.SPACE;

/**
 * The abstract class which will be the base in the chain of responsibility
 * pattern for the implementing classes to process the VIN events.
 *
 */
@Service(value = "DeviceVinEventChain")
public abstract class DeviceVinEventChain {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(DeviceVinEventChain.class);

    /**
     * Acceptable VIN length.
     */
    @Value("${vin.length}")
    private int vinLength;

    /**
     * Acceptable VIN length.
     */
    @Value("${vin.validation.enabled:true}")
    private boolean vinValidationEnabled;

    /**
     * Nhtsa base url.
     */
    @Value("${nhtsa.base.url}")
    private String nhtsaBaseUrl;

    /**
     * Allowed device types.
     */
    @Value("#{'${allowed.device.types}'.split(',')}")
    private Set<String> deviceTypes;

    /**
     * Default device type.
     */
    @Value("${default.device.type}")
    private String defaultDeviceType;

    /**
     * Map will contain the device as key and all default capability services as
     * value.
     */
    @Value("#{${default.capability.services}}")
    private Map<String, String> defaultCapabilityServicesMap;

    /**
     * Map will contain the device as key and all default provisioned services as
     * value.
     */
    @Value("#{${default.provisioned.services}}")
    private Map<String, String> defaultProvisionedServicesMap;

    /**
     * VehicleMasterInfo Mongo impl.
     */
    @Autowired
    private VehicleMasterInfoDaoMongoImpl masterInfoDao;

    /**
     * VehicleProfileApiCallService instance.
     */
    @Autowired
    protected VehicleProfileApiCallService vpApiCallService;

    /**
     * DeviceAssociationService instance.
     */
    @Autowired
    protected DeviceAssociationService deviceAssociationService;

    /**
     * AlphaNumeric constant.
     */
    private static final String ALPHANUMERIC_REGEX = "[a-zA-Z0-9]+";

    /**
     * boolean to check whether vin decoding by third party is required or not.
     */
    @Value("${vin.decode}")
    private boolean vinDecode;

    /**
     * name of third party for vin decoding, none value means no decoding.
     */
    @Value("${vin.decoder}")
    private String vinDecoder;


    /**
     * VEHICLE_FUEL_TYPE_VALUE_FOUR.
     */

    /**
     * VEHICLE_FUEL_TYPE_VALUE_FOUR.
     */

    /**
     * VEHICLE_FUEL_TYPE_VALUE_FOUR.
     */
    public static final int VEHICLE_FUEL_TYPE_VALUE_FOUR = 4;

    /**
     * VEHICLE_FUEL_TYPE_VALUE_SEVENTEEN.
     */

    /**
     * VEHICLE_FUEL_TYPE_VALUE_SEVENTEEN.
     */

    /**
     * VEHICLE_FUEL_TYPE_VALUE_SEVENTEEN.
     */
    public static final int VEHICLE_FUEL_TYPE_VALUE_SEVENTEEN = 17;

    /**
     * VEHICLE_FUEL_TYPE_VALUE_NINETEEN.
     */

    /**
     * VEHICLE_FUEL_TYPE_VALUE_NINETEEN.
     */

    /**
     * VEHICLE_FUEL_TYPE_VALUE_NINETEEN.
     */
    public static final int VEHICLE_FUEL_TYPE_VALUE_NINETEEN = 19;
    
    @Autowired
    private EncryptSensitiveDataService encryptSensitiveDataService;

    @Autowired
    private ChecksumGenerator checksumGenerator;

    private static final String VEHICLE_CLASS_CODE = "vehicleClassCode";
    private static final String MODEL = "model";
    private static final String MODEL_YEAR = "modelYear";

    /**
     * Method to process the VIN events. Implementation to be provided by the child
     * classes based on the scenarios.
     *
     * @param deviceId              - the device id
     * @param eventData             - the vin event
     * @param vpByDeviceId          - the vehicle profile based on the device id.
     * @param vpByReceivedVin       - the vehicle profile based on the received vin.
     * @param deviceDummyVinProfile - the dummy vehicle profile based on the device
     *                              id.
     * @param mmyAlertFlag          - alert flag to hold boolean value for mmy
     *                              notification event. AtomicBoolean used as it
     *                              gives option to change the boolean value for the
     *                              same object.
     * @param vinChangeAlertFlag   - alert flag to hold boolean value for vin
     * @throws URISyntaxException if there is an issue
     */
    public abstract void processVinEvent(final String deviceId, final DeviceVinEventDataV1_0 eventData,
            VehicleProfile vpByDeviceId, VehicleProfile vpByReceivedVin, VehicleProfile deviceDummyVinProfile,
            AtomicBoolean mmyAlertFlag, AtomicBoolean vinChangeAlertFlag) throws URISyntaxException;


    //@Mdc(key = TracingFocusListConstant.TF_VIN, value = "#eventData.value")
    /**
     * Method to start the VIN process. This will be the entry point for the VIN
     * processing.
     *
     * @param deviceId              - device id received
     * @param eventData             - vin event received
     * @param mmyNotificationFlag   - alert flag to hold boolean value for mmy
     *                              notification event. AtomicBoolean used as it
     *                              gives option to change the boolean value for the
     *                              same object.
     * @param vinChangeAlertFlag    - alert flag to hold boolean value for vin
     *                              notification event. AtomicBoolean used as it
     *                              gives option to change the boolean value for the
     *                              same object.
     * @throws Exception if there is an issue
     */
    public void startVinProcess(String deviceId, DeviceVinEventDataV1_0 eventData, AtomicBoolean mmyNotificationFlag,
            AtomicBoolean vinChangeAlertFlag) throws Exception {
        VehicleProfile vpByReceivedVin = null;
        if (eventData != null && eventData.getValue() != null) {
            vpByReceivedVin = vpApiCallService.getVehicleProfile("vin=" + eventData.getValue());
        }

        VehicleProfile deviceDummyVinProfile = vpApiCallService
                .getVehicleProfile("vin=" + SpCommonConstants.VIN_CONVERTED_DUMMY + deviceId);

        if (StringUtils.isEmpty(eventData.getDeviceType())) {
            if (Optional.ofNullable(deviceDummyVinProfile).isPresent()
                    && !StringUtils.isEmpty(deviceDummyVinProfile.getVehicleArchType())) {
                LOGGER.info("Empty deviceType in event. Setting default value from dummy VIN profile");
                eventData.setDeviceType(deviceDummyVinProfile.getVehicleArchType());
            } else {
                VehicleProfile deviceDummyHcpProfile = vpApiCallService
                        .getVehicleProfile("vin=" + SpCommonConstants.HCP_GENERATED_DUMMY + deviceId);
                if (Optional.ofNullable(deviceDummyHcpProfile).isPresent()
                        && !StringUtils.isEmpty(deviceDummyHcpProfile.getVehicleArchType())) {
                    LOGGER.info("Empty deviceType in event. Setting default value from HCP dummy profile");
                    eventData.setDeviceType(deviceDummyHcpProfile.getVehicleArchType());
                } else {
                    LOGGER.info("Empty deviceType in event. Setting default value from configured property");
                    eventData.setDeviceType(defaultDeviceType);
                }
            }
        }

        if (vinValidationEnabled) {
            validateVin(eventData);
        } else {
            LOGGER.info("VIN validation skipped.");
        }
        VehicleProfile vpByDeviceId = vpApiCallService.getVehicleProfile("clientId=" + deviceId);
        if (validateDeviceType(eventData)) {
            processVinEvent(deviceId, eventData, vpByDeviceId, vpByReceivedVin, deviceDummyVinProfile,
                    mmyNotificationFlag, vinChangeAlertFlag);
        } else {
            LOGGER.error("Invalid deviceType: {}, Skipping Vin event processing for deviceId: {}",
                    eventData.getDeviceType(), CommonUtils.maskContent(deviceId));
        }

    }

    /**
     * Method to kick start the decoding flow for a valid new VIN and creating a new
     * vehicle profile for the device and VIN.
     *
     * @param deviceId  - device id received
     * @param eventData - vin event received
     * @return vehicle profile dto created.
     */
    protected VehicleProfile createValidVinProfile(final String deviceId, final DeviceVinEventDataV1_0 eventData) {
        LOGGER.info("Starting VIN decoding and valid VIN profile creation flow for device: {} , vin: {} ...",
                CommonUtils.maskContent(deviceId), CommonUtils.maskContent(eventData.getValue()));

        VehicleProfile newVehicleProfile = new VehicleProfile();
        createVehicleProfileDto(deviceId, eventData, newVehicleProfile);
        LOGGER.info("Calling VIN decode API for decoder {}.", vinDecoder);

        // if configured to use any algorithm for decoding vin, then respective
        // third party API will be called else ignore
        if (vinDecode) {
            String vinDecodedResult;
            switch (vinDecoder.toLowerCase()) {
                case SpCommonConstants.DEFAULT_DECODER:
                    vinDecodedResult = vpApiCallService.decodeVin(newVehicleProfile.getVin(), VinDecoderEnums.DEFAULT,
                            null);
                    updateVehicleProfileWithDefaultDecoderResponse(newVehicleProfile, vinDecodedResult);
                    break;
                case SpCommonConstants.CODEVALUE_DECODER:
                    vinDecodedResult = vpApiCallService.decodeVin(newVehicleProfile.getVin(),
                            VinDecoderEnums.CODE_VALUE,
                            null);
                    updateVehicleProfileWithCodeValueDecoderResponse(newVehicleProfile, vinDecodedResult);
                    break;
                case SpCommonConstants.VEHICLE_SPECIFICATION:
                    vinDecodedResult = vpApiCallService.decodeVin(newVehicleProfile.getVin(),
                            VinDecoderEnums.VEHICLE_SPECIFICATION, null);
                    updateVehicleProfileWithVehicleSpecificationDecoderResponse(newVehicleProfile, vinDecodedResult);
                    break;
                default:
                    LOGGER.info("Unable to find required vin decoder from property file, vin decoder : {}", vinDecoder);
            }
        } else {
            LOGGER.info("Vin decode processing disabled. vinDecode: {}", vinDecode);
        }
        verifyMmyData(newVehicleProfile);
        return newVehicleProfile;
    }

    private void updateVehicleProfileWithCodeValueDecoderResponse(VehicleProfile vehicleProfile,
            String vinDecodedResult) {

        if (vinDecodedResult != null && !StringUtils.isEmpty(vinDecodedResult)) {

            ObjectMapper mapper = new ObjectMapper();

            LOGGER.info("Vehicle details after vin decode: {}", vinDecodedResult);
            Specification specification;
            try {
                specification = mapper.readValue(vinDecodedResult, Specification.class);
                vehicleProfile.getVehicleAttributes().setMake(specification.getManufacture());
                String modelName = specification.getModelName();
                vehicleProfile.getVehicleAttributes().setModel(modelName);
                vehicleProfile.getVehicleAttributes().setName(modelName);
                vehicleProfile.getVehicleAttributes().setModelYear(SpCommonConstants.EMPTY);
                vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.UNKNOWN_VEHICLE);
            } catch (Exception e) {
                LOGGER.error(
                        "Error occurred while processing code-value vin decoder json reponse, vinDecodedResult: {} ",
                        vinDecodedResult, e);
                LOGGER.info("Continuing with DUMMY values for MMY .... ");
            }
        }
    }

    private void updateVehicleProfileWithVehicleSpecificationDecoderResponse(VehicleProfile vehicleProfile,
            String vinDecodedResult) {
        if (vinDecodedResult != null && !StringUtils.isEmpty(vinDecodedResult)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String make = getJsonAttributeValue(vinDecodedResult, VEHICLE_CLASS_CODE);
                String model = getJsonAttributeValue(vinDecodedResult, MODEL);
                String modelYear = getJsonAttributeValue(vinDecodedResult, MODEL_YEAR);
                // make=modelClass
                vehicleProfile.getVehicleAttributes().setMake(make);
                vehicleProfile.getVehicleAttributes().setModel(model);
                vehicleProfile.getVehicleAttributes().setModelYear(modelYear);
                vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.UNKNOWN_VEHICLE);
            } catch (Exception e) {
                LOGGER.error(
                        "Error occurred while processing Position matcher vin decoder json"
                        + " response, vinDecodedResult: {} ",
                        vinDecodedResult, e);
                LOGGER.info("Continuing with DUMMY values for MMY .... ");
            }
        }
    }

    private void updateVehicleProfileWithDefaultDecoderResponse(VehicleProfile vehicleProfile,
            String vinDecodedResult) {

        try {
            if (vinDecodedResult != null && !StringUtils.isEmpty(vinDecodedResult)) {
                ObjectMapper mapper = new ObjectMapper();
                NhtsaResponse nhtsaResponse = mapper.readValue(vinDecodedResult, NhtsaResponse.class);

                if (nhtsaResponse != null) {

                    String primaryFuel = null;
                    String secondaryFuel = null;

                    for (NhtsaResult nhtsaResult : nhtsaResponse.getResults()) {
                        if (nhtsaResult != null && Optional.ofNullable(nhtsaResult.getValue()).isPresent()) {
                            setVehicleAttributes(vehicleProfile, nhtsaResult);
                            if (SpCommonConstants.PRIMARY_FUEL.equals(nhtsaResult.getVariable())) {
                                primaryFuel = nhtsaResult.getValue();
                            }
                            if (SpCommonConstants.SECONDARY_FUEL.equals(nhtsaResult.getVariable())) {
                                secondaryFuel = nhtsaResult.getValue();
                            }
                        }
                    }
                    if (!StringUtils.isBlank(primaryFuel)) {

                        if (SpCommonConstants.FUEL_TYPE_GASOLINE.equals(primaryFuel)) {

                            if (SpCommonConstants.FUEL_TYPE_ELECTRIC.equals(secondaryFuel)) {
                                vehicleProfile.getVehicleAttributes()
                                        .setFuelType(SpCommonConstants.FUEL_TYPE_HYBRID_GASOLINE_VAL);
                                vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.HYBRID_VEHICLE);
                            } else {
                                vehicleProfile.getVehicleAttributes()
                                        .setFuelType(SpCommonConstants.FUEL_TYPE_GASOLINE_VALUE);
                                vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.NON_HYBRID_VEHICLE);
                            }
                        } else if (SpCommonConstants.FUEL_TYPE_DIESEL.equals(primaryFuel)) {

                            if (SpCommonConstants.FUEL_TYPE_ELECTRIC.equals(secondaryFuel)) {
                                vehicleProfile.getVehicleAttributes()
                                        .setFuelType(SpCommonConstants.FUEL_TYPE_HYBRID_DIESEL_VAL);
                                vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.HYBRID_VEHICLE);
                            } else {
                                vehicleProfile.getVehicleAttributes()
                                        .setFuelType(SpCommonConstants.FUEL_TYPE_DIESEL_VALUE);
                                vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.NON_HYBRID_VEHICLE);
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while processing default vin decoder json reponse, vinDecodedResult: {} ",
                    vinDecodedResult, e);
            LOGGER.info("Continuing with DUMMY values for MMY .... ");

        }

    }

    private static void setVehicleAttributes(VehicleProfile vehicleProfile, NhtsaResult nhtsaResult) {
        if (SpCommonConstants.MODEL_YEAR_VARIABLE.equals(nhtsaResult.getVariable())) {
            vehicleProfile.getVehicleAttributes().setModelYear(nhtsaResult.getValue());
        }
        if (SpCommonConstants.MAKE_VARIABLE.equals(nhtsaResult.getVariable())) {
            vehicleProfile.getVehicleAttributes().setMake(nhtsaResult.getValue());
        }
        if (SpCommonConstants.MODEL_VARIABLE.equals(nhtsaResult.getVariable())) {
            vehicleProfile.getVehicleAttributes().setModel(nhtsaResult.getValue());
        }
    }
    /**
     * Method to get the attribute value from the json string.
     *
     * @param json - json string
     * @param attributeName - attribute name
     * @return string
     * @throws IOException - exception
     */
    protected String getJsonAttributeValue(String json, String attributeName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode node = root.findValue(attributeName);
        return node != null ? node.asText() : "";
    }

    /**
     * Method to prepare a Vehicle Profile DTO for a device with event data.
     *
     * @param deviceId       - device id to be populated in vehicle profile.
     * @param eventData      - event containing data to be populated in vehicle
     *                       profile.
     * @param vehicleProfile - vehicle profile dto to be populated with data.
     */
    protected void createVehicleProfileDto(String deviceId, DeviceVinEventDataV1_0 eventData,
            VehicleProfile vehicleProfile) {

        vehicleProfile.setVin(eventData.getValue());
        Date now = new Date();
        vehicleProfile.setCreatedOn(now);
        vehicleProfile.setUpdatedOn(now);
        vehicleProfile.setDummy(eventData.isDummy());
        VehicleAttributes attributes = new VehicleAttributes();
        attributes.setMake(SpCommonConstants.NOT_APPLICABLE);
        attributes.setModelYear(SpCommonConstants.NOT_APPLICABLE);
        attributes.setType(SpCommonConstants.UNKNOWN_VEHICLE);
        String modelName = eventData.getModelName();
        // set model name and nickname as model name
        if (modelName != null) {
            attributes.setName(modelName);
            attributes.setModel(modelName);
        } else {
            attributes.setModel(SpCommonConstants.NOT_APPLICABLE);
            attributes.setName(SpCommonConstants.MY_CAR);
        }
        attributes.setType(eventData.getType());

        Ecu ecu = new Ecu();
        ecu.setClientId(deviceId);

        Map<String, Ecu> ecuMap = new HashMap<>();
        ecuMap.put(eventData.getDeviceType(), ecu);
        vehicleProfile.setModemInfo(deviceAssociationService.getDeviceDetailsByDeviceId(deviceId));
        vehicleProfile.setVehicleAttributes(attributes);
        vehicleProfile.setEcus(ecuMap);
        vehicleProfile.setVehicleArchType(eventData.getDeviceType());

        vehicleProfile.setEpiddbChecksum(checksumGenerator.checksum(SpCommonConstants.DEFAULT_PIDDB_DATA));
        LOGGER.debug("Intial Vehicle Profile DTO :{}", vehicleProfile);

    }

    /**
     * Method to verify the Make, Model, Year obtained after NHTSA call against the
     * Mongo collection. If NHTSA returned values, it will be checked against Mongo.
     * If the NHTSA values exists in Mongo, the values from Mongo will be set else
     * the NHTSA values will be set. If NHTSA did not return values , MMY is set
     * with default values.
     *
     * @param vehicleProfile - vehicle profile dto to be populated with data.
     */
    private void verifyMmyData(VehicleProfile vehicleProfile) {
        LOGGER.info("Starting with MMY check in VehicleMasterInfo .....");
        if (!SpCommonConstants.NOT_APPLICABLE.equals(vehicleProfile.getVehicleAttributes().getMake())
                && (!SpCommonConstants.NOT_APPLICABLE.equals(vehicleProfile.getVehicleAttributes().getModel()))
                && (SpCommonConstants.CODEVALUE_DECODER.equalsIgnoreCase(vinDecoder)
                        || !SpCommonConstants.NOT_APPLICABLE
                                .equals(vehicleProfile.getVehicleAttributes().getModelYear()))) {

            VehicleMasterInfo masterInfo = masterInfoDao.getMasterInfoData(vehicleProfile);
            if (masterInfo != null) {
                LOGGER.info("Search for MMY returned values. Setting Car Name in format M M Y....");
                vehicleProfile.getVehicleAttributes().setMake(masterInfo.getMake());
                vehicleProfile.getVehicleAttributes().setModel(masterInfo.getModel());
                vehicleProfile.getVehicleAttributes().setModelYear(masterInfo.getYear());
                String name = null;
                if (!StringUtils.isBlank(masterInfo.getYear())) {
                    name = vehicleProfile.getVehicleAttributes().getMake() + SPACE
                            + vehicleProfile.getVehicleAttributes().getModel() + SPACE
                            + vehicleProfile.getVehicleAttributes().getModelYear();
                } else {
                    name = vehicleProfile.getVehicleAttributes().getModel();
                    if (!StringUtils.isBlank(vehicleProfile.getVehicleAttributes().getMake())) {
                        name = vehicleProfile.getVehicleAttributes().getMake() + SPACE + name;
                    }
                }
                vehicleProfile.getVehicleAttributes().setName(name);

                if (vehicleProfile.getVehicleAttributes().getFuelType() == null) {
                    int fuelType = masterInfo.getFuelType();
                    vehicleProfile.getVehicleAttributes().setFuelType(fuelType);
                    if (fuelType == 1 || fuelType == VEHICLE_FUEL_TYPE_VALUE_FOUR) {
                        vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.NON_HYBRID_VEHICLE);
                    } else if (fuelType == VEHICLE_FUEL_TYPE_VALUE_SEVENTEEN 
                            || fuelType == VEHICLE_FUEL_TYPE_VALUE_NINETEEN) {
                        vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.HYBRID_VEHICLE);
                    } else {
                        vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.UNKNOWN_VEHICLE);
                    }
                }
            } else {
                LOGGER.info("Search for MMY returned empty. Setting MMY with vin decoder obtained Values....");
                String name = vehicleProfile.getVehicleAttributes().getModel();
                if (!StringUtils.isBlank(vehicleProfile.getVehicleAttributes().getMake())) {
                    name = vehicleProfile.getVehicleAttributes().getMake() + SPACE + name;
                }
                if (!StringUtils.isBlank(vehicleProfile.getVehicleAttributes().getModelYear())) {
                    name = name + SPACE + vehicleProfile.getVehicleAttributes().getModelYear();
                }
                vehicleProfile.getVehicleAttributes().setName(name);

            }
        } else {
            LOGGER.info("Either or all of MMY is dummy..setting as dummy to be used for Notification alert...");
            setDefaultMmy(vehicleProfile);
        }

        LOGGER.info("Completed with MMY check in VehicleMasterInfo .....");

    }

    /**
     * Method to set default MMY values in the vehicle profile.
     *
     * @param vpByDeviceId - vehicle profile dto to be populated with data.
     * @return boolean
     * @throws URISyntaxException if there is an issue
     */
    protected boolean convertHcpToVinProfile(VehicleProfile vpByDeviceId) throws URISyntaxException {
        boolean isHcptoVinConverted = false;
        Date updatedNow = new Date();

        if (vpByDeviceId.getVin().startsWith(SpCommonConstants.HCP_GENERATED_DUMMY)) {
            LOGGER.info("Deleting HCP Profile :{} ", vpByDeviceId);

            vpApiCallService.deleteVehicleProfile(vpByDeviceId);

            LOGGER.info("Creating dummy VIN profile..");
            vpByDeviceId.setVehicleId(null);
            vpByDeviceId.setCreatedOn(new Date());
            String convertedVin = vpByDeviceId.getVin().replaceFirst(SpCommonConstants.HCP_GENERATED_DUMMY,
                    SpCommonConstants.VIN_CONVERTED_DUMMY);
            vpByDeviceId.setVin(convertedVin);
            vpApiCallService.createVehicleProfile(vpByDeviceId);

            isHcptoVinConverted = true;
            LOGGER.info("HCP Generated vehicle profile converted to VIN generated vehicle profile...");
        }
        vpByDeviceId.setUpdatedOn(updatedNow);

        vpApiCallService.updateVehicleProfile(vpByDeviceId.getVehicleId(), vpByDeviceId);
        return isHcptoVinConverted;

    }

    /**
     * Method to set default MMY values in the vehicle profile.
     *
     * @param vpByVinId - vehicle profile dto to be populated with data.
     * @param deviceType - device type
     * @throws URISyntaxException if there is an issue
     */
    protected void moveDeviceToDummyProfile(VehicleProfile vpByVinId, String deviceType) throws URISyntaxException {

        if (Optional.ofNullable(vpByVinId.getEcus()).isPresent()
                && Optional.ofNullable(vpByVinId.getEcus().get(deviceType)).isPresent()) {

            String deviceId = vpByVinId.getEcus().get(deviceType).getClientId();
            LOGGER.info("Moving device: {} to its dummy profile ..", CommonUtils.maskContent(deviceId));

            VehicleProfile vpByDummyVinForDeviceId = vpApiCallService
                    .getVehicleProfile("vin=" + SpCommonConstants.VIN_CONVERTED_DUMMY + deviceId);

            Map<String, Ecu> ecuMap = Optional.ofNullable(vpByDummyVinForDeviceId.getEcus()).isPresent()
                    ? (Map<String, Ecu>) vpByDummyVinForDeviceId.getEcus()
                    : new HashMap<>();

            Ecu ecu = new Ecu();
            ecu.setClientId(deviceId);
            ecuMap.put(deviceType, ecu);
            vpByDummyVinForDeviceId.setEcus(ecuMap);

            if (vpByDummyVinForDeviceId.getModemInfo() == null) {
                vpByDummyVinForDeviceId.setModemInfo(deviceAssociationService.getDeviceDetailsByDeviceId(deviceId));
            }

            // Move Device ,Carry forward MMY, Car name, Base color, Body type,
            // fuelType and type
            // from Valid VIN profile to Dummy profile.
            vpByDummyVinForDeviceId.getVehicleAttributes().setBodyType(vpByVinId.getVehicleAttributes().getBodyType());
            vpByDummyVinForDeviceId.getVehicleAttributes()
                    .setBaseColor(vpByVinId.getVehicleAttributes().getBaseColor());
            vpByDummyVinForDeviceId.getVehicleAttributes().setMake(vpByVinId.getVehicleAttributes().getMake());
            vpByDummyVinForDeviceId.getVehicleAttributes().setModel(vpByVinId.getVehicleAttributes().getModel());
            vpByDummyVinForDeviceId.getVehicleAttributes()
                    .setModelYear(vpByVinId.getVehicleAttributes().getModelYear());
            vpByDummyVinForDeviceId.getVehicleAttributes().setName(vpByVinId.getVehicleAttributes().getName());
            vpByDummyVinForDeviceId.getVehicleAttributes().setType(vpByVinId.getVehicleAttributes().getType());
            vpByDummyVinForDeviceId.getVehicleAttributes().setFuelType(vpByVinId.getVehicleAttributes().getFuelType());
            vpByDummyVinForDeviceId.setUpdatedOn(new Date());

            vpApiCallService.updateVehicleProfile(vpByDummyVinForDeviceId.getVehicleId(), vpByDummyVinForDeviceId);

        }

    }

    /**
     * Method to set default MMY values in the vehicle profile.
     *
     * @param vpByDeviceId - vehicle profile dto to be populated with data.
     * @param deviceId     - device id
     * @param newVin       - new Vin
     * @return IgniteEventImpl - ignite event
     */
    public IgniteEventImpl prepareVinChangeUserNotificationEvent(String deviceId, VehicleProfile vpByDeviceId,
                                                                 String newVin) {

        LOGGER.info("Starting vin change notification event preparation ....");
        GenericEventData eventData = new GenericEventData();
        eventData.set(SpCommonConstants.HARMAN_ID, deviceId);
        eventData.set(SpCommonConstants.PREVIOUS_VIN, vpByDeviceId.getVin());
        eventData.set(SpCommonConstants.NEW_VIN, newVin);
        IgniteEventImpl eventImpl = new IgniteEventImpl();
        eventImpl.setEventData(eventData);
        eventImpl.setEventId(SpCommonConstants.VIN_CHANGE_EVENT_ID);
        eventImpl.setTimestamp(System.currentTimeMillis());
        eventImpl.setVersion(Version.V1_0);

        LOGGER.info("Vin Change Notification Event prepared : {}", eventImpl);
        return eventImpl;

    }

    /**
     * Method to prepare MMY notification event.
     *
     * @param value        - the ignite event
     * @param vinEventData - the vin event obtained
     * @return MMY IgniteEvent
     */
    public IgniteEventImpl prepareUserNotificationEvent(final IgniteEvent value,
            final DeviceVinEventDataV1_0 vinEventData) {

        IgniteEventImpl eventImpl = new IgniteEventImpl();
        eventImpl.setBenchMode(SpCommonConstants.BENCHMODE);

        GenericEventData eventData = new GenericEventData();
        eventData.set(SpCommonConstants.DUMMY_KEY, vinEventData.isDummy());
        eventData.set(SpCommonConstants.VALUE_KEY, vinEventData.getValue());

        eventImpl.setEventData(eventData);
        eventImpl.setEventId(SpCommonConstants.MMY_EVENT_ID);
        eventImpl.setTimestamp(System.currentTimeMillis());
        eventImpl.setTimezone(value.getTimezone());
        eventImpl.setVersion(Version.V1_0);

        LOGGER.info("Prepared MMY Event");
        return eventImpl;

    }

    /**
     * Method to prepare capability event to fetch pid from vinvox.
     *
     * @param value        - the ignite event
     * @param vinEventData - the vin event obtained
     * @return VIN IgniteEvent
     */
    public IgniteEventImpl prepareCapabilityEvent(final IgniteEvent value, final DeviceVinEventDataV1_0 vinEventData) {

        IgniteEventImpl eventImpl = new IgniteEventImpl();
        eventImpl.setBenchMode(SpCommonConstants.BENCHMODE);

        GenericEventData eventData = new GenericEventData();
        eventData.set(SpCommonConstants.VALUE_KEY, vinEventData.getValue());

        eventImpl.setEventData(eventData);
        eventImpl.setEventId(SpCommonConstants.PID_DB_REQUEST);
        eventImpl.setVehicleId(value.getVehicleId());
        eventImpl.setTimestamp(System.currentTimeMillis());
        eventImpl.setTimezone(value.getTimezone());
        eventImpl.setVersion(Version.V1_0);

        LOGGER.info("Prepared PID DB Request Event");
        return eventImpl;

    }

    /**
     * Method to validate VIN event data.
     *
     * @param eventData - received vin event.
     */
    private void validateVin(DeviceVinEventDataV1_0 eventData) {
        if (!StringUtils.isEmpty(eventData.getValue())) {
            if ((eventData.getValue().length() != vinLength) || !eventData.getValue().matches(ALPHANUMERIC_REGEX)) {
                LOGGER.info("VIN value:- {} is invalid. Setting it as dummy... ",
                        CommonUtils.maskContent(eventData.getValue()));
                eventData.setDummy(true);
            }
            LOGGER.info("VIN Validation complete ..");
        }
    }

    /**
     * Method to validate deviceType.
     * 
     */
    private boolean validateDeviceType(DeviceVinEventDataV1_0 eventData) {
        if (!StringUtils.isEmpty(eventData.getValue())) {
            if (!deviceTypes.contains(eventData.getDeviceType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method to update default provisioned and capabilities services in the vehicle
     * profile.
     *
     * @param deviceId       - device id
     * @param eventData      - vin event
     * @param vpByDeviceId   - vehicle profile based on the device id.
     * @throws Exception if there is an issue
     * @return VehicleProfile - updated vehicle profile
     */
    public VehicleProfile updateDefaultProvisionedServicesandCapabilties(String deviceId,
            DeviceVinEventDataV1_0 eventData, VehicleProfile vpByDeviceId) throws Exception {
        LOGGER.info("Updating profile with default provisioned and capabilities services from configured property..");
        if (!StringUtils.isEmpty(eventData.getValue())
                && !StringUtils.isEmpty(eventData.getDeviceType())
                && defaultCapabilityServicesMap.containsKey(eventData.getDeviceType())
                && defaultProvisionedServicesMap.containsKey(eventData.getDeviceType())) {

            String[] defaultCapabilityServices = defaultCapabilityServicesMap.get(eventData.getDeviceType()).split(",");
            String[] defaultProvisionedServices = defaultProvisionedServicesMap.get(eventData.getDeviceType())
                    .split(",");

            if (Optional.ofNullable(vpByDeviceId.getEcus()).isPresent()) {
                Map<String, Ecu> ecuMap = (Map<String, Ecu>) vpByDeviceId.getEcus();

                if (ecuMap.get(eventData.getDeviceType()) != null) {
                    Ecu ecu = ecuMap.get(eventData.getDeviceType());
                    ecu = updateEcuwithServices(ecu, new HashSet<>(Arrays.asList(defaultCapabilityServices)),
                            new HashSet<>(Arrays.asList(defaultProvisionedServices)));
                    ecuMap.put(eventData.getDeviceType(), ecu);
                    vpByDeviceId.setEcus(ecuMap);
                    vpApiCallService.updateVehicleProfile(vpByDeviceId.getVehicleId(), vpByDeviceId);
                }
            }
        }
        return vpByDeviceId;
    }

    private Ecu updateEcuwithServices(Ecu ecu, Set<String> defaultCapabilities, Set<String> defaultProServices) {
        Capabilities capabilities = null;
        ProvisionedServices provisionedServices = null;

        Set<Application> capServiceList = new HashSet<>();
        Set<Application> proServiceList = new HashSet<>();

        if (defaultCapabilities != null && !defaultCapabilities.isEmpty()) {
            LOGGER.debug("Adding default capabilities: {} from configured property", defaultCapabilities);
            defaultCapabilities.forEach(c -> {
                Application service = new Application();
                service.setApplicationId(c);
                capServiceList.add(service);
            });
            if (ecu.getCapabilities() != null && (ecu.getCapabilities().getServices() != null
                    && !ecu.getCapabilities().getServices().isEmpty())) {
                capabilities = ecu.getCapabilities();
                Set<Application> collatedServices = new HashSet<>(capabilities.getServices());
                collatedServices.addAll(capServiceList);
                capabilities.setServices(new ArrayList<>(collatedServices));
            } else {
                capabilities = new Capabilities();
                capabilities.setServices(new ArrayList<>(capServiceList));
            }
            ecu.setCapabilities(capabilities);
        }

        if (defaultProServices != null && !defaultProServices.isEmpty()) {
            LOGGER.debug("Adding default provisioned services: {} from configured property", defaultProServices);
            defaultProServices.forEach(p -> {
                Application service = new Application();
                service.setApplicationId(p);
                proServiceList.add(service);
            });
            if (ecu.getProvisionedServices() != null && (ecu.getProvisionedServices().getServices() != null
                    && !ecu.getProvisionedServices().getServices().isEmpty())) {
                provisionedServices = ecu.getProvisionedServices();
                Set<Application> collatedServices = new HashSet<>(provisionedServices.getServices());
                collatedServices.addAll(proServiceList);
                provisionedServices.setServices(new ArrayList<>(collatedServices));
            } else {
                provisionedServices = new ProvisionedServices();
                provisionedServices.setServices(new ArrayList<>(proServiceList));
            }
            ecu.setProvisionedServices(provisionedServices);
        }

        LOGGER.debug("final ECUs after update: {}", ecu);
        return ecu;
    }

    /**
     * Method to set MMY with default values.
     *
     * @param vehicleProfile - vehicle profile dto
     */
    private void setDefaultMmy(VehicleProfile vehicleProfile) {
        vehicleProfile.getVehicleAttributes().setMake(SpCommonConstants.NOT_APPLICABLE);
        vehicleProfile.getVehicleAttributes().setModel(SpCommonConstants.NOT_APPLICABLE);
        vehicleProfile.getVehicleAttributes().setModelYear(SpCommonConstants.NOT_APPLICABLE);
    }

    /**
     * Method to update fuel type for vehicle attribute MMY change.
     *
     * @param eventData - the event data
     * @param vehicleId - the vehicle id
     * @throws Exception if there is an issue
     * @return VehicleProfile - updated vehicle profile
     */
    public VehicleProfile updateFuelTypeForVehicleAttributeMmychange(VehicleProfileNotificationEventDataV1_1 eventData,
                                                                     String vehicleId) throws Exception {

        LOGGER.info("Starting with FuelType update operation for MMY update ...");

        Boolean updateFuelTypeFlag = false;
        VehicleAttributes changedAttr = null;
        List<ChangeDescription> chgDescLst = eventData.getChangeDescriptions();

        // Only interested in changed vehicle attributes
        Optional<ChangeDescription> vehicleAttrChangedDesc = chgDescLst.parallelStream()
                .filter(s -> s.getKey().equals(SpCommonConstants.VEHICLE_ATTRIBUTES)).findFirst();

        if (vehicleAttrChangedDesc.isPresent()) {
            LOGGER.debug("ChangeDescription contains vehicle attributes {}..",
                    encryptSensitiveDataService.encryptLog(vehicleAttrChangedDesc.get().toString()));
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
            changedAttr = mapper.convertValue(vehicleAttrChangedDesc.get().getChanged(), VehicleAttributes.class);
            // Only interested in changed MMY of the vehicle attributes
            updateFuelTypeFlag = !(StringUtils.isEmpty(changedAttr.getMake())
                    && StringUtils.isEmpty(changedAttr.getModel()) && StringUtils.isEmpty(changedAttr.getModelYear()));
        }

        if (updateFuelTypeFlag) {
            VehicleProfile vehicleProfile = vpApiCallService.getVehicleProfile("_id=" + vehicleId);
            if (Optional.ofNullable(vehicleProfile).isPresent()) {
                updateFuelTypeAttribute(vehicleProfile);
                vpApiCallService.updateVehicleProfile(vehicleId, vehicleProfile);
                return vehicleProfile;
            }
        } else {
            LOGGER.info("Vehicle Profile MMY Attributes were not changed .... FuelType update operation skipped....");
        }
        return null;
    }

    private void updateFuelTypeAttribute(VehicleProfile vehicleProfile) {
        vehicleProfile.getVehicleAttributes().setFuelType(null);
        vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.UNKNOWN_VEHICLE);
        VehicleMasterInfo masterInfo = masterInfoDao.getMasterInfoData(vehicleProfile);
        if (Optional.ofNullable(masterInfo).isPresent()) {
            int fuelType = masterInfo.getFuelType();
            if (fuelType == SpCommonConstants.FUEL_TYPE_GASOLINE_VALUE
                    || fuelType == SpCommonConstants.FUEL_TYPE_DIESEL_VALUE) {
                vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.NON_HYBRID_VEHICLE);
            } else if (fuelType == SpCommonConstants.FUEL_TYPE_HYBRID_GASOLINE_VAL
                    || fuelType == SpCommonConstants.FUEL_TYPE_HYBRID_DIESEL_VAL) {
                vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.HYBRID_VEHICLE);
            } else {
                vehicleProfile.getVehicleAttributes().setType(SpCommonConstants.UNKNOWN_VEHICLE);
            }
            vehicleProfile.getVehicleAttributes().setFuelType(fuelType);
        }
    }

}