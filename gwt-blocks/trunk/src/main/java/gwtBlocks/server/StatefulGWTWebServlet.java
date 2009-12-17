package gwtBlocks.server;

import gwtBlocks.shared.rpc.RPCMessageException;

/**
 * @author hkrishna
 */
public abstract class StatefulGWTWebServlet extends GWTWebServlet
{
    @Override
    protected void onBeforeRequestDeserialized(String serializedRequest)
    {
        if (!getThreadLocalRequest().isRequestedSessionIdValid())
            throw new RPCMessageException("sessionExpired");
    }

    public void logout()
    {
        ctx().logout();
    }
}
