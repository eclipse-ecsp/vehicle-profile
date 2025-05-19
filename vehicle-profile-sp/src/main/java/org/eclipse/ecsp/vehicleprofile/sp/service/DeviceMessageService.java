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

package org.eclipse.ecsp.vehicleprofile.sp.service;

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.DeviceInfo;
import org.eclipse.ecsp.vehicleprofile.domain.DeviceMessageData;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleMasterInfo;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleServiceRecord;
import org.eclipse.ecsp.vehicleprofile.sp.dao.DeviceInfoDaoMongoImpl;
import org.eclipse.ecsp.vehicleprofile.sp.dao.VehicleMasterInfoDaoMongoImpl;
import org.eclipse.ecsp.vehicleprofile.sp.dao.VehicleServiceRecordDaoMongoImpl;
import org.eclipse.ecsp.vehicleprofile.sp.utils.ChecksumGenerator;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class to hold device message specific logic for vehicle profile. It will have
 * logic to query, check and prepare device message config message to be sent
 * back to the dongle / device.
 *
 *
 */
@Service
public class DeviceMessageService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(DeviceMessageService.class);

    @Value("${default.fuel.type}")
    private Integer defaultFuelType;

    @Value("${default.power.ps}")
    private Integer defaultPowerPs;

    @Value("${default.tank.capacity}")
    private Integer defaultTankCapacity;

    @Value("${default.displacement.cc}")
    private Integer defaultDisplacementCc;

    /**
     * VehicleMasterInfo Mongo impl instance.
     */
    @Autowired
    private VehicleMasterInfoDaoMongoImpl masterInfoDao;

    /**
     * DeviceInfo Mongo impl instance.
     */
    @Autowired
    private DeviceInfoDaoMongoImpl deviceInfoDao;

    /**
     * ServiceRecord Mongo impl instance.
     */
    @Autowired
    private VehicleServiceRecordDaoMongoImpl serviceRecordDao;

    @Autowired
    private ChecksumGenerator checksumGenerator;

    /**
     * Method to get the device message data for the vehicle profile.
     *
     * @param vehicleProfile the vehicle profile
     * @param deviceId       the device id
     * @return the device message data
     */
    public DeviceMessageData getDeviceMessageData(VehicleProfile vehicleProfile, String deviceId) {

        VehicleMasterInfo masterInfo = masterInfoDao.getMasterInfoData(vehicleProfile);
        Map<String, Object> vehicleProfileData = buildVehicleProfileDmData(masterInfo,
                vehicleProfile.getVehicleAttributes().getFuelType(), deviceId);

        DeviceMessageData deviceMessageData = prepareDmaEvent(deviceId, vehicleProfileData);
        if (vehicleProfile.getEpiddbChecksum() != null) {
            deviceMessageData.setEpiddbChecksum(vehicleProfile.getEpiddbChecksum());
        } else {
            deviceMessageData.setEpiddbChecksum(checksumGenerator.checksum(SpCommonConstants.DEFAULT_PIDDB_DATA));
        }
        return deviceMessageData;
    }

    private DeviceMessageData prepareDmaEvent(String deviceId, Map<String, Object> vehicleProfileData) {
        DeviceMessageData data = new DeviceMessageData();

        data.setDisplacementCc((Integer) vehicleProfileData.get(SpCommonConstants.DISPLACEMENT_CC));
        data.setTankCapacity((Integer) vehicleProfileData.get(SpCommonConstants.TANK_CAPACITY));
        data.setFuelType((Integer) vehicleProfileData.get(SpCommonConstants.FUEL_TYPE));
        data.setPowerPsAtRpm((Integer) vehicleProfileData.get(SpCommonConstants.POWER_PS_AT_RPM));
        data.setServiceMaintenanceId((String) vehicleProfileData.get(SpCommonConstants.SERVICE_MAINTENANCE_ID));

        return data;
    }

    /**
     * Method to prepare the data map which will be used in device message config to
     * be sent to device / dongle.
     *
     */
    private Map<String, Object> buildVehicleProfileDmData(VehicleMasterInfo vehicleMasterInfo, Integer fuelType,
            String deviceId) {
        if (Optional.ofNullable(vehicleMasterInfo).isPresent()) {

            Map<String, Object> vehicleProfileData = new HashMap<>();
            if (fuelType == null || fuelType.intValue() == 0) {
                if (vehicleMasterInfo.getFuelType() == 0) {
                    vehicleProfileData.put(SpCommonConstants.FUEL_TYPE, defaultFuelType);
                } else {
                    vehicleProfileData.put(SpCommonConstants.FUEL_TYPE, vehicleMasterInfo.getFuelType());
                }
            } else {
                vehicleProfileData.put(SpCommonConstants.FUEL_TYPE, fuelType);
            }
            vehicleProfileData.put(SpCommonConstants.DISPLACEMENT_CC, vehicleMasterInfo.getEngineDisplacement());
            vehicleProfileData.put(SpCommonConstants.POWER_PS_AT_RPM, vehicleMasterInfo.getPowerpsatrpm());
            vehicleProfileData.put(SpCommonConstants.TANK_CAPACITY, vehicleMasterInfo.getTankcapacity());
            vehicleProfileData.put(SpCommonConstants.SERVICE_MAINTENANCE_ID,
                    getServiceMaintenanceIdToBeSentToDevice(deviceId));
            return vehicleProfileData;
        } else {
            LOGGER.info("MMY absent for vehicle-profile. Using default values.");
            Map<String, Object> vehicleProfileData = new HashMap<>();
            vehicleProfileData.put(SpCommonConstants.FUEL_TYPE, defaultFuelType);
            vehicleProfileData.put(SpCommonConstants.DISPLACEMENT_CC, defaultDisplacementCc);
            vehicleProfileData.put(SpCommonConstants.POWER_PS_AT_RPM, defaultPowerPs);
            vehicleProfileData.put(SpCommonConstants.TANK_CAPACITY, defaultTankCapacity);
            vehicleProfileData.put(SpCommonConstants.SERVICE_MAINTENANCE_ID,
                    getServiceMaintenanceIdToBeSentToDevice(deviceId));
            return vehicleProfileData;
        }
    }
    /**
     * Method to get the service maintenance id to be sent to device.
     *
     * @param deviceId the device id
     * @return the service maintenance id
     */
    public String getServiceMaintenanceIdToBeSentToDevice(String deviceId) {
        if (Optional.ofNullable(deviceId).isPresent() && isMileageImprovementAvailable(deviceId)) {
            VehicleServiceRecord vehicleServiceRecord = serviceRecordDao.findTop1ByPdIdOrderByServiceDateDesc(deviceId);
            if (Optional.ofNullable(vehicleServiceRecord).isPresent()) {
                LOGGER.info("Obtained service records for the device ...");
                return vehicleServiceRecord.getId().toString();
            }
        }

        return SpCommonConstants.NOT_APPLICABLE;
    }

    /**
     * Method to check for device info data and the availability of the flag for
     * mileage improvement data.
     * 
     */
    private boolean isMileageImprovementAvailable(String deviceId) {

        DeviceInfo deviceInfo = deviceInfoDao.findById(deviceId);
        if (deviceInfo != null && deviceInfo.getIsMileageImprovementAvailable() != null) {
            LOGGER.info("Device Info obtained with data: {}", deviceInfo);
            return deviceInfo.getIsMileageImprovementAvailable();
        }
        return false;
    }

}
