package buildBlocks.bootstrap;

import buildBlocks.BuildError;
import buildBlocks.Project;
import buildBlocks.ProjectInfo;
import buildBlocks.java.JavaLayout;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", version = "")
public final class ProjectBuilder extends Project<JavaLayout>
{
    private BuilderCtx _builderCtx;
    private Project<?> _project;

    ProjectBuilder(String version, BuilderCtx builderCtx)
    {
        super(new JavaLayout());
        version(version);

        _builderCtx = builderCtx;
    }

    void build()
    {
        try
        {
            System.out.println();
            System.out.println("Loading project...");

            // Load project
            ProjectLoader loader = new ProjectLoader(this, _builderCtx);
            _project = loader.loadProject(_builderCtx.exportProject());

            System.out.println();

            // Execute tasks
            System.out.println(String.format("Building %s...", _project.name()));
            System.out.println();

            long start = System.currentTimeMillis();

            _project.execute(_builderCtx.tasks());

            long end = System.currentTimeMillis();

            System.out.println(String.format("Project built successfully in %ss.", (end - start) / 1000.0));
            System.out.println();
        }
        catch (BuildError be)
        {
            be.project(_project);

            throw be;
        }
    }
}
