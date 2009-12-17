package jeeBlocks.server.web;

import static jBlocks.server.Messages.*;

import jBlocks.server.AggregateException;
import jBlocks.server.AppContext;
import jBlocks.server.FeedbackException;
import jBlocks.server.IOUtils;
import jBlocks.server.Messages;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A template servlet that abstracts away common functionality. This servlet supports only GET and POST methods.
 * 
 * @author hkrishna
 */
@SuppressWarnings("serial")
public abstract class HttpWebServlet extends HttpServlet
{
    private static String       FORBIDDEN_ICON_NAME = "forbidden";
    private static String       WARNING_ICON_NAME   = "warning";
    private static String       ERROR_ICON_NAME     = "error";

    private static final Logger _logger             = LoggerFactory.getLogger(HttpWebServlet.class);

    /**
     * @return The {@link WebContext} for this application.
     */
    protected WebContext ctx()
    {
        return (WebContext) getServletContext().getAttribute(AppContext.KEY);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doServiceLocal(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doServiceLocal(req, resp);
    }

    private void doServiceLocal(HttpServletRequest req, HttpServletResponse resp)
    {
        try
        {
            doService(req, resp);
        }
        catch (SecurityException e)
        {
            _logger.error("", AggregateException.with(e, "Unable to service HTTP request due to security error."));
            writeResponse(resp, getExceptionHTML(req, forbiddenTitle, FORBIDDEN_ICON_NAME, forbiddenMessage));
        }
        catch (FeedbackException e)
        {
            _logger.error("", AggregateException.with(e, "Unable to service HTTP request due to application error."));
            writeResponse(resp, getExceptionHTML(req, alertTitle, WARNING_ICON_NAME, e.getLocalizedMessage()));
        }
        catch (Throwable e)
        {
            _logger.error("", AggregateException.with(e, "Unable to service HTTP request due to unknown error."));
            writeResponse(resp, getExceptionHTML(req, errorTitle, ERROR_ICON_NAME, errorMessage));
        }
    }

    private String getExceptionHTML(HttpServletRequest req, String title, String icon, String message)
    {
        String template = getContents(HttpWebServlet.class.getResource("Exception.html")).toString();

        Locale locale = ctx().getLocale();

        Messages formatter = formatter();

        template = template.replaceAll("#TITLE#", formatter.format(locale, title));
        template = template.replaceAll("#ICON#", icon);
        template = template.replaceFirst("#MESSAGE#", formatter.format(locale, message));

        return template;
    }

    protected StringBuffer getContents(URL url)
    {
        final StringBuffer buf = new StringBuffer();

        IOUtils.readCharURL(url, new IOUtils.LineHandler()
        {
            public void handleLine(String line) throws Exception
            {
                buf.append(line.trim());
            }

            public void endOfFile()
            {
            }
        });

        return buf;
    }

    protected void writeResponse(HttpServletResponse resp, String response)
    {
        try
        {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write(response);
        }
        catch (Throwable e)
        {
            getServletContext().log("Unable to write HTTP response.\nCause: ", e);
            throw new RuntimeException(e);
        }
    }

    protected abstract void doService(HttpServletRequest req, HttpServletResponse resp) throws Throwable;
}
