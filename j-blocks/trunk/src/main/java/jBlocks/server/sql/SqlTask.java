package jBlocks.server.sql;

/**
 * @author hkrishna
 */
public interface SqlTask<V>
{
    V execute();
}
