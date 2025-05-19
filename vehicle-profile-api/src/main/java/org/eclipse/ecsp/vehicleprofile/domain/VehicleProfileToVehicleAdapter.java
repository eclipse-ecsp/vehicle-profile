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

package org.eclipse.ecsp.vehicleprofile.domain;

import org.eclipse.ecsp.events.vehicleprofile.constants.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * VehicleProfileToVehicleAdapter class.
 */
public class VehicleProfileToVehicleAdapter implements Vehicle {

    private VehicleProfile vehicleProfile;

    /**
     * VehicleProfileToVehicleAdapter constructor.
     */
    public VehicleProfileToVehicleAdapter(VehicleProfile vehicleProfile) {
        if (null == vehicleProfile) {
            throw new RuntimeException("vehicleProfile object cannot be null");
        }
        this.vehicleProfile = vehicleProfile;
    }

    @SuppressWarnings("unused")
    private VehicleProfileToVehicleAdapter() {
    }

    @Override
    public String getVin() {
        return vehicleProfile.getVin();
    }

    @Override
    public String getVehicleId() {
        return vehicleProfile.getVehicleId();
    }

    @Override
    public VehicleAttributes getVehicleAttributes() {
        return vehicleProfile.getVehicleAttributes();
    }

    @Override
    public Set<Application> getCapabilities() {
        Map<String, Ecu> ecus = (Map<String, Ecu>) vehicleProfile.getEcus();
        Set<Application> capabilities = new HashSet<>();
        if (Optional.ofNullable(ecus).isPresent()) {
            for (Entry<String, Ecu> ecu : ecus.entrySet()) {
                if (null != ecu.getValue().getCapabilities()
                        && null != ecu.getValue().getCapabilities().getServices()) {
                    capabilities.addAll(ecu.getValue().getCapabilities().getServices());
                }
            }
        }
        return capabilities;
    }

    @Override
    public Set<Application> getProvisionedServices() {
        Map<String, Ecu> ecus = (Map<String, Ecu>) vehicleProfile.getEcus();
        Set<Application> provisionedServices = new HashSet<>();

        if (Optional.ofNullable(ecus).isPresent()) {
            for (Entry<String, Ecu> ecu : ecus.entrySet()) {
                if (null != ecu.getValue().getProvisionedServices()
                        && null != ecu.getValue().getProvisionedServices().getServices()) {
                    provisionedServices.addAll(ecu.getValue().getProvisionedServices().getServices());
                }
            }
        }
        return provisionedServices;
    }

    @Override
    public List<User> getAuthorizedUsers() {
        return vehicleProfile.getAuthorizedUsers();
    }

    @Override
    public Map<String, Map<String, String>> getCustomParams() {
        if (vehicleProfile.getCustomParams() == null) {
            return null;
        }

        HashMap<String, Map<String, String>> customParams = new HashMap<String, Map<String, String>>(
                vehicleProfile.getCustomParams());

        Iterator<Entry<String, Map<String, String>>> itr = customParams.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, Map<String, String>> entry = itr.next();
            if (!canBeExposed(entry)) {
                itr.remove();
            }
        }

        return customParams;
    }

    private boolean canBeExposed(Entry<String, Map<String, String>> entry) {
        return (entry.getValue() != null
                && Boolean.valueOf(entry.getValue().get(Constants.CUSTOM_PARAMS_EXTERNAL_PROPERTIES_IDENTIFIER))
                && null != entry.getValue().remove(Constants.CUSTOM_PARAMS_EXTERNAL_PROPERTIES_IDENTIFIER)) ? true
                        : false;
    }

    @Override
    public Boolean isDummy() {
        return vehicleProfile.isDummy();
    }

    @Override
    public Map<String, ? extends AuthorizedPartner> getAuthorizedPartners() {
        return vehicleProfile.getAuthorizedPartners();
    }
}