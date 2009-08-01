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

import static com.google.gwt.user.client.ui.HasHorizontalAlignment.*;
import static com.google.gwt.user.client.ui.HasVerticalAlignment.*;

import jBlocks.shared.SharedUtils;

import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * Helps build GWT HTMLTables. The builder is reusable, ie. it can be used to build multiple tables, although it can
 * only build one table at a time. {@link #getTable()} returns the actual table currently being built. The
 * <code>newXXXTable</code> methods reset the builder and start building a new table.
 * <p>
 * The builder builds the table sequentially starting from top left working its way to bottom right. The builder
 * maintains an internal pointer that points to the cell that's currently being built. Requests sent to the builder are
 * fulfilled against the cell currently being pointed to by the internal pointer. Setting the contents of the cell via
 * one of the <code>set</code> methods automatically moves the pointer to the next cell and hence it must be the last
 * operation on the cell. The pointer can be moved to the next row by calling {@link #nextRow()}.
 * <p>
 * Methods that are common to more than one HTML element like <code>width</code> and <code>style</code> have a suffix in
 * their names to identify the element they apply to. Methods that have a "<code>T</code>" suffix apply to table, those
 * that have an "<code>R</code>" suffix apply to table rows and "<code>C</code>" applies to table cells.
 * <p>
 * The builder has some switches that when turned on will apply the effect of the switch to all the cells of the table
 * from the current cell forward until the switch is turned off or the end of the table is reached.
 * <ul>
 * <li>The stretch switch stretches the cell widgets to fit the cell size and, can be turned on via {@link #hStretch()},
 * {@link #vStretch()} or {@link #stretch()} and turned off via {@link #dontStretch()}.
 * <li>The row style switch applies "<code>evenRow</code>" and "<code>oddRow</code>" styles to even and odd rows
 * respectively and, can be turned on via {@link #applyRowStyle()} and turned off via {@link #dontApplyRowStyle()}. In
 * order to apply "<code>evenRow</code>" style to odd rows and "<code>oddRow</code>" style to even rows, simply use
 * {@link #swapRowStyle()} instead of {@link #applyRowStyle()}. Swapping the row style twice will, as expected, return
 * the switch to the {@link #applyRowStyle()} behavior.
 * </ul>
 * 
 * @author hkrishna
 */
abstract class HTMLTableBuilder<B extends HTMLTableBuilder<B, T>, T extends HTMLTable>
{
    protected T _table;
    protected int _row, _col;
    protected boolean _applyRowStyle, _swapRowStyle, _hStretch, _vStretch;

    @SuppressWarnings("unchecked")
    private B builder()
    {
        return (B) this;
    }

    public B build(T table)
    {
        _table = table;

        _applyRowStyle = _swapRowStyle = _hStretch = _vStretch = false;

        seek(table.getRowCount(), 0);

        return builder();
    }

    public B formLayout()
    {
        return spacing(3).padding(0);
    }

    // Table methods ============================================================

    public B seek(int row, int col)
    {
        if (row < _table.getRowCount())
        {
            _row = row;

            if (col < _table.getCellCount(_row))
                _col = col;
            else
                _col = _table.getCellCount(_row);
        }
        else
        {
            _row = _table.getRowCount();
            _col = 0;
        }

        return builder();
    }

    /**
     * Sets the table's &lt;caption&gt;.
     */
    public B caption(String caption)
    {
        WidgetFactory.addCaption(_table, caption);

        return builder();
    }

    /**
     * Sets the table's <code>cellspacing</code> attribute.
     */
    public B spacing(int spacing)
    {
        _table.setCellSpacing(spacing);

        return builder();
    }

    /**
     * Sets the table's <code>cellpadding</code> attribute.
     */
    public B padding(int padding)
    {
        _table.setCellPadding(padding);

        return builder();
    }

    /**
     * Sets the table's width.
     */
    public B widthT(String width)
    {
        _table.setWidth(width);

        return builder();
    }

    /**
     * Sets the table's height.
     */
    public B heightT(String height)
    {
        _table.setHeight(height);

        return builder();
    }

    /**
     * Sets the table's width and height.
     */
    public B sizeT(String width, String height)
    {
        _table.setWidth(width);
        _table.setHeight(height);

        return builder();
    }

    /**
     * Sets the table's primary style name.
     */
    public B styleT(String styleName)
    {
        if (!SharedUtils.isBlank(styleName))
            _table.setStylePrimaryName(styleName);

        return builder();
    }

    /**
     * Sets the table's border size.
     */
    public B border(int width)
    {
        _table.setBorderWidth(width);

        return builder();
    }

    /**
     * @return The table currently being built.
     */
    public T getTable()
    {
        return _table;
    }

    // Switch methods ============================================================

    /**
     * Turns the horizontal stretch switch on and implicitly turns the vertical stretch switch off.
     */
    public B hStretch()
    {
        _hStretch = true;
        _vStretch = false;

        return builder();
    }

    /**
     * Turns the vertical stretch switch on and implicitly turns the horizontal stretch switch off.
     */
    public B vStretch()
    {
        _hStretch = false;
        _vStretch = true;

        return builder();
    }

    /**
     * Turns both the horizontal and vertical stretch switches on.
     */
    public B stretch()
    {
        _hStretch = _vStretch = true;

        return builder();
    }

    /**
     * Turns both the horizontal and vertical stretch switches off.
     */
    public B dontStretch()
    {
        _hStretch = _vStretch = false;

        return builder();
    }

    /**
     * Turns the row style switch on.
     */
    public B applyRowStyle()
    {
        _applyRowStyle = true;

        setRowStyle();

        return builder();
    }

    /**
     * Turns row style switch on and swaps the even and odd row style names.
     */
    public B swapRowStyle()
    {
        _swapRowStyle = !_swapRowStyle;

        return applyRowStyle();
    }

    /**
     * Turns the row style switch off.
     */
    public B dontApplyRowStyle()
    {
        _applyRowStyle = false;
        _swapRowStyle = false;

        return builder();
    }

    // Cell methods ==============================================================

    /**
     * Sets the cell's width.
     */
    public B widthC(String width)
    {
        _table.getCellFormatter().setWidth(_row, _col, width);

        return builder();
    }

    /**
     * Sets the cell's height.
     */
    public B heightC(String height)
    {
        _table.getCellFormatter().setHeight(_row, _col, height);

        return builder();
    }

    /**
     * Sets the cell's width and height.
     */
    public B sizeC(String width, String height)
    {
        _table.getCellFormatter().setWidth(_row, _col, width);
        _table.getCellFormatter().setHeight(_row, _col, height);

        return builder();
    }

    /**
     * Allows the cell's contents to wrap.
     */
    public B wrap()
    {
        return wrap(true);
    }

    /**
     * Disallows the cell's contents to wrap.
     */
    public B dontWrap()
    {
        return wrap(false);
    }

    private B wrap(boolean wrap)
    {
        _table.getCellFormatter().setWordWrap(_row, _col, wrap);

        return builder();
    }

    /**
     * Horizontally aligns the cell's contents left justified.
     */
    public B leftC()
    {
        _table.getCellFormatter().setHorizontalAlignment(_row, _col, ALIGN_LEFT);

        return builder();
    }

    /**
     * Horizontally aligns the cell's contents centered.
     */
    public B centerC()
    {
        _table.getCellFormatter().setHorizontalAlignment(_row, _col, ALIGN_CENTER);

        return builder();
    }

    /**
     * Horizontally aligns the cell's contents right justified.
     */
    public B rightC()
    {
        _table.getCellFormatter().setHorizontalAlignment(_row, _col, ALIGN_RIGHT);

        return builder();
    }

    /**
     * Vertically aligns the cell's contents top justified.
     */
    public B topC()
    {
        _table.getCellFormatter().setVerticalAlignment(_row, _col, ALIGN_TOP);

        return builder();
    }

    /**
     * Vertically aligns the cell's contents centered.
     */
    public B middleC()
    {
        _table.getCellFormatter().setVerticalAlignment(_row, _col, ALIGN_MIDDLE);

        return builder();
    }

    /**
     * Vertically aligns the cell's contents bottom justified.
     */
    public B bottomC()
    {
        _table.getCellFormatter().setVerticalAlignment(_row, _col, ALIGN_BOTTOM);

        return builder();
    }

    /**
     * Sets the cell's style name.
     */
    public B styleC(String styleName)
    {
        if (!SharedUtils.isBlank(styleName))
            _table.getCellFormatter().setStyleName(_row, _col, styleName);

        return builder();
    }

    /**
     * Sets the cell's content to the provided text and clips it if it doesn't fit in the cell.
     */
    public B setClipped(Object obj)
    {
        _table.getCellFormatter().setVisible(_row, _col, true);
        _table.getCellFormatter().getElement(_row, _col).getStyle().setProperty("overflow", "hidden");

        return set(obj);
    }

    /**
     * Sets the cell's content to the provided text.
     */
    public B set(Object obj)
    {
        _table.setText(_row, _col++, obj == null ? "" : obj.toString());

        return builder();
    }

    /**
     * Sets the cell's content to the provided widget.
     */
    public B set(Widget widget)
    {
        if (_hStretch)
            widget.setWidth("100%");

        if (_vStretch)
            widget.setHeight("100%");

        _table.setWidget(_row, _col++, widget);

        return builder();
    }

    /**
     * Sets the cell's content to the provided HTML.
     */
    public B setHTML(String html)
    {
        _table.setHTML(_row, _col++, html);

        return builder();
    }

    /**
     * Sets the cell's content to the requested number of non-breaking spaces (&amp;nbsp;).
     */
    public B setNbSp(int spaces)
    {
        StringBuilder b = new StringBuilder("&nbsp;");

        for (int i = 1; i < spaces; i++)
            b.append("&nbsp;");

        _table.setHTML(_row, _col++, b.toString());

        return builder();
    }

    // Row methods ================================================================

    /**
     * Moves the pointer to the next row.
     */
    public B nextRow()
    {
        seek(++_row, 0);

        return setRowStyle();
    }

    private B setRowStyle()
    {
        if (!_applyRowStyle)
            return builder();

        int rowNum = _swapRowStyle ? (_row + 1) % 2 : _row % 2;

        _table.getRowFormatter().setStyleName(_row, rowNum == 0 ? "oddRow" : "evenRow");

        return builder();
    }

    /**
     * Sets the row's primary style name.
     */
    public B styleR(String styleName)
    {
        if (!SharedUtils.isBlank(styleName))
            _table.getRowFormatter().setStylePrimaryName(_row, styleName);

        return builder();
    }

    /**
     * Removes the rows's style name.
     */
    public B removeRowStyle(String styleName)
    {
        if (!SharedUtils.isBlank(styleName))
            _table.getRowFormatter().removeStyleName(_row,
                _table.getRowFormatter().getStylePrimaryName(_row) + '-' + styleName);

        return builder();
    }
}
