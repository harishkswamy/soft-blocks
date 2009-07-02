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
import jBlocks.shared.StringUtils;

import com.google.gwt.user.client.ui.HTML;

/**
 * @author hkrishna
 */
public class DynamicHTMLView<M extends InputModel<V>, V> extends InputView<HTML, M, V>
{
    private String _prefix;
    private String _suffix;
    private String _blankValue;

    public DynamicHTMLView(M model, String prefix, String suffix, String blankValue)
    {
        super(model, prefix, suffix, blankValue);
    }

    @Override
    protected HTML buildView(M model, Object... args)
    {
        _prefix = (String) args[0];

        if (_prefix == null)
            _prefix = "";

        _suffix = (String) args[1];

        if (_suffix == null)
            _suffix = "";

        _blankValue = (String) args[2];

        return new HTML();
    }

    @Override
    public void valueChanged(M model)
    {
        String text = getModelValue();

        if (_blankValue != null && StringUtils.isEmpty(text))
            getWidget().setHTML(_blankValue);
        else
            getWidget().setHTML(_prefix + text + _suffix);
    }
}
