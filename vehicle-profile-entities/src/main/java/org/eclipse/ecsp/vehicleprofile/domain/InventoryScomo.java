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

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;

/**
 * InventoryScomo.
 */
@Entity
public class InventoryScomo {

    @JsonProperty(value = "scomo_id")
    private String scomoId;

    @JsonProperty(value = "version")
    private String version;

    /**
     * Get scomo id.
     *
     * @return string
     */
    public String getScomoId() {
        return scomoId;
    }

    /**
     * Set scomo id.
     *
     * @param scomoId string
     */
    public void setScomoId(String scomoId) {
        this.scomoId = scomoId;
    }

    /**
     * Get version.
     *
     * @return string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set version.
     *
     * @param version string
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "InventoryScomo [scomoId=" + scomoId + ", version=" + version + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((scomoId == null) ? 0 : scomoId.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        InventoryScomo other = (InventoryScomo) obj;
        if (scomoId == null) {
            if (other.scomoId != null) {
                return false;
            }
        } else if (!scomoId.equals(other.scomoId)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}