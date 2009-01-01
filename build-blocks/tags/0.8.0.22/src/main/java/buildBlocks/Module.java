package buildBlocks;

/**
 * @author hkrishna
 */
public abstract class Module<P extends Project<?>> implements TaskContainer
{
    private String _prefix;
    private String _name;
    private P      _project;

    public Module(P project)
    {
        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);

        if (info == null)
            throw new Error("Modules must be annotated with " + ModuleInfo.class);

        _prefix = info.prefix();
        _name = info.name().trim().length() == 0 ? getClass().getSimpleName() : info.name();
        _project = project;
    }

    public String prefix()
    {
        return _prefix;
    }

    public String name()
    {
        return _name;
    }

    public P project()
    {
        return _project;
    }

    @Override
    public String toString()
    {
        return name();
    }
}
