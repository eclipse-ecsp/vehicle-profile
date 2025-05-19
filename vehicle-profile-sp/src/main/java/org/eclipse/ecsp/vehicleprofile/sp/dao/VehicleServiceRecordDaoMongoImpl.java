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

import org.eclipse.ecsp.nosqldao.*;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleServiceRecord;
import org.eclipse.ecsp.vehicleprofile.sp.utils.SpCommonConstants;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ServiceRecords Mongo collection implementation.
 *
 *
 */
@Repository
public class VehicleServiceRecordDaoMongoImpl extends IgniteBaseDAOMongoImpl<String, VehicleServiceRecord> {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleServiceRecordDaoMongoImpl.class);

    /**
     * Method to filter vehicle service record.
     *
     * @param pdId string
     * @return VehicleServiceRecord
     */
    public VehicleServiceRecord findTop1ByPdIdOrderByServiceDateDesc(String pdId) {
        LOGGER.info("Fetching the latest Service record for PDId:{} ", CommonUtils.maskContent(pdId));

        IgniteCriteriaGroup cg = new IgniteCriteriaGroup();

        IgniteCriteria makeCriteria = new IgniteCriteria();
        makeCriteria = makeCriteria.field(SpCommonConstants.PD_ID).op(Operator.EQ).val(pdId);
        cg.and(makeCriteria);

        IgniteCriteria modelCriteria = new IgniteCriteria();
        modelCriteria = modelCriteria.field(SpCommonConstants.SERVICE_DATE).op(Operator.LT)
                .val(System.currentTimeMillis());
        cg.and(modelCriteria);

        IgniteOrderBy orderBy = new IgniteOrderBy();
        orderBy.byfield(SpCommonConstants.SERVICE_DATE).desc();
        IgniteQuery query = new IgniteQuery();
        query.and(cg);
        query.orderBy(orderBy);
        query.setPageSize(1);

        List<VehicleServiceRecord> obj = find(query);
        if (Optional.ofNullable(obj).isPresent() && !obj.isEmpty()) {
            LOGGER.debug("Service record obtained for PdId: {} with service record id :{} and data: {}",
                    CommonUtils.maskContent(pdId), obj.get(0).getId(), obj.get(0));
            return obj.get(0);
        }

        return null;
    }

}