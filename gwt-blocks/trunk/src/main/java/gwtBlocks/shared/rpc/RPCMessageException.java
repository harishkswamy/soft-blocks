package gwtBlocks.shared.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author hkrishna
 */
public class RPCMessageException extends RuntimeException implements IsSerializable
{
    private static final long serialVersionUID = 3776387283205215732L;

    // Need this constructor for GWT serialization, DO NOT REMOVE
    public RPCMessageException()
    {
    }
    
    public RPCMessageException(String message)
    {
        super(message);
    }
}
