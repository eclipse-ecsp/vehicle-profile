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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

/**
 * VehicleServiceRecord.
 */
@Entity(value = "servicerecords")
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class VehicleServiceRecord implements IgniteEntity {

    @Id
    private ObjectId id;

    @Property(value = "pdId")
    private String pdId;

    @Property(value = "serviceDate")
    private long serviceDate;

    @Override
    public String toString() {
        return "VehicleServiceRecord [id=" + id + ", pdId=" 
              + CommonUtils.maskContent(pdId) + ", serviceDate=" + serviceDate + "]";
    }

    @Override
    public Version getSchemaVersion() {
        return null;
    }

    @Override
    public void setSchemaVersion(Version version) {
        // Not setting version as not required.
    }

}
