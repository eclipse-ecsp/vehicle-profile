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

import org.eclipse.ecsp.vehicleprofile.test.utils.MongoServer;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.test.context.TestPropertySource;

/**
 * The suit class, that runs all test classes which has dependency on Mongo.
 */
@RunWith(Suite.class)
@TestPropertySource("classpath:application-test-base.properties")
@SuiteClasses({  VehicleManagerTest.class, VehicleDaoDefaultOemTest.class, UserV2Test.class })
public class TestSuite {
    @ClassRule
    public static MongoServer MongoServer = new MongoServer();
    
    private TestSuite() {}

}
