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
import jBlocks.server.IOUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * Primary interface to manage access to a single database. This class creates
 * new {@link SqlSession sessions} as needed but these sessions will have to be
 * ended manually after the current, executing thread is done. That means you
 * have to call {@link #endSession()} at the end of each request in a web
 * application, for example. Failure to do so will result in unexpected
 * behavior. This class, however, is intended to be used as a singleton. Here's
 * a typical usage pattern.
 * 
 * <pre><code>
 * final SqlClient sqlClient = new SqlClient(&quot;jdbc/dataSourceName&quot;, &quot;myAppDb&quot;);
 * try
 * {
 *     sqlClient.loadSchema(MyDao.class, &quot;MySqlStatements&quot;);
 * 
 *     List data = sqlClient.select(&quot;mySqlStmt&quot;, paramModelObj);
 * 
 *     transact(new SqlTask&lt;RetType&gt;()
 *     {
 *         public RetType execute()
 *         {
 *             sqlClient.executeUpdate(&quot;myUpdateStmt&quot;, paramModelObj);
 *             sqlClient.executeUpdate(&quot;myDeleteStmt&quot;, paramModelObj);
 *             sqlClient.executeUpdate(&quot;myUpdateStmt2&quot;, paramModelObj);
 *             return sqlClient.select(&quot;mySqlStmt2&quot;, paramModelObj);
 *         }
 *     });
 * }
 * finally
 * {
 *     sqlClient.endSession();
 * }
 * </code></pre>
 * 
 * <p>
 * The sql properties file is where all the SQL statements and the SQL
 * configurations are provided. Here's the full list of properties that can be
 * provided in the sql properties file.
 * <ul>
 * <li><code>&lt;name&gt;.class = &lt;fully qualified Java class that will be mapped to the SQL&gt;</code></li>
 * <li><code>&lt;name&gt;.select-clause = &lt;select clause excluding the table columns&gt;</code></li>
 * <li><code>&lt;name&gt;.sql = &lt;sql statement&gt;</code></li>
 * <li><code>&lt;name&gt;.dyn-sql = &lt;dynamic sql statement&gt;</code></li>
 * <li><code>&lt;name&gt;.[sql | dyn-sql].fetch-size = &lt;JDBC fetch size&gt;</code></li>
 * </ul>
 * <p>
 * The sql statements are of 2 types - regular and dynamic. Regular sql
 * statements can have parameters, and dynamic sql statements can have
 * placeholders in addition to parameters as explained below.
 * <p>
 * Sql statement parameters are variables in the where clause specified between
 * two <code>#</code> symbol. For example, <code>#EmpId#</code>. In this
 * example, the framework will look for a <code>getEmpId</code> method in the
 * <code>paramModelObj</code>, provided in the
 * {@link #select(String, Object)} or the {@link #executeUpdate(String, Object)}
 * methods, and substitute the value returned by the method for the parameter.
 * There is also a special parameter form, denoted by <code>#.#</code>, that
 * substitutes the string form of the <code>paramModelObj</code> itself for
 * the parameter.
 * <p>
 * Sql statement placeholders are variables that can substitute any part of the
 * SQL statement as opposed to the just value in the where clause. Placeholders
 * are specified between two <code>@</code> symbols. For example,
 * {@code <code>@Predicate@</code>} works exactly like a parameter but the
 * <code>getPredicate</code> method can return an entire where clause built
 * dynamically in code.
 * <p>
 * Here's a sample sql properties file.
 * 
 * <pre><code>
 *  stmt-name.class = java.util.ArrayList
 *  stmt-name.select-clause = select top 10
 *   
 *  stmt-name.select.sql = \
 *      SELECT case_id WHERE name like '%' + #Pattern# + '%'
 *       
 *  stmt-name.select.dyn-sql = \
 *      SELECT case_id FROM {@code @tblName@} \
 *      WHERE {@code @predicate1@} and lst_updt_dtm &gt; #updtDt#
 *       
 *  stmt-name.select.dyn-sql.fetch-size = 100
 * </code></pre>
 * 
 * @author hkrishna
 */
public class SqlClient
{
    private DataManager                   _dataManager;
    private Map<String, SqlStmt>          _stmts;
    private ThreadLocal<List<SqlSession>> _threadSessions;

    public SqlClient(String jndiName, String dbId)
    {
        this(new DataManager(jndiName, dbId));
    }

    public SqlClient(DataSource dataSource, String dbId)
    {
        this(new DataManager(dataSource, dbId));
    }

    private SqlClient(DataManager dataManager)
    {
        _dataManager = dataManager;

        _threadSessions = new ThreadLocal<List<SqlSession>>()
        {
            @Override
            protected List<SqlSession> initialValue()
            {
                return new ArrayList<SqlSession>();
            }
        };
    }

    public DataManager dataManager()
    {
        return _dataManager;
    }

    /**
     * Loads, parses and caches the provided sql properties file.
     * 
     * @param clazz
     *            The class that will be used to load the sql properties file.
     * @param name
     *            The simple name of the sql properties file without the
     *            extension. The file name extension should be
     *            <code>.sql.properties</code>. The sql properties file must
     *            be placed in the classpath alongside the provided
     *            <code>clazz</code>.
     */
    public void loadSchema(Class<?> clazz, String name)
    {
        try
        {
            Properties sqlProps = IOUtils.loadProperties(clazz
                    .getResource(name + ".sql.properties"));

            URL url = clazz.getResource(name + '.' + _dataManager.dbId() + ".sql.properties");

            if (url != null)
                sqlProps.putAll(IOUtils.loadProperties(url));

            loadSchema(sqlProps);
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to load schema: " + name + ", from class: "
                    + clazz);
        }
    }

    /**
     * Parses and caches the provided sql properties.
     * 
     * @param sqlProps
     */
    public void loadSchema(Properties sqlProps)
    {
        if (_stmts == null)
            _stmts = new HashMap<String, SqlStmt>();

        _stmts.putAll(new SqlMapParser().parse(sqlProps));
    }

    private SqlStmt stmt(String stmtId)
    {
        SqlStmt stmt = _stmts.get(stmtId);

        if (stmt == null)
            throw new IllegalArgumentException("SQL statement not found - " + stmtId);

        return stmt;
    }

    private SqlSession newSession()
    {
        SqlSession session = new SqlSession(_dataManager.getConnection());

        _threadSessions.get().add(session);

        return session;
    }

    private SqlSession session()
    {
        List<SqlSession> sessions = _threadSessions.get();

        if (sessions.isEmpty())
            return newSession();

        return sessions.get(sessions.size() - 1);
    }

    private void discardSession()
    {
        List<SqlSession> sessions = _threadSessions.get();

        if (sessions.isEmpty())
            return;

        sessions.remove(sessions.size() - 1).close();
    }

    /**
     * Ends all sessions created by the calling thread.
     */
    public void endSession()
    {
        List<SqlSession> sessions = _threadSessions.get();
        _threadSessions.remove();

        if (sessions.isEmpty())
            return;

        for (SqlSession session : sessions)
            session.close();
    }

    /**
     * Sets the JDBC fetch size for the provided statement.
     * 
     * @param stmtKey
     *            The statement name in the sql properties file.
     * @param size
     *            The number of rows to fetch.
     */
    public void setFetchSize(String stmtKey, int size)
    {
        _stmts.get(stmtKey).setFetchSize(size);
    }

    public int selectInt(String stmtId, Object paramModel)
    {
        return session().selectInt(stmt(stmtId), paramModel);
    }

    public <T> T selectOne(String stmtId, Object paramModel)
    {
        return session().selectOne(stmt(stmtId), paramModel);
    }

    public <T> List<T> select(String stmtId, Object paramModel)
    {
        return session().select(stmt(stmtId), paramModel);
    }

    public <T> List<T> select(String stmtId, Object paramModel, RowCallback<T> callback)
    {
        return session().select(stmt(stmtId), paramModel, callback);
    }

    public void addBatch(String stmtId, Object param)
    {
        session().addBatch(stmt(stmtId), param);
    }

    public int[] executeBatch(String stmtId)
    {
        return session().executeBatch(stmt(stmtId));
    }

    public int executeUpdate(String stmtId, Object param)
    {
        return session().executeUpdate(stmt(stmtId), param);
    }

    /**
     * This method will run the provided task as a unit of work within a DB
     * transaction. This method will try to recover from broken DB connections
     * and throws an {@link Exception} when the recovery fails.
     * 
     * @throws Error
     *             when the DB connection is broken and recovery attempts fail.
     */
    public <V> V transact(SqlTask<V> task)
    {
        try
        {
            return transact(task, 1);
        }
        finally
        {
            // Discard nested session
            if (_threadSessions.get().size() > 1)
                discardSession();
        }
    }

    private <V> V transact(SqlTask<V> task, int attempt)
    {
        SqlSession session = session();

        if (session.isInTransaction())
            session = newSession();

        AggregateException te = null;
        boolean committed = false;

        try
        {
            session.startTransaction();
            V result = task.execute();
            session.commit();

            committed = true;

            return result;
        }
        catch (Exception e)
        {
            throw te = AggregateException.with(e, "DB transaction failed.");
        }
        finally
        {
            try
            {
                session.endTransaction();
            }
            catch (Exception e)
            {
                if (attempt > 1)
                    throw AggregateException.with(AggregateException.with(te, e.toString()),
                            "DB transaction error. The database or the network is possibly down.");

                discardSession();

                try
                {
                    if (!committed)
                        transact(task, 2);
                }
                catch (Exception e2)
                {
                    throw AggregateException.with(AggregateException.with(AggregateException.with(
                            te, e.toString()), e2.toString()),
                            "DB transaction failed after two attempts.");
                }
            }
        }
    }
}
