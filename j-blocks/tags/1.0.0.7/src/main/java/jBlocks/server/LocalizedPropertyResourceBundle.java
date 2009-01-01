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

import jBlocks.server.tapestry.LocalizedProperties;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author hkrishna
 */
public class LocalizedPropertyResourceBundle extends ResourceBundle
{
    private static class ResourceBundleEnumeration implements Enumeration<String>
    {

        Set<String>         set;
        Iterator<String>    iterator;
        Enumeration<String> enumeration; // may remain null

        /**
         * Constructs a resource bundle enumeration.
         * 
         * @param set
         *            an set providing some elements of the enumeration
         * @param enumeration
         *            an enumeration providing more elements of the enumeration. enumeration may be null.
         */
        ResourceBundleEnumeration(Set<String> set, Enumeration<String> enumeration)
        {
            this.set = set;
            this.iterator = set.iterator();
            this.enumeration = enumeration;
        }

        String next = null;

        public boolean hasMoreElements()
        {
            if (next == null)
            {
                if (iterator.hasNext())
                {
                    next = iterator.next();
                }
                else if (enumeration != null)
                {
                    while (next == null && enumeration.hasMoreElements())
                    {
                        next = enumeration.nextElement();
                        if (set.contains(next))
                        {
                            next = null;
                        }
                    }
                }
            }
            return next != null;
        }

        public String nextElement()
        {
            if (hasMoreElements())
            {
                String result = next;
                next = null;
                return result;
            }
            else
            {
                throw new NoSuchElementException();
            }
        }
    }

    private LocalizedProperties _lookup;

    public LocalizedPropertyResourceBundle(InputStream stream)
    {
        try
        {
            _lookup = new LocalizedProperties();
            _lookup.load(stream);
        }
        catch (Exception e)
        {
            throw AggregateException.with(e);
        }
    }

    public LocalizedPropertyResourceBundle(InputStream stream, String encoding)
    {
        try
        {
            _lookup = new LocalizedProperties();
            _lookup.load(stream, encoding);
        }
        catch (Exception e)
        {
            throw AggregateException.with(e);
        }
    }

    @Override
    public Object handleGetObject(String key)
    {
        if (key == null)
            throw new NullPointerException();

        return _lookup.getProperty(key);
    }

    @Override
    public Enumeration<String> getKeys()
    {
        _lookup.getPropertyMap().keySet();
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(_lookup.getPropertyMap().keySet(), (parent != null) ? parent.getKeys()
            : null);
    }
}
