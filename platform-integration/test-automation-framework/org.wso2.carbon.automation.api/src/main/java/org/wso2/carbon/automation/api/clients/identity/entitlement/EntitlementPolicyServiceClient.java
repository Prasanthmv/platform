/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.api.clients.identity.entitlement;

import org.apache.axis2.AxisFault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceIdentityException;
import org.wso2.carbon.identity.entitlement.stub.EntitlementPolicyAdminServiceStub;
import org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO;
import org.wso2.carbon.statistics.stub.types.axis2.context.ConfigurationContext;
import org.wso2.carbon.ui.util.CharacterEncoder;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.rmi.RemoteException;

public class EntitlementPolicyServiceClient {
    private static final Log log = LogFactory.getLog(EntitlementPolicyServiceClient.class);

    private final String serviceName = "EntitlementPolicyAdminService";
    private EntitlementPolicyAdminServiceStub entitlementPolicyAdminServiceStub;
    private String endPoint;

    public EntitlementPolicyServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        entitlementPolicyAdminServiceStub = new EntitlementPolicyAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, entitlementPolicyAdminServiceStub);
    }

    public EntitlementPolicyServiceClient(String backEndUrl, String userName, String password)
            throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        entitlementPolicyAdminServiceStub = new EntitlementPolicyAdminServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, entitlementPolicyAdminServiceStub);

    }


    public void addPolicies(File policyFile) throws IOException, EntitlementPolicyAdminServiceIdentityException, ParserConfigurationException, TransformerException, SAXException {

        DataHandler policydh =
                new DataHandler(new FileDataSource(policyFile));
        String policy = convertXMLFileToString(policyFile);


        PolicyDTO policySetDTO = new PolicyDTO();
        policySetDTO.setPolicy(policy);
        policySetDTO.setActive(true);
        entitlementPolicyAdminServiceStub.addPolicy(policySetDTO);
    }

    public void removePolicy(File policyFile) throws IOException, EntitlementPolicyAdminServiceIdentityException, ParserConfigurationException, TransformerException, SAXException {

        DataHandler policydh =
                new DataHandler(new FileDataSource(policyFile));
        String policy = convertXMLFileToString(policyFile);
        PolicyDTO policySetDTO = new PolicyDTO();
        policySetDTO.setPolicy(policy);
        entitlementPolicyAdminServiceStub.removePolicy(policySetDTO);
    }


    private String convertXMLFileToString(File fileName) throws IOException, ParserConfigurationException, SAXException, TransformerException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        InputStream inputStream = new FileInputStream(fileName);
        org.w3c.dom.Document doc = documentBuilderFactory.newDocumentBuilder().parse(inputStream);
        StringWriter stw = new StringWriter();
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.transform(new DOMSource(doc), new StreamResult(stw));
        return stw.toString();
    }
}
