package jeeBlocks.server.web;

import jBlocks.server.AppContext;
import jBlocks.server.sql.DataManager;
import jBlocks.server.sql.SqlSession;

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

        // This will register the DataManager in the context
        new DataManager(_webContext, jdbcJndiName, dataSourceId);

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

            // End context SQL session, if exists
            SqlSession sqlSession = _webContext.getFromThread(SqlSession.class);

            if (sqlSession != null)
                sqlSession.close();

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
