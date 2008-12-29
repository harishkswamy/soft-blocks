// Copyright 2008 Harish Krishnaswamy
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package gwtBlocks.client;

import jBlocks.shared.StringUtils;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author hkrishna
 */
public class TextFormatters
{
    public static final TextFormatter<String> stringFormatter = new StringFormatter();

    private static abstract class AbstractFormatter<T> implements TextFormatter<T>
    {
        public String format(T value)
        {
            return value == null ? "" : value.toString();
        }
    }

    private static class StringFormatter extends AbstractFormatter<String>
    {
        public String parse(String text)
        {
            return text;
        }
    }

    private static abstract class NumberFormatter<T extends Number> extends AbstractFormatter<T>
    {
        private static final Map<String, NumberFormatter<?>> _cache = new HashMap<String, NumberFormatter<?>>();

        @SuppressWarnings("unchecked")
        private static <F extends NumberFormatter<?>> F getFormatter(String format)
        {
            return (F) _cache.get(format == null ? "default" : format);
        }

        private NumberFormat _formatter;

        private NumberFormatter(String format)
        {
            _formatter = format == null ? NumberFormat.getDecimalFormat() : NumberFormat.getFormat(format);
            _cache.put(format == null ? "default" : format, this);
        }

        public T parse(String value) throws FormatterException
        {
            if (StringUtils.isEmpty(value))
                return null;

            try
            {
                return convert(_formatter.parse(value));
            }
            catch (NumberFormatException e)
            {
                throw new FormatterException(GwtBlocksMessages.pick.invalidNumberFormat(value, _formatter.getPattern()));
            }
        }

        protected abstract T convert(Double value);

        @Override
        public String format(T value)
        {
            return value == null ? "" : _formatter.format(value.doubleValue());
        }
    }

    private static class IntegerFormatter extends NumberFormatter<Integer>
    {
        private IntegerFormatter(String format)
        {
            super(format);
        }

        @Override
        protected Integer convert(Double value)
        {
            return value.intValue();
        }
    }

    private static class DoubleFormatter extends NumberFormatter<Double>
    {
        private DoubleFormatter(String format)
        {
            super(format);
        }

        @Override
        protected Double convert(Double value)
        {
            return value;
        }
    }

    public static NumberFormatter<Integer> integerFormatter(String format)
    {
        IntegerFormatter formatter = NumberFormatter.getFormatter(format);

        if (formatter == null)
            formatter = new IntegerFormatter(format);

        return formatter;
    }

    public static NumberFormatter<Double> doubleFormatter(String format)
    {
        DoubleFormatter formatter = NumberFormatter.getFormatter(format);

        if (formatter == null)
            formatter = new DoubleFormatter(format);

        return formatter;
    }

    private TextFormatters()
    {
        // static class
    }
}
