package jBlocks.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author hkrishna
 */
public class DepGraph<N extends DepNode<N>>
{
    private List<N> _sortedGraph;

    public DepGraph(Map<?, N> graph)
    {
        sortByDeps(graph);
    }

    public void init()
    {
        for (N node : _sortedGraph)
            node.init();
    }

    private void sortByDeps(Map<?, N> graph)
    {
        List<List<N>> sortedGraph = new ArrayList<List<N>>();

        for (N node : graph.values())
        {
            int hops = maxHopsToRoot(node);

            while (hops >= sortedGraph.size())
                sortedGraph.add(new ArrayList<N>());

            List<N> hopList = sortedGraph.get(hops);

            hopList.add(node);
        }

        _sortedGraph = new ArrayList<N>();

        for (List<N> nodes : sortedGraph)
        {
            for (N node : nodes)
                _sortedGraph.add(node);
        }
    }

    private int maxHopsToRoot(N node)
    {
        if (node.getDeps() == null || node.getDeps().size() == 0)
            return 0;

        if (node.getMaxHopsToRoot() > 0)
            return node.getMaxHopsToRoot();

        node.setMaxHopsToRoot(1);

        for (N dep : node.getDeps())
        {
            int depMaxHops = maxHopsToRoot(dep) + 1;

            if (node.getMaxHopsToRoot() < depMaxHops)
                node.setMaxHopsToRoot(depMaxHops);
        }

        return node.getMaxHopsToRoot();
    }

    public void execute(int threads)
    {
        if (threads < 0)
            threads = 1;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try
        {
            List<N> nodes = new ArrayList<N>(_sortedGraph);

            do
            {
                for (Iterator<N> itr = nodes.iterator(); itr.hasNext();)
                {
                    N node = itr.next();

                    if (node.ready(0))
                    {
                        itr.remove();
                        executor.execute(node);
                    }
                }
            }
            while (nodes.size() > 0 && (nodes.get(0).ready(3000) || true));
        }
        finally
        {
            executor.shutdown();

            try
            {
                while (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS))
                    ;
            }
            catch (InterruptedException e)
            {
                //
            }
        }
    }
}
