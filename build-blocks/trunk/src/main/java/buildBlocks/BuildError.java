package buildBlocks;

/**
 * @author hkrishna
 */
public class BuildError extends Error
{
    private static final long serialVersionUID = -2731946954293597150L;

    private boolean           _showUsageHelp;
    private boolean           _showProjectHelp;
    private Project<?>        _project;

    public BuildError(String message, Throwable cause, boolean showUsageHelp, boolean showProjectHelp)
    {
        super(message, cause);

        _showUsageHelp = showUsageHelp;
        _showProjectHelp = showProjectHelp;
    }

    public BuildError(String message, boolean showUsageHelp, boolean showProjectHelp)
    {
        super(message);

        _showUsageHelp = showUsageHelp;
        _showProjectHelp = showProjectHelp;
    }

    public BuildError(Throwable cause, boolean showUsageHelp, boolean showProjectHelp)
    {
        super(cause);

        _showUsageHelp = showUsageHelp;
        _showProjectHelp = showProjectHelp;
    }

    public boolean showUsageHelp()
    {
        return _showUsageHelp;
    }

    public boolean showProjectHelp()
    {
        return _showProjectHelp;
    }

    public void project(Project<?> project)
    {
        _project = project;
    }

    public Project<?> project()
    {
        return _project;
    }
}
