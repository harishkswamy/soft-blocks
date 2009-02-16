package buildBlocks;

import java.io.File;
import java.util.Properties;

/**
 * @author hkrishna
 */
public class Context
{
    private static Context _instance;

    public static Context ctx()
    {
        if (_instance == null)
            _instance = new Context();

        return _instance;
    }

    private Properties _props;
    private boolean    _trace;

    private Context()
    {
    }

    public void setProperty(String key, String value)
    {
        props().setProperty(key, value);
    }

    public String property(String key)
    {
        return props().getProperty(key);
    }

    /**
     * @return the value for property <code>key</code>, if the property <code>key</code> is not registered, then
     *         this method will simply return the provided <code>defValue</code>.
     */
    public String property(String key, String defValue)
    {
        return props().getProperty(key, defValue);
    }

    private Properties props()
    {
        if (_props == null)
            _props = loadProps();

        return _props;
    }

    private Properties loadProps()
    {
        Properties global = loadProps(getClass().getResource("/settings.properties").getPath(), null);
        return loadProps(System.getProperty("user.home") + "/.build-blocks/settings.properties", global);
    }

    private Properties loadProps(String propsFilePath, Properties defaults)
    {
        File file = new File(propsFilePath);

        if (!file.exists())
            return defaults;

        return Utils.loadProperties(file, defaults);
    }

    public void trace(boolean trace)
    {
        _trace = trace;
    }

    public boolean traceOn()
    {
        return _trace;
    }
}
