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
        _project = project;
        moduleInfo(getClass().getAnnotation(ModuleInfo.class));
    }

    @SuppressWarnings("unchecked")
    void replaces(Class<? extends Module> moduleClass)
    {
        moduleInfo(moduleClass.getAnnotation(ModuleInfo.class));
    }

    private void moduleInfo(ModuleInfo info)
    {
        String group = getClass().getPackage().getName();
        String id = getClass().getName().substring(getClass().getName().lastIndexOf('.'));
        _name = id;

        if (info != null)
        {
            if (info.group().trim().length() > 0)
                group = info.group();

            if (info.id().trim().length() > 0)
                id = info.id();

            if (info.name().trim().length() > 0)
                _name = info.name();
        }

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
        return qid();
    }
}
