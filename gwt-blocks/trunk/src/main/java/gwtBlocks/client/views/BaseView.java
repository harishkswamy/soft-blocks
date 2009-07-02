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

import gwtBlocks.shared.models.BaseModel;
import gwtBlocks.shared.models.ValueChangeListener;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public abstract class BaseView<W extends Widget, M extends BaseModel<?>> extends Composite implements
    ValueChangeListener<M>
{
    private M _model;

    protected BaseView(M model, Object... args)
    {
        _model = model;
        initWidget(buildView(model, args));
    }

    @Override
    protected void onLoad()
    {
        super.onLoad();

        registerChangeListener();
    }

    @SuppressWarnings("unchecked")
    @Override
    public W getWidget()
    {
        return (W) super.getWidget();
    }

    public void setModel(M model)
    {
        if (_model != null)
            _model.removeChangeListener(this);

        _model = model;

        registerChangeListener();
    }

    private void registerChangeListener()
    {
        if (_model != null)
        {
            _model.registerChangeListener(this);

            valueChanged(_model);
        }
    }

    public M getModel()
    {
        return _model;
    }

    public void valueChanged(M model)
    {
    }

    protected abstract W buildView(M model, Object... args);
}
