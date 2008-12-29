package jeeBlocks.server.web;

import jBlocks.server.AppContext;
import jBlocks.server.sql.DataManager;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author hkrishna
 */
public class EnvFilter implements Filter
{
    private WebContext _webContext = new WebContext();

    public void init(FilterConfig filterConfig) throws ServletException
    {
        String envId = filterConfig.getInitParameter("env-id");

        _webContext.setEnvId(envId);

        String dataSourceId = filterConfig.getInitParameter("data-source-id");
        String jdbcJndiName = filterConfig.getInitParameter("data-source-jndi-name");

        DataManager dataManager = new DataManager(dataSourceId, jdbcJndiName);
        dataManager.setAppCtx(_webContext);

        _webContext.put(DataManager.class, dataManager);

        filterConfig.getServletContext().setAttribute(AppContext.KEY, _webContext);
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
        ServletException
    {
        try
        {
            _webContext.putInThread(HttpServletRequest.class, req);

            chain.doFilter(req, resp);
        }
        finally
        {
            // Update the session to replicate it in a cluster.
            _webContext.applySession();

            // Cleaup the thread.
            _webContext.cleanupThread();
        }
    }

    public void destroy()
    {
        _webContext.cleanup();
        _webContext = null;
    }
}
