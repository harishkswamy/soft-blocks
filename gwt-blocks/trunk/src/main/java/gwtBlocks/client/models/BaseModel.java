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
package gwtBlocks.client.models;

import gwtBlocks.client.ValueChangeHistoryListener;
import gwtBlocks.client.ValueChangeListener;

import java.util.HashSet;
import java.util.Set;

/**
 * This is the base implementation of the presentation model pattern of the MVC framework.
 * <P>
 * This is the fundamental unit that forms a tree of models to support a composite view. Every model becomes a part of
 * the tree by holding on to its parent provided to it via {@link #setParent(String, CompositeModel)}. The root model
 * of the tree has no parent and so its parent is left null.
 * <p>
 * Every model holds a single value and has listeners, registered to it via
 * {@link #registerChangeListener(ValueChangeListener)}, that listen to changes to the model's value. Every parent
 * listens to its children and every child listens to its parent and there by enabling a decoupled solution.
 * <p>
 * The specific features of this model are its ability to let sub classes hook behavior before notifying listeners and
 * its ability to batch multiple changes to its value and only notify the listeners when the batch is committed.
 * 
 * @author hkrishna
 */
public class BaseModel<V>
{
    private V                                                          _value, _oldValue;
    private Set<ValueChangeListener<? extends BaseModel<?>>>           _changeListeners;
    private Set<ValueChangeHistoryListener<? extends BaseModel<V>, V>> _changeHistoryListeners;
    private boolean                                                    _inBatchMode;
    private CompositeModel<?>                                          _parent;
    private boolean                                                    _autoCommit;

    public BaseModel()
    {
    }

    public BaseModel(String key, CompositeModel<?> parent)
    {
        setParent(key, parent);
    }

    public void setParent(String key, CompositeModel<?> parent)
    {
        if (parent == _parent)
            return;

        _parent = parent;

        if (_parent != null)
            _parent.addChild(key, this);

        // In case this model is created after the parent model value is set.
        parentValueChanged();
    }

    @SuppressWarnings("unchecked")
    public <P extends CompositeModel<?>> P getParent()
    {
        return (P) _parent;
    }

    public void setAutoCommit(boolean autoCommit)
    {
        _autoCommit = autoCommit;
    }

    public boolean isAutoCommit()
    {
        return _autoCommit || (_parent != null && _parent.isAutoCommit());
    }

    public void parentValueChanged()
    {
    }

    /**
     * Sets this model's value and notifies registered change listeners. Sub classes may implement
     * {@link #beforeNotifyChangeListeners()} method to hook behavior before notifying change listeners.
     * <p>
     * If in batch mode this method will not notify listeners until {@link #endBatch()} is called, neither will it
     * invoke the {@link #beforeNotifyChangeListeners()} hook.
     * 
     * @param value
     *            The new value for the model.
     */
    public void setValue(V value)
    {
        beforeSetValue();

        _oldValue = _value;
        _value = value;

        if (isAutoCommit())
            commit();

        if (_parent != null)
            _parent.childValueChanged(this);

        if (!isInBatchMode())
            fireValueChanged();

        afterSetValue();
    }

    protected void beforeSetValue()
    {

    }

    protected void afterSetValue()
    {

    }

    /**
     * This method must be implemented by subclasses to transfer the value in this model to the domain object.
     */
    protected void commit()
    {

    }

    /**
     * This method will {@link #commit()} if this model is not in auto-commit mode.
     */
    public final void save()
    {
        if (!isAutoCommit())
            commit();
    }

    private void fireValueChanged()
    {
        beforeNotifyChangeListeners();

        notifyChangeListeners();
    }

    @SuppressWarnings("unchecked")
    private void notifyChangeListeners()
    {
        if (_changeListeners != null)
        {
            for (ValueChangeListener listener : _changeListeners)
                listener.valueChanged(this);
        }

        if (_changeHistoryListeners != null)
        {
            for (ValueChangeHistoryListener listener : _changeHistoryListeners)
                listener.valueChanged(this, _oldValue);
        }

        _oldValue = null;
    }

    /**
     * Hook method that will be called immediately after this model's value is set but before notifying any registered
     * change listeners. This method will not be invoked when in batch mode until {@link #endBatch()} is called.
     */
    protected void beforeNotifyChangeListeners()
    {
    }

    public V getValue()
    {
        return _value;
    }

    /**
     * Starting a batch stops the changes to this model value from being propagated to listeners.
     * 
     * @see #endBatch()
     */
    public void startBatch()
    {
        _inBatchMode = true;
    }

    /**
     * If in batch mode, this method notifies all listeners and ends the batch.
     */
    public void endBatch()
    {
        if (isInBatchMode())
        {
            _inBatchMode = false;

            fireValueChanged();
        }
    }

    protected final boolean isInBatchMode()
    {
        return _inBatchMode || (_parent != null && _parent.isInBatchMode());
    }

    public void registerChangeListener(ValueChangeListener<? extends BaseModel<?>> listener)
    {
        if (_changeListeners == null)
            _changeListeners = new HashSet<ValueChangeListener<? extends BaseModel<?>>>();

        _changeListeners.add(listener);
    }

    public void removeChangeListener(ValueChangeListener<? extends BaseModel<?>> listener)
    {
        if (_changeListeners == null)
            return;

        _changeListeners.remove(listener);
    }

    public void registerChangeHistoryListener(ValueChangeHistoryListener<? extends BaseModel<V>, V> listener)
    {
        if (_changeHistoryListeners == null)
            _changeHistoryListeners = new HashSet<ValueChangeHistoryListener<? extends BaseModel<V>, V>>();

        _changeHistoryListeners.add(listener);
    }

    public void removeChangeHistoryListener(ValueChangeHistoryListener<? extends BaseModel<V>, V> listener)
    {
        if (_changeHistoryListeners == null)
            return;

        _changeHistoryListeners.remove(listener);
    }
}
