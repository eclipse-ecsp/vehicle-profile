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

package org.eclipse.ecsp.vehicleprofile.filter;

import org.eclipse.ecsp.nosqldao.*;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileFilterRequest;
import org.eclipse.ecsp.vehicleprofile.search.SearchQueryGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * FilterQueryGenerator.
 */
@Component
public class FilterQueryGenerator {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(FilterQueryGenerator.class);

    @Autowired
    private SearchQueryGenerator searchQueryGenerator;

    /**
     * generateQuery.
     *
     * @param requestBody VehicleProfileFilterRequest
     * @param filterOperations Map
     * @return IgniteQuery
     */
    public IgniteQuery generateQuery(VehicleProfileFilterRequest requestBody, Map<String, String> filterOperations) {

        IgniteQuery igniteQuery;

        Map<String, String> requestMap = generateObjectMap(requestBody);

        if (requestMap.isEmpty()) {
            LOGGER.info("Request Body is null or Empty");
            igniteQuery = new IgniteQuery();
            igniteQuery.and(new IgniteCriteriaGroup(new IgniteCriteria("vin", Operator.NEQ, "")));
        } else {
            igniteQuery = searchQueryGenerator.generateQuery(requestMap, Operator.CONTAINS_IGNORE_CASE);
        }
        for (Map.Entry<String, String> filterOperation : filterOperations.entrySet()) {
            String operation = filterOperation.getKey();
            if (operation.equals("sortBy")) {
                handleSorting(filterOperation.getValue(), igniteQuery);
            }
            if (operation.equals("fields")) {
                handleProjection(filterOperation.getValue(), igniteQuery);
            }
        }

        String pageSize = filterOperations.getOrDefault("pageSize", "0");
        String pageNumber = filterOperations.getOrDefault("pageNumber", "0");
        handlePagination(pageSize, pageNumber, igniteQuery);

        LOGGER.info("Exiting generateQuery");
        return igniteQuery;
    }

    /**
     * generateObjectMap.
     *
     * @param filterRequest VehicleProfileFilterRequest
     * @return Map<String, String>
     */
    private Map<String, String> generateObjectMap(VehicleProfileFilterRequest filterRequest) {
        LOGGER.info("Inside GenerateObjectMap with filterRequest: {}",
                filterRequest != null ? filterRequest : "");
        Map<String, String> toMap = new HashMap<>();

        if (filterRequest == null) {
            return toMap;
        }

        try {
            Field[] fields = VehicleProfileFilterRequest.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getType() != String.class) {
                    continue;
                }

                String value = (String) field.get(filterRequest);
                if (value != null) {
                    toMap.put(field.getName(), value);
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        LOGGER.info("Exiting GenerateObjectMap with filterMap keys: {}", toMap.keySet());
        return toMap;
    }

    /**
     * handlePagination.
     */
    private void handlePagination(String pageSize, String pageNumber, IgniteQuery igniteQuery) {
        LOGGER.info("Inside handlePagination with pageSize:{}, pageNumber:{}", pageSize, pageNumber);
        if (StringUtils.isBlank(pageNumber) || !NumberUtils.isCreatable(pageNumber)) {
            return;
        }
        if (StringUtils.isBlank(pageSize) || !NumberUtils.isCreatable(pageSize)) {
            return;
        }
        if (pageNumber.equals("0") || pageSize.equals("0")) {
            return;
        }

        igniteQuery.setPageNumber(NumberUtils.createInteger(pageNumber));
        igniteQuery.setPageSize(NumberUtils.createInteger(pageSize));
        LOGGER.info("Exiting handlePagination");
    }

    /**
     * handleSorting.
     */
    private void handleSorting(String sortHandle, IgniteQuery igniteQuery) {
        LOGGER.info("Inside HandleSorting with sortHandle: {}", sortHandle);
        if (!StringUtils.isEmpty(sortHandle) && sortHandle.split(",").length == 1) {
            String[] sortCriteria = sortHandle.split(":");
            IgniteOrderBy igniteOrderBy = new IgniteOrderBy();
            igniteOrderBy.byfield(sortCriteria[0]);

            if (sortCriteria[1].equalsIgnoreCase(Order.ASC.toString())) {
                igniteOrderBy.asc();
            } else {
                igniteOrderBy.desc();
            }
            igniteQuery.orderBy(igniteOrderBy);
        }
    }

    /**
     * handleProjection.
     */
    private void handleProjection(String projectedFields, IgniteQuery igniteQuery) {
        LOGGER.info("Inside HandleProjection with projectFields: {}", projectedFields);
        if (!StringUtils.isEmpty(projectedFields) && projectedFields.split(",").length > 0) {
            igniteQuery.setFieldNames(projectedFields.split(","));
        }
    }
}
