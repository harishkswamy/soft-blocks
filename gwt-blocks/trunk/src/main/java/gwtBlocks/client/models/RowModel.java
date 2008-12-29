package gwtBlocks.client.models;

import java.util.List;

/**
 * @author hkrishna
 */
public class RowModel<V> extends CompositeModel<V>
{
    public RowModel(String name, CompositeModel<List<RowModel<V>>> parent, V value)
    {
        setParent(name, parent);

        setValue(value);
    }
}
