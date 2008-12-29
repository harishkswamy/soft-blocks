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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harish Krishnaswamy
 * @version $Id: Messages.java,v 1.3 2005/10/06 21:59:25 harishkswamy Exp $
 */
public class Messages
{
    public static final String  forbiddenTitle   = "forbiddenTitle";
    public static final String  forbiddenMessage = "forbiddenMessage";
    public static final String  alertTitle       = "alertTitle";
    public static final String  errorTitle       = "errorTitle";
    public static final String  errorMessage     = "errorMessage";

    private static final Logger _logger          = LoggerFactory.getLogger(Messages.class);

    private static Messages     _defaultInstance;

    /**
     * @return This returns the default instance that loads message patterns from
     *         <code>/jBlocks/server/Messages.properties</code
     *         file.
     */
    public static Messages formatter()
    {
        if (_defaultInstance == null)
            _defaultInstance = new Messages(Messages.class.getName());

        return _defaultInstance;
    }

    private String _messageBundleBaseName;

    public Messages(String msgBundleBaseName)
    {
        _messageBundleBaseName = msgBundleBaseName;
    }

    public String format(String msgKey)
    {
        return format(Locale.getDefault(), msgKey, (Object) null);
    }

    public String format(Locale locale, String msgKey)
    {
        return format(locale, msgKey, (Object) null);
    }

    public String format(String msgKey, Object... msgParts)
    {
        return format(Locale.getDefault(), msgKey, msgParts);
    }

    public String format(Locale locale, String msgKey, Object... msgParts)
    {
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle(_messageBundleBaseName, locale);

            String pattern = bundle.getString(msgKey);

            return MessageFormat.format(pattern, msgParts);
        }
        catch (Exception e)
        {
            StringBuilder builder = new StringBuilder(msgParts.length > 0 ? String.valueOf(msgParts[0]) : "");

            for (int i = 1; i < msgParts.length; i++)
                builder.append(", ").append(msgParts[i]);

            _logger.warn("Unable to format message for pattern key {} with {} from bundle {}.", new Object[] { msgKey,
                builder.toString(), _messageBundleBaseName });

            return msgKey;
        }
    }
}
