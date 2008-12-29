package jBlocks.shared.domain;

import java.io.Serializable;

/**
 * @author hkrishna
 */
@SuppressWarnings("serial")
public class BaseDomainObject implements Serializable
{
    private Integer _id;
    private Integer _recVersion;

    public void setId(Integer id)
    {
        _id = id;
    }

    public Integer getId()
    {
        return _id;
    }

    public void setRecVersion(Integer version)
    {
        _recVersion = version;
    }

    public Integer getRecVersion()
    {
        return _recVersion;
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;

        if (obj == null || !(getClass().equals(obj.getClass())))
            return false;

        BaseDomainObject domainObj = (BaseDomainObject) obj;

        return _id == null ? (domainObj.getId() == null) : _id.equals(domainObj.getId());
    }

    public int hashCode()
    {
        return 629 + (_id == null ? 0 : _id.intValue());
    }

    @Override
    public String toString()
    {
        return "Id: " + _id;
    }
}
