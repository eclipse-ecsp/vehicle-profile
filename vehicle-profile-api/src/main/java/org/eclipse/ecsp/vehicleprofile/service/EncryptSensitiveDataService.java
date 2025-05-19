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

package org.eclipse.ecsp.vehicleprofile.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * EncryptSensitiveDataService.
 */
@Service
public class EncryptSensitiveDataService {

    public static final int GENERATE_KEY_LENGTH = 128;
    public static final int GENERATE_IV_LENGTH = 16;
    
    @Value("${encrypt.log}")
    private String encryptLog;

    /**
     * Method to encrypt log.
     *
     * @param log - string
     * @return string
     */
    public String encryptLog(String log)  {
        if (!encryptLog.equals("") && encryptLog.equalsIgnoreCase("true")) {
            return log;
        }
        return log;
    }

    /**
     * Method to encrypt log.
     *
     * @param log - string
     * @return string
     * @throws NoSuchAlgorithmException - exception
     * @throws NoSuchPaddingException - exception
     * @throws InvalidKeyException - exception
     * @throws InvalidAlgorithmParameterException - exception
     * @throws IllegalBlockSizeException - exception
     * @throws BadPaddingException - exception
     */
    public static String encrypt(String log) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKey key = generateKey(GENERATE_KEY_LENGTH);
        IvParameterSpec ivParameterSpec = generateIv();
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
        byte[] cipherText = cipher.doFinal(log.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }


    /**
     * Method to generate key.
     *
     * @param n - int
     * @return SecretKey
     * @throws NoSuchAlgorithmException - exception
     */
    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }

    /**
     * Method to generate iv.
     *
     * @return IvParameterSpec
     */
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[GENERATE_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
