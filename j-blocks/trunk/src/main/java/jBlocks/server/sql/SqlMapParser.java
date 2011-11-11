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

import jBlocks.server.Utils;
import jBlocks.shared.SharedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author hkrishna
 */
class SqlMapParser
{
    private final String         CLASS         = ".class";
    private final String         SELECT_CLAUSE = ".select-clause";
    private final String         FETCH_SIZE    = ".fetch-size";
    private final String         SQL           = ".sql";
    private final String         DYNAMIC_SQL   = ".dyn-sql";
    private final String         SELECT        = "select ";
    private final String         FROM          = " from ";

    private Map<String, SqlStmt> _sqlStmts     = new HashMap<String, SqlStmt>();
    private Map<String, SqlMap>  _sqlMaps      = new HashMap<String, SqlMap>();

    Map<String, SqlStmt> parse(Properties sqlProps)
    {
        for (Object keyObj : sqlProps.keySet())
        {
            String key = (String) keyObj, keylc = key.toLowerCase();

            if (keylc.endsWith(CLASS))
                getSqlMap(key).setClassName(sqlProps.getProperty(key));

            else if (keylc.endsWith(SELECT_CLAUSE))
                getSqlMap(key).setSelectClause(sqlProps.getProperty(key));

            else if (keylc.endsWith(SQL) || keylc.endsWith(DYNAMIC_SQL))
            {
                SqlStmt sqlStmt = newSqlStmt(key);
                sqlStmt.setFetchSize(Utils.getIntProperty(sqlProps, key + FETCH_SIZE, 0));

                String sql = sqlProps.getProperty(key);

                if (sql.substring(0, 7).equalsIgnoreCase(SELECT))
                    parseSelectSql(sql, sqlStmt);
                else
                    parseUpdateSql(sql, sqlStmt);
            }
        }

        return _sqlStmts;
    }

    private SqlStmt newSqlStmt(String key)
    {
        SqlStmt sqlStmt = new SqlStmt();
        sqlStmt.setSqlMap(getSqlMap(key));

        _sqlStmts.put(key, sqlStmt);

        return sqlStmt;
    }

    private SqlMap getSqlMap(String key)
    {
        key = key.substring(0, key.indexOf('.'));
        SqlMap sqlMap = _sqlMaps.get(key);

        if (sqlMap == null)
        {
            sqlMap = new SqlMap();
            _sqlMaps.put(key, sqlMap);
        }

        return sqlMap;
    }

    private void parseSelectSql(String sql, SqlStmt sqlStmt)
    {
        int fromIdx = sql.toLowerCase().indexOf(FROM);

        String selStr = sql.substring(0, fromIdx);

        StringBuffer stmtBuf = new StringBuffer(selStr);

        parseFields(sqlStmt, selStr.substring(7));
        parseParams(sqlStmt, sql.substring(fromIdx), stmtBuf);

        sqlStmt.setStmt(stmtBuf.toString());
    }

    private void parseFields(SqlStmt sqlStmt, String cols)
    {
        List<SqlField> fields = new ArrayList<SqlField>();

        for (String field : SharedUtils.splitQuoted(cols, ',', true))
            addField(fields, field);

        sqlStmt.setFields(fields);
    }

    private void addField(List<SqlField> fields, String field)
    {
        List<String> parts = SharedUtils.splitQuoted(field, ' ', true);

        field = parts.size() > 1 ? SharedUtils.trim(parts.get(1), "\"") : camelCase(parts.get(0)
                .substring(parts.get(0).indexOf(".") + 1));

        fields.add(new SqlField(field));
    }

    private String camelCase(String str)
    {
        StringBuffer cName = new StringBuffer();

        String[] parts = str.split("_");

        for (int i = 0; i < parts.length; i++)
            cName.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));

        return cName.toString();
    }

    private void parseUpdateSql(String sql, SqlStmt sqlStmt)
    {
        StringBuffer stmtBuf = new StringBuffer();

        parseParams(sqlStmt, sql, stmtBuf);

        sqlStmt.setStmt(stmtBuf.toString());
    }

    private void parseParams(SqlStmt sqlStmt, String sql, StringBuffer stmt)
    {
        boolean inParam = false, inDynParam = false;
        List<SqlParam> params = new ArrayList<SqlParam>();
        List<SqlParam> dynParams = new ArrayList<SqlParam>();
        int dynParamIndex = 0;
        StringBuffer param = new StringBuffer();

        for (int i = 0; i < sql.length(); i++)
        {
            char chr = sql.charAt(i);

            if (inParam)
            {
                if (chr == '#')
                {
                    params.add(new SqlParam(param.toString()));
                    param.delete(0, param.length());

                    stmt.append(" ? ");

                    inParam = false;
                }
                else
                    param.append(chr);
            }
            else if (inDynParam)
            {
                if (chr == '@')
                {
                    dynParams.add(new SqlParam(param.toString()));
                    param.delete(0, param.length());

                    stmt.append('@').append(dynParamIndex++).append('@');

                    inDynParam = false;
                }
                else
                    param.append(chr);
            }
            else if (chr == '#')
                inParam = true;
            else if (chr == '@')
                inDynParam = true;
            else
                stmt.append(chr);
        }

        sqlStmt.setParams(params.size() == 0 ? null : params);
        sqlStmt.setDynamicParams(dynParams.size() == 0 ? null : dynParams);
    }
}
