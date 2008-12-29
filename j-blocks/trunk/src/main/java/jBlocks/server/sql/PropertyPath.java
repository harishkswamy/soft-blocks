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
 * @author hkrishna
 */
public class PropertyPath
{
    protected static String[] parsePath(String path, boolean set)
    {
        if (".".equals(path))
            return new String[] { path };

        String[] props = path.split("\\$");

        int lastIndex = props.length - 1;

        for (int i = 0; i < lastIndex; i++)
            props[i] = "get" + props[i];

        props[lastIndex] = set ? "set" + props[lastIndex] : "get" + props[lastIndex];

        return props;
    }

    private String   _path;
    private String[] _properties;

    PropertyPath(String path, String[] props)
    {
        _path = path;
        _properties = props;
    }

    public String[] getProperties()
    {
        return _properties;
    }

    public String getPath()
    {
        return _path;
    }

    @Override
    public String toString()
    {
        return _path;
    }
}
