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

package org.wso2.carbon.event.builder.test.ds;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputStreamConfiguration;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2EventBuilderFactory;
import org.wso2.carbon.event.builder.test.TestBasicEventListener;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.config.InternalInputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * this class is used to get the Transport Adaptor service.
 *
 * @scr.component name="eventBuilder.test.component" immediate="true"
 * @scr.reference name="transportservice.service"
 * interface="org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setTransportAdaptorService" unbind="unsetTransportAdaptorService"
 * @scr.reference name="eventBuilder.service"
 * interface="org.wso2.carbon.event.builder.core.EventBuilderService" cardinality="1..1"
 * policy="dynamic" bind="setEventBuilderService" unbind="unsetEventBuilderService"
 * @scr.reference name="configuration.contextService.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class EventBuilderTesterDS {
    private static final Log log = LogFactory.getLog(EventBuilderTesterDS.class);

    protected void activate(ComponentContext context) {
        EventBuilderService eventBuilderService = TestEventBuilderServiceHolder.getInstance().getEventBuilderService();
        InputTransportAdaptorService transportAdaptorService = TestEventBuilderServiceHolder.getInstance().getInputTransportAdaptorService();
        ConfigurationContextService configurationContextService = TestEventBuilderServiceHolder.getInstance().getConfigurationContextService();
        AxisConfiguration axisConfiguration = configurationContextService.getClientConfigContext().getAxisConfiguration();
        InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration = new InputTransportAdaptorConfiguration();
        InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration = new InputTransportAdaptorMessageConfiguration();

        int tenantId = PrivilegedCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        String fromStreamName = "analytics_Statistics";
        String fromStreamVersion = "1.2.0";
        String toStreamName = "kpiStream";
        String toStreamVersion = "1.0.0";

        inputTransportMessageConfiguration.addInputMessageProperty("streamName", fromStreamName);
        inputTransportMessageConfiguration.addInputMessageProperty("version", fromStreamVersion);
        configureInputTransportAdaptor(transportAdaptorService, axisConfiguration, fromStreamName, fromStreamVersion,
                inputTransportAdaptorConfiguration, inputTransportMessageConfiguration);
        configureEventBuilder(eventBuilderService, axisConfiguration, inputTransportMessageConfiguration, inputTransportAdaptorConfiguration, toStreamName, toStreamVersion);

        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition(fromStreamName, fromStreamVersion);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Error creating stream definition with id " + fromStreamName + ":" + fromStreamVersion);
        }
        log.info("[TEST-Module] Successfully deployed event builder service tester");

        TestBasicEventListener testBasicEventListener = new TestBasicEventListener();
        try {
            eventBuilderService.subscribe(streamDefinition, testBasicEventListener, tenantId);
            log.info("[TEST-Module] Successfully subscribed to event builder.");
        } catch (EventBuilderConfigurationException e) {
            log.error("[TEST-Module] Error when subscribing to event builder:\n" + e);
        }
    }

    private void configureEventBuilder(EventBuilderService eventBuilderService,
                                       AxisConfiguration axisConfiguration,
                                       InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration, InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                                       String toStreamName, String toStreamVersion) {

        EventBuilderConfiguration eventBuilderConfiguration =
                new EventBuilderConfiguration(new Wso2EventBuilderFactory());
        InputStreamConfiguration inputStreamConfiguration = new InputStreamConfiguration();
        inputStreamConfiguration.setInputTransportAdaptorMessageConfiguration(inputTransportMessageConfiguration);
        inputStreamConfiguration.setTransportAdaptorName(inputTransportAdaptorConfiguration.getName());
        inputStreamConfiguration.setTransportAdaptorType(inputTransportAdaptorConfiguration.getType());
        eventBuilderConfiguration.setInputStreamConfiguration(inputStreamConfiguration);
        eventBuilderConfiguration.setToStreamName(toStreamName);
        eventBuilderConfiguration.setToStreamVersion(toStreamVersion);

        try {
            eventBuilderService.addEventBuilder(eventBuilderConfiguration, axisConfiguration);
        } catch (EventBuilderConfigurationException e) {
            log.error(e);
        }
    }

    private void configureInputTransportAdaptor(InputTransportAdaptorService transportAdaptorService,
                                                AxisConfiguration axisConfiguration,
                                                String streamName, String streamVersion,
                                                InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
                                                InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration) {
        if (inputTransportAdaptorConfiguration != null && inputTransportMessageConfiguration != null) {
            inputTransportAdaptorConfiguration.setName("localEventReceiver");
            inputTransportAdaptorConfiguration.setType("wso2event-receiver");

            InternalInputTransportAdaptorConfiguration internalTransportAdaptorConfiguration = new InternalInputTransportAdaptorConfiguration();
            internalTransportAdaptorConfiguration.addTransportAdaptorProperty("receiverURL", "tcp://localhost:76111");
            internalTransportAdaptorConfiguration.addTransportAdaptorProperty("authenticatorURL", "ssl://localhost:77111");
            internalTransportAdaptorConfiguration.addTransportAdaptorProperty("username", "admin");
            internalTransportAdaptorConfiguration.addTransportAdaptorProperty("password", "admin");
            inputTransportAdaptorConfiguration.setInputConfiguration(internalTransportAdaptorConfiguration);

            inputTransportMessageConfiguration.addInputMessageProperty("streamName", streamName);
            inputTransportMessageConfiguration.addInputMessageProperty("version", streamVersion);

            try {
                transportAdaptorService.subscribe(inputTransportAdaptorConfiguration, inputTransportMessageConfiguration, null, axisConfiguration);
            } catch (InputTransportAdaptorEventProcessingException e) {
                log.error("Error subscribing to transport adaptor:\n" + e);
            }
        } else {
            log.error("Cannot create input transport adaptor. Some parameters are null");
        }
    }

    protected void setEventBuilderService(EventBuilderService eventBuilderService) {
        TestEventBuilderServiceHolder.getInstance().registerEventBuilderService(eventBuilderService);
    }

    protected void unsetEventBuilderService(EventBuilderService eventBuilderService) {
        TestEventBuilderServiceHolder.getInstance().unregisterEventBuilderService(eventBuilderService);
    }

    protected void setTransportAdaptorService(InputTransportAdaptorService transportAdaptorService) {
        TestEventBuilderServiceHolder.getInstance().registerTransportAdaptorService(transportAdaptorService);
    }

    protected void unsetTransportAdaptorService(InputTransportAdaptorService transportAdaptorService) {
        TestEventBuilderServiceHolder.getInstance().unregisterTransportAdaptorService(transportAdaptorService);
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        TestEventBuilderServiceHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        TestEventBuilderServiceHolder.getInstance().unregisterConfigurationContextService(configurationContextService);
    }
}
