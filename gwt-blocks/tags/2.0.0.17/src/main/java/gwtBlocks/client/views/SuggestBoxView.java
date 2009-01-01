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

import gwtBlocks.client.ValueChangeListener;
import gwtBlocks.client.models.BaseModel;
import gwtBlocks.client.models.InputModel;
import jBlocks.shared.Lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public class SuggestBoxView<M extends InputModel<V>, V extends Lookup> extends BaseView<SuggestBox, M>
{
    private MultiWordSuggestOracle _oracle;
    private Map<String, V>         _lookupMap;

    protected SuggestBoxView(M model, String whiteSpaceChars)
    {
        super(model, whiteSpaceChars);
    }

    protected SuggestBoxView(M model, BaseModel<List<V>> suggestionModel, String whiteSpaceChars)
    {
        super(model, whiteSpaceChars, suggestionModel);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SuggestBox buildView(M model, Object... args)
    {
        String whiteSpaceChars = (String) args[0];

        if (args.length > 1)
        {
            BaseModel<List<V>> suggestionModel = (BaseModel<List<V>>) args[1];

            suggestionModel.registerChangeListener(new ValueChangeListener<BaseModel<List<V>>>()
            {
                public void valueChanged(BaseModel<List<V>> model)
                {
                    setSuggestions(model.getValue());
                }
            });
        }

        _oracle = new MultiWordSuggestOracle(whiteSpaceChars);

        final SuggestBox sb = new SuggestBox(_oracle);

        sb.addEventHandler(new SuggestionHandler()
        {
            public void onSuggestionSelected(SuggestionEvent event)
            {
                getModel().setValue(_lookupMap.get(event.getSelectedSuggestion().getReplacementString()));
            }
        });

        sb.addChangeListener(new ChangeListener()
        {
            public void onChange(Widget sender)
            {
                getModel().setValue(_lookupMap.get(sb.getText()));
            }
        });

        return sb;
    }

    public void setSuggestions(List<V> suggestions)
    {
        _oracle.clear();

        _lookupMap = new HashMap<String, V>();

        if (suggestions == null)
            return;

        for (V item : suggestions)
        {
            _lookupMap.put(item.getLookupName(), item);
            _oracle.add(item.getLookupName());
        }
    }

    @Override
    public void valueChanged(M model)
    {
        if (model.getValue() == null)
            getWidget().setText(null);
        else
            getWidget().setText(model.getValue().getLookupName());
    }
}
