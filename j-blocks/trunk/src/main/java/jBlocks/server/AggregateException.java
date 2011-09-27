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
    private static final long serialVersionUID = 1L;

    public static AggregateException with(Throwable e)
    {
        if (e instanceof AggregateException)
            return (AggregateException) e;

        return new AggregateException(null, e);
    }

    public static AggregateException with(Throwable e, String msg)
    {
        if (msg == null)
            return with(e);

        return new AggregateException(msg, e);
    }

    private AggregateException(String message, Throwable cause)
    {
        super(message, cause);
    }

    @Override
    public void printStackTrace()
    {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream err)
    {
        err.println(toString());

        Throwable t = this, root = this;

        while ((t = t.getCause()) != null)
        {
            err.print("Caused by: ");

            if (t.getCause() == null)
                root = t;
            else
                err.println(t);
        }

        root.printStackTrace(err);
    }

    @Override
    public void printStackTrace(PrintWriter err)
    {
        err.println(toString());

        Throwable t = this, root = this;

        while ((t = t.getCause()) != null)
        {
            err.print("Caused by: ");

            if (t.getCause() == null)
                root = t;
            else
                err.println(t);
        }

        root.printStackTrace(err);
    }

    @Override
    public StackTraceElement[] getStackTrace()
    {
        Throwable t = this, root = this;

        while ((t = t.getCause()) != null)
        {
            if (t.getCause() == null)
                root = t;
        }

        return root.getStackTrace();
    }
}
