package jeeBlocks.server.web;

import jBlocks.server.AppContext;
import jBlocks.server.Utils;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author hkrishna
 */
public class WebContext extends AppContext
{
    public static final String  LOCALE      = "locale";

    private static final String SESSION_KEY = "$ses$key$";

    public void setSession(WebSession appSession)
    {
        HttpServletRequest request = getFromThread(HttpServletRequest.class);
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_KEY, appSession);
    }

    /**
     * @return The user session object if one has been established, else returns null.
     */
    @SuppressWarnings("unchecked")
    public <T extends WebSession> T getSession()
    {
        HttpServletRequest request = getFromThread(HttpServletRequest.class);
        HttpSession session = request.getSession(false);
        return (T) (session == null ? null : session.getAttribute(SESSION_KEY));
    }

    void applySession()
    {
        HttpSession session = getHttpSession();

        if (session != null)
            session.setAttribute(SESSION_KEY, session.getAttribute(SESSION_KEY));
    }

    public void logout()
    {
        HttpSession session = getHttpSession();

        if (session != null)
            session.invalidate();
    }

    private HttpSession getHttpSession()
    {
        HttpServletRequest request = getFromThread(HttpServletRequest.class);

        if (request == null)
            return null;

        return request.getSession(false);
    }

    /**
     * Searches the user request and session objects to determine the locale. The search for the locale happens in the
     * following order and returns the first found locale.
     * <p>
     * <ol>
     * <li>{@link ServletRequest#getAttribute(String) ServletRequest.getAttribute(WebContext#LOCALE)}
     * <li>{@link ServletRequest#getParameter(String) ServletRequest.getParameter(WebContext#LOCALE)}
     * <li>{@link WebSession#getLocale()}
     * <li>{@link HttpServletRequest#getHeader(String) HttpServletRequest#getHeader("accept-language")}
     * </ol>
     * 
     * @return The {@link Locale} object if found, else null.
     */
    public Locale getLocale()
    {
        HttpServletRequest request = getFromThread(HttpServletRequest.class);

        Locale locale = (Locale) request.getAttribute(LOCALE);

        if (locale != null)
            return locale;

        String localeStr = request.getParameter(LOCALE);

        if (localeStr != null)
            return Utils.getLocale(localeStr);

        WebSession session = getSession();

        if (session != null)
        {
            locale = session.getLocale();

            if (locale != null)
                return locale;
        }

        localeStr = request.getHeader("accept-language");

        if (localeStr == null || localeStr.length() < 2)
            return null;

        if (localeStr.length() > 5)
            return Utils.getLocale(localeStr.substring(0, 2) + "_" + localeStr.substring(3, 5));
        else
            return Utils.getLocale(localeStr.substring(0, 2));
    }
}
