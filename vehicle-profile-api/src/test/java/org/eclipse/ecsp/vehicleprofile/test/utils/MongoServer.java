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

package org.eclipse.ecsp.vehicleprofile.test.utils;

import java.util.Map;

import org.bson.conversions.Bson;
import org.eclipse.ecsp.nosqldao.spring.config.AbstractIgniteDAOMongoConfig;
import org.junit.rules.ExternalResource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * MongoServer.
 */
public class MongoServer extends ExternalResource {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MongoServer.class);

    /*private MongodStarter mongodStarter;
    private MongodExecutable mongodExecutable;
    private MongodProcess mongodProcess;

    private static final int MONGO_PORT = 27017;

    private static FileChannel fc;
    private static RandomAccessFile randomAccessFile;
    private FileLock fileLock;

    private static final String MONGO_FILE_NAME = "mongo_lock";
    private static final String MONGO_LOCK_FILE = new File(System.getProperty("java.io.tmpdir"), MONGO_FILE_NAME)
            .getAbsolutePath();

    @Override
    public void before() throws Throwable {
        assertNotNull(MONGO_LOCK_FILE);
        randomAccessFile = new RandomAccessFile(MONGO_LOCK_FILE, "rw");
        fc = randomAccessFile.getChannel();
        // acquiring lock
        LOGGER.info("Acquiring lock on filename: {}", MONGO_LOCK_FILE);
        fileLock = fc.lock();
        super.before();
        startMongoServer();
    }

    @Override
    public void after() {
        super.after();
        stopMongoServer();
        if (null != fileLock) {
            try {
                LOGGER.info("Releasing lock on filename: {}", MONGO_LOCK_FILE);
                fileLock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startMongoServer() throws UnknownHostException, IOException, InterruptedException {
        mongodStarter = MongodStarter.getDefaultInstance();
        MongodConfig mongodConfig = MongodConfig.builder().version(Version.Main.V6_0)
                .net(new Net("localhost", MONGO_PORT, Network.localhostIsIPv6())).build();
        mongodExecutable = mongodStarter.prepare(mongodConfig);
        mongodProcess = mongodExecutable.start();
        try (MongoClient mongoClient = new MongoClient(new ServerAddress("localhost", MONGO_PORT))) {
           
            Map<String, Object> commandArguments = new BasicDBObject();
            commandArguments.put("createUser", "test");
            commandArguments.put("pwd", "test");
            String[] roles = { "readWrite" };
            commandArguments.put("roles", roles);
            BasicDBObject command = new BasicDBObject(commandArguments);
            MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
            adminDatabase.runCommand(command);
        }
    }

    private void stopMongoServer() {
        if (null != mongodProcess) {
            mongodProcess.stop();
        }
        if (null != mongodExecutable) {
            mongodExecutable.stop();
        }
    }*/
    
    @Container
    MongoDBContainer mongoDbContainer = new MongoDBContainer("mongo:6.0.13");
    
    protected void before() throws Throwable {
      this.mongoDbContainer.start();
      LOGGER.info("Embedded mongo DB started on address {} ", new Object[] { this.mongoDbContainer.getHost() });
      AbstractIgniteDAOMongoConfig.overridingPort = this.mongoDbContainer.getFirstMappedPort();
      LOGGER.info("MongoClient connecting for pre-work DB configuration...");
      MongoClient mongoClient = MongoClients.create(this.mongoDbContainer.getConnectionString());
      try {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("createUser", "admin");
        basicDBObject.put("pwd", "dummyPass");
        String[] roles = { "readWrite" };
        basicDBObject.put("roles", roles);
        MongoDatabase adminDatabase = mongoClient.getDatabase("admin");
        BasicDBObject command = new BasicDBObject((Map)basicDBObject);
        adminDatabase.runCommand((Bson)command);
        if (mongoClient != null)
          mongoClient.close(); 
      } catch (Throwable throwable) {
        if (mongoClient != null)
          try {
            mongoClient.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    }
    
    protected void after() {
      if (this.mongoDbContainer.isCreated())
        this.mongoDbContainer.stop(); 
    }
}
