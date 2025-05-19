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

package org.eclipse.ecsp.vehicleprofile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.GenericEventData;
import org.eclipse.ecsp.entities.IgniteEventImpl;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.commons.dto.vin.Specification;
import org.eclipse.ecsp.vehicleprofile.commons.enums.VinDecoderEnums;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.dao.VehicleDao;
import org.eclipse.ecsp.vehicleprofile.domain.Count;
import org.eclipse.ecsp.vehicleprofile.domain.Ecu;
import org.eclipse.ecsp.vehicleprofile.domain.EcusFilterDto;
import org.eclipse.ecsp.vehicleprofile.domain.FilterDto;
import org.eclipse.ecsp.vehicleprofile.domain.Filters;
import org.eclipse.ecsp.vehicleprofile.domain.Inventory;
import org.eclipse.ecsp.vehicleprofile.domain.InventoryScomo;
import org.eclipse.ecsp.vehicleprofile.domain.PageResponse;
import org.eclipse.ecsp.vehicleprofile.domain.User;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleAttributes;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileEcuFilterRequest;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileFilterRequest;
import org.eclipse.ecsp.vehicleprofile.exception.BadRequestException;
import org.eclipse.ecsp.vehicleprofile.exception.FailureReason;
import org.eclipse.ecsp.vehicleprofile.exception.InternalServerException;
import org.eclipse.ecsp.vehicleprofile.exception.NotFoundException;
import org.eclipse.ecsp.vehicleprofile.filter.FilterQueryGenerator;
import org.eclipse.ecsp.vehicleprofile.rest.mapping.AssociatedVehicle;
import org.eclipse.ecsp.vehicleprofile.rest.mapping.EcuClient;
import org.eclipse.ecsp.vehicleprofile.search.SearchQueryGenerator;
import org.eclipse.ecsp.vehicleprofile.service.AssociationHistoryService.AssociationOperation;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.Util;
import org.eclipse.ecsp.vehicleprofile.utils.Utils;
import org.eclipse.ecsp.vehicleprofile.utils.VpKafkaService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * VehicleManager service.
 */
@Service
public class VehicleManager {

