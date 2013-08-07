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
package org.wso2.carbon.transport.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.adaptor.core.InputTransportAdaptorListener;
import org.wso2.carbon.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException;

/**
 * Topic subscription call back handler implementation
 */
public class TestInputTransportAdaptorAdaptorListener implements InputTransportAdaptorListener {
    private static final Log log = LogFactory.getLog(TestInputTransportAdaptorAdaptorListener.class);
    private String transportAdaptorName;
    private String topic;

    public TestInputTransportAdaptorAdaptorListener(String transportAdaptorName, String topic) {
        this.transportAdaptorName = transportAdaptorName;
        this.topic = topic;
    }

    @Override
    public void addEventDefinition(Object object) throws
                                                  InputTransportAdaptorEventProcessingException {
        System.out.println(" Add Definition ==> " + object);
    }

    @Override
    public void removeEventDefinition(Object object)
            throws InputTransportAdaptorEventProcessingException {
        System.out.println(" Remove Definition ==> " + object);
    }

    /**
     * Received message is logged to ensure that published messages are received.
     *
     * @param object - received event
     * @throws org.wso2.carbon.transport.adaptor.core.exception.InputTransportAdaptorEventProcessingException
     *
     */
    public void onEvent(Object object) throws InputTransportAdaptorEventProcessingException {
        log.info("transportAdaptorName ==> " + transportAdaptorName);
        log.info("topic ==> " + topic);
        log.info("omElement message ==> " + object);
    }
}
