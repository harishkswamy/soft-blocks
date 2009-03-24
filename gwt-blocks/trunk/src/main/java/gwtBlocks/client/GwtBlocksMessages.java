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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * @author hkrishna
 */
public interface GwtBlocksMessages extends Messages
{
    GwtBlocksMessages pick = GWT.create(GwtBlocksMessages.class);

    @DefaultMessage("Invalid number: \"{0}\"; format: \"{1}\".")
    String invalidNumberFormat(String value, String format);

    @DefaultMessage("There is no registered message model for: \"{0}\" to show message: \"{1}\".")
    String noRegisteredMessageModel(String forModel, String msg);

    @DefaultMessage("Processing...")
    String processing();

    @DefaultMessage("Message")
    String message();

    @DefaultMessage("Confirm")
    String confirm();

    @DefaultMessage("OK")
    String ok();

    @DefaultMessage("Cancel")
    String cancel();

    @DefaultMessage("A new version of the payslip viewer is available.<br /><br />Please right-click and select \"Refresh\" in your browser to view your payslip.")
    String incompatibleRemoteServiceException();

    @DefaultMessage("An error has occurred on the server. Please report the problem to help desk.")
    String unhandledError();
}
