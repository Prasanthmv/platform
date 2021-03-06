/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.application.mgt.util;


import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementException;
import org.wso2.carbon.appfactory.application.mgt.service.UserInfoBean;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.core.ApplicationEventsListener;
import org.wso2.carbon.appfactory.core.ContinuousIntegrationSystemDriver;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.*;

/**
 *
 *
 */
public class Util {
    private static RegistryService registryService;
    private static RealmService realmService;
    private static AppFactoryConfiguration configuration;
    private static ContinuousIntegrationSystemDriver continuousIntegrationSystemDriver;
    public static String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
    public static String FIRST_NAME_CLAIM_URI = "http://wso2.org/claims/givenname";
    public static String LAST_NAME_CLAIM_URI = "http://wso2.org/claims/lastname";
    public static UserApplicationCache userApplicationCache = UserApplicationCache.getUserApplicationCache();

    /**
     * This set needs be a {@link SortedSet} ( e.g.{@link TreeSet} ) to preserve natural
     * ordering among {@link ApplicationEventsListener}s.
     * Refer
     * {@link ApplicationEventsListener#compareTo(ApplicationEventsListener)} to find out
     * how natural ordering occurs
     */
    private static Set<ApplicationEventsListener> applicationEventsListeners =
            Collections.synchronizedSet(new TreeSet<ApplicationEventsListener>());

    public static AppFactoryConfiguration getConfiguration() {
        return configuration;
    }

    public static void setConfiguration(AppFactoryConfiguration configuration) {
        Util.configuration = configuration;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }


    public static synchronized void setRegistryService(RegistryService reg) {

        registryService = reg;

    }

    public static synchronized void setRealmService(RealmService realmSer) {

        realmService = realmSer;

    }

    public static ContinuousIntegrationSystemDriver getContinuousIntegrationSystemDriver() {
        return continuousIntegrationSystemDriver;
    }

    public static void setContinuousIntegrationSystemDriver(ContinuousIntegrationSystemDriver continuousIntegrationSystemDriver) {
        Util.continuousIntegrationSystemDriver = continuousIntegrationSystemDriver;
    }


    public static void addApplicationEventsListener(ApplicationEventsListener applicationEventsListener) {
        applicationEventsListeners.add(applicationEventsListener);
    }

    public static void removeApplicationEventsListener(ApplicationEventsListener applicationEventsListener) {
        applicationEventsListeners.remove(applicationEventsListener);
    }

    public static Set<ApplicationEventsListener> getApplicationEventsListeners() {
        return applicationEventsListeners;
    }

    public static UserInfoBean getUserInfoBean(String userName) throws ApplicationManagementException {

        try {
            UserRealm realm =
                    Util.getRealmService()
                            .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID);
            String[] claims = {EMAIL_CLAIM_URI, FIRST_NAME_CLAIM_URI, LAST_NAME_CLAIM_URI};
            Map<String, String> userClaims = realm.getUserStoreManager().getUserClaimValues(userName, claims, null);
            
            /*String email =
                    realm.getUserStoreManager().getUserClaimValue(userName, EMAIL_CLAIM_URI,
                            null);
            String firstName =
                    realm.getUserStoreManager().getUserClaimValue(userName,
                            FIRST_NAME_CLAIM_URI,
                            null);
            String lastName =
                    realm.getUserStoreManager().getUserClaimValue(userName,
                            LAST_NAME_CLAIM_URI,
                            null);
            */

            String firstName = userClaims.get(FIRST_NAME_CLAIM_URI);
            String lastName = userClaims.get(LAST_NAME_CLAIM_URI);
            String email = userClaims.get(EMAIL_CLAIM_URI);
            StringBuilder displayNameBuilder = new StringBuilder();

            //Display name is constructed by concatenating first name and the last name of the user.
            if (StringUtils.isNotEmpty(firstName)) {
                displayNameBuilder.append(firstName);
            }

            if (StringUtils.isNotEmpty(lastName)) {
                displayNameBuilder.append(' ').append(lastName);
            }

            return new UserInfoBean(email, firstName,
                    lastName,
                    email, displayNameBuilder.toString());

            /*return new UserInfoBean(userName, firstName, lastName, email);*/
        } catch (UserStoreException e) {
            String msg = "Error while getting info for user " + userName;
            ;
            throw new ApplicationManagementException(msg, e);
        }
    }

    public static String[] getUniqueApplicationList(String[] roles) {
        Set<String> appSet = new HashSet<String>();
        for (String role : roles) {

            if (!(role.indexOf("_") < 0)) {
                appSet.add(role.substring(0, role.lastIndexOf("_")));
            }
        }
        return appSet.toArray(new String[appSet.size()]);
    }
}
