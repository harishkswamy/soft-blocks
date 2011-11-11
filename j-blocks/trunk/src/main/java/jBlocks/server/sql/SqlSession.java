package jBlocks.server.sql;

import jBlocks.server.AggregateException;
import jBlocks.server.ReflectUtils;

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

/**
 * @author hkrishna
 */
class SqlSession
{
    private class SqlTrn
    {
        private boolean                         _committed;
        private Map<SqlStmt, PreparedStatement> _batchStmts;

        void addBatch(SqlStmt sqlStmt, Object param)
        {
            try
            {
                PreparedStatement stmt = getStatement(sqlStmt, param);

                if (_batchStmts == null)
                    _batchStmts = new HashMap<SqlStmt, PreparedStatement>();

                if (!_batchStmts.containsKey(sqlStmt))
                    _batchStmts.put(sqlStmt, stmt);

                setStatementParams(sqlStmt, stmt, param);
                stmt.addBatch();
            }
            catch (Exception e)
            {
                throw AggregateException.with(e, "Unable to add batch " + sqlStmt + ", Parameter: "
                        + param);
            }
        }

        int[] executeBatch(SqlStmt sqlStmt)
        {
            PreparedStatement stmt = _stmts.get(sqlStmt);

            if (stmt == null)
                return null;

            try
            {
                int[] status = stmt.executeBatch();

                for (int i = 0; i < status.length; i++)
                    if (status[i] == PreparedStatement.EXECUTE_FAILED)
                        throw new BatchUpdateException("Batch execution failed at statement #: "
                                + i, status);

                return status;
            }
            catch (Exception e)
            {
                throw AggregateException.with(e, "Unable to execute batch " + sqlStmt);
            }
            finally
            {
                clear(sqlStmt);
            }
        }

        int executeUpdate(SqlStmt sqlStmt, Object param)
        {
            try
            {
                PreparedStatement stmt = getStatement(sqlStmt, param);
                setStatementParams(sqlStmt, stmt, param);

                return stmt.executeUpdate();
            }
            catch (Exception e)
            {
                throw AggregateException.with(e, "Unable to execute update " + sqlStmt
                        + ", Parameter: " + param);
            }
            finally
            {
                clear(sqlStmt);
            }
        }

        SqlSession commit()
        {
            try
            {
                _conn.commit();
                _committed = true;

                return SqlSession.this;
            }
            catch (SQLException e)
            {
                throw AggregateException.with(e, "Unable to commit DB transaction.");
            }
        }

        SqlSession end()
        {
            try
            {
                if (!_committed)
                    _conn.rollback();

                return SqlSession.this;
            }
            catch (SQLException e)
            {
                throw AggregateException.with(e, "Unable to rollback DB transaction.");
            }
            finally
            {
                if (_batchStmts != null)
                {
                    for (SqlStmt sqlStmt : _batchStmts.keySet())
                        clear(sqlStmt);

                    _batchStmts = null;
                }
            }
        }
    }

    private Connection                      _conn;
    private Map<SqlStmt, PreparedStatement> _stmts = new HashMap<SqlStmt, PreparedStatement>();
    private SqlTrn                          _trn;

    SqlSession(Connection conn)
    {
        if (conn == null)
            throw new IllegalArgumentException("SQL connection must be provided.");

        _conn = conn;
    }

    boolean isInTransaction()
    {
        return _trn != null;
    }

    SqlSession startTransaction()
    {
        if (_trn != null)
            throw new IllegalStateException("This session is already in a transaction.");

        _trn = new SqlTrn();

        return this;
    }

