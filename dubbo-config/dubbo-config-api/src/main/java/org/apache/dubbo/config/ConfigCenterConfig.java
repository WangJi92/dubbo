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
package org.apache.dubbo.config;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.config.Environment;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.config.support.Parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.dubbo.common.constants.CommonConstants.ANYHOST_VALUE;
import static org.apache.dubbo.common.constants.CommonConstants.PATH_KEY;
import static org.apache.dubbo.common.constants.CommonConstants.PROTOCOL_KEY;
import static org.apache.dubbo.config.Constants.CONFIG_CONFIGFILE_KEY;
import static org.apache.dubbo.config.Constants.CONFIG_ENABLE_KEY;
import static org.apache.dubbo.config.Constants.CONFIG_TIMEOUT_KEY;
import static org.apache.dubbo.config.Constants.ZOOKEEPER_PROTOCOL;
import static org.apache.dubbo.configcenter.Constants.CONFIG_CHECK_KEY;
import static org.apache.dubbo.configcenter.Constants.CONFIG_CLUSTER_KEY;
import static org.apache.dubbo.configcenter.Constants.CONFIG_GROUP_KEY;
import static org.apache.dubbo.configcenter.Constants.CONFIG_NAMESPACE_KEY;

/**
 * ConfigCenterConfig 配置中心的配置
 */
public class ConfigCenterConfig extends AbstractConfig {
    /**
     * 是否初始化
     */
    private AtomicBoolean inited = new AtomicBoolean(false);

    /**
     * 协议地址信息
     */
    private String protocol;
    private String address;

    /* The config center cluster, it's real meaning may very on different Config Center products. */
    private String cluster;

    /* The namespace of the config center, generally it's used for multi-tenant,
    but it's real meaning depends on the actual Config Center you use.
    */
    /**
     * 默认的命名空间就是dubbo，可以支持多租户使用不同的配置中心
     */
    private String namespace = CommonConstants.DUBBO;
    /* The group of the config center, generally it's used to identify an isolated space for a batch of config items,
    but it's real meaning depends on the actual Config Center you use.
    */
    private String group = CommonConstants.DUBBO;
    private String username;
    private String password;
    private Long timeout = 3000L;

    // 如果配置中心具有最高优先级，它将覆盖所有其他配置。
    private Boolean highestPriority = true;

    // 决定初始连接尝试失败时的行为，“true ”意味着一旦失败就中断整个过程。
    // Decide the behaviour when initial connection try fails, 'true' means interrupt the whole process once fail.
    private Boolean check = true;

    /* Used to specify the key that your properties file mapping to, most of the time you do not need to change this parameter.
    Notice that for Apollo, this parameter is meaningless, set the 'namespace' is enough.
    */
    /**
     * 配置中心中使用的默认的文件名的信息
     */
    private String configFile = CommonConstants.DEFAULT_DUBBO_PROPERTIES;

    /* the .properties file under 'configFile' is global shared while .properties under this one is limited only to this application
    */
    /**
     * 应用配置中心的文件的信息
     */
    private String appConfigFile;

    /* If the Config Center product you use have some special parameters that is not covered by this class, you can add it to here.
    For example, with XML:
      <dubbo:config-center>
           <dubbo:parameter key="config.{your key}" value="{your value}" />
      </dubbo:config-center>
     */
    /**
     * 配置中心的其他参数
     */
    private Map<String, String> parameters;

    public ConfigCenterConfig() {
    }

    public URL toUrl() {
        Map<String, String> map = new HashMap<>();

        //将当前类的信息，转换获取为Map信息
        appendParameters(map, this);

        // 访问地址的信息
        if (StringUtils.isEmpty(address)) {
            address = ANYHOST_VALUE;
        }

        // path 当前配置类的信息、
        map.put(PATH_KEY, ConfigCenterConfig.class.getSimpleName());
        // use 'zookeeper' as the default configcenter.

        //默认是 zookeeper
        if (StringUtils.isEmpty(map.get(PROTOCOL_KEY))) {
            map.put(PROTOCOL_KEY, ZOOKEEPER_PROTOCOL);
        }

        //构建URL 内部实现
        return UrlUtils.parseURL(address, map);
    }

    public boolean checkOrUpdateInited() {
        return inited.compareAndSet(false, true);
    }

    /**
     * 更新本地缓存，跟新外部化配置信息
     * @param externalConfiguration
     */
    public void setExternalConfig(Map<String, String> externalConfiguration) {
        Environment.getInstance().setExternalConfigMap(externalConfiguration);
    }

    public void setAppExternalConfig(Map<String, String> appExternalConfiguration) {
        Environment.getInstance().setAppExternalConfigMap(appExternalConfiguration);
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Parameter(excluded = true)
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Parameter(key = CONFIG_CLUSTER_KEY)
    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    @Parameter(key = CONFIG_NAMESPACE_KEY)
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Parameter(key = CONFIG_GROUP_KEY)
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Parameter(key = CONFIG_CHECK_KEY)
    public Boolean isCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    @Parameter(key = CONFIG_ENABLE_KEY)
    public Boolean isHighestPriority() {
        return highestPriority;
    }

    public void setHighestPriority(Boolean highestPriority) {
        this.highestPriority = highestPriority;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Parameter(key = CONFIG_TIMEOUT_KEY)
    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    @Parameter(key = CONFIG_CONFIGFILE_KEY)
    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    @Parameter(excluded = true)
    public String getAppConfigFile() {
        return appConfigFile;
    }

    public void setAppConfigFile(String appConfigFile) {
        this.appConfigFile = appConfigFile;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        checkParameterName(parameters);
        this.parameters = parameters;
    }

    @Override
    @Parameter(excluded = true)
    public boolean isValid() {
        if (StringUtils.isEmpty(address)) {
            return false;
        }

        return address.contains("://") || StringUtils.isNotEmpty(protocol);
    }
}
