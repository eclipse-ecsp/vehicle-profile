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

package org.eclipse.ecsp.vehicleprofile.sp.utils;

import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * ChecksumGenerator class.
 */
@Component
public class ChecksumGenerator {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(ChecksumGenerator.class);

    /**
     * Generates a checksum for the given object using SHA-256 algorithm.
     *
     * @param obj the object to generate checksum for
     * @return the checksum as a string
     */
    public String checksum(Object obj) {
        if (obj == null) {
            obj = BigInteger.ZERO;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        MessageDigest m = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();

            m = MessageDigest.getInstance("SHA-256");
            m.update(baos.toByteArray());
        } catch (Exception e) {
            LOGGER.error("Exception while generating checksum");
            return null;
        }
        return new BigInteger(1, m.digest()).toString();
    }
}
