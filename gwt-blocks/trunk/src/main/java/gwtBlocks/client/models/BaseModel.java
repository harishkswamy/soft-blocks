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
 * the tree by holding on to its parent provided to it via {@link #setParent(String, CompositeModel)}. The root model of
 * the tree has no parent and so its parent is left null.
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
    private String                                                     _key;
    private CompositeModel<?>                                          _parent;
    private boolean                                                    _discreet, _autoCommit;

    public BaseModel()
    {
    }

    /**
     * @see #setParent(String, CompositeModel)
     */
    public BaseModel(String key, CompositeModel<?> parent)
    {
        setParent(key, parent);
    }

    /**
     * Sets the parent, registers itself as a child in the parent and, invoked {@link #parentValueChanged()}.
     * 
     * @param key
     *            This model's name.
     * @param parent
     *            The parent.
     */
    public void setParent(String key, CompositeModel<?> parent)
    {
        if (parent == _parent)
            return;

        _key = key;
        _parent = parent;

        if (_parent != null)
            _parent.addChild(key, this);

        // In case this model is created after the parent model value is set.
        if (!isDiscreet())
            parentValueChanged();
    }

    public String getKey()
    {
        return _key;
    }

    /**
     * @param <P>
     *            The parent of this model.
     * @return
     */
    @SuppressWarnings("unchecked")
    public <P extends CompositeModel<?>> P getParent()
    {
        return (P) _parent;
    }

    /**
     * Sets this model's value and notifies registered change listeners. Sub classes may implement
     * {@link #beforeNotifyChangeListeners()} method to hook behavior before notifying change listeners.
     * <p>
     * If in batch mode this method will not notify listeners until {@link #discreetOff()} is called, neither will it
     * invoke the {@link #beforeNotifyChangeListeners()} hook.
     * 
     * @param value
     *            The new value for the model.
     */
    public void setValue(V value)
    {
        _oldValue = _value;
        _value = value;

        if (!isDiscreet())
            announceValueChange(true);
    }

    private void announceValueChange(boolean fire)
    {
        valueChanged();

        if (_parent != null)
            _parent.childValueChanged(this);

        if (isAutoCommit())
            commit();

        if (fire)
            fireValueChanged();
    }

    /**
     * @return The value of this model.
     */
    public V getValue()
    {
        return _value;
    }

    /**
     * Hook method called right after this model's value is set before notifying the parent.
     */
    protected void valueChanged()
    {
    }

    /**
     * Listener method that will be invoked when the parent model's value changes.
     */
    protected void parentValueChanged()
    {
    }

    /**
     * Turning auto-commit on will stop buffering the model's value and pass it through to the underlying domain object.
     */
    public void autoCommitOn()
    {
        _autoCommit = true;
    }

    /**
     * Turning auto-commit off will start buffering the value in this model and will only pass it to the underlying
     * domain object upon {@link #commit()}.
     */
    public void autoCommitOff()
    {
        _autoCommit = false;
    }

    /**
     * @return true if this model or any of its ancestors are in auto-commit mode, false otherwise.
     */
    public final boolean isAutoCommit()
    {
        return _autoCommit || (_parent != null && _parent.isAutoCommit());
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

    /**
     * Turning discreet on stops the changes to this model's value from being notified to listeners.
     * 
     * @see #discreetOff()
     */
    public void discreetOn()
    {
        _discreet = true;
    }

    /**
     * Turning discreet off starts notifying the listeners of the changes to this model's value.
     * 
     * @param fire
     *            when true, notifies the listeners of the last value in the model, when false, it only notifies future
     *            changes to the model's value.
     */
    public void discreetOff(boolean fire)
    {
        _discreet = false;

        announceValueChange(fire);
    }

    /**
     * @return true if this model or any of its ancestors are discreet, false otherwise.
     */
    public final boolean isDiscreet()
    {
        return _discreet || (_parent != null && _parent.isDiscreet());
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
     * Hook method that will be invoked before notifying any registered change listeners. This method will not be
     * invoked when in discreet mode until {@link #discreetOff()} is called.
     */
    protected void beforeNotifyChangeListeners()
    {
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
