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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;

/**
 * PageResponse class.
 */
@JsonInclude(value = Include.NON_NULL)
public class PageResponse<T> {

    private List<T> content;
    private RecordStats recordStats;

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public RecordStats getRecordStats() {
        return recordStats;
    }

    public void setRecordStats(RecordStats recordStats) {
        this.recordStats = recordStats;
    }

    /**
     * RecordStats class.
     */
    public static class RecordStats {
        private int pageSize;
        private int pageNumber;
        private long totalRecords;

        /**
         * RecordStats constructor.
         */
        public RecordStats(int pageSize, int pageNumber, long totalRecords) {
            this.pageSize = pageSize;
            this.pageNumber = pageNumber;
            this.totalRecords = totalRecords;
        }

        public RecordStats() {

        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public long getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(long totalRecords) {
            this.totalRecords = totalRecords;
        }
    }
}
