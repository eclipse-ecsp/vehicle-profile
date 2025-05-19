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

package org.eclipse.ecsp.vehicleprofile.utils;

import jakarta.annotation.PreDestroy;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.service.EncryptSensitiveDataService;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * VPKafkaService.
 */
@Service
@ConditionalOnProperty(name = "vehicleProfile.kafka.enabled", matchIfMissing = true)
public class VpKafkaService {
    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VpKafkaService.class);

    @Autowired
    private EncryptSensitiveDataService encryptSensitiveDataService;
    @Autowired
    private Producer<String, String> producer;
    private static ObjectMapper jsonMapper = new ObjectMapper();

    static {
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.registerModule(new JavaTimeModule());
        jsonMapper.setSerializationInclusion(Include.NON_NULL);
        jsonMapper.setFilterProvider(
                new SimpleFilterProvider().setDefaultFilter(SimpleBeanPropertyFilter.serializeAll()));
    }

    /**
     * sendToSink.
     */
    private void sendToSink(String topic, String key, IgniteEvent igniteEvent) throws ExecutionException {
        String message;
        try {
            message = jsonMapper.writeValueAsString(igniteEvent);
        } catch (JsonProcessingException e1) {
            LOGGER.error("Exeception occured while coverting to JSON", e1);
            return;
        }
        LOGGER.info("To Topic {}, Message to be published: {}", topic,
                encryptSensitiveDataService.encryptLog(Objects.toString(message, "")));
        Future<RecordMetadata> response = producer.send(new ProducerRecord<String, String>(topic, key, message));
        RecordMetadata record;
        try {
            record = response.get();
            LOGGER.debug("Published topic: {}, Partition: {}, Offset: {}", record.topic(), record.partition(),
                    record.offset());
        } catch (InterruptedException e) {
            // restore the interrupt status and move on
            Thread.currentThread().interrupt();
        }

    }

    /**
     * sendIgniteEvent.
     */
    public void sendIgniteEvent(String topic, String key, IgniteEvent igniteEvent) throws ExecutionException {
        try {
            sendToSink(topic, key, igniteEvent);
        } catch (ExecutionException e) {
            LOGGER.error("Error Sending Ignite Event to Kafka: ", e);
            throw e;
        }
    }

    /**
     * cleanUp.
     */
    @PreDestroy
    public void cleanUp() throws Exception {
        LOGGER.info("Flushing and closing kafka producer");
        producer.flush();
        producer.close();
    }
}
