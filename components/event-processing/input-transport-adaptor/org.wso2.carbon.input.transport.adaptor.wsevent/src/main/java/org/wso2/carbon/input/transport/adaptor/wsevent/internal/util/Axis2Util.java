/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.input.transport.adaptor.wsevent.internal.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.config.InputTransportAdaptorConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.message.config.InputTransportAdaptorMessageConfiguration;

import javax.xml.namespace.QName;

public final class Axis2Util {

    private Axis2Util() {
    }

    public static AxisService registerAxis2Service(
            InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration,
            InputTransportAdaptorListener transportAdaptorListener,
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId)
            throws AxisFault {
        //first create an Axis2 service to receive the messages to this broker
        //operation name can not have
        String axisServiceName = inputTransportAdaptorConfiguration.getName() + "Service";
        AxisService axisService = axisConfiguration.getService(axisServiceName);
        if (axisService == null) {
            // create a new axis service
            axisService = new AxisService(axisServiceName);

            axisConfiguration.addService(axisService);
            axisService.getAxisServiceGroup().addParameter(CarbonConstants.DYNAMIC_SERVICE_PARAM_NAME, "true");
        }

        String topicNameWithoutSlash = inputTransportMessageConfiguration.getInputMessageProperties().get(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME).replaceAll("/", "");
        AxisOperation axisOperation = axisService.getOperation(new QName("", topicNameWithoutSlash));
        if (axisOperation == null) {
            axisOperation = new InOnlyAxisOperation(new QName("", topicNameWithoutSlash));
            axisOperation.setMessageReceiver(new SubscriptionMessageReceiver());
            axisOperation.setSoapAction("urn:" + topicNameWithoutSlash);


            axisConfiguration.getPhasesInfo().setOperationPhases(axisOperation);
            axisService.addOperation(axisOperation);
        }

        SubscriptionMessageReceiver messageReceiver =
                (SubscriptionMessageReceiver) axisOperation.getMessageReceiver();
        messageReceiver.addTransportAdaptorListener(subscriptionId, transportAdaptorListener);
        return axisService;
    }

    /**
     * removes the operation from the Axis service.
     *
     * @param inputTransportMessageConfiguration
     *
     * @param inputTransportAdaptorConfiguration
     *
     * @param axisConfiguration
     * @param subscriptionId
     * @throws org.apache.axis2.AxisFault
     */
    public static void removeOperation(
            InputTransportAdaptorMessageConfiguration inputTransportMessageConfiguration,
            InputTransportAdaptorConfiguration inputTransportAdaptorConfiguration,
            AxisConfiguration axisConfiguration, String subscriptionId) throws
                                                                        AxisFault {


        String axisServiceName = inputTransportAdaptorConfiguration.getName() + "Service";
        AxisService axisService = axisConfiguration.getService(axisServiceName);

        if (axisService == null) {
            throw new AxisFault("There is no service with the name ==> " + axisServiceName);
        }
        String topicNameWithoutSlash = inputTransportMessageConfiguration.getInputMessageProperties().get(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME).replaceAll("/", "");
        AxisOperation axisOperation = axisService.getOperation(new QName("", topicNameWithoutSlash));
        if (axisOperation == null) {
            throw new AxisFault("There is no operation with the name ==> " + topicNameWithoutSlash);
        }
        SubscriptionMessageReceiver messageReceiver =
                (SubscriptionMessageReceiver) axisOperation.getMessageReceiver();
        if (messageReceiver == null) {
            throw new AxisFault("There is no message receiver for operation with name ==> " + topicNameWithoutSlash);
        }
        if (messageReceiver.removeTransportAdaptorListener(subscriptionId)) {
            axisService.removeOperation(new QName("", inputTransportMessageConfiguration.getInputMessageProperties().get(WSEventTransportAdaptorConstants.TRANSPORT_MESSAGE_TOPIC_NAME).replaceAll("/", "")));
        }

    }


}
