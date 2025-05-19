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

package org.eclipse.ecsp.vehicleprofile.controller;

import org.eclipse.ecsp.vehicleprofile.domain.Count;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.service.VehicleManager;
import org.eclipse.ecsp.vehicleprofile.utils.ApiResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * VehicleProfileControllerTest class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class VehicleProfileControllerTest {

    @InjectMocks
    private VehicleProfileController vehicleProfileController;

    public static final int COUNT = 10;
    public static final int SUCCESS_CODE = 200;
    
    @Mock
    private VehicleManager vehicleManager;

    @BeforeEach
    void beforeEach() {
        openMocks(this);
    }

    @Test
    public void getCountByFilter() {
        Count expectedCount = new Count();
        expectedCount.setCount(COUNT);
        Mockito.doReturn(expectedCount).when(vehicleManager).getCountByFilter(Mockito.any());
        ResponseEntity<ApiResponse<Count>> response = vehicleProfileController.getCountByFilter(null);
        Assert.assertEquals(SUCCESS_CODE, response.getStatusCode().value());
        Assert.assertEquals(COUNT, Objects.requireNonNull(response.getBody()).getData().getCount());
    }

    @Test
    public void streamByFilter() {
        VehicleProfile vehicle = new VehicleProfile();
        vehicle.setVehicleId(UUID.randomUUID().toString());
        Flux<VehicleProfile> vehicleManagerResponse = Flux.fromIterable(Collections.singletonList(vehicle));
        Mockito.doReturn(vehicleManagerResponse).when(vehicleManager).streamByFilter(Mockito.any());
        Flux<VehicleProfile> response = vehicleProfileController.streamByFilter(null);
        Assert.assertEquals(1, response.toStream().count());
    }
}
