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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * VehicleProfileWebConfig class.
 */
@Configuration
@EnableWebMvc
public class VehicleProfileWebConfig implements WebMvcConfigurer {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileWebConfig.class);
    @Value("${vehicleProfile.logging.enable.mdc:true}")
    private boolean enableMdcLogging;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (enableMdcLogging) {
            LOGGER.info("mdc logging enabled");
            registry.addInterceptor(new HandlerInterceptor() {
                public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                        throws Exception {

                    Utils.mdc(request);
                    return true;
                }
            });
        }
    }
}
