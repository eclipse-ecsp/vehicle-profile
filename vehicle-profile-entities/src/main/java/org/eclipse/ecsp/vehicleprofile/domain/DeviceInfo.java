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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;

/**
 * DeviceInfo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(value = "deviceinfo")
public class DeviceInfo implements IgniteEntity {

    @Id
    private String id;

    @Property(value = "isMileageImprovementAvailable")
    private Boolean isMileageImprovementAvailable;

    /**
     * Get device id.
     *
     * @return string
     */
    public String getId() {
        return id;
    }

    /**
     * Set device id.
     *
     * @param id string
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get mileage improvement availability.
     *
     * @return boolean
     */
    public Boolean getIsMileageImprovementAvailable() {
        return isMileageImprovementAvailable;
    }

    /**
     * Set mileage improvement availability.
     *
     * @param isMileageImprovementAvailable boolean
     */
    public void setIsMileageImprovementAvailable(Boolean isMileageImprovementAvailable) {
        this.isMileageImprovementAvailable = isMileageImprovementAvailable;
    }

    @Override
    public String toString() {
        return "DeviceInfo [id=" + CommonUtils.maskContent(id) 
              + ", isMileageImprovementAvailable=" + isMileageImprovementAvailable + "]";
    }

    @Override
    public Version getSchemaVersion() {
        return null;
    }

    @Override
    public void setSchemaVersion(Version version) {
        // Not setting version
    }
}
