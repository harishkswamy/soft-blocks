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
import jBlocks.server.AppContext;
import jBlocks.server.IOUtils;
import jBlocks.server.ReflectUtils;

import java.net.URL;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Base class to manage database access to a particular schema.
 * <p>
 * This class is thread-safe and and is intended to be used as a singleton.
 * <p>
 * 
 * 
 * @author hkrishna
 */
public abstract class AbstractSchema
{
    private static final ThreadLocal<Connection>                     _threadConnection = new ThreadLocal<Connection>();
    private static final ThreadLocal<Map<String, PreparedStatement>> _statements       = new ThreadLocal<Map<String, PreparedStatement>>();

    private DataManager                                              _dataManager;
    private Map<String, SqlStmt>                                     _sqlStmts;

    protected AbstractSchema(DataManager dataManager, String sqlPropsFileName)
    {
        _dataManager = dataManager;

        Properties sqlProps = IOUtils.loadProperties(getClass().getResource(sqlPropsFileName + ".sql.properties"));

        URL url = getClass().getResource(sqlPropsFileName + "." + dataManager.dbId() + ".sql.properties");

        if (url != null)
            sqlProps.putAll(IOUtils.loadProperties(url));

        _sqlStmts = new SqlMapParser().parse(sqlProps);
    }

    // Public methods =========================================================================

    public void startTransaction()
    {
        try
        {
            Connection conn = _threadConnection.get();

            if (conn != null)
                throw new IllegalAccessException(
                    "There is already a transaction in progress for this thread; you must complete that transaction by calling commit() or rollback() before starting a new one.");

            conn = newConnection();

            _threadConnection.set(conn);
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to start JDBC transaction.");
        }
    }

    public void commit()
    {
        Connection conn = _threadConnection.get();
        _threadConnection.remove();
        commit(conn);
    }

    public void rollback()
    {
        Connection conn = _threadConnection.get();
        _threadConnection.remove();
        rollback(conn);
    }

    // Protected methods ======================================================================

    protected AppContext appCtx()
    {
        return _dataManager.appCtx();
    }

    protected int selectInt(String stmtId, Object paramModel)
    {
        List<Integer> result = executeQuery(_sqlStmts.get(stmtId + ".select.sql"), paramModel,
            new ResultHandler<Integer>()
            {
                public List<Integer> handle(ResultSet result) throws Exception
                {
                    List<Integer> list = new ArrayList<Integer>();

                    if (result.next())
                        list.add(result.getInt(1));
                    else
                        list.add(0);

                    return list;
                }
            });

        return result.get(0);
    }

    /**
     * @return The object or null if no row was selected.
     */
    protected <T> T selectOne(final String stmtId, Object paramModel)
    {
        List<T> list = select(stmtId, paramModel);
        return list.size() > 0 ? list.get(0) : null;
    }

    protected <T> List<T> select(final String stmtId, Object paramModel)
    {
        return select(stmtId, paramModel, null);
    }

    protected <T> List<T> select(final String stmtId, Object paramModel, RowCallback<T> callback)
    {
        SqlStmt sqlStmt = _sqlStmts.get(stmtId + ".select.sql");

        return executeQuery(sqlStmt, paramModel, new ResultBuilder<T>(sqlStmt, callback));
    }

    protected void addInsertBatch(String stmtId, Object model)
    {
        addBatch(stmtId + ".insert.sql", model);
    }

    protected int[] insertBatch(String stmtId)
    {
        stmtId = stmtId + ".insert.sql";

        int[] result = executeBatch(stmtId);

        clearBatch(stmtId);

        return result;
    }

    protected void clearInsertBatch(String stmtId)
    {
        clearBatch(stmtId + ".insert.sql");
    }

    protected void addUpdateBatch(String stmtId, Object model)
    {
        addBatch(stmtId + ".update.sql", model);
    }

    protected int[] updateBatch(String stmtId)
    {
        stmtId = stmtId + ".update.sql";

        int[] result = executeBatch(stmtId);

        clearBatch(stmtId);

        return result;
    }

    protected void clearUpdateBatch(String stmtId)
    {
        clearBatch(stmtId + ".update.sql");
    }

