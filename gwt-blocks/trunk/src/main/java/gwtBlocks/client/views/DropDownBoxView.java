package gwtBlocks.client.views;

import gwtBlocks.shared.models.InputModel;
import gwtBlocks.shared.models.ValueChangeListener;
import jBlocks.shared.Lookup;

import java.util.List;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public class DropDownBoxView<M extends InputModel<V>, L extends InputModel<List<V>>, V extends Lookup> extends
    BaseView<ListBox, M>
{
    private List<V> _items;

    public DropDownBoxView(M selectedModel)
    {
        super(selectedModel);
        setStylePrimaryName("gbk-ListBoxView");
    }

    public DropDownBoxView(M selectedModel, L listModel)
    {
        super(selectedModel, listModel);
        setStylePrimaryName("gbk-ListBoxView");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ListBox buildView(final M model, Object... args)
    {
        final ListBox listBox = new ListBox();
        listBox.setMultipleSelect(false);

        listBox.addChangeListener(new ChangeListener()
        {
            public void onChange(Widget sender)
            {
                getModel().setValue(_items.get(listBox.getSelectedIndex()));
            }
        });

        if (args.length > 0)
        {
            L listModel = (L) args[0];

            listModel.registerChangeListener(new ValueChangeListener<InputModel<List<V>>>()
            {
                public void valueChanged(InputModel<List<V>> listModel)
                {
                    setItems(listModel.getValue());
                }
            });

            setItems(listModel.getValue());
        }

        return listBox;
    }

    public void setItems(List<V> items)
    {
        _items = items;

        ListBox listBox = getWidget();
        listBox.clear();

        if (items == null)
            return;

        for (Lookup item : items)
            listBox.addItem(item.getLookupName(), item.getLookupValue());

        valueChanged(getModel());
    }

    @Override
    public void valueChanged(M model)
    {
        getWidget().setSelectedIndex(findIndex(model.getValue()));
    }

    private int findIndex(V item)
    {
        if (_items == null)
            return -1;
        
        for (int i = 0; i < _items.size(); i++)
            if (_items.get(i).equals(item))
                return i;

        return -1;
    }
}
