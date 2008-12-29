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

import java.util.List;

/**
 * @author hkrishna
 */
class SqlStmt
{
    private String         _stmt;
    private List<SqlField> _fields;
    private List<SqlParam> _params;
    private List<SqlParam> _dynParams;
    private SqlMap         _sqlMap;

    public String getStmt()
    {
        return _stmt;
    }

    public void setStmt(String stmt)
    {
        _stmt = stmt;
    }

    public boolean isDynamic()
    {
        return _dynParams != null;
    }

    public List<SqlField> getFields()
    {
        return _fields;
    }

    public void setFields(List<SqlField> fields)
    {
        _fields = fields;
    }

    public List<SqlParam> getParams()
    {
        return _params;
    }

    public void setParams(List<SqlParam> params)
    {
        _params = params;
    }

    public List<SqlParam> getDynamicParams()
    {
        return _dynParams;
    }

    public void setDynamicParams(List<SqlParam> params)
    {
        _dynParams = params;
    }

    public SqlMap getSqlMap()
    {
        return _sqlMap;
    }

    public void setSqlMap(SqlMap sqlMap)
    {
        _sqlMap = sqlMap;
    }

    @Override
    public String toString()
    {
        return "Statement: " + _stmt + ", Fields: " + _fields + ", Parameters: " + _params + ", Dynamic Parameters: "
            + _dynParams + ", SQL Map: " + _sqlMap;
    }
}
