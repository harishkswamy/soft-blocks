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

import gwtBlocks.shared.models.InputModel;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public class TextBoxView<W extends TextBoxBase, M extends InputModel<V>, V> extends InputView<W, M, V> implements CanEnable
{
    public TextBoxView(final W textWidget, M model)
    {
        super(model, textWidget);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected W buildView(M model, Object... args)
    {
        final W textWidget = (W) args[0];
        
        textWidget.addChangeListener(new ChangeListener()
        {
            public void onChange(Widget sender)
            {
                setModelValue(textWidget.getText());
            }
        });

        return textWidget;
    }

    @Override
    public void valueChanged(M model)
    {
        getWidget().setText(getModelValue());
    }

    public void setEnabled(boolean flag)
    {
        getWidget().setEnabled(flag);
    }
}
