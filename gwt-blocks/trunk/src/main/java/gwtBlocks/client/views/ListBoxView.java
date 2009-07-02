package gwtBlocks.client.views;

import gwtBlocks.shared.models.InputModel;
import gwtBlocks.shared.models.ValueChangeListener;
import jBlocks.shared.Lookup;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public class ListBoxView<M extends InputModel<List<V>>, L extends InputModel<List<V>>, V extends Lookup> extends
    BaseView<ListBox, M>
{
    private List<V> _items;
    
    public ListBoxView(M selectedModel)
    {
        super(selectedModel);
        setStylePrimaryName("gbk-ListBoxView");
    }

    public ListBoxView(M selectedModel, L listModel)
    {
        super(selectedModel, listModel);
        setStylePrimaryName("gbk-ListBoxView");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ListBox buildView(M model, Object... args)
    {
        final ListBox listBox = new ListBox();
        listBox.setMultipleSelect(true);

        listBox.addChangeListener(new ChangeListener()
        {
            public void onChange(Widget sender)
            {
                getModel().setValue(buildSelectedList());
            }
        });

        if (args.length > 0)
        {
            L listModel = (L) args[0];

            listModel.registerChangeListener(new ValueChangeListener<InputModel<List<V>>>()
            {
                public void valueChanged(InputModel<List<V>> model)
                {
                    setItems(model.getValue());
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

    protected List<V> buildSelectedList()
    {
        ListBox listBox = getWidget();

        List<V> selItems = new ArrayList<V>();

        for (int i = 0; i < listBox.getItemCount(); i++)
        {
            if (listBox.isItemSelected(i))
                selItems.add(_items.get(i));
        }

        return selItems;
    }

    @Override
    public void valueChanged(M model)
    {
        refreshSelection(model.getValue());
    }

    private void refreshSelection(List<V> selItems)
    {
        ListBox listBox = getWidget();

        for (int i = 0; i < _items.size(); i++)
        {
            if (selItems.contains(_items.get(i)))
                listBox.setItemSelected(i, true);
            else
                listBox.setItemSelected(i, false);
        }
    }
}
