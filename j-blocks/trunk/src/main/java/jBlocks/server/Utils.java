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

import jBlocks.shared.SharedUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Properties;

/**
 * @author Harish Krishnaswamy
 * @version $Id: BuildUtils.java,v 1.6 2005/10/06 21:59:25 harishkswamy Exp $
 */
public class Utils extends SharedUtils
{
    public static int getIntProperty(Properties props, String key, int defValue)
    {
        String value = props.getProperty(key);

        return toInt(value, defValue);
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

    public static Properties loadProperties(URL url, Properties defaults)
    {
        InputStream inputStream = null;

        try
        {
            inputStream = url.openStream();

            Properties props = new Properties(defaults);
            props.load(inputStream);

            return props;
        }
        catch (IOException e)
        {
            throw AggregateException.with(e, "Unable to load properties from : " + url);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                    inputStream.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    public static Properties loadProperties(File file, Properties defaults)
    {
        try
        {
            return loadProperties(file.toURL(), defaults);
        }
        catch (Exception e)
        {
            throw AggregateException.with(e, "Unable to load properties from file: " + file);
        }
    }

    public static String getSHA1(byte[] data)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            return convertBytesToString(md.digest(data));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new Error(e);
        }
    }

    private static String convertBytesToString(byte[] value)
    {
        StringBuilder b = new StringBuilder(value.length * 2);

        for (int i = 0; i < value.length; i++)
        {
            int c = value[i] & 0xff;
            b.append(Integer.toString(c >> 4, 16));
            b.append(Integer.toString(c & 0xf, 16));
        }

        return b.toString();
    }

    private Utils()
    {
        // Static class
    }
}
