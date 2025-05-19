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

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.IgniteEntity;

/**
 * VehicleMasterInfo.
 */
@Entity("vehicleMasterInfo")
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class VehicleMasterInfo implements IgniteEntity {
    @Id
    private ObjectId id;

    @Property("fueltype")
    private int fuelType;

    private String make;

    private String model;

    private String year;

    @Property("enginedisplacement")
    private Integer engineDisplacement;

    private Integer powerpsatrpm;

    private Integer tankcapacity;

    @Override
    public Version getSchemaVersion() {
        return null;
    }

    @Override
    public void setSchemaVersion(Version version) {

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VehicleMasterInfo other = (VehicleMasterInfo) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "VehicleMasterInfo [id=" + id + ", fuelType=" + fuelType + ", make=" + make + ", model=" + model
                + ", year=" + year + ", engineDisplacement=" + engineDisplacement + ", powerpsatrpm=" + powerpsatrpm
                + ", tankcapacity=" + tankcapacity + "]";
    }

}
