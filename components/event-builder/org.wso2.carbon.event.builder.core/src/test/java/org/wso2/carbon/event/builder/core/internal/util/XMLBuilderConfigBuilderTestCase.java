/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.core.config.InputMapping;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLInputMapping;
import org.wso2.carbon.event.builder.core.internal.type.xml.config.XPathDefinition;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class XMLBuilderConfigBuilderTestCase {
    private XMLBuilderConfigBuilder xmlBuilderConfigBuilder;
    private OMFactory omFactory;
    private XMLInputMapping predefinedInputMapping;
    private static final String XPATH_NS = "http://ws.cdyne.com/";
    private static final String XPATH_PREFIX = "quotedata";
    private static final String XML_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                             "<eventBuilder name=\"xmlbuilder\" xmlns=\"http://wso2.org/carbon/eventbuilder\">\n" +
                                             "  <from transportAdaptorName=\"localWsEventReceiver\" transportAdaptorType=\"ws-event-local\">\n" +
                                             "    <property name=\"topic\">AllStockQuotes</property>\n" +
                                             "  </from>\n" +
                                             "  <mapping type=\"xml\" batchProcessingEnabled=\"true\" >\n" +
                                             "    <xpathDefinition namespace=\"http://ws.cdyne.com/\" prefix=\"quotedata\"/>\n" +
                                             "    <property>\n" +
                                             "      <from xpath=\"//quotedata:StockQuoteEvent/quotedata:LastTradeAmount\"/>\n" +
                                             "      <to name=\"price\" type=\"double\"/>\n" +
                                             "    </property>\n" +
                                             "    <property>\n" +
                                             "      <from xpath=\"//quotedata:StockQuoteEvent/quotedata:StockSymbol\"/>\n" +
                                             "      <to name=\"symbol\" type=\"string\"/>\n" +
                                             "    </property>\n" +
                                             "  </mapping>\n" +
                                             "  <to streamName=\"stockQuotes\" version=\"1.0.0\"/>\n" +
                                             "</eventBuilder>\n";

    @Before
    public void init() {
        xmlBuilderConfigBuilder = XMLBuilderConfigBuilder.getInstance();
        omFactory = OMAbstractFactory.getOMFactory();
        // Initialize XML input mapping
        predefinedInputMapping = new XMLInputMapping();
        predefinedInputMapping.addInputMappingAttribute(new InputMappingAttribute("//quotedata:StockQuoteEvent/quotedata:LastTradeAmount", "price", AttributeType.DOUBLE));
        predefinedInputMapping.addInputMappingAttribute(new InputMappingAttribute("//quotedata:StockQuoteEvent/quotedata:StockSymbol", "symbol", AttributeType.STRING));
        predefinedInputMapping.setBatchProcessingEnabled(true);
        predefinedInputMapping.setXpathDefinition(new XPathDefinition(XPATH_PREFIX, XPATH_NS));
    }

    @Test
    public void testFromOm() throws XMLStreamException {
        OMElement omElement = AXIOMUtil.stringToOM(XML_CONFIG);
        OMElement mappingElement = omElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_MAPPING));
        InputMapping inputMapping = xmlBuilderConfigBuilder.fromOM(mappingElement);
        Assert.assertTrue(inputMapping instanceof XMLInputMapping);
        Assert.assertEquals(predefinedInputMapping, inputMapping);
    }


    @Test
    public void testInputMappingToOM() throws XMLStreamException {
        OMElement outputOmElement = xmlBuilderConfigBuilder.inputMappingToOM(predefinedInputMapping, omFactory);
        String formattedXml = XmlFormatter.format(outputOmElement.toString());
        OMElement docElement = AXIOMUtil.stringToOM(XML_CONFIG);
        OMElement mappingElement = docElement.getFirstChildWithName(new QName(EventBuilderConstants.EB_CONF_NS, EventBuilderConstants.EB_ELEMENT_MAPPING));
        String formattedOriginalXml = XmlFormatter.format(mappingElement.toString());
        Assert.assertEquals(formattedOriginalXml, formattedXml);
    }
}
