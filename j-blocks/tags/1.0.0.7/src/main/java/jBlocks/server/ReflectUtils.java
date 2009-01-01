// Copyright 2004 The Apache Software Foundation
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

package jBlocks.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Static utility methods for handling reflection.
 * 
 * @author Howard Lewis Ship
 * @author Harish Krishnaswamy
 * @version $Id: ReflectUtils.java,v 1.8 2005/10/06 21:59:26 harishkswamy Exp $
 */
public class ReflectUtils
{
    /**
     * Map from primitive type to wrapper type.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_MAP = new HashMap<Class<?>, Class<?>>();

    static
    {
        PRIMITIVE_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_MAP.put(byte.class, Byte.class);
        PRIMITIVE_MAP.put(char.class, Character.class);
        PRIMITIVE_MAP.put(short.class, Short.class);
        PRIMITIVE_MAP.put(int.class, Integer.class);
        PRIMITIVE_MAP.put(long.class, Long.class);
        PRIMITIVE_MAP.put(float.class, Float.class);
        PRIMITIVE_MAP.put(double.class, Double.class);
    }

    private static String getTypeName(Class<?> type)
    {
        return type == null ? null : type.getName();
    }

    private static String typesToString(Class<?>... types)
    {
        if (types == null || types.length == 0)
            return "";

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < types.length - 1; i++)
            buf.append(getTypeName(types[i])).append(", ");

        String typeName = getTypeName(types[types.length - 1]);

        buf.append(typeName);

        return buf.toString();
    }

    private static boolean isCompatible(Class<?> paramType, Class<?> valueType)
    {
        if (paramType.isAssignableFrom(valueType))
            return true;

        // Reflection fudges the assignment of a wrapper class to a primitive
        // type ... we check for that the hard way.

        if (paramType.isPrimitive())
        {
            Class<?> wrapperClass = PRIMITIVE_MAP.get(paramType);

            return wrapperClass.isAssignableFrom(valueType);
        }

        return false;
    }

    private static boolean isMatch(Class<?>[] paramTypes, Class<?>... valueTypes)
    {
        if (paramTypes.length != valueTypes.length)
            return false;

        for (int i = 0; i < paramTypes.length; i++)
        {
            if (valueTypes[i] == null)
            {
                if (paramTypes[i].isPrimitive())
                    return false;

                continue;
            }

            if (!isCompatible(paramTypes[i], valueTypes[i]))
                return false;
        }

        return true;
    }

    /**
     * @return Public constructor that matches the requested signature.
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> findConstructor(Class<T> targetClass, Class<?>... argTypes)
    {
        Constructor<T>[] constructors = targetClass.getConstructors();

        for (int i = 0; i < constructors.length; i++)
        {
            if (isMatch(constructors[i].getParameterTypes(), argTypes))
                return constructors[i];
        }

        throw new IllegalArgumentException("Unable to find constructor " + targetClass + "(" + typesToString(argTypes)
            + ")");
    }

    private static Class<?>[] getTypes(Object[] args)
    {
        if (args == null)
            args = new Object[0];

        Class<?>[] argTypes = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            argTypes[i] = args[i] == null ? null : args[i].getClass();

        return argTypes;
    }

    /**
     * Searches and invokes the constructor that matches the requested signature.
     * 
     * @return A new instance of <code>targetClass</class>.
     */
    public static <T> T invokeConstructor(Class<T> targetClass, Object... args)
    {
        Class<?>[] argTypes = null;

        try
        {
            argTypes = getTypes(args);

            Constructor<T> ctor = findConstructor(targetClass, argTypes);

            return ctor.newInstance(args);
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to invoke constructor " + targetClass + "("
                + typesToString(argTypes) + ")");
        }
    }

    /**
     * Searches for a matching method in this class and all super classes.
     * 
     * @return Public method that match the requested signature.
     */
    public static Method findMethod(Class<?> targetClass, String methodName, Class<?>... argTypes)
    {
        if (argTypes == null)
            argTypes = new Class[0];

        Method[] methods = targetClass.getMethods();

        for (int i = 0; i < methods.length; i++)
        {
            if (!methods[i].getName().equals(methodName))
                continue;

            if (isMatch(methods[i].getParameterTypes(), argTypes))
                return methods[i];
        }

        throw new IllegalArgumentException("Unable to find method " + targetClass + "." + methodName + "("
            + typesToString(argTypes) + ")");
    }

    /**
     * Invokes the provided method on the provided target object with the provided arguments.
     * 
     * @return Returns the result of the method invocation.
     */
    public static Object invokeMethod(Object target, String methodName, Object... args)
    {
        Class<?>[] argTypes = getTypes(args);

        Method method = findMethod(target.getClass(), methodName, argTypes);

        try
        {
            method.setAccessible(true);
            return method.invoke(target, args);
        }
        catch (Throwable t)
        {
            while (t instanceof InvocationTargetException)
                t = ((InvocationTargetException) t).getTargetException();

            throw AggregateException.with(t, "Unable to invoke method " + method);
        }
    }
}
