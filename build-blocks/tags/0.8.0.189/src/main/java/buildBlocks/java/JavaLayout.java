package buildBlocks.java;

import buildBlocks.Layout;

/**
 * @author hkrishna
 */
public class JavaLayout extends Layout
{
    private String _main      = "main/";
    private String _test      = "test/";
    private String _java      = "java/";
    private String _resources = "resources/";
    private String _bin       = "bin/";
    private String _doc       = "doc/";
    private String _api       = "api/";

    /**
     * @param path
     *            relative to the project path.
     */
    public JavaLayout main(String path)
    {
        _main = path;
        return this;
    }

    /**
     * @param path
     *            relative to the project path.
     */
    public JavaLayout test(String path)
    {
        _test = path;
        return this;
    }

    /**
     * @param path
     *            relative to {@link #mainPath()} and {@link #testPath()}.
     */
    public JavaLayout java(String path)
    {
        _java = path;
        return this;
    }

    /**
     * @param path
     *            relative to {@link #mainPath()} and {@link #testPath()}.
     */
    public JavaLayout resources(String path)
    {
        _resources = path;
        return this;
    }

    /**
     * @param path
     *            relative to {@link #mainPath()} and {@link #testPath()}.
     */
    public JavaLayout doc(String path)
    {
        _doc = path;
        return this;
    }

    /**
     * @param path
     *            relative to {@link #sourceMainDocPath()} and {@link #sourceTestDocPath()}.
     */
    public JavaLayout api(String path)
    {
        _api = path;
        return this;
    }

    private String _mainPath;
    private String _mainJavaPath;
    private String _mainResourcePath;
    private String _mainDocPath;
    private String _mainAPIPath;

    private String _testPath;
    private String _testJavaPath;
    private String _testResourcePath;
    private String _testDocPath;
    private String _testAPIPath;

    private String _targetMainPath;
    private String _targetMainBinPath;
    private String _targetMainDocPath;
    private String _targetMainAPIPath;

    private String _targetTestPath;
    private String _targetTestBinPath;
    private String _targetTestDocPath;
    private String _targetTestAPIPath;

    /**
     * @return src/main/ by default.
     */
    public String mainPath()
    {
        if (_mainPath == null)
            _mainPath = new StringBuilder(sourcePath()).append(_main).toString();

        return _mainPath;
    }

    /**
     * @return src/main/java/ by default.
     */
    public String mainJavaPath()
    {
        if (_mainJavaPath == null)
            _mainJavaPath = new StringBuilder(mainPath()).append(_java).toString();

        return _mainJavaPath;
    }

    /**
     * @return src/main/resources/ by default.
     */
    public String mainResourcePath()
    {
        if (_mainResourcePath == null)
            _mainResourcePath = new StringBuilder(mainPath()).append(_resources).toString();

        return _mainResourcePath;
    }

    /**
     * @return src/main/doc/ by default.
     */
    public String mainDocPath()
    {
        if (_mainDocPath == null)
            _mainDocPath = new StringBuilder(mainPath()).append(_doc).toString();

        return _mainDocPath;
    }

    /**
     * @return src/main/doc/api/ by default.
     */
    public String mainAPIPath()
    {
        if (_mainAPIPath == null)
            _mainAPIPath = new StringBuilder(mainDocPath()).append(_api).toString();

        return _mainAPIPath;
    }

    /**
     * @return src/test/ by default.
     */
    public String testPath()
    {
        if (_testPath == null)
            _testPath = new StringBuilder(sourcePath()).append(_test).toString();

        return _testPath;
    }

    /**
     * @return src/test/java/ by default.
     */
    public String testJavaPath()
    {
        if (_testJavaPath == null)
            _testJavaPath = new StringBuilder(testPath()).append(_java).toString();

        return _testJavaPath;
    }

    /**
     * @return src/test/resources/ by default.
     */
    public String testResourcePath()
    {
        if (_testResourcePath == null)
            _testResourcePath = new StringBuilder(testPath()).append(_resources).toString();

        return _testResourcePath;
    }

    /**
     * @return src/test/doc/ by default.
     */
    public String testDocPath()
    {
        if (_testDocPath == null)
            _testDocPath = new StringBuilder(testPath()).append(_doc).toString();

        return _testDocPath;
    }

    /**
     * @return src/test/doc/api/ by default.
     */
    public String testAPIPath()
    {
        if (_testAPIPath == null)
            _testAPIPath = new StringBuilder(testDocPath()).append(_api).toString();

        return _testAPIPath;
    }

    // Target paths ======================================================================

    /**
     * @return target/main/ by default.
     */
    public String targetMainPath()
    {
        if (_targetMainPath == null)
            _targetMainPath = new StringBuilder(targetPath()).append(_main).toString();

        return _targetMainPath;
    }

    /**
     * @return target/main/bin/ by default.
     */
    public String targetMainBinPath()
    {
        if (_targetMainBinPath == null)
            _targetMainBinPath = new StringBuilder(targetMainPath()).append(_bin).toString();

        return _targetMainBinPath;
    }

    /**
     * @return target/main/doc/ by default.
     */
    public String targetMainDocPath()
    {
        if (_targetMainDocPath == null)
            _targetMainDocPath = new StringBuilder(targetMainPath()).append(_doc).toString();

        return _targetMainDocPath;
    }

    /**
     * @return target/main/doc/api/ by default.
     */
    public String targetMainAPIPath()
    {
        if (_targetMainAPIPath == null)
            _targetMainAPIPath = new StringBuilder(targetMainDocPath()).append(_api).toString();

        return _targetMainAPIPath;
    }

    /**
     * @return target/test/ by default.
     */
    public String targetTestPath()
    {
        if (_targetTestPath == null)
            _targetTestPath = new StringBuilder(targetPath()).append(_test).toString();

        return _targetTestPath;
    }

    /**
     * @return target/test/bin/ by default.
     */
    public String targetTestBinPath()
    {
        if (_targetTestBinPath == null)
            _targetTestBinPath = new StringBuilder(targetTestPath()).append(_bin).toString();

        return _targetTestBinPath;
    }

    /**
     * @return target/test/doc/ by default.
     */
    public String targetTestDocPath()
    {
        if (_targetTestDocPath == null)
            _targetTestDocPath = new StringBuilder(targetTestPath()).append(_doc).toString();

        return _targetTestDocPath;
    }

    /**
     * @return target/test/doc/api/ by default.
     */
    public String targetTestAPIPath()
    {
        if (_targetTestAPIPath == null)
            _targetTestAPIPath = new StringBuilder(targetTestDocPath()).append(_api).toString();

        return _targetTestAPIPath;
    }
}
