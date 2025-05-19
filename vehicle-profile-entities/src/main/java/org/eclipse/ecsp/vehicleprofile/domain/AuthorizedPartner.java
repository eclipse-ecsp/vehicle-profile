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
import java.util.Map;

/**
 * AuthorizedPartner.
 */
@Entity
public class AuthorizedPartner {
    private String partnerId;
    private Map<String, ? extends ServiceClaim> serviceClaims;

    /**
     * Get partner id.
     *
     * @return string
     */
    public String getPartnerId() {
        return partnerId;
    }

    /**
     * Set the partner id.
     *
     * @param partnerId string
     */
    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    /**
     * Get service claims.
     *
     * @return map
     */
    public Map<String, ? extends ServiceClaim> getServiceClaims() {
        return serviceClaims;
    }

    /**
     * Set the service claims.
     *
     * @param serviceClaims map
     */
    public void setServiceClaims(Map<String, ? extends ServiceClaim> serviceClaims) {
        this.serviceClaims = serviceClaims;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((partnerId == null) ? 0 : partnerId.hashCode());
        result = prime * result + ((serviceClaims == null) ? 0 : serviceClaims.hashCode());
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
        AuthorizedPartner other = (AuthorizedPartner) obj;
        if (partnerId == null) {
            if (other.partnerId != null) {
                return false;
            }
        } else if (!partnerId.equals(other.partnerId)) {
            return false;
        }
        if (serviceClaims == null) {
            if (other.serviceClaims != null) {
                return false;
            }
        } else if (!serviceClaims.equals(other.serviceClaims)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AuthorizedPartner [" + (partnerId != null ? "partnerId=" + partnerId + ", " : "")
                + (serviceClaims != null ? "serviceClaims=" + serviceClaims : "") + "]";
    }

}
