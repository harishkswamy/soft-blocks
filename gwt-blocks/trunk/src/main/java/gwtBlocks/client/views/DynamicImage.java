package gwtBlocks.client.views;

import gwtBlocks.shared.models.BaseModel;

import com.google.gwt.user.client.ui.Image;

/**
 * @author hkrishna
 */
public class DynamicImage extends BaseView<Image, BaseModel<String>>
{
    public DynamicImage(BaseModel<String> model)
    {
        super(model);
    }

    @Override
    protected Image buildView(BaseModel<String> model, Object... args)
    {
        return new Image();
    }
    
    public void valueChanged(BaseModel<String> model)
    {
        getWidget().setUrl(model.getValue());
    }
}
