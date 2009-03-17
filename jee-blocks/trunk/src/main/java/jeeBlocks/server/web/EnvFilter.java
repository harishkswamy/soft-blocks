package jeeBlocks.server.web;

import jBlocks.server.AppContext;
import jBlocks.server.sql.SqlClient;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

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

        _webContext.put(SqlClient.class, new SqlClient(jdbcJndiName, dataSourceId));

        filterConfig.getServletContext().setAttribute(AppContext.KEY, _webContext);
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
        ServletException
    {
        final String remoteAddrKey = "RemoteAddr";

        try
        {
            _webContext.putInThread(HttpServletRequest.class, (HttpServletRequest) req);

            // Store context for logs
            MDC.put(remoteAddrKey, req.getRemoteAddr());

            chain.doFilter(req, resp);
        }
        finally
        {
            // Update the session to replicate it in a cluster.
            _webContext.applySession();

            // End SQL session for the request
            _webContext.get(SqlClient.class).endSession();

            // Cleaup the thread.
            _webContext.cleanupThread();

            // Clear context stored for logs
            MDC.remove(remoteAddrKey);
        }
    }

    public void destroy()
    {
        _webContext.cleanup();
        _webContext = null;
    }
}
