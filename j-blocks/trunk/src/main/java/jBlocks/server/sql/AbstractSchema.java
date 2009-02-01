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

    public void startTransaction()
    {
        for (int i = 0;; i++)
        {
            try
            {
                session().rollback();

                break;
            }
            catch (Exception e)
            {
                _dataManager.discardThreadSession();

                if (i > 0)
                    throw AggregateException.with(e);
            }
        }
    }

    public void commit()
    {
        session().commit();
    }

    public void rollback()
    {
        session().rollback();
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

    protected void addBatch(String stmtId, Object param)
    {
        session().addBatch(stmt(stmtId), param);
    }

    protected int[] executeBatch(String stmtId)
    {
        return session().executeBatch(stmt(stmtId));
    }

    protected int executeUpdate(String stmtId, Object param)
    {
        return session().executeUpdate(stmt(stmtId), param);
    }

    protected int executeAndCommit(String stmtId, Object param)
    {
        SqlSession session = _dataManager.newSession();

        try
        {
            int result = session.executeUpdate(stmt(stmtId), param);
            session.commit().close();

            return result;
        }
        finally
        {
            session.rollback().close();
        }
    }

    protected void clear(String stmtId)
    {
        session().clear(stmt(stmtId));
    }
}
