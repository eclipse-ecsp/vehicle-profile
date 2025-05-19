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

package org.eclipse.ecsp.vehicleprofile.dao;


import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.nosqldao.mongodb.IgniteBaseDAOMongoImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.domain.AssociationHistory;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AssociationHistoryDao class.
 */
@Repository
public class AssociationHistoryDao extends IgniteBaseDAOMongoImpl<String, AssociationHistory> {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(AssociationHistory.class);

    /**
     * find by id.
     *
     * @param userId         string
     * @param vehicleId      string
     * @param isDisassociated boolean
     * @return list of AssociationHistory
     */
    public List<AssociationHistory> find(String userId, String vehicleId, boolean isDisassociated) {
        LOGGER.info("find userId {} vehicleId {} isDisassociated {}", CommonUtils.maskContent(userId),
                CommonUtils.maskContent(vehicleId), isDisassociated);
        IgniteCriteria userIdCriteria = new IgniteCriteria(Constants.DB_FIELD_NAME_USERID, Operator.EQ, userId);
        IgniteCriteria vehicleIdCriteria = new IgniteCriteria(Constants.DB_FIELD_NAME_VEHICLEID, Operator.EQ,
                vehicleId);
        IgniteCriteria disassociationCriteria = new IgniteCriteria(Constants.DB_FIELD_NAME_DISASSOCIATED, Operator.EQ,
                isDisassociated);
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup();
        criteriaGroup.and(userIdCriteria);
        criteriaGroup.and(vehicleIdCriteria);
        criteriaGroup.and(disassociationCriteria);
        IgniteQuery igniteQuery = new IgniteQuery(criteriaGroup);
        List<AssociationHistory> list = find(igniteQuery);
        LOGGER.info("found records {}", list);
        return list;
    }
}
