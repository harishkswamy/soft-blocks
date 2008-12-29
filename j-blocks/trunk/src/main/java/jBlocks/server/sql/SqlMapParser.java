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
    private final String         CLASS       = ".class";
    private final String         SQL         = ".sql";
    private final String         DYNAMIC_SQL = ".dyn-sql";
    private final String         SELECT      = "select ";
    private final String         FROM        = " from ";

    private Map<String, SqlStmt> _sqlStmts   = new HashMap<String, SqlStmt>();
    private Map<String, SqlMap>  _sqlMaps    = new HashMap<String, SqlMap>();

    Map<String, SqlStmt> parse(Properties sqlProps)
    {
        for (Object keyObj : sqlProps.keySet())
        {
            String key = (String) keyObj;

            if (key.endsWith(CLASS))
            {
                SqlMap sqlMap = getSqlMap(key);
                sqlMap.setClassName(sqlProps.getProperty(key));

                continue;
            }

            // Ignore all other properties, if exists
            //
            if (!(key.endsWith(SQL) || key.endsWith(DYNAMIC_SQL)))
                continue;

            SqlStmt sqlStmt = newSqlStmt(key);
            String sql = sqlProps.getProperty(key);

            if (key.endsWith(DYNAMIC_SQL))
                key = key.replaceFirst(DYNAMIC_SQL, SQL);

            if (sql.startsWith(SELECT))
                parseSelectSql(key, sql, sqlStmt);
            else
                parseUpdateSql(key, sql, sqlStmt);
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

    private void parseSelectSql(String key, String sql, SqlStmt sqlStmt)
    {
        int fromIdx = sql.indexOf(FROM);

        String selStr = sql.substring(0, fromIdx);

        StringBuffer stmtBuf = new StringBuffer(selStr);

        parseFields(sqlStmt, selStr.substring(7));
        parseParams(sqlStmt, sql.substring(fromIdx), stmtBuf);

        sqlStmt.setStmt(stmtBuf.toString());
    }

    private void parseFields(SqlStmt sqlStmt, String cols)
    {
        int inFunc = 0;
        List<SqlField> fields = new ArrayList<SqlField>();
        StringBuffer field = new StringBuffer();

        for (int i = 0; i < cols.length(); i++)
        {
            char chr = cols.charAt(i);

            if (chr == '(')
                inFunc++;
            else if (chr == ')')
                inFunc--;
            else if (inFunc == 0)
            {
                switch (chr)
                {
                case ',':
                    addField(fields, field);
                case ' ':
                    field.delete(0, field.length());
                    break;
                default:
                    field.append(chr);
                }
            }
        }

        addField(fields, field);

        sqlStmt.setFields(fields);
    }

    private void addField(List<SqlField> fields, StringBuffer field)
    {
        fields.add(new SqlField(camelCase(field.substring(field.indexOf(".") + 1))));
    }

    private String camelCase(String str)
    {
        StringBuffer cName = new StringBuffer();

        String[] parts = str.split("_");

        for (int i = 0; i < parts.length; i++)
            cName.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));

        return cName.toString();
    }

    private void parseUpdateSql(String key, String sql, SqlStmt sqlStmt)
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
