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


import org.eclipse.ecsp.nosqldao.IgniteQuery;
import org.eclipse.ecsp.nosqldao.Operator;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileFilterRequest;
import org.eclipse.ecsp.vehicleprofile.search.SearchQueryGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * FilterQueryGeneratorTest.
 */
@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations = "classpath:application-test-base.properties")
public class FilterQueryGeneratorTest {

    @InjectMocks
    FilterQueryGenerator filterQueryGenerator;
    @Mock
    SearchQueryGenerator searchQueryGenerator;

    @Before
    public void setup() {
        Mockito.when(searchQueryGenerator.generateQuery(Mockito.anyMap(), Mockito.any(Operator.class)))
                .thenReturn(new IgniteQuery());
    }

    @Test
    public void testGenerateQuery() {

        Map<String, String> filterParams = new HashMap<>();
        filterParams.put("sortBy", "vin:asc");
        filterParams.put("fields", "vin,dealer");

        IgniteQuery query = filterQueryGenerator.generateQuery(null, filterParams);
        Assert.assertNotNull(query);
        
        VehicleProfileFilterRequest filterRequest = new VehicleProfileFilterRequest();
        query = filterQueryGenerator.generateQuery(filterRequest, filterParams);
        Assert.assertNotNull(query);

        filterRequest.setVin("abc");

        // case1: no page size, pagenumber and fields query
        query = filterQueryGenerator.generateQuery(filterRequest, filterParams);
        Assert.assertNotNull(query);

        // case2: pagenumber is empty
        filterParams.put("pageNumber", "");
        query = filterQueryGenerator.generateQuery(filterRequest, filterParams);
        Assert.assertNotNull(query);

        // case3: pagesize is empty
        filterParams.put("pageSize", "");
        filterParams.remove("pageNumber");
        query = filterQueryGenerator.generateQuery(filterRequest, filterParams);
        Assert.assertNotNull(query);

        // case4: pagenumber is illegal
        filterParams.put("pageNumber", "a");
        query = filterQueryGenerator.generateQuery(filterRequest, filterParams);
        Assert.assertNotNull(query);

        // case5: pagesize is illegal
        filterParams.put("pageSize", "b");
        filterParams.remove("pageNumber");
        query = filterQueryGenerator.generateQuery(filterRequest, filterParams);
        Assert.assertNotNull(query);

        // case5: pagesize and pagenumber in query
        filterParams.put("pageSize", "10");
        filterParams.put("pageNumber", "2");
        query = filterQueryGenerator.generateQuery(filterRequest, filterParams);
        Assert.assertNotNull(query);

        // case6: desc orderby field
        filterParams.put("sortBy", "vin:desc");
        query = filterQueryGenerator.generateQuery(filterRequest, filterParams);
        Assert.assertNotNull(query);
    }
}
