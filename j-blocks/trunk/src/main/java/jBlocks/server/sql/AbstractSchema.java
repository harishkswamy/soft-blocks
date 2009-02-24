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
import java.util.concurrent.Callable;

/**
 * Base class to manage database access to a particular schema.
 * <p>
 * This class is thread-safe and and is intended to be used as a singleton.
 * <p>
 * 
 * @author hkrishna
 */
public abstract class AbstractSchema
{
    private DataManager          _dataManager;
    private Map<String, SqlStmt> _stmts;

    protected AbstractSchema(DataManager dataManager, String sqlPropsFileName)
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

    protected DataManager dataManager()
    {
        return _dataManager;
    }

    protected SqlStmt stmt(String stmtId)
    {
        return _stmts.get(stmtId);
    }

    /**
     * @return The {@link SqlSession} for the current thread.
     */
    protected SqlSession session()
    {
        return _dataManager.threadSession();
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

    protected SqlSession addBatch(String stmtId, Object param)
    {
        return session().addBatch(stmt(stmtId), param);
    }

    protected int[] executeBatch(String stmtId)
    {
        return session().executeBatch(stmt(stmtId));
    }

    protected int executeUpdate(String stmtId, Object param)
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
    public <V> V transact(Callable<V> task)
    {
        return transact(task, 1, false);
    }

    /**
     * This method is exactly the same as {@link #transact(Runnable)} except this method will run the transaction in a
     * new session.
     * 
     * @throws Error
     *             when the DB connection is broken and recovery attempts fail.
     */
    public <V> V transactInNewSession(Callable<V> task)
    {
        return transact(task, 1, true);
    }

    private <V> V transact(Callable<V> task, int attempt, boolean inNewSession)
    {
        SqlSession session = inNewSession ? _dataManager.newSession() : session();

        boolean committed = false;

        try
        {
            session.startTransaction();
            V result = task.call();
            session.commit();

            committed = true;

            return result;
        }
        catch (Exception e)
        {
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
                    throw new Error("DB connection error. The database or the network is possibly down.");

                if (inNewSession)
                    session.close();
                else
                    _dataManager.discardThreadSession();

                if (!committed)
                    transact(task, 2, inNewSession);
            }
            finally
            {
                if (inNewSession)
                    session.close();
            }
        }
    }
}
