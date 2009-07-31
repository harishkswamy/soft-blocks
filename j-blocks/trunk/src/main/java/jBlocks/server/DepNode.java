package jBlocks.server;

import java.util.List;

/**
 * @author hkrishna
 */
public interface DepNode<T extends DepNode<?>> extends Runnable
{
    void init();

    List<T> getDeps();

    int getMaxHopsToRoot();

    void setMaxHopsToRoot(int hops);

    boolean ready(int awaitMs);
}
