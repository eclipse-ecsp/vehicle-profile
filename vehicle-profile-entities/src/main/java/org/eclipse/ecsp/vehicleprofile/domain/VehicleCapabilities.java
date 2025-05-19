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

import dev.morphia.annotations.Entity;

import java.util.List;

/**
 * VehicleCapabilities.
 */
@Entity
public class VehicleCapabilities {
    // source from where event is being sent i.e. Dongle or HeadUnit
    private String sourceType;
    // list of pids
    private List<String> pids;
    private List<String> useCases;

    /**
     * Get the source type.
     *
     * @return string
     */
    public String getSourceType() {
        return sourceType;
    }

    /**
     * Set the source type.
     *
     * @param sourceType string
     */
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * Get pids.
     *
     * @return list
     */
    public List<String> getPids() {
        return pids;
    }

    /**
     * Set the pids.
     *
     * @param pids list
     */
    public void setPids(List<String> pids) {
        this.pids = pids;
    }

    /**
     * Get use cases.
     *
     * @return list
     */
    public List<String> getUseCases() {
        return useCases;
    }

    /**
     * Set the use cases.
     *
     * @param useCases list
     */
    public void setUseCases(List<String> useCases) {
        this.useCases = useCases;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "VehicleCapabilities [sourceType=" + sourceType + ", pids=" + pids + ", useCases=" + useCases + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pids == null) ? 0 : pids.hashCode());
        result = prime * result + ((useCases == null) ? 0 : useCases.hashCode());
        result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        VehicleCapabilities other = (VehicleCapabilities) obj;
        if (pids == null) {
            if (other.pids != null) {
                return false;
            }
        } else if (!pids.equals(other.pids)) {
            return false;
        }
        if (sourceType == null) {
            if (other.sourceType != null) {
                return false;
            }
        } else if (!sourceType.equals(other.sourceType)) {
            return false;
        }
        if (useCases == null) {
            if (other.useCases != null) {
                return false;
            }
        } else if (!useCases.equals(other.useCases)) {
            return false;
        }
        return true;
    }
}
