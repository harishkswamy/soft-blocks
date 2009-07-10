// Copyright 2007 Harish Krishnaswamy
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package jBlocks.server.sql;

import jBlocks.server.AggregateException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataManager
{
    private String     _dbId;
    private String     _jndiName;
    private DataSource _dataSource;

    DataManager(String jndiName, String dbId)
    {
        _dbId = dbId;
        _jndiName = jndiName;
    }

    DataManager(DataSource dataSource, String dbId)
    {
        _dbId = dbId;
        _dataSource = dataSource;
    }

    Connection getConnection()
    {
        if (_dataSource == null)
            loadDataSource();

        Connection conn = null;

        // Try twice before throwing an exception 
        for (int i = 0;; i++)
        {
            try
            {
                conn = _dataSource.getConnection();
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                break;
            }
            catch (SQLException e)
            {
                if (i > 0)
                    throw AggregateException.with(e, "unable-to-get-jdbc-connection");
            }
        }

        return conn;
    }

    private void loadDataSource()
    {
        try
        {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            _dataSource = (DataSource) envContext.lookup(_jndiName);
        }
        catch (NamingException e)
        {
            throw AggregateException.with(e, "unable-to-lookup-jdbc-datasource");
        }
    }

    public String dbId()
    {
        return _dbId;
    }
}
