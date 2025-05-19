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

import org.eclipse.ecsp.nosqldao.IgniteCriteriaGroup;
import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.constants.Constants;
import org.eclipse.ecsp.vehicleprofile.service.EncryptSensitiveDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Responsible for building db query for a provided search
 *         query parameter.
 */
@Component
public class SearchQueryGenerator {

    @Autowired
    private EncryptSensitiveDataService encryptSensitiveDataService;

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(SearchQueryGenerator.class);
    private Map<String, SearchRequestQueryBuilder> requestQueryToCollectionMapper
                               = new HashMap<String, SearchRequestQueryBuilder>();
    public static final String DEFAULT_QUERY_BUILDER = "DEFAULT_QUERY_BUILDER";

    /**
     * register key.
     */
    public void register(String searchKey, SearchRequestQueryBuilder queryBuilder, boolean override) {
        if (!override && requestQueryToCollectionMapper.containsKey(searchKey)) {
            return;
        }
        requestQueryToCollectionMapper.put(searchKey, queryBuilder);
    }

    /**
     * It generate the criteria group for search parameters It currently support
     * simple search with EQ operator, in future it needs to be extended to support
     * different logical operators.
     */
    public IgniteQuery generateQuery(Map<String, String> searchParameters) {
        IgniteQuery igniteQuery = new IgniteQuery();

        String operator = searchParameters.remove(Constants.LOGICAL_OPERATOR_KEY);

        for (Entry<String, String> entry : searchParameters.entrySet()) {
            SearchRequestQueryBuilder queryBuilder = requestQueryToCollectionMapper.get(entry.getKey());
            if (queryBuilder == null) {
                queryBuilder = requestQueryToCollectionMapper.get(DEFAULT_QUERY_BUILDER);
            }
            IgniteCriteriaGroup igniteCriteriaGroup = queryBuilder.build(entry.getKey(), entry.getValue());
            if (igniteCriteriaGroup != null) {
                if (Constants.OR_OPERATOR.equalsIgnoreCase(operator)) {
                    igniteQuery.or(igniteCriteriaGroup);
                } else {
                    igniteQuery.and(igniteCriteriaGroup);
                }

            }
        }
        LOGGER.debug("Generated Query {} for search keys{}",
                encryptSensitiveDataService.encryptLog(igniteQuery.toString()), searchParameters.keySet());
        return igniteQuery;
    }

    /**
     * It generate the criteria group for search parameters It currently support
     * simple search with EQ operator, in future it needs to be extended to support
     * different logical operators.
     */
    public IgniteQuery generateQuery(Map<String, String> searchParameters, Operator operator) {
        IgniteQuery igniteQuery = new IgniteQuery();

        String queryOperator = searchParameters.remove(Constants.LOGICAL_OPERATOR_KEY);

        for (Entry<String, String> entry : searchParameters.entrySet()) {
            SearchRequestQueryBuilder queryBuilder = requestQueryToCollectionMapper.get(entry.getKey());
            if (queryBuilder == null) {
                queryBuilder = requestQueryToCollectionMapper.get(DEFAULT_QUERY_BUILDER);
            }
            IgniteCriteriaGroup igniteCriteriaGroup = queryBuilder.build(entry.getKey(), entry.getValue(), operator);
            if (igniteCriteriaGroup != null) {
                if (Constants.OR_OPERATOR.equalsIgnoreCase(queryOperator)) {
                    igniteQuery.or(igniteCriteriaGroup);
                } else {
                    igniteQuery.and(igniteCriteriaGroup);
                }

            }
        }
        LOGGER.debug("Generated Query {} for search keys{}",
                encryptSensitiveDataService.encryptLog(igniteQuery.toString()), searchParameters.keySet());
        return igniteQuery;
    }

}
