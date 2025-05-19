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

import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import dev.morphia.annotations.Entity;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Map;

/**
 * User.
 */
@Entity
@EqualsAndHashCode
public class User {
    private String userId;
    private String role = "VEHICLE_OWNER"; // for now, it is always
    // "VEHICLE_OWNER"
    private String source;
    private String status;
    private Map<String, TermsAndConditions> tc;
    private Map<String, TermsAndConditions> pp;
    private Date createdOn;
    private Date updatedOn;

    /**
     * User.
     *
     * @param userId string
     */
    public User(String userId) {
        super();
        this.userId = userId;
    }

    /**
     * User.
     */
    public User() {
        super();
    }

    /**
     * Get user id.
     *
     * @return string
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user id.
     *
     * @param userId string
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get role.
     *
     * @return string
     */
    public String getRole() {
        return role;
    }

    /**
     * Get created on.
     *
     * @return date
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * Set the created on.
     *
     * @param createdOn date
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Set the role.
     *
     * @param role string
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Get terms and conditions.
     *
     * @return map
     */
    public Map<String, TermsAndConditions> getTc() {
        return tc;
    }

    /**
     * Set terms and conditions.
     *
     * @param tc map
     */
    public void setTc(Map<String, TermsAndConditions> tc) {
        this.tc = tc;
    }

    /**
     * Get status.
     *
     * @return string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the status.
     *
     * @param status string
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get source.
     *
     * @return string
     */
    public String getSource() {
        return source;
    }

    /**
     * Set the source.
     *
     * @param source string
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Get updated on.
     *
     * @return date
     */
    public Date getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Set the updated on.
     *
     * @param updatedOn date
     */
    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    /**
     * Get privacy policy.
     *
     * @return map
     */
    public Map<String, TermsAndConditions> getPp() {
        return pp;
    }

    /**
     * Set privacy policy.
     *
     * @param pp map
     */
    public void setPp(Map<String, TermsAndConditions> pp) {
        this.pp = pp;
    }

    @Override
    public String toString() {
        return "User [" + (userId != null ? "userId=" + CommonUtils.maskContent(userId) + ", " : "")
                + (role != null ? "role=" + role + ", " : "") + (source != null ? "source=" + source + ", " : "")
                + (status != null ? "status=" + status + ", " : "") + (tc != null ? "tc=" + tc + ", " : "")
                + (pp != null ? "pp=" + pp + ", " : "") + (createdOn != null ? "createdOn=" + createdOn + ", " : "")
                + (updatedOn != null ? "updatedOn=" + updatedOn : "") + "]";
    }
}
