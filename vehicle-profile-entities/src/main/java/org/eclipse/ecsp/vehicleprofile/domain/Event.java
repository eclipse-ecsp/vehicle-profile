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
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Event.
 */

@Entity
@Getter
@Setter
public class Event {

    private String eventId;
    private Date eventDate;
    private Date lastReceivedOn;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Event [eventId=" + eventId + ", eventDate=" + eventDate + ", lastReceivedOn=" + lastReceivedOn + "]";
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventDate == null) ? 0 : eventDate.hashCode());
        result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
        result = prime * result + ((lastReceivedOn == null) ? 0 : lastReceivedOn.hashCode());
        return result;
    }
    
    /* (non-Javadoc)
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
        Event other = (Event) obj;
        if (eventDate == null) {
            if (other.eventDate != null) {
                return false;
            }
        } else if (!eventDate.equals(other.eventDate)) {
            return false;
        }
        if (eventId == null) {
            if (other.eventId != null) {
                return false;
            }
        } else if (!eventId.equals(other.eventId)) {
            return false;
        }
        if (lastReceivedOn == null) {
            if (other.lastReceivedOn != null) {
                return false;
            }
        } else if (!lastReceivedOn.equals(other.lastReceivedOn)) {
            return false;
        }
        return true;
    }

}
