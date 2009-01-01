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
    public static final String    KEY  = "$app$ctx$";

    private int                   _envId;

    private Map<Class<?>, Object> _ctx = new HashMap<Class<?>, Object>();

    public AppContext()
    {
        _ctx.put(ThreadLocal.class, new ThreadLocal<Map<Class<?>, Object>>()
        {
            protected Map<Class<?>, Object> initialValue()
            {
                return new HashMap<Class<?>, Object>();
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

    public void put(Class<?> key, Object value)
    {
        _ctx.put(key, value);
    }

    public void cleanup()
    {
        _ctx.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T getFromThread(Class<T> key)
    {
        ThreadLocal<Map<Class<?>, Object>> threadLocal = get(ThreadLocal.class);
        return key.cast(threadLocal.get().get(key));
    }

    @SuppressWarnings("unchecked")
    public void putInThread(Class<?> key, Object value)
    {
        ThreadLocal<Map<Class<?>, Object>> threadLocal = get(ThreadLocal.class);
        threadLocal.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public void cleanupThread()
    {
        ThreadLocal<Map<Class<?>, Object>> threadLocal = get(ThreadLocal.class);
        threadLocal.remove();
    }
}
