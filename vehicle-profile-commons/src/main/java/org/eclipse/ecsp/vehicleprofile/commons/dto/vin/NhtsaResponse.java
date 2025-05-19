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
import java.util.List;

/**
 * NhtsaResponse.
 *
 */
public class NhtsaResponse implements Serializable {

    private static final long serialVersionUID = 168062459489781519L;

    @JsonProperty(value = "Count")
    private int count;

    @JsonProperty(value = "Message")
    private String message;

    @JsonProperty(value = "SearchCriteria")
    private String searchCriteria;

    @JsonProperty(value = "Results")
    private List<NhtsaResult> results;

    /**
     * Get the count.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Set the count.
     *
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Get the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message.
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the search criteria.
     *
     * @return the search criteria
     */
    public String getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * Set the search criteria.
     *
     * @param searchCriteria the search criteria to set
     */
    public void setSearchCriteria(String searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    /**
     * Get the results.
     *
     * @return the results
     */
    public List<NhtsaResult> getResults() {
        return results;
    }

    /**
     * Set the results.
     *
     * @param results the results to set
     */
    public void setResults(List<NhtsaResult> results) {
        this.results = results;
    }

}
