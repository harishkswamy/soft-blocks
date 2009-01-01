package jBlocks.shared.domain;

import jBlocks.shared.Lookup;

/**
 * @author hkrishna
 */
public class LookupImpl extends LookupDomainObject implements Lookup
{
    private static final long serialVersionUID = -5480257209602473255L;

    public LookupImpl()
    {

    }

    public LookupImpl(String lookupName, Integer lookupValue)
    {
        setName(lookupName);
        setId(lookupValue);
    }

    public String getLookupName()
    {
        return getName();
    }

    public String getLookupValue()
    {
        return getId() == null ? "null" : getId().toString();
    }
}
