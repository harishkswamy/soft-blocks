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
package gwtBlocks.client.views;

import gwtBlocks.client.FormatterException;
import gwtBlocks.client.TextFormatter;
import gwtBlocks.shared.models.InputModel;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public abstract class InputView<W extends Widget, M extends InputModel<V>, V> extends BaseView<W, M>
{
    private TextFormatter<V> _formatter;

    public InputView(M model)
    {
        super(model);
    }

    protected InputView(M model, Object... args)
    {
        super(model, args);
    }

    public void setFormatter(TextFormatter<V> format)
    {
        _formatter = format;
    }

    public TextFormatter<V> getFormatter()
    {
        return _formatter;
    }

    @SuppressWarnings("unchecked")
    protected void setModelValue(String text)
    {
        try
        {
            V value = null;

            // Convert
            if (_formatter != null)
            {
                getModel().clearMessages();
                value = _formatter.parse(text);
            }
            else
                value = (V) text; // Assuming V is String

            // setValue
            getModel().setValue(value);
        }
        catch (FormatterException e)
        {
            getModel().addMessage(e.getMessage(), null);
        }
    }

    protected String getModelValue()
    {
        V value = getModel().getValue();

        if (_formatter == null)
            return value == null ? "" : value.toString();

        return _formatter.format(value);
    }
}
