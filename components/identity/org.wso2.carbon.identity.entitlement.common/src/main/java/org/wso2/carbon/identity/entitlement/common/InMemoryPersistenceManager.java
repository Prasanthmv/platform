/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.entitlement.common.dto.PolicyEditorDataHolder;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class InMemoryPersistenceManager implements DataPersistenceManager{
    
    private Map<String,String> xmlConfig = new HashMap<String, String>();

    private static Log log = LogFactory.getLog(InMemoryPersistenceManager.class);

    @Override
    public Map<String, PolicyEditorDataHolder> buildDataHolder() throws PolicyEditorException {
        xmlConfig = this.getConfig();
        Map<String, PolicyEditorDataHolder>  holders = new HashMap<String, PolicyEditorDataHolder>();
        for(String type : EntitlementConstants.PolicyEditor.EDITOR_TYPES){
            PolicyEditorDataHolder holder = buildDataHolder(type, xmlConfig.get(type));
            if(holder != null){
                holders.put(type, holder);
            }
        }
        return holders;
    }

    private PolicyEditorDataHolder buildDataHolder(String type, String xmlConfig)  throws PolicyEditorException{

        if(xmlConfig == null){
            return null;
        }

        PolicyEditorDataHolder holder = new PolicyEditorDataHolder();
        ByteArrayInputStream inputStream;
        Element root = null;

        inputStream = new ByteArrayInputStream(xmlConfig.getBytes());
        DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
        try {
            Document doc = builder.newDocumentBuilder().parse(inputStream);
            root = doc.getDocumentElement();
        } catch (Exception e) {
            log.error("DOM of request element can not be created from String", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Error in closing input stream of XACML request");
            }
        }
        
        if(root == null){
            return holder;
        }

        NodeList nodeList = root.getChildNodes();
        
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if(node.getNodeName().equals("categories")){
                parseCategories(type, node, holder);
            } else if(node.getNodeName().equals("ruleCombiningAlgorithm")){
                parseRuleAlgorithm(node, holder);
            } else if(node.getNodeName().equals("attributeIds")){
                parseAttributeIds(node, holder);
            } else if(node.getNodeName().equals("dataTypes")){
                parseDataTypes(node, holder);
            } else if(node.getNodeName().equals("functions")){
                parseFunctions(node, holder);
            } else if(node.getNodeName().equals("preFunctions")){
                parsePreFunctions(node, holder);
            } else if(node.getNodeName().equals("rule")){
                parseRule(node, holder);
            } else if(node.getNodeName().equals("policyDescription")){
                if("true".equals(node.getTextContent())){
                    holder.setShowPolicyDescription(true);
                }
            }
        }
        
        return holder;
    }

    @Override
    public void persistConfig(String policyEditorType, String xmlConfig) throws PolicyEditorException {
        // to verify
        buildDataHolder(policyEditorType, xmlConfig);
        this.xmlConfig.put(policyEditorType, xmlConfig);
    }

    @Override
    public Map<String, String> getConfig() {
        return xmlConfig;
    }

    private void parseCategories(String type, Node root, PolicyEditorDataHolder holder) throws PolicyEditorException {

        NodeList nodeList = root.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if("category".equals(node.getNodeName())){

                String name = null;
                String uri = null;
                Set<String> attributeIds = null;
                Set<String> dataTypes = null;

                NodeList childList = node.getChildNodes();
                for(int j = 0; j < childList.getLength(); j++){
                    Node child = childList.item(j);
                    if("name".equals(child.getNodeName())){
                        name = child.getTextContent();
                        if(EntitlementConstants.PolicyEditor.BASIC.equals(type)){
                            if(!Utils.isValidCategory(name)){
                                throw new PolicyEditorException("Invalid Category : " + name
                                + "  Basic policy editor supports only for Subject, " +
                                        "Resource, Action and Environment category names. " +
                                        "But you can change the URI of them");
                            }
                        }
                    } else if("uri".equals(child.getNodeName())){
                        uri = child.getTextContent();
                    } else if("supportedAttributeIds".equals(child.getNodeName())){
                        attributeIds = new HashSet<String>();
                        NodeList list = child.getChildNodes();                        
                        for(int k = 0; k < list.getLength(); k++){
                            Node nextChild = list.item(k);
                            if("attributeId".equals(nextChild.getNodeName())){
                                attributeIds.add(nextChild.getTextContent());
                            }
                        }
                    } else if("supportedDataTypes".equals(child.getNodeName())){
                        dataTypes = new HashSet<String>();
                        NodeList list = child.getChildNodes();
                        for(int k = 0; k < list.getLength(); k++){
                            Node nextChild = list.item(k);
                            if("dataType".equals(nextChild.getNodeName())){
                                dataTypes.add(nextChild.getTextContent());
                            }
                        }
                    }
                }
                if(name != null){
                    if(uri != null){
                        holder.getCategoryMap().put(name, uri);
                    }
                    if(attributeIds != null){
                        holder.getCategoryAttributeIdMap().put(name, attributeIds);
                    }
                    if(dataTypes != null){
                        holder.getCategoryDataTypeMap().put(name, dataTypes);
                    }
                }
            }
        }               
    }

    private void parseRuleAlgorithm(Node root, PolicyEditorDataHolder holder)
                                                                    throws PolicyEditorException {

        NodeList nodeList = root.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if("algorithms".equals(node.getNodeName())){
                String name = null;
                String uri = null;
                NodeList childList = node.getChildNodes();
                for(int j = 0; j < childList.getLength(); j++){
                    Node child = childList.item(j);
                    if("algorithm".equals(child.getNodeName())){
                        NodeList list = child.getChildNodes();
                        for(int k = 0; k < list.getLength(); k++){
                            Node nextChild = list.item(k);
                            if("name".equals(nextChild.getNodeName())){
                                name = nextChild.getTextContent();
                            } else if("uri".equals(nextChild.getNodeName())){
                                uri = nextChild.getTextContent();
                            }
                            if(name != null && uri != null){
                                if(!Utils.isValidRuleAlgorithm(uri)){
                                    throw new PolicyEditorException("Invalid Algorithm : " + uri);
                                }
                                holder.getRuleCombiningAlgorithms().put(name, uri);
                            }
                        }
                    }
                }
            } else if("display".equals(node.getNodeName())){
                if("true".equals(node.getTextContent())){
                    holder.setShowRuleAlgorithms(true);
                }
            } else if("defaultAlgorithm".equals(node.getNodeName())){
                holder.setDefaultRuleAlgorithm(node.getTextContent());
            }
        }
    }

    private void parseAttributeIds(Node root, PolicyEditorDataHolder holder){

        NodeList nodeList = root.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if("attributeId".equals(node.getNodeName())){

                String name = null;
                String uri = null;
                String dataType = null;

                NodeList childList = node.getChildNodes();
                for(int j = 0; j < childList.getLength(); j++){
                    Node child = childList.item(j);
                    if("name".equals(child.getNodeName())){
                        name = child.getTextContent();
                    } else if("uri".equals(child.getNodeName())){
                        uri = child.getTextContent();
                    } else if("dataType".equals(child.getNodeName())){
                        dataType = child.getTextContent();
                    }
                }
                if(name != null){
                    if(uri != null){
                        holder.getAttributeIdMap().put(name, uri);
                    }
                    if(dataType != null){
                        holder.getAttributeIdDataTypeMap().put(name, dataType);
                    }
                }
            }
        }
    }

    private void parseDataTypes(Node root, PolicyEditorDataHolder holder) throws PolicyEditorException {

        NodeList nodeList = root.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if("dataType".equals(node.getNodeName())){

                String name = null;
                String uri = null;

                NodeList childList = node.getChildNodes();
                for(int j = 0; j < childList.getLength(); j++){
                    Node child = childList.item(j);
                    if("name".equals(child.getNodeName())){
                        name = child.getTextContent();
                    } else if("uri".equals(child.getNodeName())){
                        uri = child.getTextContent();
                    }
                }
                if(name != null && uri != null){
                    if(!Utils.isValidDataType(uri)){
                        throw new PolicyEditorException("Invalid DataType : " + uri);
                    }
                    holder.getDataTypeMap().put(name, uri);
                }
            }
        }
    }

    private void parseFunctions(Node root, PolicyEditorDataHolder holder) throws PolicyEditorException {

        NodeList nodeList = root.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if("function".equals(node.getNodeName())){

                String name = null;
                String uri = null;
                boolean  targetFunction = false;

                NodeList childList = node.getChildNodes();
                for(int j = 0; j < childList.getLength(); j++){
                    Node child = childList.item(j);
                    if("name".equals(child.getNodeName())){
                        name = child.getTextContent();
                    } else if("uri".equals(child.getNodeName())){
                        uri = child.getTextContent();
                    } else if("targetFunction".equals(child.getNodeName())){
                        targetFunction = true;
                    }
                }
                if(name != null && uri != null){
                    if(!Utils.isValidFunction(uri)){
                        throw new PolicyEditorException("Invalid Function : " + uri);
                    }
                    holder.getFunctionMap().put(name, uri);
                    holder.getRuleFunctions().add(name);
                    if(targetFunction){
                        holder.getTargetFunctions().add(name);
                    }
                }
            }
        }
    }

    private void parsePreFunctions(Node root, PolicyEditorDataHolder holder) throws PolicyEditorException {

        NodeList nodeList = root.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if("preFunction".equals(node.getNodeName())){

                String name = null;
                String uri = null;

                NodeList childList = node.getChildNodes();
                for(int j = 0; j < childList.getLength(); j++){
                    Node child = childList.item(j);
                    if("name".equals(child.getNodeName())){
                        name = child.getTextContent();
                    } else if("uri".equals(child.getNodeName())){
                        uri = child.getTextContent();
                    }
                }
                if(name != null && uri != null){
                    if(!Utils.isValidPreFunction(uri)){
                        throw new PolicyEditorException("Invalid PreFunction : " + uri);
                    }
                    holder.getPreFunctionMap().put(name, uri);
                }
            }
        }
    }

    private void parseRule(Node root, PolicyEditorDataHolder holder) throws PolicyEditorException {

        NodeList nodeList = root.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if("ruleId".equals(node.getNodeName())){
                if("true".equals(node.getTextContent())){
                    holder.setShowRuleId(true);
                }
            } else if("ruleEffect".equals(node.getNodeName())) {
                NodeList childList = node.getChildNodes();
                for(int j = 0; j < childList.getLength(); j++){
                    Node child = childList.item(j);
                    if("display".equals(child.getNodeName())){
                        if("true".equals(child.getTextContent())){
                            holder.setShowRuleEffect(true);
                        }
                    } else if("defaultEffect".equals(child.getNodeName())){
                        if(child.getTextContent() != null){
                            String uri = child.getTextContent();
                            if(!Utils.isValidEffect(uri)){
                                throw new PolicyEditorException("Invalid Rule Effect : " + uri);
                            }
                            holder.setDefaultEffect(child.getTextContent());
                        }
                    } else if("effect".equals(child.getNodeName())){
                        NodeList childList1 = child.getChildNodes();
                        String name = null;
                        String uri = null;
                        for(int k = 0; k < childList1.getLength(); k++){
                            Node child1 = childList1.item(k);
                            if("name".equals(child1.getNodeName())){
                                if(child1.getTextContent() != null ){
                                    name = child1.getTextContent();
                                }
                            } else if("uri".equals(child1.getNodeName())){
                                if(child1.getTextContent() != null ){
                                    uri = child1.getTextContent();
                                }
                            }
                        }

                        if(name != null && uri != null){
                            if(!Utils.isValidEffect(uri)){
                                throw new PolicyEditorException("Invalid Rule Effect : " + uri);
                            }
                            holder.getRuleEffectMap().put(name, uri);
                        }

                        if(child.getTextContent() != null){
                            holder.setDefaultEffect(child.getTextContent());
                        }
                    }
                }
            } else if("lastRule".equals(node.getNodeName())) {
                NodeList childList = node.getChildNodes();
                for(int j = 0; j < childList.getLength(); j++){
                    Node child = childList.item(j);
                    if("add".equals(child.getNodeName())){
                        if("true".equals(child.getTextContent())){
                            holder.setAddLastRule(true);
                        }
                    } else if("effect".equals(child.getNodeName())){
                        if(child.getTextContent() != null){
                            String uri = child.getTextContent();
                            if(!Utils.isValidEffect(uri)){
                                throw new PolicyEditorException("Invalid Rule Effect : " + uri);
                            }                            
                            holder.setLastRuleEffect(uri);
                        }
                    }
                }
            }
        }
    }

    protected String getDefaultConfig(){
        
        return "<policyEditor>\n" +
                "    <categories>\n" +
                "        <category>\n" +
                "            <name>Subject</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:1.0:subject-category:access-subject</uri>\n" +
                "            <supportedAttributeIds>\n" +
                "                <attributeId>UserName</attributeId>\n" +
                "                <attributeId>Email</attributeId>\n" +
                "                <attributeId>Roles</attributeId>\n" +
                "                <attributeId>Age</attributeId>\n" +
                "            </supportedAttributeIds>\n" +
                "        </category>\n" +
                "        <category>\n" +
                "            <name>Resource</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:3.0:attribute-category:resource</uri>\n" +
                "            <supportedAttributeIds>\n" +
                "                <attributeId>resource-id</attributeId>\n" +
                "            </supportedAttributeIds>\n" +
                "        </category>\n" +
                "        <category>\n" +
                "            <name>Action</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:3.0:attribute-category:action</uri>\n" +
                "            <supportedAttributeIds>\n" +
                "                <attributeId>action-id</attributeId>\n" +
                "            </supportedAttributeIds>\n" +
                "        </category>\n" +
                "        <category>\n" +
                "            <name>Environment</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:3.0:attribute-category:environment</uri>\n" +
                "            <supportedAttributeIds>\n" +
                "                <attributeId>Domain</attributeId>\n" +
                "\t\t<attributeId>Date</attributeId>\n" +
                "\t\t<attributeId>Time</attributeId>\n" +
                "\t\t<attributeId>DateTime</attributeId>\n" +
                "            </supportedAttributeIds>\n" +
                "        </category>\n" +
                "    </categories>\n" +
                "    <attributeIds>\n" +
                "        <attributeId>\n" +
                "            <name>resource-id</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:1.0:resource:resource-id</uri>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>action-id</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:1.0:action:action-id</uri>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>UserName</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:1.0:subject:subject-id</uri>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>Role</name>\n" +
                "            <uri>http://wso2.org/claims/role</uri>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>Email</name>\n" +
                "            <uri>http://wso2.org/claims/emailaddress</uri>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>Environment</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:1.0:environment:environment-id</uri>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>Domain</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:1.0:environment:environment-id</uri>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>Time</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:1.0:environment:current-time</uri>\n" +
                "            <dataType>http://www.w3.org/2001/XMLSchema#time</dataType>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>Date</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:1.0:environment:current-date</uri>\n" +
                "\t    <dataType>http://www.w3.org/2001/XMLSchema#date</dataType>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>DateTime</name>\n" +
                "            <uri>urn:oasis:names:tc:xacml:1.0:environment:current-dateTime</uri>\n" +
                "\t    <dataType>http://www.w3.org/2001/XMLSchema#dateTime</dataType>\n" +
                "        </attributeId>\n" +
                "        <attributeId>\n" +
                "            <name>Age</name>\n" +
                "            <uri>http://wso2.org/claims/age</uri>\n" +
                "            <dataType>http://www.w3.org/2001/XMLSchema#integer</dataType>\n" +
                "        </attributeId>\n" +
                "    </attributeIds>\n" +
                "    <dataTypes>    \n" +
                "    </dataTypes>\n" +
                "    <ruleCombiningAlgorithm>\n" +
                "        <display>true</display>\n" +
                "        <defaultAlgorithm>first-applicable</defaultAlgorithm>\n" +
                "        <algorithms>\n" +
                "            <algorithm>\n" +
                "                <name>Deny Overrides</name>\n" +
                "                <uri>urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides</uri>\n" +
                "            </algorithm>\n" +
                "            <algorithm>\n" +
                "                <name>First Applicable</name>\n" +
                "                <uri>urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable</uri>\n" +
                "            </algorithm>\n" +
                "            <algorithm>\n" +
                "                <name>Permit Overrides</name>\n" +
                "                <uri>urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides</uri>\n" +
                "            </algorithm>\n" +
                "            <algorithm>\n" +
                "                <name>Deny Unless Permit</name>\n" +
                "                <uri>urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit</uri>\n" +
                "            </algorithm>\n" +
                "            <algorithm>\n" +
                "                <name>Permit Unless Deny</name>\n" +
                "                <uri>urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny</uri>\n" +
                "            </algorithm>\n" +
                "        </algorithms>\n" +
                "    </ruleCombiningAlgorithm>\n" +
                "    <functions>\n" +
                "        <function>\n" +
                "            <name>equal</name>\n" +
                "            <uri>equal</uri>\n" +
                "            <targetFunction>true</targetFunction>\n" +
                "        </function>\n" +
                "        <function>\n" +
                "            <name>equals-with-regexp-match</name>\n" +
                "            <uri>regexp-match</uri>\n" +
                "\t    <targetFunction>true</targetFunction>\n" +
                "        </function>\n" +
                "        <function>\n" +
                "            <name>at-least-one-member-of</name>\n" +
                "            <uri>at-least-one-member-of</uri>\n" +
                "        </function>\n" +
                "        <function>\n" +
                "            <name>is-in</name>\n" +
                "            <uri>is-in</uri>\n" +
                "        </function>\n" +
                "        <function>\n" +
                "            <name>set-equals</name>\n" +
                "            <uri>set-equals</uri>\n" +
                "        </function>\n" +
                "    </functions>\n" +
                "    <preFunctions>\n" +
                "        <preFunction>\n" +
                "            <name>is/are</name>\n" +
                "            <uri>is</uri>\n" +
                "        </preFunction>\n" +
                "        <preFunction>\n" +
                "            <name>is not/are not</name>\n" +
                "            <uri>not</uri>\n" +
                "        </preFunction>\n" +
                "    </preFunctions>\n" +
                "    <policyDescription>\n" +
                "        <display>true</display>\n" +
                "    </policyDescription>\n" +
                "    <rule>\n" +
                "        <ruleId>true</ruleId>\n" +
                "        <ruleEffect>\n" +
                "            <display>true</display>\n" +
                "            <defaultEffect>Permit</defaultEffect>\n" +
                "\t    \t<effect>\n" +
                "            \t\t<name>Permit</name>\n" +
                "            \t\t<uri>Permit</uri>\n" +
                "\t\t</effect>\n" +
                "\t    \t<effect>\n" +
                "            \t\t<name>Deny</name>\n" +
                "            \t\t<uri>Deny</uri>\n" +
                "\t\t</effect>\t\n" +
                "        </ruleEffect>\n" +
                "        <lastRule>\n" +
                "            <add>false</add>\n" +
                "            <effect>Deny</effect>\n" +
                "        </lastRule>\n" +
                "    </rule>\n" +
                "</policyEditor>";
    }
}
