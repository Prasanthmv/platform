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

package org.wso2.carbon.input.transport.adaptor.email.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.input.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SubscriptionEmailMessageReceiver extends AbstractInMessageReceiver {

    private static final Log log = LogFactory.getLog(SubscriptionEmailMessageReceiver.class);

    private List<InputTransportAdaptorListener> transportAdaptorListeners;
    private ConcurrentHashMap<String, InputTransportAdaptorListener> transportAdaptorListenerMap;

    public SubscriptionEmailMessageReceiver() {
        this.transportAdaptorListenerMap = new ConcurrentHashMap<String, InputTransportAdaptorListener>();
        this.transportAdaptorListeners = new ArrayList<InputTransportAdaptorListener>();
    }

    public void addTransportAdaptorListener(String subscriptionId,
                                            InputTransportAdaptorListener transportAdaptorListener) {
        if (null == transportAdaptorListenerMap.putIfAbsent(subscriptionId, transportAdaptorListener)) {
            this.transportAdaptorListeners = new ArrayList<InputTransportAdaptorListener>(transportAdaptorListenerMap.values());
        }
    }

    public boolean removeTransportAdaptorListener(String subscriptionId) {
        if (null != transportAdaptorListenerMap.remove(subscriptionId)) {
            this.transportAdaptorListeners = new ArrayList<InputTransportAdaptorListener>(transportAdaptorListenerMap.values());
        }
        if (transportAdaptorListeners.size() == 0) {
            return true;
        }
        return false;
    }

    protected void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {

        SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
        OMElement bodyElement = soapEnvelope.getBody().getFirstElement();

        byte[] bytes = DatatypeConverter.parseBase64Binary(bodyElement.getText());
        try {
            for (InputTransportAdaptorListener transportAdaptorListener : this.transportAdaptorListeners) {
                transportAdaptorListener.onEventCall(bytes);
            }
        } catch (InputTransportAdaptorEventProcessingException e) {
            log.error("Can not process the received event ", e);
        }
    }
}