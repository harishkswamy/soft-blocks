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
import jBlocks.server.ClassUtils;
import jBlocks.server.ReflectUtils;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author hkrishna
 */
public class ResultBuilder<T> implements ResultHandler<T>
{
    // TODO : This class builds the result from the ResultSet object. This builder identity-aware, meaning it will not
    // create duplicate objects for the same entity. This is based on the assumption that the identity column name is
    // "ID".

    private static final SimpleDateFormat[] DATE_FORMATS = { new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sssss"),
        new SimpleDateFormat("yyyy-MM-dd")              };

    private SqlStmt                         _sqlStmt;
    private RowCallback<T>                  _rowCallback;

    ResultBuilder(SqlStmt sqlStmt, RowCallback<T> callback)
    {
        _sqlStmt = sqlStmt;
        _rowCallback = callback;
    }

    public List<T> handle(ResultSet result) throws Exception
    {
        List<T> modelList = new ArrayList<T>();

        while (result.next())
            modelList.add(getModel(result));

        return modelList;
    }

    private T getModel(ResultSet result) throws Exception
    {
        return newModel(result);
    }

    @SuppressWarnings("unchecked")
    private T newModel(ResultSet result) throws Exception
    {
        T obj = (T) ClassUtils.newInstance(_sqlStmt.getSqlMap().getClassName());

        List<SqlField> fields = _sqlStmt.getFields();

        for (int i = 0; i < fields.size(); i++)
        {
            SqlField field = fields.get(i);
            String value = result.getString(i + 1);

            if (field.requestsCallback())
                ReflectUtils.invokeMethod(_rowCallback, field.callbackName(), obj, value);
            else
                setComplexPropertyValue(obj, field.getProperties(), value);
        }

        return obj;
    }

    @SuppressWarnings("unchecked")
    private void setComplexPropertyValue(final Object model, String[] props, String val)
    {
        if (model instanceof Map)
        {
            ((Map<String, String>) model).put(props[0], val);
            return;
        }

        Object obj = model;

        for (int i = 0; i < props.length - 1; i++)
            obj = getOrCreateRelation(obj, props[i]);

        setPropertyValue(obj, props[props.length - 1], val);
    }

    private Object getOrCreateRelation(Object obj, String methodName)
    {
        try
        {
            Object relation = ReflectUtils.invokeMethod(obj, methodName, (Object[]) null);

            if (relation == null)
            {
                Method method = ReflectUtils.findMethod(obj.getClass(), methodName, (Class[]) null);
                Class<?> relationType = method.getReturnType();
                relation = relationType.newInstance();
                ReflectUtils.invokeMethod(obj, methodName.replaceFirst("get", "set"), relation);
            }

            return relation;
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "unable-to-get-relation-object");
        }
    }

    private void setPropertyValue(Object obj, String methodName, String val)
    {
        Object arg = toArgType(obj.getClass(), methodName, val);

        ReflectUtils.invokeMethod(obj, methodName, arg);
    }

    private Object toArgType(Class<?> clazz, String methodName, String val)
    {
        if (val == null)
            return null;

        try
        {
            Method[] methods = clazz.getDeclaredMethods();

            for (int i = 0; i < methods.length; i++)
            {
                if (methodName.equals(methods[i].getName()))
                {
                    Class<?> argType = methods[i].getParameterTypes()[0];

                    if (String.class == argType)
                        return val;

                    if (argType == Date.class)
                        return parseDate(val);

                    if (argType == boolean.class || argType == Boolean.class)
                        return parseBoolean(val);

                    if (argType == int.class)
                        argType = Integer.class;
                    else if (argType == float.class)
                        argType = Float.class;
                    else if (argType == long.class)
                        argType = Long.class;
                    else if (argType == double.class)
                        argType = Double.class;

                    return argType.getConstructor(String.class).newInstance(val);
                }
            }

            return clazz.getSuperclass() == null ? null : toArgType(clazz.getSuperclass(), methodName, val);
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to convert value: " + val + " to argument type for " + clazz + "."
                + methodName);
        }
    }

    private Object parseBoolean(String val)
    {
        if ("y".equalsIgnoreCase(val) || "yes".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val))
            return Boolean.TRUE;

        return Boolean.FALSE;
    }

    private Object parseDate(String val) throws ParseException
    {
        ParseException ex = null;

        for (int i = 0; i < DATE_FORMATS.length; i++)
        {
            try
            {
                return DATE_FORMATS[i].parse(val);
            }
            catch (ParseException e)
            {
                ex = e;
            }
        }

        throw ex;
    }
}
