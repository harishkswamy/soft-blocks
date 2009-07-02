package gwtBlocks.shared.models;

import java.util.List;

/**
 * @author hkrishna
 */
public class TableModel<V> extends CompositeModel<List<V>>
{
    public TableModel()
    {
    }

    public TableModel(String name, CompositeModel<?> parent)
    {
        setParent(name, parent);
    }

    public void onEditCell(int row, int cell)
    {
        // 
    }

    public String onAcceptEdit()
    {
        return "";
    }

    public void onCancelEdit()
    {
        //
    }
}