    PreparedStatement getStatement(SqlStmt sqlStmt, Object param)
    {
        try
        {
            PreparedStatement stmt = _stmts.get(sqlStmt);

            if (stmt == null)
            {
                stmt = prepareStatement(sqlStmt, param);
                _stmts.put(sqlStmt, stmt);
            }

            return stmt;
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to prepare statement " + sqlStmt
                    + ", Parameter: " + param);
        }
    }

    int selectInt(SqlStmt sqlStmt, Object paramModel)
    {
        List<Integer> result = executeQuery(sqlStmt, paramModel, new ResultHandler<Integer>()
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
    <T> T selectOne(SqlStmt sqlStmt, Object paramModel)
    {
        List<T> list = select(sqlStmt, paramModel);
        return list.size() > 0 ? list.get(0) : null;
    }

    <T> List<T> select(SqlStmt sqlStmt, Object paramModel)
    {
        return select(sqlStmt, paramModel, null);
    }

    <T> List<T> select(SqlStmt sqlStmt, Object paramModel, RowCallback<T> callback)
    {
        return executeQuery(sqlStmt, paramModel, new ResultBuilder<T>(sqlStmt, callback));
    }

    <T> List<T> executeQuery(SqlStmt sqlStmt, Object param, ResultHandler<T> handler)
    {
        ResultSet result = null;

        try
        {
            PreparedStatement stmt = getStatement(sqlStmt, param);
            setStatementParams(sqlStmt, stmt, param);

            return handler.handle(result = stmt.executeQuery());
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to execute query " + sqlStmt + ", Parameter: "
                    + param);
        }
        finally
        {
            close(result);
            clear(sqlStmt);
        }
    }

    void addBatch(SqlStmt sqlStmt, Object param)
    {
        trn().addBatch(sqlStmt, param);
    }

    int[] executeBatch(SqlStmt sqlStmt)
    {
        return trn().executeBatch(sqlStmt);
    }

    int executeUpdate(SqlStmt sqlStmt, Object param)
    {
        return trn().executeUpdate(sqlStmt, param);
    }

    SqlSession commit()
    {
        return trn().commit();
    }

    SqlSession endTransaction()
    {
        try
        {
            return trn().end();
        }
        finally
        {
            _trn = null;
        }
    }

    void close()
    {
        closeStatements();
        closeConnection();
    }

    @Override
    protected void finalize() throws Throwable
    {
        close();
    }

    // Private methods
    // ===========================================================================

    private SqlTrn trn()
    {
        if (_trn == null)
            throw new IllegalStateException(
                    "No transaction in progress; this operation requires a transaction.");

        return _trn;
    }

    private PreparedStatement prepareStatement(SqlStmt sqlStmt, Object model) throws SQLException
    {
        String sql = sqlStmt.getStmt();

        if (sqlStmt.isDynamic())
            sql = setDynamicSqlSubstitutions(sqlStmt, model);

        PreparedStatement stmt = _conn.prepareStatement(sql);

        sqlStmt.applyProperties(stmt);

        return stmt;
    }

    private String setDynamicSqlSubstitutions(SqlStmt sqlStmt, Object model)
    {
        String sql = sqlStmt.getStmt();
        List<SqlParam> dynParams = sqlStmt.getDynamicParams();

        for (int i = 0; i < dynParams.size(); i++)
        {
            Object value = getComplexPropertyValue(model, dynParams.get(i));
            sql = sql.replaceFirst("@" + i + "@", String.valueOf(value));
        }

        return sql;
    }

    private void setStatementParams(SqlStmt sqlStmt, PreparedStatement stmt, Object model)
            throws SQLException
    {
        List<SqlParam> params = sqlStmt.getParams();

        if (params == null)
            return;

        for (int i = 0; i < params.size(); i++)
            setStatementParam(stmt, i + 1, params.get(i), model);
    }

    private void setStatementParam(PreparedStatement stmt, int i, SqlParam param, Object model)
            throws SQLException
    {
        Object val = getComplexPropertyValue(model, param);

        if (val == null)
        {
            stmt.setNull(i, Types.NULL);
            return;
        }

        String typeName = val.getClass().getSimpleName();

        if ("Date".equals(typeName))
        {
            val = new java.sql.Timestamp(((Date) val).getTime());
            typeName = "Timestamp";
        }
        else if ("Integer".equals(typeName))
            typeName = "Int";

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

    private void clear(SqlStmt sqlStmt)
    {
        PreparedStatement stmt = _stmts.get(sqlStmt);

        if (stmt == null)
            return;

        try
        {
            stmt.clearBatch();
            stmt.clearParameters();
            stmt.clearWarnings();
        }
        catch (Exception e)
        {
            close(_stmts.remove(sqlStmt));
        }
    }

    private void close(ResultSet result)
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
    }

    private void closeStatements()
    {
        if (_stmts == null)
            return;

        try
        {
            for (PreparedStatement stmt : _stmts.values())
                close(stmt);
        }
        finally
        {
            _stmts = null;
        }
    }

    private void close(Statement stmt)
    {
        try
        {
            if (stmt != null)
                stmt.close();
        }
        catch (Exception e)
        {
            // Ignore
        }
    }

    private void closeConnection()
    {
        if (_conn == null)
            return;

        try
        {
            try
            {
                _conn.rollback();
            }
            finally
            {
                _conn.close();
            }
        }
        catch (Exception e)
        {
            // Ignore
        }
        finally
        {
            _conn = null;
        }
    }
}
