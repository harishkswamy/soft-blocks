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
public class SqlSession
{
    private Connection                      _conn;
    private Map<SqlStmt, PreparedStatement> _stmts = new HashMap<SqlStmt, PreparedStatement>();

    SqlSession(Connection conn)
    {
        if (conn == null)
            throw new IllegalArgumentException("SQL connection must be provided.");

        _conn = conn;
    }

    public PreparedStatement getStatement(SqlStmt sqlStmt, Object model)
    {
        try
        {
            PreparedStatement stmt = _stmts.get(sqlStmt);

            if (stmt == null)
            {
                stmt = prepareStatement(sqlStmt, model);
                _stmts.put(sqlStmt, stmt);
            }

            return stmt;
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to get prepared statement for " + sqlStmt);
        }
    }

    public int selectInt(SqlStmt sqlStmt, Object paramModel)
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
    public <T> T selectOne(SqlStmt sqlStmt, Object paramModel)
    {
        List<T> list = select(sqlStmt, paramModel);
        return list.size() > 0 ? list.get(0) : null;
    }

    public <T> List<T> select(SqlStmt sqlStmt, Object paramModel)
    {
        return select(sqlStmt, paramModel, null);
    }

    public <T> List<T> select(SqlStmt sqlStmt, Object paramModel, RowCallback<T> callback)
    {
        return executeQuery(sqlStmt, paramModel, new ResultBuilder<T>(sqlStmt, callback));
    }

    public <T> List<T> executeQuery(SqlStmt sqlStmt, Object param, ResultHandler<T> handler)
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
            throw AggregateException.with(e, "Unable to execute SQL " + sqlStmt);
        }
        finally
        {
            close(result);
            clear(sqlStmt);
        }
    }

    public SqlSession addBatch(SqlStmt sqlStmt, Object param)
    {
        try
        {
            PreparedStatement stmt = getStatement(sqlStmt, param);
            setStatementParams(sqlStmt, stmt, param);
            stmt.addBatch();

            return this;
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to add to JDBC batch for statement: " + sqlStmt);
        }
    }

    public int[] executeBatch(SqlStmt sqlStmt)
    {
        PreparedStatement stmt = _stmts.get(sqlStmt);

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
            throw AggregateException.with(e, "Unable to execute JDBC batch for statement: " + sqlStmt);
        }
        finally
        {
            clear(sqlStmt);
        }
    }

    public int executeUpdate(SqlStmt sqlStmt, Object param)
    {
        try
        {
            PreparedStatement stmt = getStatement(sqlStmt, param);
            setStatementParams(sqlStmt, stmt, param);

            return stmt.executeUpdate();
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to execute SQL " + sqlStmt);
        }
        finally
        {
            clear(sqlStmt);
        }
    }

    public SqlSession clear(SqlStmt sqlStmt)
    {
        PreparedStatement stmt = _stmts.get(sqlStmt);

        if (stmt == null)
            return this;

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

        return this;
    }

    public SqlSession commit()
    {
        try
        {
            _conn.commit();

            return this;
        }
        catch (SQLException e)
        {
            throw AggregateException.with(e, "unable-to-commit-DB-transaction");
        }
    }

    public SqlSession rollback()
    {
        try
        {
            _conn.rollback();

            return this;
        }
        catch (SQLException e)
        {
            throw AggregateException.with(e, "unable-to-rollback-DB-transaction");
        }
    }

    public void close()
    {
        closeStatements();
        closeConnection();

        _conn = null;
        _stmts = null;
    }

    @Override
    protected void finalize() throws Throwable
    {
        close();
    }

    // Private methods ===========================================================================

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

        for (PreparedStatement stmt : _stmts.values())
            close(stmt);
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
    }
}
