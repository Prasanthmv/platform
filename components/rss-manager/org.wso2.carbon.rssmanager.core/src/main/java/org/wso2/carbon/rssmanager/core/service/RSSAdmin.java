/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceMetaInfo;
import org.wso2.carbon.rssmanager.common.exception.RSSManagerCommonException;
import org.wso2.carbon.rssmanager.core.config.RSSConfigurationManager;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.entity.*;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.RSSManagerDataHolder;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RSSAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(RSSAdmin.class);

    public void createRSSInstance(RSSEnvironmentContext ctx,
                                  RSSInstance rssInstance) throws RSSManagerException {
        this.getRSSManager().createRSSInstance(ctx, rssInstance);
    }

    public void dropRSSInstance(RSSEnvironmentContext ctx,
                                String rssInstanceName) throws RSSManagerException {
        this.getRSSManager().dropRSSInstance(ctx, rssInstanceName);
    }

    public void editRSSInstance(RSSEnvironmentContext ctx,
                                RSSInstance rssInstance) throws RSSManagerException {
        this.getRSSManager().editRSSInstance(ctx, rssInstance);
    }

    public RSSInstance getRSSInstance(RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.getRSSManager().getRSSInstance(ctx, ctx.getRssInstanceName());
    }

    public RSSInstance[] getRSSInstances(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.getRSSManager().getRSSInstances(ctx);
    }

    public Database createDatabase(RSSEnvironmentContext ctx,
                               Database database) throws RSSManagerException {
        return this.getRSSManager().createDatabase(ctx, database);
    }

    public void dropDatabase(RSSEnvironmentContext ctx,
                             String databaseName) throws RSSManagerException {
        this.getRSSManager().dropDatabase(ctx, ctx.getRssInstanceName(), databaseName);
    }

    public Database[] getDatabases(RSSEnvironmentContext ctx) throws RSSManagerException {
        Database[] databases = new Database[0];
        try {
            databases = this.getRSSManager().getDatabases(ctx);
        } catch (RSSManagerException e) {
            String msg = "Error occurred while retrieving the database list of the tenant '" +
                    getTenantDomain() + "'";
            handleException(msg, e);
        }
        return databases;
    }

    public Database getDatabase(RSSEnvironmentContext ctx,
                                        String databaseName) throws RSSManagerException {
        return this.getRSSManager().getDatabase(ctx, ctx.getRssInstanceName(), databaseName);
    }

    public boolean isDatabaseExist(RSSEnvironmentContext ctx,
                                   String databaseName) throws RSSManagerException {
        return this.getRSSManager().isDatabaseExist(ctx, ctx.getRssInstanceName(), databaseName);
    }

    public boolean isDatabaseUserExist(RSSEnvironmentContext ctx,
                                       String databaseUsername) throws RSSManagerException {
        return this.getRSSManager().isDatabaseUserExist(ctx, ctx.getRssInstanceName(),
                databaseUsername);
    }

    public boolean isDatabasePrivilegesTemplateExist(
            RSSEnvironmentContext ctx, String templateName) throws RSSManagerException {
        return this.getRSSManager().isDatabasePrivilegeTemplateExist(ctx, templateName);
    }

    public DatabaseUser createDatabaseUser(RSSEnvironmentContext ctx,
                                   DatabaseUser user) throws RSSManagerException {
        return this.getRSSManager().createDatabaseUser(ctx, user);
    }

    public void dropDatabaseUser(RSSEnvironmentContext ctx,
                                 String username) throws RSSManagerException {
        this.getRSSManager().dropDatabaseUser(ctx, ctx.getRssInstanceName(), username);
    }

	public boolean deleteTenantRSSData(RSSEnvironmentContext ctx, int tenantId)
			throws RSSManagerException {
		return this.getRSSManager().deleteTenantRSSData(ctx, tenantId);
	}
	
    public void editDatabaseUserPrivileges(RSSEnvironmentContext ctx,
                                           DatabasePrivilegeSet privileges,
                                           DatabaseUser user,
                                           String databaseName) throws RSSManagerException {
        this.getRSSManager().editDatabaseUserPrivileges(ctx, privileges, user, databaseName);
    }

    public DatabaseUser getDatabaseUser(RSSEnvironmentContext ctx,
                                                String username) throws RSSManagerException {
        return this.getRSSManager().getDatabaseUser(ctx, ctx.getRssInstanceName(), username);
    }

    public DatabaseUser[] getDatabaseUsers(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.getRSSManager().getDatabaseUsers(ctx);
    }

    public void createDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.getRSSManager().createDatabasePrivilegesTemplate(ctx, template);
    }

    public void dropDatabasePrivilegesTemplate(RSSEnvironmentContext ctx,
                                               String templateName) throws RSSManagerException {
        this.getRSSManager().dropDatabasePrivilegesTemplate(ctx, templateName);
    }

    public void editDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx,
            DatabasePrivilegeTemplate template) throws RSSManagerException {
        this.getRSSManager().editDatabasePrivilegesTemplate(ctx, template);
    }

    public DatabasePrivilegeTemplate[] getDatabasePrivilegesTemplates(
            RSSEnvironmentContext ctx) throws RSSManagerException {
        return this.getRSSManager().getDatabasePrivilegeTemplates(ctx);
    }

    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
            RSSEnvironmentContext ctx, String templateName) throws RSSManagerException {
        return this.getRSSManager().getDatabasePrivilegeTemplate(ctx, templateName);
    }

    public void attachUserToDatabase(RSSEnvironmentContext ctx, UserDatabaseEntry ude,
                                     String templateName) throws RSSManagerException {
        this.getRSSManager().attachUserToDatabase(ctx, ude, templateName);
    }

    public void detachUserFromDatabase(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry ude) throws RSSManagerException {
        this.getRSSManager().detachUserFromDatabase(ctx, ude);
    }

    public DatabaseUser[] getUsersAttachedToDatabase(
            RSSEnvironmentContext ctx, String databaseName) throws RSSManagerException {
        return this.getRSSManager().getUsersAttachedToDatabase(ctx, ctx.getRssInstanceName(),
                databaseName);
    }

    public DatabaseUser[] getAvailableUsersToAttachToDatabase(
            RSSEnvironmentContext ctx, String databaseName) throws RSSManagerException {
        return this.getRSSManager().getAvailableUsersToAttachToDatabase(ctx,
                ctx.getRssInstanceName(), databaseName);
    }

    public void createCarbonDataSource(RSSEnvironmentContext ctx,
                                       UserDatabaseEntry entry) throws RSSManagerException {
        Database database = this.getRSSManager().getDatabase(ctx, entry.getRssInstanceName(),
                entry.getDatabaseName());
        DataSourceMetaInfo metaInfo =
                RSSManagerUtil.createDSMetaInfo(database, entry.getUsername());
        try {
            RSSManagerDataHolder.getInstance().getDataSourceService().addDataSource(metaInfo);
        } catch (DataSourceException e) {
            String msg = "Error occurred while creating carbon datasource for the database '" +
                    entry.getDatabaseName() + "'";
            handleException(msg, e);
        }
    }

    public DatabasePrivilegeSet getUserDatabasePermissions(
            RSSEnvironmentContext ctx, String databaseName,
            String username) throws RSSManagerException {
        return this.getRSSManager().getUserDatabasePrivileges(ctx, ctx.getRssInstanceName(),
                databaseName, username);
    }

    public Database[] getDatabasesForTenant(RSSEnvironmentContext ctx,
                                            String tenantDomain) throws RSSManagerException {
        int tenantId = -1;
        Database[] databases = null;
        if (!isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized. " +
                    "Tenant domain :" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain() +
                    " permission denied";
            throw new RSSManagerException(msg);
        }
        try {
            tenantId = RSSManagerUtil.getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            databases = this.getDatabases(ctx);
        } catch (RSSManagerCommonException e) {
            String msg = "Error occurred while retrieving database list of tenant '" +
                    tenantDomain + "'";
            throw new RSSManagerException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return databases;
    }

    public void createDatabaseForTenant(RSSEnvironmentContext ctx, Database database,
                                        String tenantDomain) throws RSSManagerException {
        if (!isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized to perform " +
                    "this operation permission denied";
            log.error(msg);
            throw new RSSManagerException(msg);
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            createDatabase(ctx, database);
        } catch (RSSManagerException e) {
            log.error("Error occurred while creating database for tenant : " + e.getMessage(), e);
            throw e;
        } catch (RSSManagerCommonException e) {
            String msg = "Error occurred while creating database '" + database.getName() +
                    "' for tenant '" + tenantDomain + "' on RSS instance '" +
                    database.getRssInstanceName() + "'";
            throw new RSSManagerException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public Database getDatabaseForTenant(RSSEnvironmentContext ctx, String databaseName,
            String tenantDomain) throws RSSManagerException {
        Database metaData = null;
        if (!isSuperTenantUser()) {
            String msg = "Unauthorized operation, only super tenant is authorized to perform " +
                    "this operation permission denied";
            log.error(msg);
            throw new RSSManagerException(msg);
        }
        try {
            int tenantId = RSSManagerUtil.getTenantId(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            metaData = this.getDatabase(ctx, databaseName);
        } catch (RSSManagerCommonException e) {
            String msg = "Error occurred while retrieving metadata of the database '" +
                    databaseName + "' belonging to tenant '" + tenantDomain + "'";
            throw new RSSManagerException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return metaData;
    }

    public String[] getRSSEnvironmentNames() throws RSSManagerException {
        return getRSSManager().getEnvironmentNames();
    }

     /**
     * Test the RSS instance connection using a mock database connection test.
     *
     * @param driverClass JDBC Driver class
     * @param jdbcURL     JDBC url
     * @param username    username
     * @param password    password
     * @return Success or failure message
     * @throws RSSManagerException RSSDAOException
     */
    public void testConnection(String driverClass, String jdbcURL, String username,
                               String password) throws RSSManagerException {
        Connection conn = null;
        int tenantId = RSSManagerUtil.getTenantId();

        if (driverClass == null || driverClass.length() == 0) {
            String msg = "Driver class is missing";
            throw new RSSManagerException(msg);
        }
        if (jdbcURL == null || jdbcURL.length() == 0) {
            String msg = "Driver connection URL is missing";
            throw new RSSManagerException(msg);
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            Class.forName(driverClass).newInstance();
            conn = DriverManager.getConnection(jdbcURL, username, password);
            if (conn == null) {
                String msg = "Unable to establish a JDBC connection with the database server";
                throw new RSSManagerException(msg);
            }
        } catch (Exception e) {
            String msg = "Error occurred while testing database connectivity : " + e.getMessage();
            handleException(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e);
                }
            }
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private boolean isSuperTenantUser() throws RSSManagerException {
        try {
            return (RSSManagerUtil.getTenantId(getTenantDomain()) ==
                    MultitenantConstants.SUPER_TENANT_ID);
        } catch (RSSManagerCommonException e) {
            throw new RSSManagerException("Error occurred while checking if the current tenant " +
                    "is super tenant", e);
        }
    }

    private RSSManager getRSSManager() throws RSSManagerException {
        RSSManager rssManager = RSSConfigurationManager.getInstance().getRSSManager();
        if (rssManager == null) {
            throw new RSSManagerException("RSS Manager is not initialized properly and " +
                    "thus, is null");
        }
        return rssManager;
    }

    private void handleException(String msg, Exception e) throws RSSManagerException {
        log.error(msg, e);
        throw new RSSManagerException(msg, e);
    }

}
