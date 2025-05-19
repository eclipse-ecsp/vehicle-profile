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

package org.eclipse.ecsp.vehicleprofile.utils;

import jakarta.annotation.PostConstruct;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfile;
import org.eclipse.ecsp.vehicleprofile.domain.VehicleProfileToVehicleAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *  Responsible for configuring and providing appropriate
 *  adapter for VehicleProfile to Vehicle.
 */
@Component
public class VehicleProfileToVehicleAdapterProvider {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory
            .getLogger(VehicleProfileToVehicleAdapterProvider.class);
    @Value("${vehicleprofile.vehicle.adapter.class}")
    private String adapterClass;
    private Constructor<?> adapterConstructor;

    @PostConstruct
    private void init() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        LOGGER.debug("initializing provider with adapter class {}", adapterClass);
        if (null != adapterClass && !adapterClass.trim().isEmpty()) {
            Class<?> adapterclass;
            adapterclass = Class.forName(adapterClass);
            adapterConstructor = adapterclass.getConstructor(VehicleProfile.class);
        }
    }

    /**
     * VehicleProfileToVehicleAdapter.
     */
    public VehicleProfileToVehicleAdapter getAdapter(VehicleProfile vehicleProfile) {
        if (adapterConstructor == null) {
            return new VehicleProfileToVehicleAdapter(vehicleProfile);
        }

        try {
            return (VehicleProfileToVehicleAdapter) adapterConstructor.newInstance(vehicleProfile);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            LOGGER.error("exception occured while creating the adapter instance", e);
        }
        return null;
    }

}
