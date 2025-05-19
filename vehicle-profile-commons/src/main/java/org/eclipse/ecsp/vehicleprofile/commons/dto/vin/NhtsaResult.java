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

package org.eclipse.ecsp.vehicleprofile.commons.dto.vin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * NhtsaResult.
 *
 */
public class NhtsaResult implements Serializable {

    private static final long serialVersionUID = -5759707078479756228L;

    @JsonProperty(value = "Value")
    private String value;

    @JsonProperty(value = "ValueId")
    private String valueId;

    @JsonProperty(value = "Variable")
    private String variable;

    @JsonProperty(value = "VariableId")
    private int variableId;

    /**
     * Get the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the value id.
     *
     * @return the value id
     */
    public String getValueId() {
        return valueId;
    }

    /**
     * Sets the value id.
     *
     * @param valueId the new value id
     */
    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    /**
     * Get the variable.
     *
     * @return the variable
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Sets the variable.
     *
     * @param variable the new variable
     */
    public void setVariable(String variable) {
        this.variable = variable;
    }

    /**
     * Get the variable id.
     *
     * @return the variable id
     */
    public int getVariableId() {
        return variableId;
    }

    /**
     * Sets the variable id.
     *
     * @param variableId the new variable id
     */
    public void setVariableId(int variableId) {
        this.variableId = variableId;
    }

}
