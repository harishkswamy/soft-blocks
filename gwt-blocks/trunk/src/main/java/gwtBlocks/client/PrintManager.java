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

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.UIObject;

/**
 * <pre>
 * Generic printing class can be used to print UIObjects (Widgets) and plain HTML.
 * 
 * Usage:
 *      You must insert this iframe in your host page:
 *              &lt;iframe id=&quot;__printingFrame&quot; style=&quot;width:0;height:0;border:0&quot;&gt;&lt;/iframe&gt;
 * 
 *      Objects/HTML using styles:
 *              PrintManager.print(new String[] {&quot;/paperStyle.css&quot;}, RootPanel.get('myId'));
 * </pre>
 */
public class PrintManager
{
    private static native boolean populatePrintingFrame(String html) /*-{
     var frame = $doc.getElementById('__printingFrame');
     if (!frame) {
     $wnd.alert("Error: Can't find printing frame.");
     return false;
     }
     var doc = frame.contentWindow.document;
     doc.open();
     doc.write(html);
     doc.close();
     return true;
     }-*/;

    private static native void printFrame() /*-{
     var frame = $doc.getElementById('__printingFrame');
     frame = frame.contentWindow;
     frame.focus();
     frame.print();
     }-*/;

    private static native void openPreviewWindow(String html) /*-{
     var win = $wnd.open('', 'printPreview', 'menubar=yes,scrollbars=yes,resizable=yes,toolbar=no,location=no,status=no');
     win.document.write(html);
     win.document.close();
     }-*/;

    public static void print(String html)
    {
        if (populatePrintingFrame(html))
            DeferredCommand.addCommand(new Command()
            {
                public void execute()
                {
                    printFrame();
                }
            });
    }

    public static void printPreview(String html)
    {
        openPreviewWindow(html);
    }

    public static void printPreview(String[] styles, String htmlBody)
    {
        printPreview("<html><head>" + buildStyleLinks(styles) + "</head><body>" + htmlBody + "</body></html>");
    }

    public static void printPreview(String[] styles, UIObject obj)
    {
        printPreview(styles, obj.getElement().toString());
    }

    public static void print(String[] styles, String htmlBody)
    {
        print("<html><head>" + buildStyleLinks(styles) + "</head><body>" + htmlBody + "</body></html>");
    }

    public static void print(String[] styles, UIObject obj)
    {
        print(styles, obj.getElement().getInnerHTML());
    }

    private static String buildStyleLinks(String[] styles)
    {
        String styleStr = "";

        for (int i = 0; i < styles.length; i++)
            styleStr += "<link rel='StyleSheet' type='text/css' href='" + styles[i] + "' />";

        return styleStr;
    }
}