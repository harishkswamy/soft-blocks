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

/**
 * A dynamic callback interface intended to be used to be notified for each row fetched from the database. This can
 * typically be used to populate relationships in the domain model. The callback methods will be invoked via reflection
 * and so must be public and follow the following pattern.
 * <p>
 * <code>public void setSqlColumnAliasName(T model, string value);</code>
 * <p>
 * The SQL column alias name must be a camel case name with a trailing '$' sign. For example, if you had the following
 * SQL
 * <p>
 * <code>select id, name, parent_id Parent$ from family;</code>
 * <p>
 * then the framework will call the following method in the callback
 * <p>
 * <code>public void setParent(Family model, String value)</code>
 * <P>
 * assuming the callback is of type <code>RowCallback&lt;Family&gt;</code>.
 * 
 * @author hkrishna
 * @see SqlSchema
 */
public interface RowCallback<T>
{
}
