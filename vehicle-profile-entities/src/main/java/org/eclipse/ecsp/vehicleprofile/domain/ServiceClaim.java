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

import java.util.Date;

/**
 * ServiceClaim.
 */
@Entity
public class ServiceClaim {
    private Date start;
    private Date expire;

    /**
     * Get start date.
     *
     * @return date
     */
    public Date getStart() {
        return start;
    }

    /**
     * Set the start date.
     *
     * @param start date
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * Get expire date.
     *
     * @return date
     */
    public Date getExpire() {
        return expire;
    }

    /**
     * Set the expiry date.
     *
     * @param expire date
     */
    public void setExpire(Date expire) {
        this.expire = expire;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expire == null) ? 0 : expire.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
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
        ServiceClaim other = (ServiceClaim) obj;
        if (expire == null) {
            if (other.expire != null) {
                return false;
            }
        } else if (!expire.equals(other.expire)) {
            return false;
        }
        if (start == null) {
            if (other.start != null) {
                return false;
            }
        } else if (!start.equals(other.start)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "ServiceClaim [" + (start != null ? "start=" + start + ", " : "")
                + (expire != null ? "expire=" + expire : "") + "]";
    }
}
