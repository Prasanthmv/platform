/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.synapse.message.processor.impl.forwarder;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.util.MessageHelper;

import java.util.Iterator;
import java.util.Map;

public class BlockingMessageSender {
    public final static String DEFAULT_CLIENT_REPO = "./samples/axis2Client/client_repo"; // TODO : why we take it from the samples ????
    public final static String DEFAULT_AXIS2_XML = "./samples/axis2Client/client_repo/conf/axis2.xml";
    public final static String TARGET_MESSAGE_TYPE = "target.messageType";

    private static Log log = LogFactory.getLog(BlockingMessageSender.class);
    private String clientRepository = null;
    private String axis2xml = null;
    private ConfigurationContext configurationContext = null;
    private MessageStoreServiceClient sc = null;

    public void init() {
        try {
            configurationContext
                    = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    clientRepository != null ? clientRepository : DEFAULT_CLIENT_REPO,
                    axis2xml != null ? axis2xml : DEFAULT_AXIS2_XML);
            sc = new MessageStoreServiceClient(configurationContext, null);
        } catch (AxisFault e) {
            String msg = "Error initializing BlockingMessageSender : " + e.getMessage();
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }
    }

    public MessageContext send(EndpointDefinition endpoint, MessageContext messageIn)
            throws Exception {
        String serviceUrl = endpoint.getAddress();
        if(log.isDebugEnabled()) {
            log.debug("Start Sending the Message ");
        }
        try {
            MessageContext messageOut = MessageHelper.cloneMessageContext(messageIn);
            Options options = new Options();
            options.setTo(new EndpointReference(serviceUrl));
            if (messageIn.getSoapAction() != null) {
                options.setAction(messageIn.getSoapAction());
            } else {
                if (messageIn.isSOAP11()) {
                    options.setProperty(Constants.Configuration.DISABLE_SOAP_ACTION, true);
                } else {
                    Axis2MessageContext axis2smc = (Axis2MessageContext) messageOut;
                    org.apache.axis2.context.MessageContext axis2MessageCtx =
                            axis2smc.getAxis2MessageContext();
                    axis2MessageCtx.getTransportOut().addParameter(
                            new Parameter(HTTPConstants.OMIT_SOAP_12_ACTION, true));
                }
            }
            //After setting all the options we need to find the MEP of the Message
            org.apache.axis2.context.MessageContext axis2Ctx =
                    ((Axis2MessageContext) messageOut).getAxis2MessageContext();

            boolean outOnlyMessage = isOutOnly(messageIn, axis2Ctx);
            // Here We consider all other Messages that evaluates to outOnlyMessage == false
            // follows out in mep.
            if (log.isDebugEnabled()) {
                log.debug("Invoking service Url " + serviceUrl + " with Message" +
                        messageIn.getMessageID());
            }
            options.setProperty(
                    AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);

            //clean existing headers
            // otherwise when retrying same header element will add multiple times
            sc.removeHeaders();
            Iterator itr = axis2Ctx.getEnvelope().getHeader().getChildren();
            while (itr.hasNext()) {
                Object o =itr.next();
                if ( o instanceof OMElement ){
                    sc.addHeader((OMElement)o);
                }
            }
            if (endpoint.isUseMTOM()) {
                options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            } else if (endpoint.isUseSwa()) {
                options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
            }
            if(endpoint.isForcePOX()){
            	axis2Ctx.setDoingREST(true);
            }

            //Set whether to retry on SOAPFault
            options.setExceptionToBeThrownOnSOAPFault("true".equals(messageIn.getProperty(SynapseConstants.RETRY_ON_SOAPFAULT)));

            // Disable throwing a AxisFault on SOAPFault
            // options.setExceptionToBeThrownOnSOAPFault(false);  TODO: implement this properly
            sc.setOptions(options);
            OMElement result = null;
            boolean isDoingJSON = false;
            try {
                OMElement payload = axis2Ctx.getEnvelope().getBody();
                if (outOnlyMessage) {
                    sc.sendRobust(payload, axis2Ctx, endpoint, messageIn);
                } else {
                    org.apache.axis2.context.MessageContext resultMc
                            = sc.sendReceive(payload, axis2Ctx, endpoint, messageIn);
                    result = resultMc.getEnvelope().getBody();
                    isDoingJSON = sc.isDoingJSON(resultMc);
                }
            } catch (Exception axisFault) {
                // Here if Message is not a Out only Message
                // To indicate that it is a Error we set a new Message Context property
                // and return the message context
                // If its not we throw an Exception
                if (!outOnlyMessage) {
                    messageOut.setProperty(ForwardingProcessorConstants.BLOCKING_SENDER_ERROR, "true");
                    if (axisFault instanceof AxisFault) {
                        AxisFault fault = (AxisFault) axisFault;
                        messageOut.setProperty(SynapseConstants.ERROR_CODE,
                                               fault.getFaultCode() != null ?
                                               fault.getFaultCode().getLocalPart() : "");
                        messageOut.setProperty(SynapseConstants.ERROR_MESSAGE, fault.getMessage());
                        messageOut.setProperty(SynapseConstants.ERROR_DETAIL,
                                               fault.getDetail() != null ?
                                               fault.getDetail().getText() : "");
                        messageOut.setProperty(SynapseConstants.ERROR_EXCEPTION, axisFault);
                        org.apache.axis2.context.MessageContext faultMC = fault.getFaultMessageContext();
                        if (faultMC != null) {
                            messageOut.setProperty(NhttpConstants.HTTP_SC,
                                                   faultMC.getProperty("transport.http.statusCode"));
                            messageOut.setEnvelope(faultMC.getEnvelope());
                        }
                    }
                    return messageOut;
                }
                log.error("Error sending Message to url : " + serviceUrl, axisFault);
                throw new Exception("Error while Sending Message", axisFault);
            }

            if(!outOnlyMessage) {
                    if(result != null) {
					Map<String, org.apache.axis2.context.MessageContext> outMessageContexts = sc.getServiceContext()
					                                                                            .getLastOperationContext()
					                                                                            .getMessageContexts();
					if (outMessageContexts != null) {
						org.apache.axis2.context.MessageContext outmc = outMessageContexts.get("Out");
						if (outmc != null) {
							messageOut.setProperty(NhttpConstants.HTTP_SC,
							                       outmc.getProperty("transport.http.statusCode"));
							String soapNamespaceURI = axis2Ctx.getEnvelope().getNamespace().getNamespaceURI();
							SOAPEnvelope envelope = createSOAPEnvelope(result, soapNamespaceURI, isDoingJSON);
							axis2Ctx.setEnvelope(envelope);
							return messageOut;
						}
					}
					return messageOut;
                    }
            }
        } catch (AxisFault axisFault) {
            log.error("Error sending Message to url : " + serviceUrl ,axisFault);
            throw new Exception("Error while Sending message " , axisFault);
        }
        return null;
    }

    private boolean isOutOnly(MessageContext messageIn,
                              org.apache.axis2.context.MessageContext axis2Ctx) {
        if ("true".equals(messageIn.getProperty(SynapseConstants.OUT_ONLY))) {
            return true;
        }
        if (axis2Ctx.getOperationContext() != null) {
            return WSDL2Constants.MEP_URI_IN_ONLY.equals(
                    axis2Ctx.getOperationContext()
                            .getAxisOperation().getMessageExchangePattern());
        }
        return false;
    }

    public String getClientRepository() {
        return clientRepository;
    }

    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    public String getAxis2xml() {
        return axis2xml;
    }

    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }

    private SOAPEnvelope createSOAPEnvelope(OMElement payload , String soapNamespaceUri, boolean isDoingJSON) {
        SOAPFactory soapFactory;
        if (soapNamespaceUri.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        } else {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        }
        SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
        if (isDoingJSON) {
            Iterator<OMElement> elems = payload.getChildElements();
            while (elems.hasNext()) {
                OMElement elem = elems.next();
                envelope.getBody().addChild(elem);
            }
        } else if (payload.getFirstElement() != null) {
            envelope.getBody().addChild(payload.getFirstElement());
        }
        return envelope;
    }
}
