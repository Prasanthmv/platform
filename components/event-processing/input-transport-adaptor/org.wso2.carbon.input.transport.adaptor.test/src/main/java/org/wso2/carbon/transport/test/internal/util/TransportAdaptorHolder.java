/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.transport.test.internal.util;

import org.wso2.carbon.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * singleton class to hold the transport adaptor service
 */
public final class TransportAdaptorHolder {
    private InputTransportAdaptorService inputTransportAdaptorService;
    private ConfigurationContextService configurationContextService;
    private static TransportAdaptorHolder instance = new TransportAdaptorHolder();

    private TransportAdaptorHolder() {
    }

    public InputTransportAdaptorService getInputTransportAdaptorService() {
        return inputTransportAdaptorService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static TransportAdaptorHolder getInstance() {
        return instance;
    }

    public void registerTransportService(InputTransportAdaptorService inputTransportAdaptorService) {
        this.inputTransportAdaptorService = inputTransportAdaptorService;
    }

    public void unRegisterTransportService(InputTransportAdaptorService inputTransportAdaptorService) {
        this.inputTransportAdaptorService = null;
    }

    public void registerConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public void unRegisterConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = null;
    }


}
