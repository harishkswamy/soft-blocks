package buildBlocks.java.jee;

import buildBlocks.java.JavaLayout;

/**
 * @author hkrishna
 */
public class JEELayout extends JavaLayout
{
    private String _webapp = "webapp/";

    /**
     * @param path
     *            relative to {@link JavaLayout#mainPath()}.
     */
    public JEELayout webapp(String path)
    {
        _webapp = path;
        return this;
    }

    private String _sourceWebappPath;
    private String _targetWebappPath;
    private String _targetWebinfLib;

    /**
     * @return src/main/webapp/ by default.
     */
    public String sourceWebappPath()
    {
        if (_sourceWebappPath == null)
            _sourceWebappPath = new StringBuilder(mainPath()).append(_webapp).toString();

        return _sourceWebappPath;
    }

    /**
     * @return target/webapp/ by default.
     */
    public String targetWebappPath()
    {
        if (_targetWebappPath == null)
            _targetWebappPath = new StringBuilder(targetPath()).append(_webapp).toString();

        return _targetWebappPath;
    }

    /**
     * @return target/WEB-INF/lib/ by default.
     */
    public String targetWebinfLib()
    {
        if (_targetWebinfLib == null)
            _targetWebinfLib = new StringBuilder(targetWebappPath()).append("WEB-INF/lib/").toString();

        return _targetWebinfLib;
    }
}
