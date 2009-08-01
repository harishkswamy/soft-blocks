// Copyright 2009 Harish Krishnaswamy
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

package jBlocks.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hkrishna
 */
public class SharedUtils
{
    public static List<String> splitQuoted(String str, char delimiter)
    {
        List<String> tokens = new ArrayList<String>();

        if (isBlank(str))
            return tokens;

        StringBuffer sb = new StringBuffer(str);

        int start = 0, ch;
        boolean quoted = false;

        for (int i = 0; i < sb.length(); i++)
        {
            ch = sb.charAt(i);

            if (ch == '"')
            {
                quoted = !quoted;
                continue;
            }

            if (quoted)
                continue;

            if (ch == delimiter)
            {
                tokens.add(strip(sb.substring(start, i).trim(), "\"").replaceAll("\"\"", "\""));
                start = i + 1;
            }
        }

        tokens.add(strip(sb.substring(start).trim(), "\"").replaceAll("\"\"", "\""));

        return tokens;
    }

    public static boolean isBlank(String value)
    {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Strips leading and trailing characters that match the provided strip string. This differs from trim in that this
     * method only strips the first leading occurance and the last trailing occurance. For example
     * <code>strip("##123.###", "#")</code> will return <code>#123.##</code>.
     */
    public static String strip(String str, String strip)
    {
        if (str.startsWith(strip))
            str = str.substring(strip.length());

        if (str.endsWith(strip))
            str = str.substring(0, str.length() - strip.length());

        return str;
    }

    public static String initCaps(String str)
    {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static int toInt(String val, int defaultValue)
    {
        return isBlank(val) ? defaultValue : Integer.parseInt(val);
    }
    
    public static Integer toInteger(String val)
    {
        return isBlank(val) ? null : new Integer(val.trim());
    }

    public static String trim(String str, String trim)
    {
        int start = 0;

        while (str.startsWith(trim, start))
            start += trim.length();

        str = str.substring(start);

        while (str.endsWith(trim))
            str = str.substring(0, str.length() - trim.length());

        return str;
    }

    public static final String toString(Object obj)
    {
        return obj == null || isBlank(obj.toString()) ? null : obj.toString().trim();
    }
}
