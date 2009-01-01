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

import gwtBlocks.client.GwtBlocksMessages;

/**
 * This is a {@link BaseModel} that wraps the {@link MessageModel} and has a name. This is a convenience model that can
 * be used to provide user feedback.
 * 
 * @author hkrishna
 */
public class FeedbackModel<V> extends BaseModel<V>
{
    private String       _name;
    private MessageModel _messageModel;

    public void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

    protected MessageModel getMessageModel()
    {
        return getParent() == null ? _messageModel : getParent().getMessageModel();
    }

    public void setMessageModel(MessageModel model)
    {
        _messageModel = model;
    }

    public void addMessage(String msg, FeedbackModel<?>[] affectedModels)
    {
        MessageModel msgModel = getMessageModel();

        if (msgModel == null)
            throw new IllegalArgumentException(GwtBlocksMessages.pick.noRegisteredMessageModel(_name, msg));

        msgModel.addMessage(this, msg, affectedModels);
    }

    /**
     * Clears messages pertaining to this model only.
     */
    public void clearMessages()
    {
        MessageModel msgModel = getMessageModel();

        if (msgModel == null)
            return;

        msgModel.clear(this);
    }

    public boolean hasErrors()
    {
        MessageModel msgModel = getMessageModel();

        return msgModel == null ? false : msgModel.hasErrors();
    }

    public String getMessages()
    {
        MessageModel msgModel = getMessageModel();

        return msgModel == null ? null : msgModel.toString();
    }
}
