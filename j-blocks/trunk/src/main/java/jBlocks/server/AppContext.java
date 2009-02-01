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

import java.util.HashMap;
import java.util.Map;

/**
 * @author hkrishna
 */
public class AppContext
{
    public static final String KEY = "$app$ctx$";

    private static class Key
    {
        private Class<?> clas;
        private String   classifier;

        Key(Class<?> clas, String classifier)
        {
            this.clas = clas;
            this.classifier = classifier;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
                return true;

            if (obj == null || !(obj instanceof Key))
                return false;

            Key that = (Key) obj;

            boolean classEq = clas == null ? that.clas == null : clas.equals(that.clas);
            boolean classifierEq = classifier == null ? that.classifier == null : classifier.equals(that.classifier);

            return classEq && classifierEq;
        }

        @Override
        public int hashCode()
        {
            int result = 17;
            result = 37 * result + (clas == null ? 0 : clas.hashCode());
            result = 37 * result + (classifier == null ? 0 : classifier.hashCode());
            return result;
        }
    }

    private int                 _envId;

    private Map<Object, Object> _ctx = new HashMap<Object, Object>();

    public AppContext()
    {
        _ctx.put(ThreadLocal.class, new ThreadLocal<Map<Object, Object>>()
        {
            protected Map<Object, Object> initialValue()
            {
                return new HashMap<Object, Object>();
            }
        });
    }

    public void setEnvId(String envId)
    {
        if ("dev".equalsIgnoreCase(envId))
            _envId = 1;
    }

    public boolean isEnvDev()
    {
        return _envId == 1;
    }

    public <T> T get(Class<T> key)
    {
        return key.cast(_ctx.get(key));
    }

    public <T> T get(Class<T> key, String classifier)
    {
        return key.cast(_ctx.get(new Key(key, classifier)));
    }

    public <T> T remove(Class<T> key)
    {
        return key.cast(_ctx.remove(key));
    }

    public <T> T remove(Class<T> key, String classifier)
    {
        return key.cast(_ctx.remove(new Key(key, classifier)));
    }

    public void put(Class<?> key, Object value)
    {
        if (!key.isAssignableFrom(value.getClass()))
            throw new IllegalArgumentException("Value must be assignable to the key class");

        _ctx.put(key, value);
    }

    public void put(Class<?> key, String classifier, Object value)
    {
        if (!key.isAssignableFrom(value.getClass()))
            throw new IllegalArgumentException("Value must be assignable to the key class");

        _ctx.put(new Key(key, classifier), value);
    }

    public void cleanup()
    {
        _ctx.clear();
        _ctx = null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getFromThread(Class<T> key)
    {
        ThreadLocal<Map<Object, Object>> threadLocal = get(ThreadLocal.class);
        return key.cast(threadLocal.get().get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T getFromThread(Class<T> key, String classifier)
    {
        ThreadLocal<Map<Object, Object>> threadLocal = get(ThreadLocal.class);
        return key.cast(threadLocal.get().get(new Key(key, classifier)));
    }

    @SuppressWarnings("unchecked")
    public <T> T removeFromThread(Class<T> key)
    {
        ThreadLocal<Map<Object, Object>> threadLocal = get(ThreadLocal.class);
        return key.cast(threadLocal.get().remove(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T removeFromThread(Class<T> key, String classifier)
    {
        ThreadLocal<Map<Object, Object>> threadLocal = get(ThreadLocal.class);
        return key.cast(threadLocal.get().remove(new Key(key, classifier)));
    }

    @SuppressWarnings("unchecked")
    public void putInThread(Class<?> key, Object value)
    {
        if (!key.isAssignableFrom(value.getClass()))
            throw new IllegalArgumentException("Value must be assignable to the key class");

        ThreadLocal<Map<Object, Object>> threadLocal = get(ThreadLocal.class);
        threadLocal.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public void putInThread(Class<?> key, String classifier, Object value)
    {
        if (!key.isAssignableFrom(value.getClass()))
            throw new IllegalArgumentException("Value must be assignable to the key class");

        ThreadLocal<Map<Object, Object>> threadLocal = get(ThreadLocal.class);
        threadLocal.get().put(new Key(key, classifier), value);
    }

    @SuppressWarnings("unchecked")
    public void cleanupThread()
    {
        ThreadLocal<Map<Object, Object>> threadLocal = get(ThreadLocal.class);
        threadLocal.remove();
    }
}