    public static final String VEHICLE_PROFILE_FOUND_FOR_DEVICE_ID_PROFILE
            = "Vehicle profile found for deviceId: {}, Profile: {}";
    @Autowired
    private VehicleDao dao;
    @Value("${vehicleprofile.auto.generate.vehicleId}")
    private boolean autoGenerateVehicleId;
    @Value("#{'${allowed.device.types}'.split(',')}")
    private Set<String> allowedEcus;
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleManager.class);
    @Autowired
    private SearchQueryGenerator searchQueryGenerator;
    @Value("#{'${vehicleprofile.allowed.association.status}'.split(',')}")
    private Set<String> allowedStatus;
    @Value("${vehicleprofile.max.allowed.associations}")
    private int maxAllowedAssociations;
    @Autowired
    private AssociationHistoryService associationHistoryService;
    @Value("${notification.topic.name}")
    private String notificationTopic;
    @Autowired
    @Lazy
    private VpKafkaService kafkaService;
    @Autowired
    private VinDecodeService vinDecodeService;
    @Value("${vin.decoder}")
    private String vinDecoder;
    @Value("${vin.update.notification.id:VinReplace}")
    private String vinUpdateNotificationId;
    @Autowired
    private VehicleAssociationService vehicleAssociationService;
    @Value("${vehicle.association.base.url}")
    private String deviceAssociateBaseUrl;
    @Value("${vehicleProfile.block.enrollment}")
    private String blockEnrollment;
    @Autowired
    private FilterQueryGenerator filterQueryGenerator;
    @Autowired
    private EncryptSensitiveDataService encryptSensitiveDataService;
    @Autowired
    private Util util;

    // Added as part of US 295583.
    @Value("${disable.dev.assoc.check}")
    private String disableDevAssocCheck;

    /**
     * findByVin.
     */
    private List<VehicleProfile> findByVin(String vin) {
        IgniteCriteria ic = new IgniteCriteria("vin", Operator.EQ, vin);
        IgniteCriteriaGroup icg = new IgniteCriteriaGroup(ic);
        IgniteQuery iq = new IgniteQuery(icg);
        return dao.find(iq);
    }

    /**
     * createVehicle.
     */
    public String createVehicle(VehicleProfile profile) {
        LOGGER.debug("create vehicle {}", profile);
        if (null != profile.getVehicleId()) {
            throw new BadRequestException(FailureReason.VEHICLE_ID_IS_SYSTEM_CREATED);
        } else {
            String vin = profile.getVin();
            if ((null == vin) || (vin.length() == 0)) {
                throw new BadRequestException(FailureReason.VIN_MUST_BE_PROVIDED);
            } else if (!findByVin(vin).isEmpty()) {
                throw new BadRequestException(FailureReason.VEHICLE_WITH_VIN_ALREADY_EXIST);
            } else if (!util.hasValidEcus(profile)) {
                throw new BadRequestException(FailureReason.DOES_NOT_HAVE_VALID_ECUS);
            } else if (!autoGenerateVehicleId) {
                profile.setVehicleId(vin);
            }
        }
        Date now = new Date();
        profile.setCreatedOn(now);
        profile.setUpdatedOn(now);
        dao.save(profile);
        util.checkEmptyNullEcuType(profile, null);
        String id = profile.getVehicleId();
        if (null == id) {
            throw new InternalServerException(FailureReason.CREATION_FAILED);
        }

        return id;
    }

    /**
     * updateVehicleById.
     */
    public boolean update(VehicleProfile vehicleProfile, String userId) {
        LOGGER.info("VehicleManager update method - disableDevAssocCheck: {}", disableDevAssocCheck);
        if (!util.hasValidEcus(vehicleProfile)) {
            throw new IllegalArgumentException("Does not have valid ECUs");
        }
        VehicleProfile dbVehicleProfile = dao.findById(vehicleProfile.getVehicleId());
        if (dbVehicleProfile == null) {
            throw new NotFoundException(FailureReason.NO_VEHICLE_FOUND);
        }

        // Added as part of US 295583. Skip device association check/call if
        // disableHCPCalls is set to true.
        if (!Boolean.valueOf(disableDevAssocCheck)) {
            LOGGER.info("VehicleManager update method - performing association check");
            if (StringUtils.isNotBlank(userId) && !isDeviceAssociatedWithUser(dbVehicleProfile, userId)) {
                throw new BadRequestException(FailureReason.VEHICLE_PROFILE_DOES_NOT_BELONG_TO_USER);
            }
        }
        util.checkEmptyNullEcuType(vehicleProfile, dbVehicleProfile);
        // Don't update the schema version, createdOn.
        vehicleProfile.setSchemaVersion(null);
        vehicleProfile.setCreatedOn(null);
        vehicleProfile.setUpdatedOn(new Date());
        return dao.getAndUpdate(vehicleProfile);

    }

    /**
     * getVehicleById.
     */
    public Object get(String id, String path) {
        LOGGER.info("Get id {}, path {}", CommonUtils.maskContent(id), path);
        VehicleProfile profile = dao.findById(id);
        if (null == profile) {
            throw new NotFoundException(FailureReason.NO_VEHICLE_FOUND);
        }
        String[] nodes = Utils.getNodesFromPath(path, id);
        if (null != nodes) {
            return Utils.readNodeValue(profile, nodes);
        }

        return profile;
    }

    /**
     * findVehicleById.
     */
    public VehicleProfile findVehicleById(String vehicleId) {
        VehicleProfile vehicleProfile = dao.findById(vehicleId);
        if (vehicleProfile == null) {
            throw new NotFoundException(FailureReason.NO_VEHICLE_FOUND);
        }
        return vehicleProfile;
    }

    /**
     * put vehicle details.
     */
    public boolean put(VehicleProfile vehicleProfile) {
        VehicleProfile dbVehicleProfile = dao.findById(vehicleProfile.getVehicleId());
        if (dbVehicleProfile == null) {
            throw new NotFoundException(FailureReason.NO_VEHICLE_FOUND);
        } else if (!vehicleProfile.getVin().equals(dbVehicleProfile.getVin())) {
            throw new BadRequestException(FailureReason.VIN_DOES_NOT_MATCH_WITH_EXISTING_RECORD);
        } else if (!util.hasValidEcus(vehicleProfile)) {
            throw new BadRequestException(FailureReason.DOES_NOT_HAVE_VALID_ECUS);
        }
        util.checkEmptyNullEcuType(vehicleProfile, dbVehicleProfile);
        return dao.update(vehicleProfile);
    }

    public List<VehicleProfile> search(Map<String, String> searchParams) {
        return dao.find(searchQueryGenerator.generateQuery(searchParams));
    }

    /**
     * Retrieves the ECU (Electronic Control Unit) details for a given client ID.
     *
     * @param clientId The client ID for which the ECU details are to be retrieved.
     * @return A `VehicleProfileEcuFilterRequest` object containing the ECU details
     *         associated with the given client ID, or `null` if no matching vehicle profile is found.
     */
    public VehicleProfileEcuFilterRequest getEcuByClientId(String clientId) {
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("clientId", clientId);
        List<VehicleProfile> vehicleProfileList = search(searchParams);
        VehicleProfile vehicleProfile = null;
        VehicleProfileEcuFilterRequest vehicleProfileEcuFilterRequest = new VehicleProfileEcuFilterRequest();
        if (vehicleProfileList != null && !vehicleProfileList.isEmpty() && vehicleProfileList.get(0) != null) {
            vehicleProfile = vehicleProfileList.get(0);
            LOGGER.debug(VEHICLE_PROFILE_FOUND_FOR_DEVICE_ID_PROFILE, CommonUtils.maskContent(clientId),
                    vehicleProfile);
            vehicleProfileEcuFilterRequest.setVehicleId(vehicleProfile.getVehicleId());
            vehicleProfileEcuFilterRequest.setConnectedPlatform(vehicleProfile.getConnectedPlatform());
            Map<String, EcusFilterDto> ecu = new HashMap<>();
            if (null != vehicleProfile.getEcus()) {
                for (Map.Entry<String, ? extends Ecu> ecuMap : vehicleProfile.getEcus().entrySet()) {
                    LOGGER.debug("Vehicle profile ecu: {}", ecuMap.getKey());
                    LOGGER.debug("Vehicle profile ecu clientId: {}", ecuMap.getValue().getClientId());
                    if (null != ecuMap.getValue().getClientId()
                            && clientId.equalsIgnoreCase(ecuMap.getValue().getClientId())) {
                        vehicleProfileEcuFilterRequest.setDeviceType(ecuMap.getKey());
                    }
                    EcusFilterDto ecusFilterDto = new EcusFilterDto();
                    ecusFilterDto.setClientId(ecuMap.getValue().getClientId());
                    ecusFilterDto.setSerialNo(ecuMap.getValue().getSerialNo());
                    ecusFilterDto.setEcuType(ecuMap.getValue().getEcuType());
                    ecu.put(ecuMap.getKey(), ecusFilterDto);
                    LOGGER.debug("map: {}", ecu);
                }
                LOGGER.debug("Vehicle profile map: {}", ecu);
                vehicleProfileEcuFilterRequest.setEcus(ecu);
            } else {
                vehicleProfileEcuFilterRequest.setDeviceType(null);
                vehicleProfileEcuFilterRequest.setEcus(null);
            }
            return vehicleProfileEcuFilterRequest;
        } else {
            return null;
        }
    }

    /**
     * associate.
     */
    public boolean associate(String vehicleId, User user) {
        LOGGER.debug("associate {} {}", CommonUtils.maskContent(vehicleId), user);
        if (user == null || StringUtils.isBlank(user.getUserId())) {
            throw new BadRequestException(FailureReason.ASSOCIATION_USER_ID_MUST_BE_PRESENT);
        }

        if (StringUtils.isBlank(user.getStatus()) || !hasValidAssociationStatus(user.getStatus())) {
            throw new BadRequestException(FailureReason.ASSOCIATION_USER_INVALID_ASSOCIATION_STATUS);
        }

        VehicleProfile vehicleProfile = dao.findById(vehicleId);

        if (null == vehicleProfile) {
            throw new NotFoundException(FailureReason.NO_VEHICLE_FOUND);
        }
        if (!isAssociationsAllowed(vehicleProfile)) {
            throw new BadRequestException(FailureReason.ASSOCIATION_MAX_NUMBER_OF_ASSOCIATIONS_REACHED);
        }
        if ("true".equalsIgnoreCase(blockEnrollment)
                || (vehicleProfile.getBlockEnrollment() != null && vehicleProfile.getBlockEnrollment() == true)) {
            throw new BadRequestException(FailureReason.VEHICLE_PROFILE_ENROLLMENT_NOT_ALLOWED);
        }
        if (isAssociationExist(user.getUserId(), vehicleProfile)) {
            throw new BadRequestException(FailureReason.ASSOCIATION_USER_VEHICLE_ASSOCIATION_ALREADY_EXIST);
        }
        boolean result = dao.associate(vehicleId, user, null == vehicleProfile.getAuthorizedUsers());
        if (result) {
            associationHistoryService.logHistory(vehicleId, user, null, null, AssociationOperation.CREATED);
        }
        LOGGER.debug("Associating user, result {}", result);
        return result;
    }

    /**
     * disassociate.
     */
    public boolean disassociate(String vehicleId, String userId, String reason, String clientIdOrigin) {
        LOGGER.debug("Disassociating user {} vehicle {}", CommonUtils.maskContent(userId),
                CommonUtils.maskContent(vehicleId));
        validateAssociationRequest(vehicleId, userId);
        boolean result = dao.disassociate(vehicleId, userId);
        if (result) {
            associationHistoryService.logHistory(vehicleId, new User(userId), reason, clientIdOrigin,
                    AssociationOperation.DELETED);
        }
        return result;
    }

    /**
     * updateAssociation by object.
     */
    public boolean updateAssociation(String vehicleId, User user) {
        LOGGER.debug("Update associaton vehicleId {}, user {}", CommonUtils.maskContent(vehicleId), user);
        validateAssociationRequest(vehicleId, user);

        if (user.getStatus() != null && !hasValidAssociationStatus(user.getStatus())) {
            throw new BadRequestException(FailureReason.ASSOCIATION_USER_INVALID_ASSOCIATION_STATUS);
        }
        boolean result = dao.updateAssociation(vehicleId, user);
        if (result) {
            associationHistoryService.logHistory(vehicleId, user, null, null, AssociationOperation.UPDATED);
        }
        return result;
    }

    /**
     * validateAssociationRequest by object.
     */
    private void validateAssociationRequest(String vehicleId, User user) {
        if (user == null) {
            throw new BadRequestException(FailureReason.ASSOCIATION_USER_ID_MUST_BE_PRESENT);
        }
        validateAssociationRequest(vehicleId, user.getUserId());
    }

    /**
     * validateAssociationRequest.
     */
    private void validateAssociationRequest(String vehicleId, String userId) {
        if (userId == null || StringUtils.isBlank(userId)) {
            throw new BadRequestException(FailureReason.ASSOCIATION_USER_ID_MUST_BE_PRESENT);
        }

        if (!isAssociationExist(userId, dao.findById(vehicleId))) {
            throw new NotFoundException(FailureReason.ASSOCIATION_USER_VEHICLE_ASSOCIATION_DOES_NOT_EXIST);
        }
    }

    /**
     * hasValidAssociationStatus.
     */
    private boolean hasValidAssociationStatus(String status) {
        return allowedStatus.contains(status);
    }

    /**
     * isAssociationExist.
     */
    private boolean isAssociationExist(String userId, VehicleProfile vehicleProfile) {
        if (null == vehicleProfile) {
            return false;
        }
        List<User> users = vehicleProfile.getAuthorizedUsers();
        if (users == null) {
            return false;
        }
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * searchClientIds.
     */
    public List<EcuClient> searchClientIds(String vehicleId, String serviceId) {
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put(Constants.DB_FIELD_NAME_VEHICLEID, vehicleId);
        searchParams.put(Constants.SEARCH_QUERY_PARAMETER_NAME_SERVICEID, serviceId);
        List<VehicleProfile> vehicleProfiles = search(searchParams);
        if (CollectionUtils.isEmpty(vehicleProfiles)) {
            return Collections.emptyList();
        }
        if (vehicleProfiles.size() > 1) {
            throw new InternalServerException(FailureReason.INTERNAL_DATA_INCONSITENT_ERROR);
        }

        Map<String, ? extends Ecu> ecus = vehicleProfiles.get(0).getEcus();
        if (ecus == null || ecus.isEmpty()) {
            return Collections.emptyList();
        }

        List<EcuClient> clientIds = new ArrayList<>();
        for (Entry<String, ? extends Ecu> ecuEntry : ecus.entrySet()) {
            if (ecuEntry.getValue() != null) {
                clientIds.add(new EcuClient(ecuEntry.getKey(), ecuEntry.getValue().getClientId()));
            }
        }
        return clientIds;

    }

    /**
     * getAssociatedVehicles.
     */
    public List<AssociatedVehicle> getAssociatedVehicles(String userId) {
        List<VehicleProfile> vehicleProfiles = dao.findAssociatedVehicles(userId);
        List<AssociatedVehicle> associatedVehicles = new ArrayList<>();
        for (VehicleProfile vehicleProfile : vehicleProfiles) {
            List<User> users = vehicleProfile.getAuthorizedUsers();
            for (User user : users) {
                if (user.getUserId().equals(userId)) {
                    associatedVehicles.add(new AssociatedVehicle(vehicleProfile.getVehicleId(), user.getRole()));
                }
            }
        }
        return associatedVehicles;
    }

    /**
     * delete by id.
     */
    public boolean delete(String vehicleId) {
        VehicleProfile profile = dao.findById(vehicleId);
        if (Optional.ofNullable(profile).isPresent()) {
            return dao.delete(profile);
        }
        throw new NotFoundException(FailureReason.DELETION_FAILED);
    }

    /**
     * delete by deviceId.
     */
    public boolean terminateDevice(String deviceId) {
        boolean deleteVehicleProfile = deleteDevice(deviceId);
        if (deleteVehicleProfile) {
            LOGGER.debug("Vehicle profile deleted successfully for deviceId: {}", deviceId);
        }
        deleteVinDetailsbyVinId(Constants.VIN, deviceId);
        deleteVinDetailsbyVinId(Constants.HCP, deviceId);
        return deleteVehicleProfile;
    }

    /**
     * deleteVinDetailsbyVinId.
     */
    public boolean deleteVinDetailsbyVinId(String vinKey, String deviceId) {
        String vinId = vinKey + deviceId;
        Map<String, String> params = new HashMap<>();
        params.put(Constants.VIN_KEY, vinId);
        List<VehicleProfile> vehicleProfileList = search(params);
        VehicleProfile vehicleProfile = null;
        if (vehicleProfileList != null && !vehicleProfileList.isEmpty() && vehicleProfileList.get(0) != null) {
            vehicleProfile = vehicleProfileList.get(0);
            if (deleteVehicleProfile(vehicleProfile)) {
                LOGGER.debug("Vehicle profile deleted using {} search for vinId: {}", vinKey, vinId);
                return true;
            } else {
                LOGGER.error("Failed to delete in vehicle profile using {} search for vin id: {}", vinKey, vinId);
            }
        } else {
            LOGGER.error("failed to get vehicle Ids for deletion in vehicle profile using {} search for vin id: {}",
                    vinKey, vinId);
        }
        return false;
    }

    /**
     * deleteVehicleProfile.
     */
    public boolean deleteVehicleProfile(VehicleProfile vehicleProfile) {
        if (Optional.ofNullable(vehicleProfile).isPresent()) {
            return dao.delete(vehicleProfile);
        }
        throw new NotFoundException(FailureReason.DELETION_FAILED);
    }

    private boolean deleteDevice(String deviceId) {
        VehicleProfile vehicleProfile = null;
        Map<String, String> params = new HashMap<>();
        params.put(Constants.DB_FIELD_NAME_CLIENTID, deviceId);
        List<VehicleProfile> vehicleProfileList = search(params);

        if (vehicleProfileList != null && !vehicleProfileList.isEmpty() && vehicleProfileList.get(0) != null) {
            vehicleProfile = vehicleProfileList.get(0);
            LOGGER.debug(VEHICLE_PROFILE_FOUND_FOR_DEVICE_ID_PROFILE, CommonUtils.maskContent(deviceId),
                    vehicleProfile);
            try {
                if (vehicleProfile.getEcus() != null && vehicleProfile.getEcus().size() == 1) {
                    LOGGER.debug("Vehicle profile delete starts");
                    return dao.delete(vehicleProfile);
                } else {
                    LOGGER.debug("Vehicle profile delete for deviceId: {}", deviceId);
                    for (Map.Entry<String, ? extends Ecu> set : vehicleProfile.getEcus().entrySet()) {
                        if (null != set.getValue().getClientId()
                                && deviceId.equalsIgnoreCase(set.getValue().getClientId())) {
                            vehicleProfile.getEcus().remove(set.getKey());
                            LOGGER.debug("Vehicle profile removed ecu: {}", set.getKey());
                            return dao.update(vehicleProfile);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Vehicle profile deletion failed for clientId {}", deviceId);
            }
        } else {
            LOGGER.error("Vehicle profile not found for clientId {}", deviceId);
        }
        return false;
    }

    /**
     * isAssociationsAllowed.
     */
    private boolean isAssociationsAllowed(VehicleProfile profile) {
        int currentAssociations = (profile == null || profile.getAuthorizedUsers() == null) ? 0
                : profile.getAuthorizedUsers().size();
        return (currentAssociations >= maxAllowedAssociations) ? false : true;
    }

    /**
     * replaceVin.
     */
    public String replaceVin(String deviceId, String newVin) throws Exception {

        VehicleProfile vehicleProfile = updateProfileWithNewVinDetails(deviceId, newVin);

        vehicleAssociationService.replaceVinForDevice(deviceId, newVin);

        dao.update(vehicleProfile);
        LOGGER.info("Successfully updated vehicle profile: {}", vehicleProfile);
        IgniteEventImpl igniteEvent = null;
        if (Constants.NOT_APPLICABLE.equalsIgnoreCase(vehicleProfile.getVehicleAttributes().getModel())) {
            igniteEvent = prepareMmyNotificationEvent(vehicleProfile, deviceId);
            sendIgniteEvent(deviceId, igniteEvent);
        }
        igniteEvent = prepareVinUpdateNotificationEvent(vehicleProfile, deviceId);
        sendIgniteEvent(deviceId, igniteEvent);
        return Constants.API_SUCCESS;
    }

    /**
     * prepareVinUpdateNotificationEvent.
     */
    private IgniteEventImpl prepareVinUpdateNotificationEvent(VehicleProfile vehicleProfile, String deviceId) {

        IgniteEventImpl eventImpl = new IgniteEventImpl();
        eventImpl.setBenchMode(Constants.BENCHMODE);

        GenericEventData eventData = new GenericEventData();
        eventData.set(Constants.VIN_KEY, vehicleProfile.getVin());

        eventImpl.setEventData(eventData);
        eventImpl.setEventId(vinUpdateNotificationId);
        eventImpl.setTimestamp(System.currentTimeMillis());
        eventImpl.setVersion(Version.V1_0);
        eventImpl.setTimezone(Constants.TIMEZONE_VALUE_300);
        eventImpl.setVehicleId(deviceId);

        LOGGER.debug("Prepared VIN update Event: {}", encryptSensitiveDataService.encryptLog(eventImpl.toString()));
        return eventImpl;
    }

    /**
     * updateProfileWithNewVinDetails.
     */
    private VehicleProfile updateProfileWithNewVinDetails(String deviceId, String newVin) {

        VehicleProfile vehicleProfile = null;
        Map<String, String> params = new HashMap<>();
        params.put(Constants.DB_FIELD_NAME_CLIENTID, deviceId);
        List<VehicleProfile> vehicleProfileList = search(params);

        if (vehicleProfileList != null && !vehicleProfileList.isEmpty() && vehicleProfileList.get(0) != null) {

            vehicleProfile = vehicleProfileList.get(0);
            LOGGER.debug(VEHICLE_PROFILE_FOUND_FOR_DEVICE_ID_PROFILE, CommonUtils.maskContent(deviceId),
                    vehicleProfile);
            vehicleProfile.setVin(newVin);
            VehicleAttributes attributes = new VehicleAttributes();

            attributes.setMake(Constants.NOT_APPLICABLE);
            attributes.setModel(Constants.NOT_APPLICABLE);
            attributes.setModelYear(Constants.NOT_APPLICABLE);
            attributes.setName(Constants.MY_CAR);
            attributes.setType(Constants.UNKNOWN);

            try {
                if (VinDecoderEnums.CODE_VALUE.getDecoderType().equals(vinDecoder)) {
                    String vehicleDetails = vinDecodeService.decodeVin(newVin, vinDecoder);
                    LOGGER.debug("Vehicle details after vin decode: {}", vehicleDetails);
                    final ObjectMapper mapper = new ObjectMapper();
                    Specification vinEventData = mapper.readValue(vehicleDetails, Specification.class);
                    attributes.setMake(vinEventData.getManufacture());
                    attributes.setModel(vinEventData.getModelName());
                } else {
                    LOGGER.info("No matching VIN decoder found");
                }
            } catch (Exception e) {
                LOGGER.error("Error occured while fetching MMY for VIN: {}", CommonUtils.maskContent(newVin), e);
            }
            vehicleProfile.setVehicleAttributes(attributes);
        } else {
            throw new NotFoundException(FailureReason.NO_VEHICLE_FOUND);
        }

        return vehicleProfile;
    }

    /**
     * Method to prepare MMY notification event.
     *
     * @param vehicleProfile - VehicleProfile
     * @param deviceId       - the vin event obtained
     * @return MMY IgniteEvent
     */
    private IgniteEventImpl prepareMmyNotificationEvent(VehicleProfile vehicleProfile, String deviceId) {

        IgniteEventImpl eventImpl = new IgniteEventImpl();
        eventImpl.setBenchMode(Constants.BENCHMODE);

        GenericEventData eventData = new GenericEventData();
        eventData.set(Constants.DUMMY_KEY, false);
        eventData.set(Constants.VALUE_KEY, vehicleProfile.getVin());

        eventImpl.setEventData(eventData);
        eventImpl.setEventId(Constants.MMY_EVENT_ID);
        eventImpl.setTimestamp(System.currentTimeMillis());
        eventImpl.setTimezone(Constants.TIMEZONE_VALUE_300);
        eventImpl.setVersion(Version.V1_0);

        LOGGER.debug("Prepared MMY Event: {}", encryptSensitiveDataService.encryptLog(eventImpl.toString()));
        return eventImpl;

    }

    /**
     * sendIgniteEvent.
     */
    private void sendIgniteEvent(String key, IgniteEventImpl igniteEvent) {
        try {
            kafkaService.sendIgniteEvent(notificationTopic, key, igniteEvent);
        } catch (ExecutionException e) {
            LOGGER.error("Exception occured while sending message", e);
        }
    }

    /**
     * Method to pass filter criteria with request body and request params.
     */
    public PageResponse<VehicleProfile> filter(VehicleProfileFilterRequest filterRequest,
                                               Map<String, String> filterOperations) {
        LOGGER.info("Inside Filter with request: {}, operations: {}",
                filterRequest != null ? filterRequest.maskedToString() : "", filterOperations);
        PageResponse<VehicleProfile> pageResponse = new PageResponse<>();
        PageResponse.RecordStats recordStats = new PageResponse.RecordStats();
        pageResponse.setRecordStats(recordStats);

        if ((filterOperations == null || filterOperations.isEmpty()) && filterRequest == null) {
            LOGGER.info("Fetching full result set");
            List<VehicleProfile> vehicleProfiles = dao.findAll();
            pageResponse.getRecordStats().setTotalRecords(vehicleProfiles.size());
            pageResponse.setContent(vehicleProfiles);
        } else {
            IgniteQuery igniteQuery = filterQueryGenerator.generateQuery(filterRequest, filterOperations);

            LOGGER.debug("Final Ignite Query: {}",
                    encryptSensitiveDataService.encryptLog(Objects.toString(igniteQuery, "")));

            pageResponse.getRecordStats().setPageNumber(igniteQuery.getPageNumber());
            pageResponse.getRecordStats().setPageSize(igniteQuery.getPageSize());

            List<VehicleProfile> vehicleProfiles = dao.find(igniteQuery);
            long count = vehicleProfiles.size();

            if (igniteQuery.getPageNumber() != 0 || igniteQuery.getPageSize() != 0) {
                filterOperations.remove("pageSize");
                filterOperations.remove("pageNumber");
                IgniteQuery igniteQueryCount = filterQueryGenerator.generateQuery(filterRequest, filterOperations);
                count = dao.countByQuery(igniteQueryCount);
            }

            pageResponse.getRecordStats().setTotalRecords(count);
            pageResponse.setContent(vehicleProfiles);
        }
        LOGGER.info("Exiting Filter");
        return pageResponse;
    }

    /**
     * isDeviceAssociatedWithUser.
     */
    private boolean isDeviceAssociatedWithUser(VehicleProfile dbVehicleProfile, String userId) {

        Map<String, Ecu> ecus = (Map<String, Ecu>) dbVehicleProfile.getEcus();
        if (ecus != null) {
            Iterator<Entry<String, Ecu>> ecuItr = ecus.entrySet().iterator();
            while (ecuItr.hasNext()) {
                Ecu ecu = ecuItr.next().getValue();
                if (ecu != null && userId.equalsIgnoreCase(
                        vehicleAssociationService.getAssociatedUserIdfromHarmanId(ecu.getClientId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get vehicle profile count by filter.
     *
     * @param filterDto filter dto
     * @return Count object
     */
    public Count getCountByFilter(FilterDto filterDto) {
        IgniteQuery igniteQuery = buildIgniteQuery(filterDto);
        int size;
        if (igniteQuery == null) {
            size = (int) dao.countAll();
        } else {
            size = (int) dao.countByQuery(igniteQuery);
        }
        LOGGER.info("Total vehicle profile count: {}", size);
        Count count = new Count();
        count.setCount(size);
        return count;
    }

    /**
     * streamByFilter.
     */
    public Flux<VehicleProfile> streamByFilter(FilterDto filterDto) {
        IgniteQuery igniteQuery = buildIgniteQuery(filterDto);
        if (igniteQuery == null) {
            return dao.streamFindAll();
        }
        return dao.streamFind(igniteQuery);
    }

    /**
     * buildIgniteQuery.
     */
    private IgniteQuery buildIgniteQuery(FilterDto filterDto) {
        IgniteQuery igniteQuery = null;
        if (!CollectionUtils.isEmpty(filterDto.getFilters())) {
            IgniteCriteriaGroup igniteCriteriaGroup;
            for (Filters filter : filterDto.getFilters()) {
                igniteCriteriaGroup = null;
                igniteCriteriaGroup = addCriteria(createCriteria("vehicleAttributes.make", filter.getMakes()),
                        igniteCriteriaGroup);
                igniteCriteriaGroup = addCriteria(createCriteria("vehicleAttributes.model", filter.getModels()),
                        igniteCriteriaGroup);
                igniteCriteriaGroup = addCriteria(createCriteria("vehicleAttributes.modelYear", filter.getYears()),
                        igniteCriteriaGroup);
                igniteCriteriaGroup = addCriteria(createCriteria("soldRegion", filter.getSoldRegions()),
                        igniteCriteriaGroup);
                igniteCriteriaGroup = addCriteria(createCriteria("vin", filter.getVins()), igniteCriteriaGroup);
                igniteCriteriaGroup = addCriteria(createCriteria("modemInfo.imei", filter.getImeis()),
                        igniteCriteriaGroup);

                if (igniteCriteriaGroup != null) {
                    if (igniteQuery == null) {
                        igniteQuery = new IgniteQuery(igniteCriteriaGroup);
                    } else {
                        igniteQuery.or(igniteCriteriaGroup);
                    }
                }
            }
        }
        return igniteQuery;
    }

    /**
     * createCriteria.
     */
    private IgniteCriteria createCriteria(String field, List<String> values) {
        if (!CollectionUtils.isEmpty(values)) {
            return new IgniteCriteria(field, Operator.IN, values);
        }
        return null;
    }

    /**
     * addCriteria.
     */
    private IgniteCriteriaGroup addCriteria(IgniteCriteria igniteCriteria, IgniteCriteriaGroup igniteCriteriaGroup) {
        if (igniteCriteria != null) {
            if (igniteCriteriaGroup == null) {
                igniteCriteriaGroup = new IgniteCriteriaGroup(igniteCriteria);
            } else {
                igniteCriteriaGroup.and(igniteCriteria);
            }
        }
        return igniteCriteriaGroup;
    }

    /**
     * updateInventory.
     */
    public boolean updateInventory(String id, Inventory inventory) {
        LOGGER.info("VehicleManager updateInventory method - disableDevAssocCheck: {}", disableDevAssocCheck);

        VehicleProfile vehicleProfile = dao.findById(id);

        if (null == vehicleProfile) {
            throw new NotFoundException(FailureReason.NO_VEHICLE_FOUND);
        }

        LOGGER.info("vehicleProfile : " + vehicleProfile);

        if (null != vehicleProfile.getVin() && !vehicleProfile.getVin().equalsIgnoreCase(inventory.getVin())) {
            LOGGER.error("Vin mismatch");
            throw new BadRequestException(FailureReason.VEHICLE_ID_VIN_CANNOT_BE_MODIFIED);
        }

        Map<String, Map<String, Map<String, InventoryScomo>>> partNumberToScomoMap = util
                .getPartNumberToScomoMap(inventory.getInventoryEcuMap());

        Map<String, Ecu> vpEcus = (Map<String, Ecu>) vehicleProfile.getEcus();
        if (Optional.ofNullable(vpEcus).isPresent()) {
            for (Entry<String, Ecu> ecu : vpEcus.entrySet()) {
                if (null != ecu && null != ecu.getValue() && null != ecu.getValue().getPartNumber()
                        && null != partNumberToScomoMap.get(ecu.getValue().getPartNumber())) {
                    ecu.getValue().setInventory(partNumberToScomoMap.get(ecu.getValue().getPartNumber()));
                    partNumberToScomoMap.remove(ecu.getValue().getPartNumber());
                }
            }
        }
        if (null != partNumberToScomoMap && null != partNumberToScomoMap.keySet()
                && !partNumberToScomoMap.keySet().isEmpty()) {
            LOGGER.warn("{} partnumber not found on vehicle profile", partNumberToScomoMap.keySet());
        }

        vehicleProfile.setUpdatedOn(new Date());
        return dao.update(vehicleProfile);
    }
}
