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

package org.eclipse.ecsp.vehicleprofile.search;

import jakarta.annotation.PostConstruct;
import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * EcuSearchRequestQueryBuilder.
 */
@Component
public class EcuSearchRequestQueryBuilder
        implements SearchRequestQueryBuilder, ApplicationListener<ContextRefreshedEvent> {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(EcuSearchRequestQueryBuilder.class);
    private Map<String, List<String>> requestQueryToCollectionQueryMapper = new HashMap<String, List<String>>();
    private static final String SEARCHREQUEST_BY_CLIENTID = "clientId";
    private static final String SEARCHREQUEST_BY_SERIALNO = "serialNo";
    private static final String ECU_DB_SEARCH_BY_CLIENTID = "ecus.%s.clientId";
    private static final String ECU_DB_SEARCH_BY_SERIALNO = "ecus.%s.serialNo";
    private static final String ECU_DB_SEARCH_BY_SERVICEID = "ecus.%s.provisionedServices.services.applicationId";

    @Autowired
    private SearchQueryGenerator searchQueryGenerator;

    @Value("#{'${allowed.device.types}'.split(',')}")
    private Set<String> allowedEcus;

    @PostConstruct
    private void populateSearchQueries() {
        requestQueryToCollectionQueryMapper.put(SEARCHREQUEST_BY_CLIENTID, new ArrayList<String>());
        requestQueryToCollectionQueryMapper.put(SEARCHREQUEST_BY_SERIALNO, new ArrayList<String>());
        requestQueryToCollectionQueryMapper.put(Constants.SEARCH_QUERY_PARAMETER_NAME_SERVICEID,
                new ArrayList<String>());

        for (String ecu : allowedEcus) {
            requestQueryToCollectionQueryMapper.get(SEARCHREQUEST_BY_CLIENTID)
                    .add(String.format(ECU_DB_SEARCH_BY_CLIENTID, ecu));
            requestQueryToCollectionQueryMapper.get(SEARCHREQUEST_BY_SERIALNO)
                    .add(String.format(ECU_DB_SEARCH_BY_SERIALNO, ecu));
            requestQueryToCollectionQueryMapper.get(Constants.SEARCH_QUERY_PARAMETER_NAME_SERVICEID)
                    .add(String.format(ECU_DB_SEARCH_BY_SERVICEID, ecu));
        }
    }

    @Override
    public IgniteCriteriaGroup build(String requestQueryKey, String requestQueryValue) {
        List<String> dbQueries = requestQueryToCollectionQueryMapper.get(requestQueryKey);
        if (dbQueries == null) {
            return null;
        }
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup();
        for (String dbQuery : dbQueries) {
            criteriaGroup.or(new IgniteCriteria(dbQuery, Operator.EQ, requestQueryValue));
            LOGGER.info("Generating search request {} : {}", dbQuery, CommonUtils.maskContent(requestQueryValue));
        }
        return criteriaGroup;
    }

    @Override
    public IgniteCriteriaGroup build(String requestQueryKey, String requestQueryValue, Operator operator) {
        List<String> dbQueries = requestQueryToCollectionQueryMapper.get(requestQueryKey);
        if (dbQueries == null) {
            return null;
        }
        IgniteCriteriaGroup criteriaGroup = new IgniteCriteriaGroup();
        for (String dbQuery : dbQueries) {
            criteriaGroup.or(new IgniteCriteria(dbQuery, operator, requestQueryValue));
            LOGGER.info("Generating search request {} : {}", dbQuery, CommonUtils.maskContent(requestQueryValue));
        }
        return criteriaGroup;
    }
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOGGER.info("received context refresh envent {}, registering search queries", event);
        searchQueryGenerator.register(SEARCHREQUEST_BY_CLIENTID, this, false);
        searchQueryGenerator.register(SEARCHREQUEST_BY_SERIALNO, this, false);
        searchQueryGenerator.register(Constants.SEARCH_QUERY_PARAMETER_NAME_SERVICEID, this, false);
    }

}