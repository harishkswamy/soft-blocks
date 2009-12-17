// Copyright 2008 Harish Krishnaswamy
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package gwtBlocks.client;

import gwtBlocks.client.views.MessageBox;
import gwtBlocks.shared.rpc.RPCMessageException;
import gwtBlocks.shared.rpc.RPCServiceException;

import java.util.Map;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;

/**
 * @author hkrishna
 */
public abstract class FeedbackAsyncCallback<T> implements AsyncCallback<T>
{
    private static ProcessingIndicator  _indicator;
    private static int                  _callCount, _sessionTimerDelay;
    private static ConstantsWithLookup  _rpcMessages;
    private static Map<String, Command> _rpcErrorCommands;
    private static Timer                _sessionTimer;

    public static void setProcessingIndicator(ProcessingIndicator indicator)
    {
        _indicator = indicator;
    }

    public static void setRPCMessages(ConstantsWithLookup rpcMessages)
    {
        _rpcMessages = rpcMessages;
    }

    public static void setRPCErrorCommands(Map<String, Command> commands)
    {
        _rpcErrorCommands = commands;
    }

    public static void startSessionTimer(Timer timer, int delay)
    {
        _sessionTimer = timer;
        _sessionTimerDelay = delay;

        _sessionTimer.schedule(delay);
    }
    
    private Barrier _barrier;

    public FeedbackAsyncCallback()
    {
        beginCall();
    }

    public FeedbackAsyncCallback(Barrier barrier)
    {
        this();

        _barrier = barrier;
    }

    private void beginCall()
    {
        synchronized (this)
        {
            _callCount++;
        }
        _indicator.setVisible(true);
        
        if (_sessionTimer != null)
            _sessionTimer.schedule(_sessionTimerDelay);
    }

    private void endCall()
    {
        synchronized (this)
        {
            _callCount--;
        }
        if (_callCount == 0)
            _indicator.setVisible(false);
    }

    public void onFailure(Throwable caught)
    {
        endCall();

        try
        {
            throw caught;
        }
        catch (RPCServiceException e)
        {
            handleServiceException(e);
        }
        catch (RPCMessageException e)
        {
            MessageBox.alert(getMessage(e.getLocalizedMessage()), getErrorCommand(e.getLocalizedMessage()));
        }
        catch (IncompatibleRemoteServiceException e)
        {
            MessageBox.error(GwtBlocksMessages.pick.incompatibleRemoteServiceException(),
                getErrorCommand("incompatibleRemoteServiceException"));
        }
        catch (Throwable t)
        {
            MessageBox.error(GwtBlocksMessages.pick.unhandledError(), getErrorCommand("unhandledError"));
        }

        if (_barrier != null)
            _barrier.failed();
    }

    public void onSuccess(T result)
    {
        try
        {
            handleResult(result);

            if (_barrier != null)
                _barrier.arrive();
        }
        finally
        {
            endCall();
        }
    }

    protected abstract void handleResult(T result);

    protected void handleServiceException(RPCServiceException caught)
    {
        // 
    }

    protected String getMessage(String msgCode)
    {
        try
        {
            if (_rpcMessages != null)
                return _rpcMessages.getString(msgCode);
        }
        catch (Exception e)
        {
            // Fall through
        }

        return msgCode;
    }

    private Command getErrorCommand(String msgCode)
    {
        if (_rpcErrorCommands == null)
            return null;

        return _rpcErrorCommands.get(msgCode);
    }
}
