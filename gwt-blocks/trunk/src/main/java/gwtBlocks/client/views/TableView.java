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

import gwtBlocks.shared.models.TableModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public abstract class TableView<V> extends BaseView<TableView<V>.TableWidget, TableModel<V>>
{
    public static final int SCROLLABLE = 0x1;
    public static final int PAGEABLE   = 0x2;

    class TableWidget extends ComplexPanel
    {
        private Element _headerDiv;
        private Element _bodyDiv;

        TableWidget(FlexTable header, FlexTable body)
        {
            Element mainDiv = DOM.createDiv();
            DOM.setElementAttribute(mainDiv, "class", "gbk-TableView");
            setElement(mainDiv);

            // Header
            _headerDiv = DOM.createDiv();
            DOM.setElementAttribute(_headerDiv, "class", "headerDiv");

            // Body
            _bodyDiv = DOM.createDiv();
            DOM.setElementAttribute(_bodyDiv, "class", "bodyDiv");

            if (isScrollable())
                _bodyDiv.getStyle().setProperty("overflow", "auto");
            else
                _bodyDiv.getStyle().setProperty("overflow", "visible");

            _bodyDiv.getStyle().setProperty("overflowX", "hidden");

            init(header, body);
        }

        void reset(FlexTable header, FlexTable body)
        {
            clear();
            init(header, body);
        }

        private void init(FlexTable header, FlexTable body)
        {
            // Adopt
            ownTable(header, _headerDiv);
            ownTable(body, _bodyDiv);

            adjustWidth();
        }

        private void ownTable(FlexTable table, Element parent)
        {
            // Adopt
            adoptTable(table, parent);

            // Personalize
            table.getElement().getStyle().setProperty("tableLayout", "fixed");
        }

        private void adoptTable(FlexTable table, Element parent)
        {
            getChildren().add(table);

            parent.appendChild(table.getElement());

            getElement().appendChild(parent);

            adopt(table);
        }

        @Override
        protected void onLoad()
        {
            super.onLoad();
            adjustWidth();
            adjustHeight();
        }

        @Override
        public void setWidth(String widthStr)
        {
            super.setWidth(widthStr);
            adjustWidth();
        }

        private void adjustWidth()
        {
            if (!isAttached())
                return;

            FlexTable header = (FlexTable) getWidget(0);
            FlexTable body = (FlexTable) getWidget(1);

            if (isScrollable())
            {
                int width = getOffsetWidth();

                header.setWidth(width - 17 + "px");
                body.setWidth(width - 17 + "px");
            }
            else
            {
                header.setWidth("100%");
                body.setWidth("100%");
            }
        }

        @Override
        public void setHeight(String height)
        {
            super.setHeight(height);
            adjustHeight();
        }

        private void adjustHeight()
        {
            if (!isAttached() || !isScrollable())
                return;

            int height = getOffsetHeight();

            if (height > 0)
                DOM.setStyleAttribute(_bodyDiv, "height", (height - _headerDiv.getOffsetHeight()) + "px");
        }
    }

    private class TableCellEditor extends DecoratedPopupPanel
    {
        private int _row, _col;

        private TableCellEditor(Widget widget)
        {
            super(true, true);
            addStyleDependentName("gbk-TableCellEditor");

            FlexTableBuilder b = new FlexTableBuilder().formLayout();
            setWidget(b.getTable());

            b.set(widget);

            Image acceptBtn = WidgetFactory.newIconButton("gwtBlocks/images/checkGreen.gif", "Accept Changes",
                new ClickListener()
                {
                    public void onClick(Widget sender)
                    {
                        hide();
                        _bodyBuilder.getTable().setHTML(_row, _col, getModel().onAcceptEdit());
                    }
                });
            b.set(acceptBtn);

            Image cancelBtn = WidgetFactory.newIconButton("gwtBlocks/images/xRed.gif", "Cancel Changes",
                new ClickListener()
                {
                    public void onClick(Widget sender)
                    {
                        hide();
                        getModel().onCancelEdit();
                    }
                });
            b.set(cancelBtn);
        }

        private void open(int row, int cell)
        {
            _row = row;
            _col = cell;

            Element cellElem = _bodyBuilder.getTable().getCellFormatter().getElement(row, cell);

            int top = DOM.getAbsoluteTop(cellElem);
            int left = DOM.getAbsoluteLeft(cellElem);

            setPopupPosition(left, top);

            show();
        }
    }

    private int                           _features;
    private FlexTableBuilder              _bodyBuilder;
    private Map<Integer, TableListener>   _cellListeners;
    private Map<Integer, TableCellEditor> _cellEditors;

    public TableView(TableModel<V> model)
    {
        super(model);
    }

    public TableView(TableModel<V> model, int features)
    {
        super(model, features);
    }

    @Override
    protected final TableWidget buildView(TableModel<V> model, Object... args)
    {
        if (args.length > 0)
            _features = (Integer) args[0];

        _bodyBuilder = new FlexTableBuilder();

        return new TableWidget(newHeaderTable(), newBodyTable());
    }

    private FlexTable newHeaderTable()
    {
        FlexTable header = _bodyBuilder.newTable().getTable();

        initHeaderTable(_bodyBuilder);
        buildHeader(_bodyBuilder);

        return header;
    }

    private FlexTable newBodyTable()
    {
        FlexTable body = _bodyBuilder.newTable().getTable();

        initBodyTable(_bodyBuilder);

        body.addTableListener(new TableListener()
        {
            public void onCellClicked(SourcesTableEvents sender, int row, int cell)
            {
                TableListener listener = _cellListeners == null ? null : _cellListeners.get(cell);

                if (listener == null)
                    editCell(row, cell);
                else
                    listener.onCellClicked(sender, row, cell);
            }
        });

        return body;
    }

    private void editCell(int rowNum, int colNum)
    {
        if (_cellEditors == null)
            return;

        TableCellEditor editor = _cellEditors.get(colNum);

        if (editor == null)
            return;

        editor.open(rowNum, colNum);

        getModel().onEditCell(rowNum, colNum);
    }

    public void setCellListener(int colNum, TableListener listener)
    {
        if (_cellListeners == null)
            _cellListeners = new HashMap<Integer, TableListener>();

        _cellListeners.put(colNum, listener);
    }

    public void setCellEditor(int colNum, Widget widget)
    {
        if (_cellEditors == null)
            _cellEditors = new HashMap<Integer, TableCellEditor>();

        _cellEditors.put(colNum, new TableCellEditor(widget));
    }

    @Override
    public void valueChanged(TableModel<V> model)
    {
        super.valueChanged(model);

        refresh();
    }

    private void refresh()
    {
        _bodyBuilder.reset(0);

        List<V> rows = getModel().getValue();

        if (rows == null)
            return;

        for (V row : rows)
        {
            buildBodyRow(_bodyBuilder, row);

            _bodyBuilder.nextRow();
        }
    }

    @Override
    public void setSize(String width, String height)
    {
        getWidget().setSize(width, height);
    }

    @Override
    public void setWidth(String width)
    {
        getWidget().setWidth(width);
    }

    @Override
    public void setHeight(String height)
    {
        getWidget().setHeight(height);
    }

    public boolean isScrollable()
    {
        return (_features & SCROLLABLE) == SCROLLABLE;
    }

    public boolean isPageable()
    {
        return (_features & PAGEABLE) == PAGEABLE;
    }

    protected abstract void initHeaderTable(FlexTableBuilder builder);

    protected abstract void buildHeader(FlexTableBuilder builder);

    protected abstract void initBodyTable(FlexTableBuilder builder);

    protected abstract void buildBodyRow(FlexTableBuilder builder, V row);
}
