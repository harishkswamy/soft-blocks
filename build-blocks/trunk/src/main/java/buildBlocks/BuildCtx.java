package buildBlocks;

import jBlocks.server.AppContext;
import jBlocks.server.Utils;

import java.io.File;
import java.util.Properties;

/**
 * @author hkrishna
 */
public class BuildCtx extends AppContext
{
    private static BuildCtx _instance;

    public static BuildCtx ctx()
    {
        if (_instance == null)
            _instance = new BuildCtx();

        return _instance;
    }

    private Properties _settings;
    
    private BuildCtx()
    {
        Properties global = loadProps(getClass().getResource("/settings.properties").getPath(), null);
        _settings = loadProps(System.getProperty("user.home") + "/.build-blocks/settings.properties", global);
    }

    private Properties loadProps(String propsFilePath, Properties defaults)
    {
        File file = new File(propsFilePath);

        if (!file.exists())
            return defaults;

        return Utils.loadProperties(file, defaults);
    }

    public void putInThread(String key, String value)
    {
        putInThread(String.class, key, value);
    }

    public String getFromThread(String key)
    {
        return getFromThread(String.class, key);
    }

    public String getFromThread(String key, String defValue)
    {
        String value = getFromThread(key);

        return value == null ? defValue : value;
    }

    public String get(String key)
    {
        String value = get(String.class, key);

        if (value != null)
            return value;

        if (_settings == null)
            return null;

        return _settings.getProperty(key);
    }

    /**
     * @return the value for property <code>key</code>, if the property <code>key</code> is not registered, then
     *         this method will simply return the provided <code>defValue</code>.
     */
    public String get(String key, String defValue)
    {
        String value = get(key);

        return value == null ? defValue : value;
    }

    public Integer get(String key, Integer defValue)
    {
        String value = get(key);

        return value == null ? defValue : Integer.parseInt(value);
    }

    public void setTrace(boolean trace)
    {
        putInThread(Boolean.class, "trace", trace);
    }

    public boolean traceOn()
    {
        Boolean trace = getFromThread(Boolean.class, "trace");

        return trace == null ? false : trace;
    }

    public String localReposPath()
    {
        return ctx().get("local.repository.path", System.getProperty("user.home") + "/.m2/repository/");
    }

    public String[] remoteReposUrls()
    {
        return ctx().get("remote.repository.urls",
            "http://repo1.maven.org/maven2/, http://mirrors.ibiblio.org/pub/mirrors/maven2/").split(",");
    }
}
