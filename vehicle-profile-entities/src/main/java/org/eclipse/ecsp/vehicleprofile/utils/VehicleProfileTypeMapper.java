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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VehicleProfileTypeMapper Responsible for returning the configured classes
 *         deserialzing JSON.
 */
public class VehicleProfileTypeMapper {

    private VehicleProfileTypeMapper() {}
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VehicleProfileTypeMapper.class);
    private static String typePrefix = "";

    /**
     * Set the type prefix.
     *
     * @param typePrefix the type prefix
     */
    public static void setTypePrefix(String typePrefix) {
        VehicleProfileTypeMapper.typePrefix = typePrefix;
    }


    /**
     * Get the desired type.
     *
     * @param defaultImpl the default implementation
     * @return the desired type
     */
    public static Class<?> getDesiredType(Class<?> defaultImpl) {
        if (typePrefix != null && !typePrefix.isEmpty() && !defaultImpl.getSimpleName().startsWith(typePrefix)) {
            try {
                defaultImpl = Class
                        .forName(defaultImpl.getPackage().getName() + "." + typePrefix + defaultImpl.getSimpleName());
                LOGGER.info("returning custom class {}", defaultImpl);
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }
        return defaultImpl;
    }

}
