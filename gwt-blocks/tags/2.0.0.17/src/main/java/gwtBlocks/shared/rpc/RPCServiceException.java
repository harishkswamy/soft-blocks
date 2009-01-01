package gwtBlocks.shared.rpc;

/**
 * @author hkrishna
 */
public class RPCServiceException extends RuntimeException
{
    private static final long serialVersionUID = 152735573796815484L;

    public RPCServiceException()
    {
        super();
    }

    public RPCServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RPCServiceException(String message)
    {
        super(message);
    }

    public RPCServiceException(Throwable cause)
    {
        super(cause);
    }
}
