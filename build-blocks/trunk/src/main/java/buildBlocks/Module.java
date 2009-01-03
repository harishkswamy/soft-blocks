package buildBlocks;

/**
 * @author hkrishna
 */
public abstract class Module<P extends Project<?>> implements TaskContainer
{
    private String _qid;
    private String _name;
    private P      _project;

    public Module(P project)
    {
        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);

        if (info == null)
            throw new Error("Modules must be annotated with " + ModuleInfo.class);

        String group = info.group().trim().length() == 0 ? getClass().getPackage().getName() : info.group();
        String id = info.id().trim().length() == 0 ? getClass().getSimpleName() : info.id();

        _name = info.name().trim().length() == 0 ? id : info.name();
        _project = project;
        _qid = group + '.' + id;
    }

    public String qid()
    {
        return _qid;
    }

    public String name()
    {
        return _name;
    }

    public P project()
    {
        return _project;
    }

    protected void execute(String... taskIds)
    {
        _project.execute(taskIds);
    }

    @Override
    public String toString()
    {
        return name();
    }
}
