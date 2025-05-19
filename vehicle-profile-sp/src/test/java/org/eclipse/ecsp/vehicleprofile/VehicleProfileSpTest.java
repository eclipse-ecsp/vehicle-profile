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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.ecsp.analytics.stream.base.Launcher;
import org.eclipse.ecsp.analytics.stream.base.PropertyNames;
import org.eclipse.ecsp.analytics.stream.base.discovery.PropBasedDiscoveryServiceImpl;
import org.eclipse.ecsp.analytics.stream.base.utils.KafkaTestUtils;
import org.eclipse.ecsp.entities.IgniteEvent;
import org.eclipse.ecsp.key.IgniteStringKey;
import org.eclipse.ecsp.transform.GenericIgniteEventTransformer;
import org.eclipse.ecsp.transform.IgniteKeyTransformerStringImpl;
import org.eclipse.ecsp.utils.logger.IgniteLogger;
import org.eclipse.ecsp.utils.logger.IgniteLoggerFactory;
import org.eclipse.ecsp.vehicleprofile.sp.VehicleProfileStreamProcessor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * VehicleProfileSpTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Launcher.class })
@TestPropertySource("/application.properties")
@Ignore
public class VehicleProfileSpTest {

    private static final IgniteLogger LOGGER = IgniteLoggerFactory.getLogger(VehicleProfileSpTest.class);
    private static final String IN_TOPIC_NAME = "vin-events";

    @Autowired
    private IgniteKeyTransformerStringImpl igniteKeyTransformer;

    @Autowired
    private GenericIgniteEventTransformer valueTransformer;

    @Autowired
    private ObjectMapper mapper;

    // @Mock
    // private EncryptDecryptInterface encryptDecryptInterface;


    /**
     * THREAD_SLEEP_TIME.
     */
    public static final int THREAD_SLEEP_TIME = 5000;
    
    //@Override
    @Before
    public void setup() throws Exception {
        //   Mockito.when(encryptDecryptInterface.encrypt(Mockito.anyString())).thenReturn(" ");
        // empty the collection before running the test cases
//        super.setup();
//        createTopics(IN_TOPIC_NAME, IN_TOPIC_NAME);
//        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "tc-consumer");
//        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
//                Serdes.String().deserializer().getClass().getName());
//        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
//                Serdes.String().deserializer().getClass().getName());
//        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
//                Serdes.ByteArray().serializer().getClass().getName());
//        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
//                Serdes.ByteArray().serializer().getClass().getName());
//        ksProps.put(PropertyNames.DISCOVERY_SERVICE_IMPL, PropBasedDiscoveryServiceImpl.class.getName());
//        ksProps.put(PropertyNames.SOURCE_TOPIC_NAME, IN_TOPIC_NAME);
//        ksProps.put(PropertyNames.APPLICATION_ID, "pt");
//
//        ksProps.put("event.transformer.classes", "org.eclipse.ecsp.transform.GenericIgniteEventTransformer");
//        ksProps.put("ignite.key.transformer.class", "org.eclipse.ecsp.transform.IgniteKeyTransformerStringImpl");
//        ksProps.put("ingestion.serializer.class", "org.eclipse.ecsp.serializer.IngestionSerializerFSTImpl");

    }

    /**
     * Test to create a Data Collection Policy in DB and verify there is no
     * exception while saving the data to MongoDB.
     *
     */
    @Test
    @Ignore
    public void testVehicleProfileSp() throws Exception {
//        ksProps.put(PropertyNames.PRE_PROCESSORS,
//                "com.harman.analytics.stream.base.processors.TaskContextInitializer,"
//                + "com.harman.analytics.stream.base.processors.ProtocolTranslatorPreProcessor");
//        ksProps.put(PropertyNames.SERVICE_STREAM_PROCESSORS, VehicleProfileStreamProcessor.class.getName());
//        ksProps.put(PropertyNames.POST_PROCESSORS,
//                "com.harman.analytics.stream.base.processors.ProtocolTranslatorPostProcessor");
//        ksProps.put(PropertyNames.APPLICATION_ID, "chaining" + System.currentTimeMillis());
//        launchApplication();

        Thread.sleep(THREAD_SLEEP_TIME);
        IgniteStringKey igniteStringKey = new IgniteStringKey();
        String key1 = UUID.randomUUID().toString();
        igniteStringKey.setKey(key1);

        IgniteEvent event = null;

//        KafkaTestUtils.sendMessages(IN_TOPIC_NAME, producerProps, igniteKeyTransformer.toBlob(igniteStringKey),
//                valueTransformer.toBlob(event));
//        Thread.sleep(THREAD_SLEEP_TIME);

        //shutDownApplication();
    }
}