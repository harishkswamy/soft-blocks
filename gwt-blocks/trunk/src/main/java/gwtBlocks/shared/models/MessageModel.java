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
package gwtBlocks.shared.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hkrishna
 */
public class MessageModel extends BaseModel<Map<FeedbackModel<?>, Map<String, List<FeedbackModel<?>>>>>
{
    public void addMessage(FeedbackModel<?> valModel, String msg, FeedbackModel<?>[] msgModels)
    {
        msgModels = msgModels == null || msgModels.length == 0 ? new FeedbackModel[] { valModel } : msgModels;
        List<FeedbackModel<?>> msgModelList = concat(new ArrayList<FeedbackModel<?>>(), msgModels);
        Map<FeedbackModel<?>, Map<String, List<FeedbackModel<?>>>> messageMap = getMessageMap();
        Map<String, List<FeedbackModel<?>>> modelMessages = getModelMessages(messageMap, valModel);
        List<FeedbackModel<?>> oldMsgModels = modelMessages.get(msg);

        if (oldMsgModels != null)
            msgModelList.addAll(oldMsgModels);

        modelMessages.put(msg, msgModelList);
        setValue(messageMap); // This needs to be called to notify change listeners
    }

    private List<FeedbackModel<?>> concat(List<FeedbackModel<?>> msgModelList, FeedbackModel<?>[] msgModels)
    {
        if (msgModels == null)
            return msgModelList;

        for (int i = 0; i < msgModels.length; i++)
            msgModelList.add(msgModels[i]);

        return msgModelList;
    }

    public boolean hasErrors()
    {
        return !getMessageMap().isEmpty();
    }

    public void clear(FeedbackModel<?> model)
    {
        Map<FeedbackModel<?>, Map<String, List<FeedbackModel<?>>>> messageMap = getMessageMap();
        if (messageMap.remove(model) != null)
            setValue(messageMap); // This needs to be called to notify change listeners
    }

    public void clear()
    {
        Map<FeedbackModel<?>, Map<String, List<FeedbackModel<?>>>> messageMap = getMessageMap();
        messageMap.clear();
        setValue(messageMap); // This needs to be called to notify change listeners
    }

    private Map<FeedbackModel<?>, Map<String, List<FeedbackModel<?>>>> getMessageMap()
    {
        Map<FeedbackModel<?>, Map<String, List<FeedbackModel<?>>>> messageMap = getValue();
        return messageMap == null ? new HashMap<FeedbackModel<?>, Map<String, List<FeedbackModel<?>>>>() : messageMap;
    }

    private Map<String, List<FeedbackModel<?>>> getModelMessages(
        final Map<FeedbackModel<?>, Map<String, List<FeedbackModel<?>>>> messageMap, FeedbackModel<?> model)
    {
        Map<String, List<FeedbackModel<?>>> messages = messageMap.get(model);

        if (messages == null)
        {
            messages = new HashMap<String, List<FeedbackModel<?>>>();
            messageMap.put(model, messages);
        }

        return messages;
    }

    @Override
    public String toString()
    {
        Map<FeedbackModel<?>, Map<String, List<FeedbackModel<?>>>> modelMsgMap = getValue();

        if (modelMsgMap == null)
            return null;

        StringBuffer msgBuf = new StringBuffer();

        for (Map<String, List<FeedbackModel<?>>> msgMap : modelMsgMap.values())
        {
            for (String msg : msgMap.keySet())
                msgBuf.append(msg).append('\n');
        }

        return msgBuf.toString();
    }
}
