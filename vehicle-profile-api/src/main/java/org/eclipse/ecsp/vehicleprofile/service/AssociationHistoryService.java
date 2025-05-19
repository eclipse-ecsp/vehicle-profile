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

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.dao.AssociationHistoryDao;
import org.eclipse.ecsp.vehicleprofile.domain.AssociationHistory;
import org.eclipse.ecsp.vehicleprofile.domain.TermsAndConditions;
import org.eclipse.ecsp.vehicleprofile.domain.User;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * AssociationHistoryService.
 */
@Service
public class AssociationHistoryService {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(AssociationHistoryService.class);
    
    @Autowired
    private AssociationHistoryDao associationHistoryDao;

    /**
     * logHistory.
     *
     * @param vehicleId string
     * @param user User
     * @param reason string
     * @param source string
     * @param operation AssociationOperation
     */
    public void logHistory(String vehicleId, User user, String reason, String source, AssociationOperation operation) {
        LOGGER.debug("logHistory vehicleId {} user {} reason {} operations - Start", CommonUtils.maskContent(vehicleId),
                user, reason, operation);
        List<AssociationHistory> historyRecords = associationHistoryDao.find(user.getUserId(), vehicleId, false);
        AssociationHistory associationHistory = null;
        if (CollectionUtils.isNotEmpty(historyRecords)) {
            LOGGER.info("found a associationHistory in db {}", historyRecords);
            associationHistory = historyRecords.get(0);
        } else {
            // most likely, first time association
            associationHistory = new AssociationHistory();
            associationHistory.setUserId(user.getUserId());
            associationHistory.setVehicleId(vehicleId);
            associationHistory.setCreatedOn(new Date());
            associationHistory.setId(new ObjectId().toString());
            updateLegalDocsInHistory(associationHistory, user);
            associationHistoryDao.save(associationHistory);
            LOGGER.info("successfully saved associationHistory for the first time {}", associationHistory);
            return;
        }
        associationHistory.setUpdatedOn(new Date());

        if (operation.equals(AssociationOperation.UPDATED)) {
            // user either accepted or decline TCs, hence updating in history
            updateLegalDocsInHistory(associationHistory, user);
            associationHistoryDao.update(associationHistory);
            LOGGER.info("successfully saved associationHistory for UPDATE {}, received user {}", associationHistory,
                    user);
        } else if (operation.equals(AssociationOperation.DELETED)) {
            associationHistory.setReason(reason);
            associationHistory.setDisassociated(true);
            associationHistory.setSource(source);
            updateEndOfLegalDocs(associationHistory);
            LOGGER.info("successfully saved associationHistory for DELETE {}", associationHistory);
            associationHistoryDao.update(associationHistory);
        }
        LOGGER.debug("logHistory vehicleId {} user {} reason {} operations {} - End",
                CommonUtils.maskContent(vehicleId), user, reason, operation);
    }

    /**
     * updateEndOfLegalDocs.
     *
     * @param associationHistory AssociationHistory
     */
    public void updateEndOfLegalDocs(AssociationHistory associationHistory) {

        if (CollectionUtils.isNotEmpty(associationHistory.getTc())) {
            for (TermsAndConditions tc : associationHistory.getTc()) {
                if (tc.getEndOfAcceptance() == null) {
                    tc.setEndOfAcceptance(new Date());
                }
            }
        }

        if (CollectionUtils.isNotEmpty(associationHistory.getPp())) {
            for (TermsAndConditions pp : associationHistory.getPp()) {
                if (pp.getEndOfAcceptance() == null) {
                    pp.setEndOfAcceptance(new Date());
                }
            }
        }

    }

    /**
     * updateLegalDocsInHistory.
     *
     * @param associationHistory AssociationHistory
     * @param user User
     */
    public void updateLegalDocsInHistory(AssociationHistory associationHistory, User user) {
        if (MapUtils.isEmpty(user.getTc()) && MapUtils.isEmpty(user.getPp())) {
            return;
        }

        if (associationHistory.getTc() == null) {
            associationHistory.setTc(new ArrayList<>());
        }

        if (associationHistory.getPp() == null) {
            associationHistory.setPp(new ArrayList<>());
        }
        Map<String, TermsAndConditions> receivedTcs = user.getTc();
        if (MapUtils.isNotEmpty(user.getTc())) {
            for (Entry<String, TermsAndConditions> tcEntry : receivedTcs.entrySet()) {
                TermsAndConditions tc = tcEntry.getValue();
                tc.setName(tcEntry.getKey());
                associationHistory.getTc().add(tc);
                // if (tc.getStatus().equals(Constants.TC_STATUS_DISAGREE)) {
                // updateEndOfTCsByType(associationHistory, tc.getName());
                // }
            }
        }

        if (MapUtils.isNotEmpty(user.getPp())) {
            for (Entry<String, TermsAndConditions> ppEntry : user.getPp().entrySet()) {
                ppEntry.getValue().setName(ppEntry.getKey());
                associationHistory.getPp().add(ppEntry.getValue());

            }
        }
    }

    // void updateEndOfTCsByType(AssociationHistory associationHistory, String
    // tc) {
    // if (tc == null || associationHistory == null ||
    // associationHistory.getTc() == null) {
    // return;
    // }
    // Date date = new Date();
    // for (TermsAndConditions acceptedTc : associationHistory.getTc()) {
    // if (tc.equals(acceptedTc.getName()) && acceptedTc.getEndOfAcceptance() ==
    // null) {
    // acceptedTc.setEndOfAcceptance(date);
    // }
    // }
    // }

    /**
     * AssociationOperation.
     */
    public static enum AssociationOperation {
        CREATED, UPDATED, DELETED;
    }
}
