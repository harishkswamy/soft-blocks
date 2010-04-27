package buildBlocks;

/**
 * @author hkrishna
 */
public abstract class Layout
{
    private String _projectPath = "./";
    private String _source      = "src";
    private String _target      = "target";

    private String _sourcePath;
    private String _targetPath;

    /**
     * @param name
     *            Project folder path relative to the main project.
     */
    public void projectPath(String name)
    {
        if (name == null || name.trim().length() == 0)
            return;

        _projectPath = name.endsWith("/") ? name : name + '/';
    }

    /**
     * @return Empty string by default.
     */
    public String projectPath()
    {
        return _projectPath;
    }

    /**
     * @param name
     *            Source folder path relative to the project path.
     */
    public void sourcePath(String name)
    {
        _source = name;
    }

    /**
     * @return src/ by default.
     */
    public String sourcePath()
    {
        if (_sourcePath == null)
            _sourcePath = new StringBuilder(projectPath()).append(_source).append('/').toString();

        return _sourcePath;
    }

    /**
     * @param name
     *            Target folder path relative to the project path.
     */
    public void targetPath(String name)
    {
        _target = name;
    }

    /**
     * @return target/ by default.
     */
    public String targetPath()
    {
        if (_targetPath == null)
            _targetPath = new StringBuilder(projectPath()).append(_target).append('/').toString();

        return _targetPath;
    }
}
