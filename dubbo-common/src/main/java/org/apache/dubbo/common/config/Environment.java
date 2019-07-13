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
package org.apache.dubbo.common.config;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO load as SPI will be better?
 * 将Dubbo中的环境信息进行封装
 */
public class Environment {
    private static final Environment INSTANCE = new Environment();
    /**
     * 属性变量 System.getProperty --> dubbo.properties
     */
    private Map<String, PropertiesConfiguration> propertiesConfigs = new ConcurrentHashMap<>();
    /**
     * 系统变量 操作系统层面  System.getenv
     */
    private Map<String, SystemConfiguration> systemConfigs = new ConcurrentHashMap<>();

    /**
     * 环境变量 System.getProperty
     */
    private Map<String, EnvironmentConfiguration> environmentConfigs = new ConcurrentHashMap<>();
    /**
     * 从外部读取的，通常为配置中心获取的信息 （内存性的配置） 全局的
     */
    private Map<String, InmemoryConfiguration> externalConfigs = new ConcurrentHashMap<>();
    /**
     * 从外部读取的，通常为配置中心获取的信息 （内存性的配置） 应用的配置
     */
    private Map<String, InmemoryConfiguration> appExternalConfigs = new ConcurrentHashMap<>();

    /**
     * 外部化配置的Map读取的信息
     */
    private Map<String, String> externalConfigurationMap = new HashMap<>();
    /**
     * 读取外部化配置额信息
     */
    private Map<String, String> appExternalConfigurationMap = new HashMap<>();

    /**
     * 配置中心的优先级是否高？
     */
    private boolean configCenterFirst = true;

    /**
     * 此实例始终是一种动态配置类型，configcenterconfig将在启动时加载该实例并将其分配给此处。 动态配置 比如从zookeeper中获取的属性
     * FIXME, this instance will always be a type of DynamicConfiguration, ConfigCenterConfig will load the instance at startup and assign it to here.
     */
    private Configuration dynamicConfiguration;

    public static Environment getInstance() {
        return INSTANCE;
    }

    public PropertiesConfiguration getPropertiesConfig(String prefix, String id) {
        return propertiesConfigs.computeIfAbsent(toKey(prefix, id), k -> new PropertiesConfiguration(prefix, id));
    }

    public SystemConfiguration getSystemConfig(String prefix, String id) {
        return systemConfigs.computeIfAbsent(toKey(prefix, id), k -> new SystemConfiguration(prefix, id));
    }

    public InmemoryConfiguration getExternalConfig(String prefix, String id) {
        return externalConfigs.computeIfAbsent(toKey(prefix, id), k -> {
            InmemoryConfiguration configuration = new InmemoryConfiguration(prefix, id);
            configuration.setProperties(externalConfigurationMap);
            return configuration;
        });
    }

    public InmemoryConfiguration getAppExternalConfig(String prefix, String id) {
        return appExternalConfigs.computeIfAbsent(toKey(prefix, id), k -> {
            InmemoryConfiguration configuration = new InmemoryConfiguration(prefix, id);
            configuration.setProperties(appExternalConfigurationMap);
            return configuration;
        });
    }

    public EnvironmentConfiguration getEnvironmentConfig(String prefix, String id) {
        return environmentConfigs.computeIfAbsent(toKey(prefix, id), k -> new EnvironmentConfiguration(prefix, id));
    }

    public void setExternalConfigMap(Map<String, String> externalConfiguration) {
        this.externalConfigurationMap = externalConfiguration;
    }

    public void setAppExternalConfigMap(Map<String, String> appExternalConfiguration) {
        this.appExternalConfigurationMap = appExternalConfiguration;
    }

    public Map<String, String> getExternalConfigurationMap() {
        return externalConfigurationMap;
    }

    public Map<String, String> getAppExternalConfigurationMap() {
        return appExternalConfigurationMap;
    }

    public void updateExternalConfigurationMap(Map<String, String> externalMap) {
        this.externalConfigurationMap.putAll(externalMap);
    }

    public void updateAppExternalConfigurationMap(Map<String, String> externalMap) {
        this.appExternalConfigurationMap.putAll(externalMap);
    }

    /**
     * 每个调用创建一个新的实例，因为它只在启动时调用，我认为潜在的成本不会很大。否则，如果使用缓存，我们应该确保每个配置都有一个唯一的ID，
     * 这是很难保证的，因为它在用户方面，特别是在serviceconfig和referenceconfig方面
     * Create new instance for each call, since it will be called only at startup, I think there's no big deal of the potential cost.
     * Otherwise, if use cache, we should make sure each Config has a unique id which is difficult to guarantee because is on the user's side,
     * especially when it comes to ServiceConfig and ReferenceConfig.
     *
     * @param prefix
     * @param id
     * @return
     */
    public CompositeConfiguration getConfiguration(String prefix, String id) {
        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
        // Config center has the highest priority  这里的优先级根据顺序哦
        compositeConfiguration.addConfiguration(this.getSystemConfig(prefix, id));
        compositeConfiguration.addConfiguration(this.getEnvironmentConfig(prefix, id));
        compositeConfiguration.addConfiguration(this.getAppExternalConfig(prefix, id));
        compositeConfiguration.addConfiguration(this.getExternalConfig(prefix, id));
        compositeConfiguration.addConfiguration(this.getPropertiesConfig(prefix, id));
        return compositeConfiguration;
    }

    public Configuration getConfiguration() {
        return getConfiguration(null, null);
    }

    private static String toKey(String prefix, String id) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(prefix)) {
            sb.append(prefix);
        }
        if (StringUtils.isNotEmpty(id)) {
            sb.append(id);
        }

        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '.') {
            sb.append(".");
        }

        if (sb.length() > 0) {
            return sb.toString();
        }
        return CommonConstants.DUBBO;
    }

    public boolean isConfigCenterFirst() {
        return configCenterFirst;
    }

    public void setConfigCenterFirst(boolean configCenterFirst) {
        this.configCenterFirst = configCenterFirst;
    }

    /**
     * 喜欢使用Java8 的啦啊~~
     * @return
     */
    public Optional<Configuration> getDynamicConfiguration() {
        return Optional.ofNullable(dynamicConfiguration);
    }

    public void setDynamicConfiguration(Configuration dynamicConfiguration) {
        this.dynamicConfiguration = dynamicConfiguration;
    }

    // For test
    public void clearExternalConfigs() {
        this.externalConfigs.clear();
        this.externalConfigurationMap.clear();
    }

    // For test
    public void clearAppExternalConfigs() {
        this.appExternalConfigs.clear();
        this.appExternalConfigurationMap.clear();
    }
}
