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

import lombok.Getter;
import lombok.Setter;
import org.eclipse.ecsp.domain.Version;
import org.eclipse.ecsp.entities.IgniteEntity;
import org.eclipse.ecsp.vehicleprofile.utils.CommonUtils;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.Indexes;

import java.util.Date;
import java.util.List;

/**
 * AssociationHistory.
 */
@Entity(value = "associationHistory")
@Indexes({
    @Index(fields = { @Field(value = "vehicleId"), @Field(value = "userId"), @Field(value = "disassociated") }) })

@Getter
@Setter
public class AssociationHistory implements IgniteEntity {

    private Version version = Version.V1_0;
    @Id
    private String id;
    private String userId;
    private String vehicleId;
    private String reason;
    private String source;
    private boolean disassociated;
    private Date createdOn;
    private Date updatedOn;
    private List<TermsAndConditions> tc;
    private List<TermsAndConditions> pp;

    @Override
    public Version getSchemaVersion() {
        return version;
    }

    @Override
    public void setSchemaVersion(Version version) {
        this.version = version;

    }

    /**
     * To string.
     *
     * @return string
     */
    @Override
    public String toString() {
        return "AssociationHistory [" + (version != null ? "version=" + version + ", " : "")
                + (id != null ? "id=" + CommonUtils.maskContent(id) + ", " : "")
                + (userId != null ? "userId=" + CommonUtils.maskContent(userId) + ", " : "")
                + (vehicleId != null ? "vehicleId=" + CommonUtils.maskContent(vehicleId) + ", " : "")
                + (reason != null ? "reason=" + reason + ", " : "") + (source != null ? "source=" + source + ", " : "")
                + "disassociated=" + disassociated + ", " + (createdOn != null ? "createdOn=" + createdOn + ", " : "")
                + (updatedOn != null ? "updatedOn=" + updatedOn + ", " : "") + (tc != null ? "tc=" + tc + ", " : "")
                + (pp != null ? "pp=" + pp : "") + "]";
    }
}
