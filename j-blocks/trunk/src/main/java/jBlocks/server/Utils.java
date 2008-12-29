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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * @author Harish Krishnaswamy
 * @version $Id: Utils.java,v 1.6 2005/10/06 21:59:25 harishkswamy Exp $
 */
public class Utils
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
        if (value == null || value.trim().length() == 0)
            return true;

        return false;
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

    public static int getIntProperty(Properties props, String key, int defValue)
    {
        String value = props.getProperty(key);

        return toInt(value, defValue);
    }

    public static int toInt(String val, int defaultValue)
    {
        if (val == null || val.trim().length() == 0)
            return 0;

        return Integer.parseInt(val);
    }

    public static Locale getLocale(String localeStr)
    {
        if (localeStr == null || localeStr.trim().length() == 0)
            return Locale.getDefault();

        String[] parts = localeStr.trim().split("_");

        if (parts.length == 1)
            return new Locale(parts[0]);
        else if (parts.length == 2)
            return new Locale(parts[0], parts[1]);
        else
            return new Locale(parts[0], parts[1], parts[2]);
    }

    private Utils()
    {
        // Static class
    }
}
