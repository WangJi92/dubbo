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

import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 组合多个配置属性，根据先后的顺序进行获取信息（单独使用感觉蛮危险）
 */
public class CompositeConfiguration implements Configuration {
    private Logger logger = LoggerFactory.getLogger(CompositeConfiguration.class);

    /**
     * List holding all the configuration，组合多个属性
     */
    private List<Configuration> configList = new LinkedList<Configuration>();

    public CompositeConfiguration() {

    }

    public CompositeConfiguration(Configuration... configurations) {
        if (configurations != null && configurations.length > 0) {
            //这里感觉有点意思，不存在的时候才去添加哦
            Arrays.stream(configurations).filter(config -> !configList.contains(config)).forEach(configList::add);
        }
    }

    public void addConfiguration(Configuration configuration) {
        if (configList.contains(configuration)) {
            return;
        }
        this.configList.add(configuration);
    }

    public void addConfigurationFirst(Configuration configuration) {
        this.addConfiguration(0, configuration);
    }

    public void addConfiguration(int pos, Configuration configuration) {
        this.configList.add(pos, configuration);
    }

    @Override
    public Object getInternalProperty(String key) {
        // 根据顺序进行遍历，从而实现了优先级的处理哦！
        Configuration firstMatchingConfiguration = null;
        for (Configuration config : configList) {
            try {
                if (config.containsKey(key)) {
                    firstMatchingConfiguration = config;
                    break;
                }
            } catch (Exception e) {
                logger.error("Error when trying to get value for key " + key + " from " + config + ", will continue to try the next one.");
            }
        }
        if (firstMatchingConfiguration != null) {
            return firstMatchingConfiguration.getProperty(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean containsKey(String key) {
        return configList.stream().anyMatch(c -> c.containsKey(key));
    }
}