    protected void addDeleteBatch(String stmtId, Object model)
    {
        addBatch(stmtId + ".delete.sql", model);
    }

    protected int[] deleteBatch(String stmtId)
    {
        stmtId = stmtId + ".delete.sql";

        int[] result = executeBatch(stmtId);

        clearBatch(stmtId);

        return result;
    }

    protected void clearDeleteBatch(String stmtId)
    {
        clearBatch(stmtId + ".delete.sql");
    }

    @Deprecated
    protected void insert(String stmtId, Object model, boolean autoId)
    {
        if (autoId)
            ReflectUtils.invokeMethod(model, "setId", new Object[] { selectInt(stmtId, null) });

        executeUpdate(_sqlStmts.get(stmtId + ".insert.sql"), model, null);
    }

    protected void insert(String stmtId, Object model)
    {
        executeUpdate(_sqlStmts.get(stmtId + ".insert.sql"), model, null);
    }

    protected void insertAndCommit(String stmtId, Object model)
    {
        Connection conn = newConnection();
        executeUpdate(_sqlStmts.get(stmtId + ".insert.sql"), model, conn);
        commit(conn);
    }

    protected int update(String stmtId, Object model)
    {
        return executeUpdate(_sqlStmts.get(stmtId + ".update.sql"), model, null);
    }

    protected int updateAndCommit(String stmtId, Object model)
    {
        Connection conn = newConnection();
        int count = executeUpdate(_sqlStmts.get(stmtId + ".update.sql"), model, conn);
        commit(conn);
        return count;
    }

    protected int delete(String stmtId, Object model)
    {
        return executeUpdate(_sqlStmts.get(stmtId + ".delete.sql"), model, null);
    }

    protected int deleteAndCommit(String stmtId, Object model)
    {
        Connection conn = newConnection();
        int count = executeUpdate(_sqlStmts.get(stmtId + ".delete.sql"), model, conn);
        commit(conn);
        return count;
    }

    // Private methods ===========================================================================

    private Connection newConnection()
    {
        return _dataManager.getConnection();
    }

    private Connection getConnection()
    {
        Connection conn = _threadConnection.get();

        return conn == null ? newConnection() : conn;
    }

