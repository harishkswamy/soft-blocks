package gwtBlocks.client.views;

import gwtBlocks.client.models.InputModel;
import jBlocks.shared.Lookup;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public class RadioGroupView<M extends InputModel<V>, V extends Lookup> extends BaseView<FlexTable, M>
{
    private List<V>           _items;
    private List<RadioButton> _buttons;

    protected RadioGroupView(M model, List<V> items, RadioGroupLayout layout)
    {
        super(model, items, layout);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected FlexTable buildView(M model, Object... args)
    {
        _items = (List<V>) args[0];
        RadioGroupLayout layout = (RadioGroupLayout) args[1];

        buildButtons(model);

        return layoutButtons(layout);
    }

    private void buildButtons(M model)
    {
        _buttons = new ArrayList<RadioButton>();

        String group = model.toString();

        for (final V item : _items)
        {
            final RadioButton rb = new RadioButton(group, item.getLookupName());

            rb.addClickListener(new ClickListener()
            {
                public void onClick(Widget sender)
                {
                    if (rb.isChecked())
                        getModel().setValue(item);
                }
            });

            _buttons.add(rb);
        }
    }

    private FlexTable layoutButtons(RadioGroupLayout layout)
    {
        FlexTableBuilder b = new FlexTableBuilder().formLayout();

        if (RadioGroupLayout.HORIZONTAL.equals(layout))
        {
            for (RadioButton rb : _buttons)
                b.set(rb);
        }
        else if (RadioGroupLayout.VERTICAL.equals(layout))
        {
            for (RadioButton rb : _buttons)
                b.set(rb).nextRow();
        }
        else if (RadioGroupLayout.FLOW.equals(layout))
        {
            FlowPanel fp = new FlowPanel();

            for (RadioButton rb : _buttons)
                fp.add(rb);

            b.set(fp);
        }

        return b.getTable();
    }

    public void valueChanged(M model)
    {
        int idx = _items.indexOf(model.getValue());

        if (idx > -1)
            _buttons.get(idx).setChecked(true);
        else
        {
            for (RadioButton rb : _buttons)
                rb.setChecked(false);
        }
    }
}
