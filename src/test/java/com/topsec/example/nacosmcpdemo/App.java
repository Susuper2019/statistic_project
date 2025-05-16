/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.topsec.example.nacosmcpdemo;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.lock.LockService;
import com.alibaba.nacos.api.lock.NacosLockFactory;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Properties;

/**
 * Hello world.
 *
 * @author xxc
 */
public class App {
  public static void main(String[] args) throws NacosException {
    Properties properties = new Properties();
    properties.setProperty("serverAddr", "10.7.211.69:8848");
    properties.setProperty("namespace", "example");
    NamingService naming = NamingFactory.createNamingService(properties);

    naming.registerInstance("nacos.test.3", "11.11.11.11", 8888, "TEST1");
    System.out.println("[Instances after register]  " + naming.getAllInstances("nacos.test.3", List.of("TEST1")));

    naming.registerInstance("nacos.test.3", "2.2.2.2", 9999, "DEFAULT");
    System.out.println("[Instances after register]  " + naming.getAllInstances("nacos.test.3", List.of("DEFAULT")));


//    String serverAddr = "10.7.211.69:8848";
//
//    ConfigService configService = NacosFactory.createConfigService(serverAddr);
//
//    NamingService namingService = NacosFactory.createNamingService(serverAddr);
//    namingService.registerInstance("wilkes","10.7.211.69",22010);
//    List<Instance> wilkes = namingService.getAllInstances("wilkes");
//    System.out.println(wilkes.toString());
//    Properties properties = new Properties();
//    properties.setProperty(PropertyKeyConst.SERVER_ADDR, "localhost:8848");
//    properties.setProperty(PropertyKeyConst.NAMESPACE, "${namespaceId}");

//    ConfigService configService = NacosFactory.createConfigService(properties);

//    NamingService namingService = NacosFactory.createNamingService(properties);

//    LockService lockService = NacosLockFactory.createLockService(properties);

  }
}
