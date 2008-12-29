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

import gwtBlocks.client.TextFormatter;
import gwtBlocks.client.TextFormatters;
import gwtBlocks.client.models.BaseModel;
import gwtBlocks.client.models.InputModel;
import jBlocks.shared.Lookup;

import java.util.List;

import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public class ViewFactory
{
    public <M extends InputModel<String>> TextBoxView<TextBox, M, String> newStringBox(M model, String name)
    {
        model.setName(name);

        TextBox widget = new TextBox();
        widget.addStyleDependentName("String");

        TextBoxView<TextBox, M, String> view = new TextBoxView<TextBox, M, String>(widget, model);
        view.setFormatter(TextFormatters.stringFormatter);

        return view;
    }

    public <M extends InputModel<String>> TextBoxView<PasswordTextBox, M, String> newPasswordBox(M model, String name)
    {
        model.setName(name);

        TextBoxView<PasswordTextBox, M, String> view = new TextBoxView<PasswordTextBox, M, String>(
            new PasswordTextBox(), model);
        view.setFormatter(TextFormatters.stringFormatter);

        return view;
    }

    public <M extends InputModel<String>> TextBoxView<TextArea, M, String> newTextArea(M model, String name)
    {
        model.setName(name);

        TextBoxView<TextArea, M, String> view = new TextBoxView<TextArea, M, String>(new TextArea(), model);
        view.setFormatter(TextFormatters.stringFormatter);

        return view;
    }

    public <M extends InputModel<Integer>> TextBoxView<TextBox, M, Integer> newIntegerBox(M model, String name)
    {
        return newIntegerBox(model, name, null);
    }

    public <M extends InputModel<Integer>> TextBoxView<TextBox, M, Integer> newIntegerBox(M model, String name,
        String format)
    {
        return newNumberBox(model, name, TextFormatters.integerFormatter(format));
    }

    public <M extends InputModel<Double>> TextBoxView<TextBox, M, Double> newDecimalBox(M model, String name)
    {
        return newDecimalBox(model, name, null);
    }

    public <M extends InputModel<Double>> TextBoxView<TextBox, M, Double> newDecimalBox(M model, String name,
        String format)
    {
        return newNumberBox(model, name, TextFormatters.doubleFormatter(format));
    }

    private <M extends InputModel<V>, V extends Number> TextBoxView<TextBox, M, V> newNumberBox(M model, String name,
        TextFormatter<V> formatter)
    {
        model.setName(name);

        TextBox widget = new TextBox();
        widget.addStyleDependentName("Number");
        widget.addKeyboardListener(new KeyboardListenerAdapter()
        {
            @Override
            public void onKeyPress(Widget sender, char keyCode, int modifiers)
            {
                if ((!Character.isDigit(keyCode)) && (keyCode != (char) KEY_TAB) && (keyCode != (char) KEY_BACKSPACE)
                    && (keyCode != (char) KEY_DELETE) && (keyCode != (char) KEY_ENTER) && (keyCode != (char) KEY_HOME)
                    && (keyCode != (char) KEY_END) && (keyCode != (char) KEY_LEFT) && (keyCode != (char) KEY_UP)
                    && (keyCode != (char) KEY_RIGHT) && (keyCode != (char) KEY_DOWN))
                {
                    ((TextBox) sender).cancelKey();
                }
            }
        });

        TextBoxView<TextBox, M, V> view = new TextBoxView<TextBox, M, V>(widget, model);
        view.setFormatter(formatter);

        return view;
    }

    public <M extends InputModel<V>, V> DynamicHTMLView<M, V> newDynamicHTML(M model)
    {
        return newDynamicHTML(model, null, null, null);
    }

    public <M extends InputModel<V>, V> DynamicHTMLView<M, V> newDynamicHTML(M model, String prefix, String suffix,
        String blankValue)
    {
        return new DynamicHTMLView<M, V>(model, prefix, suffix, blankValue);
    }

    public <M extends InputModel<V>, V extends Lookup> SuggestBoxView<M, V> newSuggestBox(M model)
    {
        return new SuggestBoxView<M, V>(model, " ");
    }

    public <M extends InputModel<V>, V extends Lookup> SuggestBoxView<M, V> newSuggestBox(M model, List<V> suggestions,
        String whiteSpaceChars)
    {
        SuggestBoxView<M, V> view = new SuggestBoxView<M, V>(model, whiteSpaceChars);

        view.setSuggestions(suggestions);

        return view;
    }

    public <M extends InputModel<V>, V extends Lookup> SuggestBoxView<M, V> newSuggestBox(M model,
        BaseModel<List<V>> suggestionsModel, String whiteSpaceChars)
    {
        return new SuggestBoxView<M, V>(model, suggestionsModel, whiteSpaceChars);
    }

    public <M extends InputModel<V>, L extends InputModel<List<V>>, V extends Lookup> DropDownBoxView<M, L, V> newDropDownBox(
        M selectedModel, L listModel)
    {
        return new DropDownBoxView<M, L, V>(selectedModel, listModel);
    }

    public <M extends InputModel<V>, V extends Lookup> DropDownBoxView<M, InputModel<List<V>>, V> newDropDownBox(
        M selectedModel)
    {
        return new DropDownBoxView<M, InputModel<List<V>>, V>(selectedModel);
    }

    public <M extends InputModel<List<V>>, L extends InputModel<List<V>>, V extends Lookup> ListBoxView<M, L, V> newListBox(
        M selectedModel, L listModel)
    {
        return new ListBoxView<M, L, V>(selectedModel, listModel);
    }

    public <M extends InputModel<List<V>>, V extends Lookup> ListBoxView<M, InputModel<List<V>>, V> newListBox(
        M selectedModel)
    {
        return new ListBoxView<M, InputModel<List<V>>, V>(selectedModel);
    }

    @SuppressWarnings("unchecked")
    public <M extends InputModel<V>, V extends Lookup> RadioGroupView<M, V> newRadioGroup(M model, List<V> values,
        RadioGroupLayout layout)
    {
        return new RadioGroupView(model, values, layout);
    }
}
