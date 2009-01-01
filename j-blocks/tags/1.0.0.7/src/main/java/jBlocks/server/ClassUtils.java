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

package jBlocks.server;

import java.net.URL;
import java.util.Enumeration;

/**
 * @author Harish Krishnaswamy
 * @version $Id: ClassUtils.java,v 1.7 2005/10/06 21:59:26 harishkswamy Exp $
 */
public class ClassUtils
{
    /**
     * @return Returns the current thread's context class loader; when not found it returns the system class loader.
     */
    public static ClassLoader getClassLoader()
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        return loader == null ? ClassLoader.getSystemClassLoader() : loader;
    }

    /**
     * @return Returns the class loader of the provided class; when not found it returns the class loader from
     *         {@link #getClassLoader()}.
     */
    public static ClassLoader getClassLoader(Class<?> clazz)
    {
        ClassLoader loader = clazz.getClassLoader();

        return loader == null ? getClassLoader() : loader;
    }

    /**
     * Loads the resource from the class loader returned by {@link #getClassLoader()}.
     * 
     * @param path
     *            The classpath of the resource.
     * @return The URL of the resource identified by the provided path.
     * @throws UsageException
     *             When the provided path does not translate to a valid resource.
     * @see ClassLoader#getResource(java.lang.String)
     */
    public static URL getResource(String path)
    {
        URL url = getClassLoader().getResource(path);

        if (url == null)
            throw new IllegalArgumentException("Unable to find resource at " + path);

        return url;
    }

    /**
     * Creates and returns the {@link URL}from the provided URL string.
     * 
     * @throws WrapperException
     *             {@link Messages#CANNOT_BUILD_URL}: When a valid URL cannot be built from the provided URL string.
     */
    public static URL newUrl(String urlStr)
    {
        try
        {
            return new URL(urlStr);
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to create URL from " + urlStr);
        }
    }

    /**
     * @return Returns an enumeration of URLs to resources found with provided string path using the class loader
     *         returned by {@link #getClassLoader()}.
     * @throws WrapperException
     *             {@link Messages#CANNOT_GET_RESOURCE}
     * @see ClassLoader#getResources(java.lang.String)
     */
    public static Enumeration<URL> getResources(String path)
    {
        try
        {
            Enumeration<URL> e = getClassLoader().getResources(path);

            return e;
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to find resource at " + path);
        }
    }

    /**
     * Creates and returns a new instance of the provided class using the no args constructor.
     * 
     * @return Returns a new instance of the provided class that is created via reflection.
     * @throws WrapperException
     *             {@link Messages#CANNOT_INSTANTIATE_OBJECT}
     */
    public static <T> T newInstance(Class<T> clazz)
    {
        try
        {
            return clazz.newInstance();
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to instantiate " + clazz);
        }
    }

    /**
     * @return Loads and returns the Class for the provided class name from the class loader returned by
     *         {@link #getClassLoader()}.
     * @throws WrapperException
     *             {@link Messages#CANNOT_LOAD_CLASS}
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String className)
    {
        try
        {
            return (Class<T>) getClassLoader().loadClass(className);
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to load class " + className);
        }
    }

    /**
     * Loads the class for the provided class name and returns a new instance that is created via reflection.
     * 
     * @see #loadClass(String)
     * @see #newInstance(Class)
     */
    public static <T> T newInstance(String className)
    {
        Class<T> clazz = loadClass(className);

        return newInstance(clazz);
    }

    private ClassUtils()
    {
        // This is constructor is here only for testing to create a class proxy.
    }
}
