// Copyright 2007 Harish Krishnaswamy
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package jBlocks.server;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author hkrishna
 */
public class AggregateException extends RuntimeException
{
    private static final long serialVersionUID = 3761409720300681522L;

    /**
     * Wraps and returns the provided exception in AggregateException.
     */
    public static AggregateException with(Throwable t)
    {
        return with(t, null);
    }

    /**
     * Convenience method for {@link #with(Throwable, String, Object...)}.
     */
    public static AggregateException with(Throwable t, String message)
    {
        if (t instanceof AggregateException)
        {
            AggregateException ae = (AggregateException) t;

            if (message != null)
                ae.setMessage(message + "\nCause: " + ae.getLocalizedMessage());

            return ae;
        }

        StringBuffer msgBuffer = new StringBuffer();
        t = getRootCause(t, msgBuffer);
        String msgs = msgBuffer.toString();
        msgs = (message == null) ? msgs : (message + "\nCause: " + msgs);

        if (t instanceof AggregateException)
        {
            AggregateException ae = (AggregateException) t;
            ae.setMessage(msgs);
            return ae;
        }
        else
            return new AggregateException(t, msgs);
    }

    private static Throwable getRootCause(Throwable t, StringBuffer msgbuffer)
    {
        msgbuffer.append(t.getClass().getName() + ": " + t.getLocalizedMessage());

        while (t.getCause() != null)
        {
            t = t.getCause();
            msgbuffer.append("\nCause: " + t.getClass().getName() + ": " + t.getLocalizedMessage());
        }

        return t;
    }

    // ~ Instance Variables
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private String    _message;
    private Throwable _rootCause;

    // ~ Constructors
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private AggregateException(Throwable t)
    {
        setCause(t);
    }

    private AggregateException(Throwable t, String msg)
    {
        setCause(t);
        _message = msg;
    }

    private void setCause(Throwable t)
    {
        _rootCause = t;
        setStackTrace(t.getStackTrace());
    }

    void setMessage(String msg)
    {
        _message = msg;
    }

    // ~ Inherited Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Returns the message for this and all wrapped exceptions.
     */
    @Override
    public String getMessage()
    {
        return _message == null ? _rootCause.getMessage() : _message;
    }

    @Override
    public String toString()
    {
        return getClass().getName() + ", Root Cause: " + _rootCause.toString();
    }

    @Override
    public void printStackTrace()
    {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream out)
    {
        synchronized (out)
        {
            if (_message != null)
                out.println(getLocalizedMessage());

            _rootCause.printStackTrace(out);
        }
    }

    @Override
    public void printStackTrace(PrintWriter out)
    {
        synchronized (out)
        {
            if (_message != null)
                out.println(getLocalizedMessage());

            _rootCause.printStackTrace(out);
        }
    }

    @Override
    public StackTraceElement[] getStackTrace()
    {
        return _rootCause.getStackTrace();
    }

    // ~ Public Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public Throwable getRootCause()
    {
        return _rootCause;
    }
}
