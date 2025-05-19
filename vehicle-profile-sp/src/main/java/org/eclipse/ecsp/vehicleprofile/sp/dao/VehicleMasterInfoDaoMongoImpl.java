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

package org.eclipse.ecsp.vehicleprofile.sp.dao;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleMasterInfo;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VehicleMasterInfo Mongo collection implementation.
 *
 */
@Repository
public class VehicleMasterInfoDaoMongoImpl extends IgniteBaseDAOMongoImpl<String, VehicleMasterInfo> {

    /**
     * name of third party for vin decoding, none value means no decoding.
     */
    @Value("${vin.decoder}")
    private String vinDecoder;

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleMasterInfoDaoMongoImpl.class);

    /**
     * Method to obtain VehicleMasterInfo data for the Make, Model, Year to be used
     * from Mongo Collection. Data obtained will contain FuelType, DisplacementCC,
     * PowerPsAtRpm and TankCapacity which is used to formulate device message config
     * to be sent to device / dongle.
     *
     * @param vehicleProfile vehicleProfile
     * @return VehicleMasterInfo
     */
    public VehicleMasterInfo getMasterInfoData(VehicleProfile vehicleProfile) {

        if (vehicleProfile != null && vehicleProfile.getVehicleAttributes() != null
                && StringUtils.isNotBlank(vehicleProfile.getVehicleAttributes().getMake())) {
            LOGGER.info("Querying VehicleMasterInfo for Make: {} ,Model: {}, Year: {}...",
                    vehicleProfile.getVehicleAttributes().getMake(), vehicleProfile.getVehicleAttributes().getModel(),
                    vehicleProfile.getVehicleAttributes().getModelYear());

            IgniteCriteriaGroup cg = new IgniteCriteriaGroup();

            IgniteCriteria makeCriteria = new IgniteCriteria();
            makeCriteria = makeCriteria.field(SpCommonConstants.MAKE).op(Operator.EQI)
                    .val(vehicleProfile.getVehicleAttributes().getMake());
            cg.and(makeCriteria);

            IgniteCriteria modelCriteria = new IgniteCriteria();
            modelCriteria = modelCriteria.field(SpCommonConstants.MODEL).op(Operator.EQI)
                    .val(vehicleProfile.getVehicleAttributes().getModel());
            cg.and(modelCriteria);

            if (!StringUtils.isBlank(vehicleProfile.getVehicleAttributes().getModelYear())
                    || !SpCommonConstants.CODEVALUE_DECODER.equalsIgnoreCase(vinDecoder)) {

                IgniteCriteria yearCriteria = new IgniteCriteria();
                yearCriteria = yearCriteria.field(SpCommonConstants.YEAR).op(Operator.EQI)
                        .val(vehicleProfile.getVehicleAttributes().getModelYear());
                cg.and(yearCriteria);

            }

            if (vehicleProfile.getVehicleAttributes().getFuelType() != null) {

                IgniteCriteria fuelCriteria = new IgniteCriteria();
                fuelCriteria = fuelCriteria.field(SpCommonConstants.DB_FUEL_TYPE).op(Operator.EQ)
                        .val(vehicleProfile.getVehicleAttributes().getFuelType());
                cg.and(fuelCriteria);

            }
            
            IgniteQuery query = new IgniteQuery();
            query.and(cg);

            List<VehicleMasterInfo> masterInfoLst = this.find(query);
            if (!masterInfoLst.isEmpty()) {
                LOGGER.info("MasterInfo returned result: {}", masterInfoLst.get(0));
                return masterInfoLst.get(0);
            }

            LOGGER.info("No records found for the MMY ....");
        }

        return null;
    }
}
