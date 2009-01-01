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

import jBlocks.shared.StringUtils;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerAdapter;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public class WidgetFactory
{
    private static MouseListener _iconMouseListener;

    private WidgetFactory()
    {
        // Static class
    }

    public static final FlexTable newFlexTable()
    {
        FlexTable table = new FlexTable();
        table.setCellSpacing(0);
        table.setCellPadding(0);

        return table;
    }

    public static final Grid newGrid()
    {
        Grid grid = new Grid();
        grid.setCellSpacing(0);
        grid.setCellPadding(0);

        return grid;
    }

    public static final HTMLTable addCaption(HTMLTable table, String caption)
    {
        if (caption == null)
            return table;

        Element captionEle = DOM.createCaption();
        DOM.setInnerText(captionEle, caption);
        DOM.appendChild(table.getElement(), captionEle);

        return table;
    }

    public static DecoratorPanel newDecoratorPanel(Widget widget)
    {
        DecoratorPanel dPanel = new DecoratorPanel();
        dPanel.setWidget(widget);

        return dPanel;
    }

    public static Image newIconButton(final String imageName, String tip, ClickListener listener)
    {
        Image.prefetch("gwtBlocks/images/iconButtonHoverBkg.jpg");
        Image.prefetch("gwtBlocks/images/iconButtonDownBkg.jpg");

        String simpleName = imageName.substring(imageName.lastIndexOf('/') + 1, imageName.lastIndexOf('.'));

        final Image icon = new Image(imageName);
        icon.setSize("20px", "20px");
        icon.setStylePrimaryName("gbk-iconButton");
        icon.addStyleDependentName(simpleName);
        icon.setTitle(tip);

        if (listener != null)
            icon.addClickListener(listener);

        icon.addMouseListener(getIconMouseListener(simpleName));

        return icon;
    }

    public static void disableIconButton(Image icon)
    {
        icon.removeMouseListener(getIconMouseListener(null));
        icon.addStyleName("disable");
        int end = icon.getUrl().lastIndexOf("Off.gif");
        if (end == -1)
            icon.setUrl(icon.getUrl().substring(0, icon.getUrl().lastIndexOf(".gif")) + "Off.gif");
    }

    public static void enableIconButton(Image icon)
    {
        icon.removeMouseListener(getIconMouseListener(null)); // This is to avoid duplicate listners.
        icon.addMouseListener(getIconMouseListener(null));
        icon.removeStyleName("disable");
        int end = icon.getUrl().lastIndexOf("Off.gif");
        if (end > -1)
            icon.setUrl(icon.getUrl().substring(0, end) + ".gif");
    }

    private static MouseListener getIconMouseListener(final String suffix)
    {
        if (_iconMouseListener == null)
            _iconMouseListener = new MouseListenerAdapter()
            {
                public void onMouseDown(Widget sender, int x, int y)
                {
                    sender.addStyleDependentName("down");
                    if (suffix != null)
                        sender.addStyleDependentName("down-" + suffix);
                }

                public void onMouseUp(Widget sender, int x, int y)
                {
                    sender.removeStyleDependentName("down");
                    sender.removeStyleDependentName("down-" + suffix);
                }

                public void onMouseEnter(Widget sender)
                {
                    sender.addStyleDependentName("hover");
                    if (suffix != null)
                        sender.addStyleDependentName("hover-" + suffix);
                }

                public void onMouseLeave(Widget sender)
                {
                    sender.removeStyleDependentName("hover");
                    sender.removeStyleDependentName("down");
                    sender.removeStyleDependentName("hover-" + suffix);
                    sender.removeStyleDependentName("down-" + suffix);
                }
            };

        return _iconMouseListener;
    }

    public static Widget newIconTextButton(String imageName, String text, final ClickListener listener)
    {
        FlexTableBuilder builder = new FlexTableBuilder().styleT("gbk-iconTextButton").formLayout();
        final FlexTable table = builder.getTable();

        table.addTableListener(new TableListener()
        {
            public void onCellClicked(SourcesTableEvents sender, int row, int cell)
            {
                listener.onClick(table);
            }
        });

        builder.set(new Image(imageName)).set(text);

        return table;
    }

    public static Label newButtonGroupSeparator(String styleName)
    {
        Label separator = new Label("|");
        separator.setWidth("16px");
        separator.getElement().getStyle().setProperty("font-size", "140%");
        separator.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        separator.setStylePrimaryName("gbk-buttonGroupSeparator");

        if (!StringUtils.isEmpty(styleName))
            separator.addStyleDependentName(styleName);

        return separator;
    }

    public static final ScrollPanel newStretchedScrollPanel(Widget widget)
    {
        ScrollPanel scrollPanel = new ScrollPanel();

        scrollPanel.setStylePrimaryName("gbk-stretchedScrollPanel");
        scrollPanel.setSize("100%", "100%");

        if (widget != null)
            scrollPanel.setWidget(widget);

        return scrollPanel;
    }
}