    private void commit(Connection conn)
    {
        try
        {
            if (conn == null)
                return;

            conn.commit();
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "unable-to-commit-DB-transaction");
        }
        finally
        {
            cleanup(null, null, conn);
        }
    }

    private void rollback(Connection conn)
    {
        try
        {
            if (conn == null)
                return;

            conn.rollback();
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "unable-to-rollback-DB-transaction");
        }
        finally
        {
            cleanup(null, null, conn);
        }
    }

    private Map<String, PreparedStatement> getStatementMap()
    {
        Map<String, PreparedStatement> stmtMap = _statements.get();

        if (stmtMap == null)
        {
            stmtMap = new HashMap<String, PreparedStatement>();
            _statements.set(stmtMap);
        }

        return stmtMap;
    }

    private void addBatch(String stmtId, Object model)
    {
        try
        {
            Map<String, PreparedStatement> stmtMap = getStatementMap();
            PreparedStatement stmt = stmtMap.get(stmtId);
            SqlStmt sqlStmt = _sqlStmts.get(stmtId);

            if (stmt == null)
            {
                stmt = prepareStatement(sqlStmt, null, getConnection());
                stmtMap.put(stmtId, stmt);
            }

            setStatementParams(sqlStmt, stmt, model);
            stmt.addBatch();
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to add to JDBC batch for statement: " + stmtId);
        }
    }

    private int[] executeBatch(String stmtId)
    {
        PreparedStatement stmt = getStatementMap().get(stmtId);

        if (stmt == null)
            return null;

        try
        {
            int[] status = stmt.executeBatch();

            for (int i = 0; i < status.length; i++)
                if (status[i] == PreparedStatement.EXECUTE_FAILED)
                    throw new BatchUpdateException("JDBC batch execution failed at statement #: " + i, status);

            return status;
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to execute JDBC batch for statement: " + stmtId);
        }
    }

    private void clearBatch(String stmtId)
    {
        PreparedStatement stmt = getStatementMap().remove(stmtId);

        if (stmt == null)
            return;

        try
        {
            stmt.clearBatch();
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to clear JDBC batch for statement: " + stmtId);
        }
        finally
        {
            cleanup(null, stmt, null);
        }
    }

    private <T> List<T> executeQuery(SqlStmt sqlStmt, Object param, ResultHandler<T> handler)
    {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet result = null;

        try
        {
            conn = getConnection();
            stmt = prepareStatement(sqlStmt, param, conn);

            return handler.handle(stmt.executeQuery());
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to execute SQL " + sqlStmt);
        }
        finally
        {
            cleanup(result, stmt, conn);
        }
    }

    private int executeUpdate(SqlStmt sqlStmt, Object param, Connection conn)
    {
        Connection tConn = conn;
        PreparedStatement stmt = null;
        ResultSet result = null;

        try
        {
            conn = conn == null ? getConnection() : conn;
            stmt = prepareStatement(sqlStmt, param, conn);

            return stmt.executeUpdate();
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to execute SQL " + sqlStmt);
        }
        finally
        {
            cleanup(result, stmt, conn == tConn ? null : conn);
        }
    }

    private PreparedStatement prepareStatement(SqlStmt sqlStmt, Object model, Connection conn) throws SQLException
    {
        String sql = sqlStmt.getStmt();

        if (sqlStmt.isDynamic())
            sql = setDynamicSqlSubstitutions(sqlStmt, sql, model);

        PreparedStatement stmt = conn.prepareStatement(sql);

        if (model == null)
            return stmt;

        setStatementParams(sqlStmt, stmt, model);

        return stmt;
    }

    private String setDynamicSqlSubstitutions(SqlStmt sqlStmt, String sql, Object model)
    {
        List<SqlParam> dynParams = sqlStmt.getDynamicParams();

        for (int i = 0; i < dynParams.size(); i++)
        {
            Object value = getComplexPropertyValue(model, dynParams.get(i));
            sql = sql.replaceFirst("@" + i + "@", String.valueOf(value));
        }

        return sql;
    }

    private void setStatementParams(SqlStmt sqlStmt, PreparedStatement stmt, Object model) throws SQLException
    {
        List<SqlParam> params = sqlStmt.getParams();

        if (params == null)
            return;

        for (int i = 0; i < params.size(); i++)
            setStatementParam(stmt, i + 1, params.get(i), model);
    }

    private void setStatementParam(PreparedStatement stmt, int i, SqlParam param, Object model) throws SQLException
    {
        Object val = getComplexPropertyValue(model, param);

        if (val == null)
        {
            stmt.setNull(i, Types.NULL);
            return;
        }

        String typeName = val.getClass().getName();

        if (typeName.indexOf("Date") > -1)
        {
            val = new java.sql.Timestamp(((Date) val).getTime());
            typeName = "Timestamp";
        }
        else if (typeName.indexOf("Integer") > -1)
            typeName = "Int";
        else
            typeName = typeName.substring(typeName.lastIndexOf('.') + 1);

        ReflectUtils.invokeMethod(stmt, "set" + typeName, new Object[] { new Integer(i), val });
    }

    @SuppressWarnings("unchecked")
    private Object getComplexPropertyValue(final Object model, SqlParam param)
    {
        if (".".equals(param.getPath()))
            return model;

        if (model instanceof Map)
            return ((Map<String, ?>) model).get(param.getPath());

        Object obj = model;
        String[] props = param.getProperties();

        for (int i = 0; obj != null && i < props.length; i++)
            obj = ReflectUtils.invokeMethod(obj, props[i], (Object[]) null);

        return obj;
    }

    private void cleanup(ResultSet result, Statement stmt, Connection conn)
    {
        try
        {
            if (result != null)
                result.close();
        }
        catch (Exception e)
        {
            // Ignore
        }

        try
        {
            if (stmt != null)
                stmt.close();
        }
        catch (Exception e)
        {
            // Ignore
        }

        try
        {
            if (conn != null && conn != _threadConnection.get())
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception e)
                {
                    // Ignore
                }
                conn.close();
            }
        }
        catch (Exception e)
        {
            // Ignore
        }
    }
}
