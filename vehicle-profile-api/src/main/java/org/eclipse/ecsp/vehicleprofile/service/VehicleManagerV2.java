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

import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.dao.VehicleDao;
import org.eclipse.ecsp.vehicleprofile.domain.Ecu;
import org.eclipse.ecsp.vehicleprofile.domain.User;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.enums.ApiMessageEnum;
import org.eclipse.ecsp.vehicleprofile.exception.ApiResourceNotFoundException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiTechnicalException;
import org.eclipse.ecsp.vehicleprofile.exception.ApiValidationFailedException;
import org.eclipse.ecsp.vehicleprofile.exception.FailureReason;
import org.eclipse.ecsp.vehicleprofile.exception.InternalServerException;
import org.eclipse.ecsp.vehicleprofile.exception.NotFoundException;
import org.eclipse.ecsp.vehicleprofile.rest.mapping.AssociatedVehicle;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.eclipse.ecsp.vehicleprofile.utils.Util;
import org.eclipse.ecsp.vehicleprofile.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * VehicleManagerV2 service.
 */
@Service
public class VehicleManagerV2 {

    @Autowired
    private VehicleDao dao;

    @Autowired
    private Util util;

    @Value("#{'${allowed.device.types}'.split(',')}")
    private Set<String> allowedEcus;

    @Value("${vehicleprofile.auto.generate.vehicleId}")
    private boolean autoGenerateVehicleId;

    @Autowired
    private VehicleAssociationService vehicleAssociationService;

    @Value("${vehicle.association.base.url}")
    private String deviceAssociateBaseUrl;

    // Added as part of US 295583.
    @Value("${disable.dev.assoc.check}")
    private String disableDevAssocCheck;

