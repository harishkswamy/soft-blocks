package gwtBlocks.client.views;

import gwtBlocks.client.GwtBlocksMessages;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author hkrishna
 */
public class MessageBox extends DialogBox
{
    private static final MessageBox _msgBox            = new MessageBox();

    private static final String     INFO_ICON_URL      = "jeeBlocks/images/info.png";
    private static final String     WARNING_ICON_URL   = "jeeBlocks/images/warning.png";
    private static final String     ERROR_ICON_URL     = "jeeBlocks/images/error.png";

    private static final String     INFO_ICON_STYLE    = "gbk-infoPng";
    private static final String     WARNING_ICON_STYLE = "gbk-warningPng";
    private static final String     ERROR_ICON_STYLE   = "gbk-errorPng";

    public static void info(String msg)
    {
        _msgBox.showBox(msg, INFO_ICON_URL, INFO_ICON_STYLE);
    }

    public static void alert(String msg)
    {
        _msgBox.showBox(msg, WARNING_ICON_URL, WARNING_ICON_STYLE);
    }

    public static void alert(String msg, Command command)
    {
        _msgBox._command = command;
        _msgBox.showBox(msg, WARNING_ICON_URL, WARNING_ICON_STYLE);
    }

    public static void error(String msg)
    {
        _msgBox.showBox(msg, ERROR_ICON_URL, ERROR_ICON_STYLE);
    }

    public static void error(String msg, Command command)
    {
        _msgBox._command = command;
        _msgBox.showBox(msg, ERROR_ICON_URL, ERROR_ICON_STYLE);
    }

    private SimplePanel _iconContainer;
    private Image       _icon;
    private HTML        _msg;
    private Command     _command;

    private MessageBox()
    {
        Image.prefetch(INFO_ICON_URL);
        Image.prefetch(WARNING_ICON_URL);
        Image.prefetch(ERROR_ICON_URL);

        _msg = new HTML();
        _msg.setWordWrap(false);
        _msg.getElement().getStyle().setPropertyPx("margin", 10);

        Button okBtn = new Button(GwtBlocksMessages.pick.ok(), new ClickListener()
        {
            public void onClick(Widget sender)
            {
                hide();
                
                if (_command != null)
                {
                    _command.execute();
                    _command = null;
                }
            }
        });
        okBtn.getElement().getStyle().setPropertyPx("margin", 10);

        _icon = new Image();
        _icon.setStylePrimaryName("gbk-png");

        _iconContainer = new SimplePanel();
        _iconContainer.setStylePrimaryName("gbk-pngContainer");
        _iconContainer.setSize("48px", "48px");
        _iconContainer.setWidget(_icon);

        FlexTableBuilder b = new FlexTableBuilder();
        b.widthC("48px").set(_iconContainer).set(_msg).nextRow();
        b.colSpan(2).centerC().set(okBtn).nextRow();

        SimplePanel captionPanel = new SimplePanel();
        captionPanel.setWidget(new HTML(GwtBlocksMessages.pick.message(), false));
        setHTML(captionPanel.getElement().getString());
        setWidget(b.getTable());
    }

    private void showBox(String msg, String iconUrl, String iconStyle)
    {
        resetStyles();

        _iconContainer.addStyleName(iconStyle);

        _icon.setUrl(iconUrl);
        _msg.setHTML(msg);

        center();
    }

    private void resetStyles()
    {
        _iconContainer.removeStyleName(INFO_ICON_STYLE);
        _iconContainer.removeStyleName(WARNING_ICON_STYLE);
        _iconContainer.removeStyleName(ERROR_ICON_STYLE);
    }
}
