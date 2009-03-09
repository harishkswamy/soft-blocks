package gwtBlocks.client.views;

import gwtBlocks.client.GwtBlocksMessages;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
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
        _msgBox.showBox(msg, INFO_ICON_URL, INFO_ICON_STYLE, false);
    }

    public static void alert(String msg)
    {
        _msgBox.showBox(msg, WARNING_ICON_URL, WARNING_ICON_STYLE, false);
    }

    public static void alert(String msg, Command command)
    {
        _msgBox._command = command;
        _msgBox.showBox(msg, WARNING_ICON_URL, WARNING_ICON_STYLE, false);
    }

    public static void error(String msg)
    {
        _msgBox.showBox(msg, ERROR_ICON_URL, ERROR_ICON_STYLE, false);
    }

    public static void error(String msg, Command command)
    {
        _msgBox._command = command;
        _msgBox.showBox(msg, ERROR_ICON_URL, ERROR_ICON_STYLE, false);
    }

    public static void confirm(String msg, Command command)
    {
        _msgBox._command = command;
        _msgBox.showBox(msg, WARNING_ICON_URL, WARNING_ICON_STYLE, true);
    }

    private SimplePanel _iconContainer;
    private Image       _icon;
    private HTML        _msg;
    private Button      _cancelBtn;
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

        _cancelBtn = new Button(GwtBlocksMessages.pick.cancel(), new ClickListener()
        {
            public void onClick(Widget sender)
            {
                hide();
                _command = null;
            }
        });

        FlexTableBuilder b = new FlexTableBuilder();
        FlexTable btnTable = b.set(okBtn).setNbSp(1).set(_cancelBtn).getTable();
        btnTable.getElement().getStyle().setProperty("margin", "10px 0px");

        _icon = new Image();
        _icon.setStylePrimaryName("gbk-png");

        _iconContainer = new SimplePanel();
        _iconContainer.setStylePrimaryName("gbk-pngContainer");
        _iconContainer.setSize("48px", "48px");
        _iconContainer.setWidget(_icon);

        b.newTable().widthC("48px").set(_iconContainer).set(_msg).nextRow();
        b.colSpan(2).centerC().set(btnTable).nextRow();

        setWidget(b.getTable());
    }

    private void showBox(String msg, String iconUrl, String iconStyle, boolean confirm)
    {
        resetStyles();

        _iconContainer.addStyleName(iconStyle);

        _icon.setUrl(iconUrl);
        _msg.setHTML(msg);

        if (confirm)
        {
            setHTML(GwtBlocksMessages.pick.confirm());
            _cancelBtn.setVisible(true);
        }
        else
        {
            setHTML(GwtBlocksMessages.pick.message());
            _cancelBtn.setVisible(false);
        }

        center();
    }

    private void resetStyles()
    {
        _iconContainer.removeStyleName(INFO_ICON_STYLE);
        _iconContainer.removeStyleName(WARNING_ICON_STYLE);
        _iconContainer.removeStyleName(ERROR_ICON_STYLE);
    }
}