    public static final int PAGE_SIZE = 100;
    
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleManagerV2.class);

    /**
     * createVehicle.
     */
    public String createVehicle(VehicleProfile profile) {
        LOGGER.debug("create vehicle {}", profile);
        if (null != profile.getVehicleId()) {

            throw new ApiValidationFailedException(ApiMessageEnum.VEHICLE_ID_IS_SYSTEM_CREATED.getCode(),
                    ApiMessageEnum.VEHICLE_ID_IS_SYSTEM_CREATED.getMessage(),
                    ApiMessageEnum.VEHICLE_ID_IS_SYSTEM_CREATED.getGeneralMessage());
        } else {
            String vin = profile.getVin();
            if ((null == vin) || (vin.length() == 0)) {

                throw new ApiValidationFailedException(ApiMessageEnum.VIN_MUST_BE_PROVIDED.getCode(),
                        ApiMessageEnum.VIN_MUST_BE_PROVIDED.getMessage(),
                        ApiMessageEnum.VIN_MUST_BE_PROVIDED.getGeneralMessage());
            } else if (!findByVin(vin).isEmpty()) {

                throw new ApiValidationFailedException(ApiMessageEnum.VEHICLE_WITH_VIN_ALREADY_EXIST.getCode(),
                        ApiMessageEnum.VEHICLE_WITH_VIN_ALREADY_EXIST.getMessage(),
                        ApiMessageEnum.VEHICLE_WITH_VIN_ALREADY_EXIST.getGeneralMessage());
            } else if (!util.hasValidEcus(profile)) {

                throw new ApiValidationFailedException(ApiMessageEnum.DOES_NOT_HAVE_VALID_ECUS.getCode(),
                        ApiMessageEnum.DOES_NOT_HAVE_VALID_ECUS.getMessage(),
                        ApiMessageEnum.DOES_NOT_HAVE_VALID_ECUS.getGeneralMessage());
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
            throw new ApiTechnicalException(ApiMessageEnum.CREATION_FAILED.getCode(),
                    ApiMessageEnum.CREATION_FAILED.getMessage(), ApiMessageEnum.CREATION_FAILED.getGeneralMessage());
        }

        return id;
    }

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
     * get vehicleProfile object.
     */
    public Object get(String id, String path) {
        LOGGER.info("Get id {}, path {}", CommonUtils.maskContent(id), path);
        VehicleProfile profile = dao.findById(id);
        if (null == profile) {
            throw new ApiResourceNotFoundException(ApiMessageEnum.NO_VEHICLE_FOUND.getCode(),
                    ApiMessageEnum.NO_VEHICLE_FOUND.getMessage(), ApiMessageEnum.NO_VEHICLE_FOUND.getGeneralMessage());
        }
        String[] nodes = Utils.getNodesFromPath(path, id);
        if (null != nodes) {
            return Utils.readNodeValue(profile, nodes);
        }

        return profile;
    }

    /**
     * put vehicleProfile details.
     */
    public boolean put(VehicleProfile vehicleProfile) {
        VehicleProfile dbVehicleProfile = dao.findById(vehicleProfile.getVehicleId());
        if (dbVehicleProfile == null) {

            throw new ApiResourceNotFoundException(ApiMessageEnum.NO_VEHICLE_FOUND.getCode(),
                    ApiMessageEnum.NO_VEHICLE_FOUND.getMessage(), ApiMessageEnum.NO_VEHICLE_FOUND.getGeneralMessage());
        } else if (!vehicleProfile.getVin().equals(dbVehicleProfile.getVin())) {

            throw new ApiValidationFailedException(ApiMessageEnum.VIN_DOES_NOT_MATCH_WITH_EXISTING_RECORD.getCode(),
                    ApiMessageEnum.VIN_DOES_NOT_MATCH_WITH_EXISTING_RECORD.getMessage(),
                    ApiMessageEnum.VIN_DOES_NOT_MATCH_WITH_EXISTING_RECORD.getGeneralMessage());
        } else if (!util.hasValidEcus(vehicleProfile)) {

            throw new ApiValidationFailedException(ApiMessageEnum.DOES_NOT_HAVE_VALID_ECUS.getCode(),
                    ApiMessageEnum.DOES_NOT_HAVE_VALID_ECUS.getMessage(),
                    ApiMessageEnum.DOES_NOT_HAVE_VALID_ECUS.getGeneralMessage());

        }
        util.checkEmptyNullEcuType(vehicleProfile, dbVehicleProfile);
        return dao.update(vehicleProfile);
    }

    /**
     * update vehicleProfile.
     */
    public boolean update(VehicleProfile vehicleProfile, String userId) {
        LOGGER.info("VehicleManagerV2 update method - disableDevAssocCheck: {}", disableDevAssocCheck);
        if (!util.hasValidEcus(vehicleProfile)) {

            throw new ApiValidationFailedException(ApiMessageEnum.DOES_NOT_HAVE_VALID_ECUS.getCode(),
                    ApiMessageEnum.DOES_NOT_HAVE_VALID_ECUS.getMessage(),
                    ApiMessageEnum.DOES_NOT_HAVE_VALID_ECUS.getGeneralMessage());
        }

        VehicleProfile dbVehicleProfile = dao.findById(vehicleProfile.getVehicleId());

        if (dbVehicleProfile == null) {

            throw new ApiResourceNotFoundException(ApiMessageEnum.NO_VEHICLE_FOUND.getCode(),
                    ApiMessageEnum.NO_VEHICLE_FOUND.getMessage(), ApiMessageEnum.NO_VEHICLE_FOUND.getGeneralMessage());
        }

        // Added as part of US 295583. Skip device association check/call if
        // disableHCPCalls is set to true.
        if (!Boolean.valueOf(disableDevAssocCheck)) {
            LOGGER.info("VehicleManagerV2 update method - performing association check");
            if (StringUtils.isNotBlank(userId) && !isDeviceAssociatedWithUser(dbVehicleProfile, userId)) {

                throw new ApiValidationFailedException(ApiMessageEnum.VEHICLE_PROFILE_DOES_NOT_BELONG_TO_USER.getCode(),
                        ApiMessageEnum.VEHICLE_PROFILE_DOES_NOT_BELONG_TO_USER.getMessage(),
                        ApiMessageEnum.VEHICLE_PROFILE_DOES_NOT_BELONG_TO_USER.getGeneralMessage());
            }
        }
        util.checkEmptyNullEcuType(vehicleProfile, dbVehicleProfile);
        // Don't update the schema version, createdOn.
        vehicleProfile.setSchemaVersion(null);
        vehicleProfile.setCreatedOn(null);
        vehicleProfile.setUpdatedOn(new Date());
        try {

            return dao.getAndUpdate(vehicleProfile);

        } catch (InternalServerException e) {

            throw new ApiTechnicalException(ApiMessageEnum.UPDATE_FAILED.getCode(),
                    ApiMessageEnum.UPDATE_FAILED.getMessage(), ApiMessageEnum.UPDATE_FAILED.getGeneralMessage());

        }

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
     * getAssociatedVehiclesData.
     */
    public List<AssociatedVehicle> getAssociatedVehiclesData(String userId, String pageNumber, String pageSize,
            String sortby, String orderby) {
        LOGGER.info("Fetching vehicle data for user {}", CommonUtils.maskContent(userId));
        pageNumber = (null == pageNumber || "0".equals(pageNumber) || StringUtils.isBlank(pageNumber))
                ? String.valueOf(1)
                : pageNumber;
        if (null == pageSize || "0".equals(pageSize) || StringUtils.isBlank(pageSize)) {
            pageSize = String.valueOf(PAGE_SIZE);
        } else if (Integer.parseInt(pageSize) > PAGE_SIZE) {
            LOGGER.warn("Page size is greater than 100, changing page size to 100");
            pageSize = String.valueOf(PAGE_SIZE);
        }
        LOGGER.debug("pageNumber {}, pageSize {}, sortBy {}, orderBy {}", pageNumber, pageSize, sortby, orderby);
        List<VehicleProfile> vehicleProfiles = dao.findAssociatedVehiclesData(userId, pageNumber, pageSize, sortby,
                orderby);
        if (null == vehicleProfiles || vehicleProfiles.isEmpty()) {
            throw new NotFoundException(FailureReason.NO_VEHICLE_FOUND);
        }
        LOGGER.info("VP data fetched {} ", vehicleProfiles);
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
}
