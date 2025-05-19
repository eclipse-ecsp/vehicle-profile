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


import org.eclipse.ecsp.nosqldao.IgniteCriteria;
import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SimpleRequestQueryBuilder.
 */
@Component
public class SimpleRequestQueryBuilder
        implements SearchRequestQueryBuilder, ApplicationListener<ContextRefreshedEvent> {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(SimpleRequestQueryBuilder.class);
    private static Map<String, String> requestQueryToCollectionQueryMapper = new HashMap<String, String>();

    @Autowired
    private SearchQueryGenerator searchQueryGenerator;
    
    static {
        requestQueryToCollectionQueryMapper.put("msisdn", "modemInfo.msisdn");
    }

    @Override
    public IgniteCriteriaGroup build(String requestQueryKey, String requestQueryValue) {
        String dbQuery = requestQueryToCollectionQueryMapper.get(requestQueryKey);
        if (null == dbQuery) {
            dbQuery = requestQueryKey;
        }
        LOGGER.info("serach query {} : {}", dbQuery, CommonUtils.maskContent(requestQueryValue));
        return new IgniteCriteriaGroup(new IgniteCriteria(dbQuery, Operator.EQ, requestQueryValue));
    }

    @Override
    public IgniteCriteriaGroup build(String requestQueryKey, String requestQueryValue, Operator operator) {
        String dbQuery = requestQueryToCollectionQueryMapper.get(requestQueryKey);
        if (null == dbQuery) {
            dbQuery = requestQueryKey;
        }
        LOGGER.info("serach query {} : {}", dbQuery, CommonUtils.maskContent(requestQueryValue));
        return new IgniteCriteriaGroup(new IgniteCriteria(dbQuery, operator, requestQueryValue));
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOGGER.info("received context refresh envent {}, registering search queries", event);
        searchQueryGenerator.register(SearchQueryGenerator.DEFAULT_QUERY_BUILDER, this, false);
        Iterator<Entry<String, String>> itr = requestQueryToCollectionQueryMapper.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, String> queryEntry = itr.next();
            searchQueryGenerator.register(queryEntry.getKey(), this, false);
        }
    }

}