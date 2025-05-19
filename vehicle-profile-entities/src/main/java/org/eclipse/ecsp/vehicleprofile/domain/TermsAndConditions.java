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
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

/**
 * TermsAndConditions.
 */
@EqualsAndHashCode
@Entity
@Setter
@Getter
public class TermsAndConditions {
    private String name;
    private String status;
    private String countryCode;
    private String version;
    private Date lastAcceptedOn; // we are not maintaining the history here, for
    // simplicity.
    private Date endOfAcceptance;
    private int offset;

    @Override
    public String toString() {
        return "TermsAndConditions [" + (name != null ? "name=" + name + ", " : "")
                + (status != null ? "status=" + CommonUtils.maskContent(status) + ", " : "")
                + (countryCode != null ? "countryCode=" + CommonUtils.maskContent(countryCode) + ", " : "")
                + (version != null ? "version=" + CommonUtils.maskContent(version) + ", " : "")
                + (lastAcceptedOn != null
                        ? "lastAcceptedOn=" + CommonUtils.maskContent(Objects.toString(lastAcceptedOn, "")) + ", "
                        : "")
                + (endOfAcceptance != null
                        ? "endOfAcceptance=" + CommonUtils.maskContent(Objects.toString(endOfAcceptance, "")) + ", "
                        : "")
                + "offset=" + offset + "]";
    }

}
