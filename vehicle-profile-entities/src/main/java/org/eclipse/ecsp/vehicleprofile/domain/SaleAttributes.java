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
 * SaleAttributes.
 */
@Entity
@Setter
@Getter
@EqualsAndHashCode
public class SaleAttributes {
    private String dealerCode;
    private Date saleDate;
    private String eventType;
    private Date eventDate;
    private Date warrantyStartDate;
    private String marketCode;
    private String salesChannel;
    private String customerSegment;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SaleAttributes [" + (dealerCode != null ? "dealerCode=" + dealerCode + ", " : "")
                + (saleDate != null ? "saleDate=" + saleDate + ", " : "")
                + (eventType != null ? "eventType=" + eventType + ", " : "")
                + (eventDate != null ? "eventDate=" + eventDate + ", " : "")
                + (warrantyStartDate != null ? "warrantyStartDate=" + warrantyStartDate + ", " : "")
                + (marketCode != null ? "marketCode=" + marketCode + ", " : "")
                + (salesChannel != null ? "salesChannel=" + salesChannel + ", " : "")
                + (customerSegment != null ? "customerSegment=" + customerSegment : "") + "]";
    }

    /**
     * maskedToString.
     *
     * @return string
     */
    public String maskedToString() {
        return "SaleAttributes ["
                + (dealerCode != null ? "dealerCode=" + CommonUtils.maskContent(dealerCode) + ", " : "")
                + (saleDate != null ? "saleDate=" + CommonUtils.maskContent(Objects.toString(saleDate, "")) + ", " : "")
                + (eventType != null ? "eventType=" + eventType + ", " : "")
                + (eventDate != null ? "eventDate=" + eventDate + ", " : "")
                + (warrantyStartDate != null
                        ? "warrantyStartDate=" + CommonUtils.maskContent(Objects.toString(warrantyStartDate, "")) + ", "
                        : "")
                + (marketCode != null ? "marketCode=" + marketCode + ", " : "")
                + (salesChannel != null ? "salesChannel=" + salesChannel + ", " : "")
                + (customerSegment != null ? "customerSegment=" + customerSegment : "") + "]";
    }
}
