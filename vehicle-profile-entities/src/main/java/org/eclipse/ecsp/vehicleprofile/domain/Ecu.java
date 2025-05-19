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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Ecu.
 */
@ToString(exclude = {"serialNo", "clientId"})
@EqualsAndHashCode
@Entity
@Setter
@Getter
public class Ecu {
    private String swVersion;
    private String hwVersion;
    private String partNumber;
    private String os;
    private String screenSize;
    private String manufacturer;
    private String ecuType;
    private String serialNo;
    private String clientId;
    private Capabilities capabilities;
    private ProvisionedServices provisionedServices;
    private Map<String, Map<String, InventoryScomo>> inventory;
}
