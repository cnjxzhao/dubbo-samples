/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.samples.empty;

import org.apache.dubbo.common.Version;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.qos.command.impl.Offline;
import org.apache.dubbo.rpc.model.FrameworkModel;
import org.apache.dubbo.samples.api.GreetingsService;
import org.apache.dubbo.samples.provider.GreetingsServiceImpl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class DefaultIT {
    @After
    public void after() {
        FrameworkModel.destroyAll();
    }

    @Test
    public void testDefault() throws InterruptedException {
        LoggerFactory.setLoggerAdapter(FrameworkModel.defaultModel(), "log4j");
        String nacosAddress = System.getProperty("nacos.address", "localhost");
        String nacosPort = System.getProperty("nacos.port", "8848");

        ServiceConfig<GreetingsService> serviceConfig = new ServiceConfig<>();
        serviceConfig.setInterface(GreetingsService.class);
        serviceConfig.setRef(new GreetingsServiceImpl());
        serviceConfig.setApplication(new ApplicationConfig("provider"));
        serviceConfig.setRegistry(new RegistryConfig("nacos://" + nacosAddress + ":" + nacosPort + "?username=nacos&password=nacos"));
        serviceConfig.export();
        Thread.sleep(1000);

        ReferenceConfig<GreetingsService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(GreetingsService.class);
        referenceConfig.setRegistry(new RegistryConfig("nacos://" + nacosAddress + ":" + nacosPort + "?username=nacos&password=nacos"));
        referenceConfig.setScope("remote");
        GreetingsService greetingsService = referenceConfig.get();

        Assert.assertEquals("hi, dubbo", greetingsService.sayHi("dubbo"));

        new Offline(FrameworkModel.defaultModel()).offline("org.apache.dubbo.samples.api.GreetingsService");
        Thread.sleep(1000);

        if (Version.getVersion().compareTo("3.2.0") >= 0) {
            try {
                greetingsService.sayHi("dubbo");
                Assert.fail();
            } catch (Exception e) {
                Assert.assertTrue(e.getMessage().contains("No provider available"));
            }
        } else {
            Assert.assertEquals("hi, dubbo", greetingsService.sayHi("dubbo"));
        }
    }
}
