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

/**
 * ModemInfo.
 */
@Entity
@EqualsAndHashCode
@Setter
@Getter
public class ModemInfo {
    private String eid;
    private String iccid;
    private String imei;
    private String msisdn;
    private String imsi;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ModemInfo [eid=" + eid + ", iccid=" + iccid + ", imei=" + imei + ", msisdn=" + msisdn + ", imsi=" + imsi
                + "]";
    }

    /**
     * maskedToString.
     *
     * @return string
     */
    public String maskedToString() {
        return "ModemInfo [" + (eid != null ? "eid=" + CommonUtils.maskContent(eid) + ", " : "")
                + (iccid != null ? "iccid=" + CommonUtils.maskContent(iccid) + ", " : "")
                + (imei != null ? "imei=" + CommonUtils.maskContent(imei) + ", " : "")
                + (msisdn != null ? "msisdn=" + CommonUtils.maskContent(msisdn) + ", " : "")
                + (imsi != null ? "imsi=" + CommonUtils.maskContent(imsi) + ", " : "") + "]";
    }
}
