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

package org.wso2.carbon.output.transport.adaptor.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.output.transport.adaptor.core.AbstractOutputTransportAdaptor;
import org.wso2.carbon.output.transport.adaptor.core.MessageType;
import org.wso2.carbon.output.transport.adaptor.core.Property;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.exception.OutputTransportAdaptorEventProcessingException;
import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.output.transport.adaptor.jms.internal.util.JMSConnectionFactory;
import org.wso2.carbon.output.transport.adaptor.jms.internal.util.JMSConstants;
import org.wso2.carbon.output.transport.adaptor.jms.internal.util.JMSMessageSender;
import org.wso2.carbon.output.transport.adaptor.jms.internal.util.JMSTransportAdaptorConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public final class JMSTransportAdaptorType extends AbstractOutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(JMSTransportAdaptorType.class);
    private static JMSTransportAdaptorType jmsTransportAdaptorAdaptor = new JMSTransportAdaptorType();
    private ResourceBundle resourceBundle;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, PublisherDetails>> publisherMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, PublisherDetails>>();

    private JMSTransportAdaptorType() {

    }

    @Override
    protected List<String> getSupportedOutputMessageTypes() {
        List<String> supportOutputMessageTypes = new ArrayList<String>();
        supportOutputMessageTypes.add(MessageType.XML);
        supportOutputMessageTypes.add(MessageType.JSON);
        supportOutputMessageTypes.add(MessageType.MAP);
        supportOutputMessageTypes.add(MessageType.TEXT);
        return supportOutputMessageTypes;
    }

    /**
     * @return agent transport adaptor instance
     */
    public static JMSTransportAdaptorType getInstance() {

        return jmsTransportAdaptorAdaptor;
    }

    /**
     * @return name of the agent transport adaptor
     */
    @Override
    protected String getName() {
        return JMSTransportAdaptorConstants.TRANSPORT_TYPE_JMS;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.output.transport.adaptor.jms.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // JNDI initial context factory class
        Property initialContextProperty = new Property(JMSTransportAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS);
        initialContextProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS));
        initialContextProperty.setRequired(true);
        initialContextProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.JNDI_INITIAL_CONTEXT_FACTORY_CLASS_HINT));
        propertyList.add(initialContextProperty);


        // JNDI Provider URL
        Property javaNamingProviderUrlProperty = new Property(JMSTransportAdaptorConstants.JAVA_NAMING_PROVIDER_URL);
        javaNamingProviderUrlProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JAVA_NAMING_PROVIDER_URL));
        javaNamingProviderUrlProperty.setRequired(true);
        javaNamingProviderUrlProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.JAVA_NAMING_PROVIDER_URL_HINT));
        propertyList.add(javaNamingProviderUrlProperty);


        // JNDI Username
        Property userNameProperty = new Property(JMSTransportAdaptorConstants.JAVA_NAMING_SECURITY_PRINCIPAL);
        userNameProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JAVA_NAMING_SECURITY_PRINCIPAL));
        propertyList.add(userNameProperty);


        // JNDI Password
        Property passwordProperty = new Property(JMSTransportAdaptorConstants.JAVA_NAMING_SECURITY_CREDENTIALS);
        passwordProperty.setSecured(true);
        passwordProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.JAVA_NAMING_SECURITY_CREDENTIALS));
        propertyList.add(passwordProperty);

        // Connection Factory JNDI Name
        Property connectionFactoryNameProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME);
        connectionFactoryNameProperty.setRequired(true);
        connectionFactoryNameProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME));
        connectionFactoryNameProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_CONNECTION_FACTORY_JNDINAME_HINT));
        propertyList.add(connectionFactoryNameProperty);


        // Destination Type
        Property destinationTypeProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION_TYPE);
        destinationTypeProperty.setRequired(true);
        destinationTypeProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION_TYPE));
        destinationTypeProperty.setOptions(new String[]{"queue", "topic"});
        destinationTypeProperty.setDefaultValue("topic");
        destinationTypeProperty.setHint(resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION_TYPE_HINT));
        propertyList.add(destinationTypeProperty);


        return propertyList;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {
        List<Property> propertyList = new ArrayList<Property>();

        // Topic
        Property topicProperty = new Property(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION);
        topicProperty.setDisplayName(
                resourceBundle.getString(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION));
        topicProperty.setRequired(true);
        propertyList.add(topicProperty);

        return propertyList;

    }


    /**
     * @param outputTransportMessageConfiguration
     *                - topic name to publish messages
     * @param message - is and Object[]{Event, EventDefinition}
     * @param outputTransportAdaptorConfiguration
     * @param tenantId
     */
    public void publish(
            OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration,
            Object message,
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration, int tenantId) {

        ConcurrentHashMap<String, PublisherDetails> topicEventSender = publisherMap.get(outputTransportAdaptorConfiguration.getName());
        if (null == topicEventSender) {
            topicEventSender = new ConcurrentHashMap<String, PublisherDetails>();
            if (null != publisherMap.putIfAbsent(outputTransportAdaptorConfiguration.getName(), topicEventSender)) {
                topicEventSender = publisherMap.get(outputTransportAdaptorConfiguration.getName());
            }
        }

        String topicName = outputTransportMessageConfiguration.getOutputMessageProperties().get(JMSTransportAdaptorConstants.TRANSPORT_JMS_DESTINATION);
        PublisherDetails publisherDetails = topicEventSender.get(topicName);
        try {
            if (null == publisherDetails) {

                Hashtable<String, String> adaptorProperties = new Hashtable<String, String>();
                adaptorProperties.putAll(outputTransportAdaptorConfiguration.getOutputProperties());


                JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, outputTransportAdaptorConfiguration.getName());
                Map<String, String> messageConfig = new HashMap<String, String>();
                messageConfig.put(JMSConstants.PARAM_DESTINATION, topicName);

                JMSMessageSender jmsMessageSender = new JMSMessageSender(jmsConnectionFactory, messageConfig);
                jmsMessageSender.send(message, messageConfig);

                publisherDetails = new PublisherDetails(jmsConnectionFactory, jmsMessageSender);
                topicEventSender.put(topicName, publisherDetails);

            } else {
                Map<String, String> messageConfig = new HashMap<String, String>();
                messageConfig.put(JMSConstants.PARAM_DESTINATION, topicName);
                publisherDetails.getJmsMessageSender().send(message, messageConfig);

            }

        } catch (RuntimeException e) {
            publisherDetails = topicEventSender.remove(topicName);
            if (publisherDetails != null) {
                publisherDetails.getJmsMessageSender().close();
                publisherDetails.getJmsConnectionFactory().stop();
            }
        }

    }


    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration, int tenantId) {
        try {
            Hashtable<String, String> adaptorProperties = new Hashtable<String, String>();
            adaptorProperties.putAll(outputTransportAdaptorConfiguration.getOutputProperties());

            JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, outputTransportAdaptorConfiguration.getName());
            Map<String, String> messageConfig = new HashMap<String, String>();
            messageConfig.put(JMSConstants.PARAM_DESTINATION, "TestTopic");

            JMSMessageSender jmsMessageSender = new JMSMessageSender(jmsConnectionFactory, messageConfig);
            jmsMessageSender.send("Test Message", messageConfig);
        } catch (Exception e) {
            throw new OutputTransportAdaptorEventProcessingException(e);
        }
    }


    class PublisherDetails {
        private final JMSConnectionFactory jmsConnectionFactory;
        private final JMSMessageSender jmsMessageSender;

        public PublisherDetails(JMSConnectionFactory jmsConnectionFactory,
                                JMSMessageSender jmsMessageSender) {
            this.jmsConnectionFactory = jmsConnectionFactory;
            this.jmsMessageSender = jmsMessageSender;
        }

        public JMSConnectionFactory getJmsConnectionFactory() {
            return jmsConnectionFactory;
        }

        public JMSMessageSender getJmsMessageSender() {
            return jmsMessageSender;
        }
    }


}
