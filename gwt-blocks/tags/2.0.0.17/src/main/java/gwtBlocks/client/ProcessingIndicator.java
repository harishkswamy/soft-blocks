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
package gwtBlocks.client;

import gwtBlocks.client.views.FlexTableBuilder;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author hkrishna
 */
public class ProcessingIndicator extends Composite
{
    public ProcessingIndicator()
    {
        FlexTableBuilder b = new FlexTableBuilder();
        b.styleC("image").set("").middleC().set(GwtBlocksMessages.pick.processing());

        initWidget(b.getTable());
        setStyleName("gbk-ProcessingIndicator");
    }

    public void setVisible(boolean show)
    {
        if (show)
            RootPanel.get().addStyleName("processing");
        else
            RootPanel.get().removeStyleName("processing");

        super.setVisible(show);
    }
}
