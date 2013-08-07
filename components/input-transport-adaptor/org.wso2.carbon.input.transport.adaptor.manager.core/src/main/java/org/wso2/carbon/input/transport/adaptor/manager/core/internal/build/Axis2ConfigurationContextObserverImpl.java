package org.wso2.carbon.input.transport.adaptor.manager.core.internal.build;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

public class Axis2ConfigurationContextObserverImpl
        extends AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(Axis2ConfigurationContextObserverImpl.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        //TODO check the usage of this method
        String tenantDomain = PrivilegedCarbonContext.getCurrentContext(
                configurationContext).getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getCurrentContext(
                configurationContext).getTenantId();
        System.out.println("--------------- A new Axis2 Configuration context is created for : " +
                           tenantDomain);
        log.info("Loading Input Transport Adaptors Specific to tenant when the tenant logged in");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getCurrentContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getCurrentContext().getTenantDomain(true);
        } catch (Exception e) {
            log.error("Unable to load input transport adaptors ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
