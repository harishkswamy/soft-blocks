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
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Base class to manage database access to a particular schema.
 * <p>
 * This class is thread-safe and and is intended to be used as a singleton.
 * <p>
 * 
 * @author hkrishna
 */
public abstract class SqlSchema
{
    private DataManager          _dataManager;
    private Map<String, SqlStmt> _stmts;

    private boolean              _concurrentSession;
    private SqlSession           _session;

    protected SqlSchema(DataManager dataManager, String sqlPropsFileName)
    {
        if (dataManager == null)
            throw new IllegalArgumentException("DataManager must be provided.");

        _dataManager = dataManager;

        Properties sqlProps = IOUtils.loadProperties(getClass().getResource(sqlPropsFileName + ".sql.properties"));

        URL url = getClass().getResource(sqlPropsFileName + '.' + _dataManager.dbId() + ".sql.properties");

        if (url != null)
            sqlProps.putAll(IOUtils.loadProperties(url));

        _stmts = new SqlMapParser().parse(sqlProps);
    }

    protected SqlSchema(SqlSchema schema)
    {
        _dataManager = schema._dataManager;
        _stmts = schema._stmts;
        _concurrentSession = true;
    }

    protected abstract SqlSchema newSession();

    protected DataManager dataManager()
    {
        return _dataManager;
    }

    protected SqlStmt stmt(String stmtId)
    {
        return _stmts.get(stmtId);
    }

    protected SqlSession session()
    {
        if (_concurrentSession)
        {
            if (_session == null)
                _session = _dataManager.newSession();

            return _session;
        }

        return _dataManager.threadSession();
    }

    protected void discardSession()
    {
        if (_session == null)
            _dataManager.discardThreadSession();
        else
            _session.close();
    }

    protected void setFetchSize(String stmtKey, int size)
    {
        _stmts.get(stmtKey).setProperty("setFetchSize", size);
    }

    protected int selectInt(String stmtId, Object paramModel)
    {
        return session().selectInt(stmt(stmtId), paramModel);
    }

    protected <T> T selectOne(String stmtId, Object paramModel)
    {
        return session().selectOne(stmt(stmtId), paramModel);
    }

    protected <T> List<T> select(String stmtId, Object paramModel)
    {
        return session().select(stmt(stmtId), paramModel);
    }

    protected <T> List<T> select(String stmtId, Object paramModel, RowCallback<T> callback)
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
        return transact(task, 1);
    }

    /**
     * This method is exactly the same as {@link #transact(Runnable)} except this method will run the transaction and
     * close the session at the end.
     * 
     * @throws Error
     *             when the DB connection is broken and recovery attempts fail.
     */
    public <V> V transactAndEndSession(SqlTask<V> task)
    {
        try
        {
            return transact(task, 1);
        }
        finally
        {
            session().close();
        }
    }

    private <V> V transact(SqlTask<V> task, int attempt)
    {
        SqlSession session = session();

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
