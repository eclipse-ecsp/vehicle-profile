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

package org.eclipse.ecsp.vehicleprofile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.utils.VehicleProfileTypeMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * TheApplication.
 */
@SpringBootApplication(scanBasePackages = { "org.eclipse" }, 
    excludeName = {"org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration"})
@EnableAsync
public class TheApplication implements ApplicationContextAware {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(TheApplication.class);

    @Value("${vehicleProfile.rest.ignoreUnknown.properties:false}")
    private boolean ignoreUnknownFields;

    public static void main(String[] args) {
        SpringApplication.run(TheApplication.class, args);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String typePrefix = applicationContext.getBean(Environment.class).getProperty("vehicleprofile.bean.prefix");
        if (typePrefix != null) {
            typePrefix = typePrefix.trim();
        }
        LOGGER.info("The vehicleprofile.bean.prefix that is being used is {} ", typePrefix);
        VehicleProfileTypeMapper.setTypePrefix(typePrefix);
    }

    /**
     * serializingObjectMapper.
     */
    @Bean
    @Primary
    public ObjectMapper serializingObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        if (ignoreUnknownFields) {
            objectMapper.findAndRegisterModules().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(false));
        return objectMapper;
    }

}
