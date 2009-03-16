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

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hkrishna
 */
public class SimpleDataSource implements DataSource
{
    private static final Logger            _logger   = LoggerFactory.getLogger(SimpleDataSource.class);

    private static final Queue<Connection> _connPool = new ConcurrentLinkedQueue<Connection>();

    private String                         _jdbcUrl;
    private Properties                     _jdbcProps;
    private boolean                        _shutdown;

    public SimpleDataSource(String jdbcDriverClassName, String jdbcUrl, Properties jdbcProps)
    {
        try
        {
            Class.forName(jdbcDriverClassName);
        }
        catch (ClassNotFoundException e)
        {
            throw AggregateException.with(e, "JDBC driver class cannot be found: " + jdbcDriverClassName);
        }

        _jdbcUrl = jdbcUrl;
        _jdbcProps = jdbcProps;
    }

    public Connection getConnection() throws SQLException
    {
        return getConnection(null, null);
    }

    public Connection getConnection(String username, String password) throws SQLException
    {
        if (_shutdown)
            throw AggregateException.with(new IllegalAccessException(
                "Data source has been shutdown, cannot get connection."));

        Connection conn = pollConnection();

        if (conn != null)
            return conn;

        conn = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Connection.class },
            new InvocationHandler()
            {
                Connection _conn = DriverManager.getConnection(_jdbcUrl, _jdbcProps);

                public Object invoke(Object connProxy, Method method, Object[] args) throws Throwable
                {
                    if ("close".equals(method.getName()))
                    {
                        if (_shutdown)
                            _conn.close();
                        else
                            _connPool.offer((Connection) connProxy);

                        return null;
                    }
                    else
                    {
                        try
                        {
                            return method.invoke(_conn, args);
                        }
                        catch (InvocationTargetException e)
                        {
                            throw e.getCause();
                        }
                    }
                }
            });

        return conn;
    }

    private Connection pollConnection()
    {
        Connection conn = null;

        while ((conn = _connPool.poll()) != null)
        {
            try
            {
                conn.setAutoCommit(false);
                conn.rollback();
                break;
            }
            catch (SQLException e)
            {
                _logger.info("Possible stale connection, trashing " + conn, e);
            }
        }

        return conn;
    }

    public PrintWriter getLogWriter() throws SQLException
    {
        return null;
    }

    public int getLoginTimeout() throws SQLException
    {
        return 0;
    }

    public void setLogWriter(PrintWriter out) throws SQLException
    {
    }

    public void setLoginTimeout(int seconds) throws SQLException
    {
    }

    public void shutdown() throws SQLException
    {
        _shutdown = true;

        Connection conn = null;

        while ((conn = _connPool.poll()) != null)
            conn.close();
    }
}
