package jBlocks.shared.domain;

/**
 * @author hkrishna
 */
@SuppressWarnings("serial")
public class LookupDomainObject extends BaseDomainObject
{
    private String _name;

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    @Override
    public String toString()
    {
        return super.toString() + ", Name: " + getName();
    }
}
