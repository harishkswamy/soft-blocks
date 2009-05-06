package gwtBlocks.server;

import gwtBlocks.shared.rpc.RPCMessageException;
import jBlocks.server.AggregateException;
import jBlocks.server.AppContext;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jeeBlocks.server.web.WebContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author hkrishna
 */
public abstract class GWTWebServlet extends RemoteServiceServlet
{
    private static final long   serialVersionUID = 1L;
    private static final Logger _logger          = LoggerFactory.getLogger(GWTWebServlet.class);

    /**
     * @return The {@link WebContext} for this application.
     */
    protected WebContext ctx()
    {
        return (WebContext) getServletContext().getAttribute(AppContext.KEY);
    }

    @Override
    protected void doUnexpectedFailure(Throwable e)
    {
        if (e instanceof RPCMessageException)
        {
            try
            {
                writeResponse(getThreadLocalRequest(), getThreadLocalResponse(), RPC.encodeResponseForFailure(null, e));
                return;
            }
            catch (Throwable se)
            {
                e = AggregateException.with(se, "Unable to write exception response.");

                // Fall through
            }
        }
        else
            e = AggregateException.with(e, "Unable to process GWT request due to an unexpected error.");

        _logger.error("Unexpected Error.", e);

        super.doUnexpectedFailure(e);
    }

    private void writeResponse(HttpServletRequest request, HttpServletResponse response, String responsePayload)
        throws IOException
    {
        boolean gzipEncode = RPCServletUtils.acceptsGzipEncoding(request)
            && shouldCompressResponse(request, response, responsePayload);

        RPCServletUtils.writeResponse(getServletContext(), response, responsePayload, gzipEncode);
    }
}
