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

import gwtBlocks.client.ValidationException;
import gwtBlocks.client.Validator;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a {@link FeedbackModel} that maintains a validator chain.
 * 
 * @author hkrishna
 */
public class ValidatableModel<V> extends FeedbackModel<V>
{
    private Set<Validator<V>> _validators;

    public void addValidator(Validator<V> validator)
    {
        if (_validators == null)
            _validators = new HashSet<Validator<V>>();

        _validators.add(validator);

        validate(validator);
    }

    public void removeValidator(Validator<V> validator)
    {
        if (_validators == null)
            return;

        _validators.remove(validator);
    }

    /**
     * Validation is plugged in after setting value but before notifying change listeners.
     */
    @Override
    protected void beforeNotifyChangeListeners()
    {
        super.beforeNotifyChangeListeners();

        validate();
    }

    protected final void validate()
    {
        // Clears messages pertaining to this model only.
        clearMessages();

        try
        {
            onValidate();
        }
        catch (ValidationException e)
        {
            addMessage(e.getMessage(), e.getMessageModels());
        }

        if (_validators == null)
            return;

        // Validate
        for (Validator<V> validator : _validators)
            validate(validator);
    }

    /**
     * Placeholder method to be used by subclasses for validation.
     */
    protected void onValidate()
    {
    }

    private void validate(Validator<V> validator)
    {
        try
        {
            validator.validate(this);
        }
        catch (ValidationException e)
        {
            addMessage(e.getMessage(), e.getMessageModels());
        }
    }
}
