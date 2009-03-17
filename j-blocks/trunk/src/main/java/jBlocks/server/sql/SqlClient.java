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
 * Base class to manage access to a single database.
 * 
 * @author hkrishna
 */
public class SqlClient
{
    private DataManager                   _dataManager;
    private Map<String, SqlStmt>          _stmts;
    private ThreadLocal<List<SqlSession>> _threadSessions;

    /**
     * Creates a new client and registers itself in the context against the key - SqlClient.class.
     */
    public SqlClient(String jndiName, String dbId)
    {
        this(new DataManager(jndiName, dbId));
    }

    /**
     * Creates a new client and registers itself in the context against the key - SqlClient.class.
     */
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

    public void loadSchema(Class<?> clazz, String name)
    {
        Properties sqlProps = IOUtils.loadProperties(clazz.getResource(name + ".sql.properties"));

        URL url = clazz.getResource(name + '.' + _dataManager.dbId() + ".sql.properties");

        if (url != null)
            sqlProps.putAll(IOUtils.loadProperties(url));

        if (_stmts == null)
            _stmts = new HashMap<String, SqlStmt>();

        _stmts.putAll(new SqlMapParser().parse(sqlProps));
    }

    public SqlStmt stmt(String stmtId)
    {
        return _stmts.get(stmtId);
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

    public void endSession()
    {
        List<SqlSession> sessions = _threadSessions.get();
        _threadSessions.remove();

        if (sessions.isEmpty())
            return;

        for (SqlSession session : sessions)
            session.close();
    }

    public void setFetchSize(String stmtKey, int size)
    {
        _stmts.get(stmtKey).setProperty("setFetchSize", size);
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

    public SqlSession addBatch(String stmtId, Object param)
    {
        return session().addBatch(stmt(stmtId), param);
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
     * This method will run the provided task as a unit of work within a DB transaction. This method will try to recover
     * from broken DB connections and throws an {@link Error} when the recovery fails.
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

        Exception te = null;
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
            te = e;
            throw AggregateException.with(e, "DB transaction failed.");
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
                    throw AggregateException.with(
                        "DB transaction error. The database or the network is possibly down.", te, e);

                discardSession();

                try
                {
                    if (!committed)
                        transact(task, 2);
                }
                catch (Exception e2)
                {
                    throw new Error(AggregateException.with("DB transaction failed after two attempts.", e, e2)
                        .getLocalizedMessage(), te);
                }
            }
        }
    }
}
