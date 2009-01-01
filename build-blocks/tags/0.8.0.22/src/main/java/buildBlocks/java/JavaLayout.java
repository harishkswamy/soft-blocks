package buildBlocks.java;

import buildBlocks.Layout;

/**
 * @author hkrishna
 */
public class JavaLayout extends Layout
{
    private String _main      = "main";
    private String _test      = "test";
    private String _java      = "java";
    private String _resources = "resources";

    private String _sourceMainPath;
    private String _sourceTestPath;

    /**
     * @return src/main/ by default.
     */
    public String sourceMainPath()
    {
        if (_sourceMainPath == null)
            _sourceMainPath = new StringBuilder(sourcePath()).append(_main).append('/').toString();

        return _sourceMainPath;
    }

    /**
     * @return src/main/java by default.
     */
    public String mainJavaPath()
    {
        return new StringBuilder(sourceMainPath()).append(_java).toString();
    }

    /**
     * @return src/main/resources by default.
     */
    public String mainResourcePath()
    {
        return new StringBuilder(sourceMainPath()).append(_resources).toString();
    }

    /**
     * @return src/test/ by default.
     */
    public String sourceTestPath()
    {
        if (_sourceTestPath == null)
            _sourceTestPath = new StringBuilder(sourcePath()).append(_test).append('/').toString();

        return _sourceTestPath;
    }

    /**
     * @return src/test/java by default.
     */
    public String testJavaPath()
    {
        return new StringBuilder(sourceTestPath()).append(_java).toString();
    }

    /**
     * @return src/test/resources by default.
     */
    public String testResourcePath()
    {
        return new StringBuilder(sourceTestPath()).append(_resources).toString();
    }

    /**
     * @return target/main by default.
     */
    public String targetMainPath()
    {
        return new StringBuilder(targetPath()).append(_main).toString();
    }

    /**
     * @return target/test by default.
     */
    public String targetTestPath()
    {
        return new StringBuilder(targetPath()).append(_test).toString();
    }
}
