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

package org.eclipse.ecsp.vehicleprofile;

import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.domain.AssociationHistory;
import org.eclipse.ecsp.vehicleprofile.domain.TermsAndConditions;
import org.eclipse.ecsp.vehicleprofile.domain.User;
import org.eclipse.ecsp.vehicleprofile.service.AssociationHistoryService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * AssociationHistoryServiceTest class.
 */
public class AssociationHistoryServiceTest {

    private AssociationHistoryService service = new AssociationHistoryService();

    public static final int TERMS_AND_CONDITIONS_SIZE = 4;
    
    @Test
    public void testUpdateEndOfTcs() {
        AssociationHistory associationHistory = getHistoryMockData();
        service.updateEndOfLegalDocs(associationHistory);
        assertNotNull(associationHistory.getTc().get(0).getEndOfAcceptance());
        assertNotNull(associationHistory.getTc().get(1).getEndOfAcceptance());
    }

    @Test
    public void testUpdateEndOfTcsForNullInHistory() {
        AssociationHistory associationHistory = getHistoryMockData();
        associationHistory.setTc(null);
        service.updateEndOfLegalDocs(associationHistory);
        assertNull(associationHistory.getTc());
    }

    @Test
    public void testUpdateTcsInHistory() {
        User user = new User();
        Map<String, TermsAndConditions> map = new HashMap<String, TermsAndConditions>();
        AssociationHistory associationHistory = getHistoryMockData();
        map.put(associationHistory.getTc().get(0).getName(), associationHistory.getTc().get(0));
        map.put(associationHistory.getTc().get(1).getName(), associationHistory.getTc().get(1));
        user.setTc(map);

        service.updateLegalDocsInHistory(associationHistory, user);
        assertEquals(TERMS_AND_CONDITIONS_SIZE, associationHistory.getTc().size());

    }

    private User getUserMockData() {
        User user = new User();
        Map<String, TermsAndConditions> map = new HashMap<String, TermsAndConditions>();
        AssociationHistory associationHistory = getHistoryMockData();
        map.put(associationHistory.getTc().get(0).getName(), associationHistory.getTc().get(0));
        map.put(associationHistory.getTc().get(1).getName(), associationHistory.getTc().get(1));
        user.setTc(map);
        return user;
    }

    private AssociationHistory getHistoryMockData() {
        TermsAndConditions tc = new TermsAndConditions();
        tc.setName("activation");
        tc.setCountryCode("US");
        tc.setStatus(Constants.TC_STATUS_AGREE);
        TermsAndConditions tc2 = new TermsAndConditions();
        tc2.setName("registration");
        tc2.setCountryCode("IN");
        tc2.setStatus(Constants.TC_STATUS_AGREE);
        List<TermsAndConditions> tcs = new ArrayList<>();
        tcs.add(tc);
        tcs.add(tc2);
        AssociationHistory associationHistory = new AssociationHistory();
        associationHistory.setTc(tcs);
        return associationHistory;
    }
}
