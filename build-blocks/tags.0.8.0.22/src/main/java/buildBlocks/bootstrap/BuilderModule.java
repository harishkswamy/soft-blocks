package buildBlocks.bootstrap;

import static buildBlocks.Context.*;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import buildBlocks.Context;
import buildBlocks.FileTask;
import buildBlocks.ModuleInfo;
import buildBlocks.Project;
import buildBlocks.ProjectInfo;
import buildBlocks.Utils;
import buildBlocks.java.JavaModule;

/**
 * @author hkrishna
 */
@ModuleInfo(prefix = "_bbbm_")
public class BuilderModule extends JavaModule<Builder>
{
    private String _bbHome;

    public BuilderModule(Builder builder, String bbHome)
    {
        super(builder);

        _bbHome = bbHome;
    }

    Project<?> buildProject(boolean exportBuilder)
    {
        Builder b = project();

        b.layout().projectPath(ctx().property("build.dir", "build"));

        if (!new File(b.layout().mainJavaPath()).exists())
            throw new Error("Project class has not been implemented.");

        compile();

        try
        {
            URLClassLoader classLoader = new URLClassLoader(
                new URL[] { new File(b.layout().targetMainPath()).toURL() }, getClass().getClassLoader());

            Project<?> project = discoverProject(".", classLoader).newInstance();

            String buildNum = Context.ctx().property("build#");

            if (buildNum != null)
                project.buildNumber(buildNum);

            // b.classifier(project.id() + '-' + project.version());
            b.classifier(project.id());

            if (exportBuilder)
            {
                jar();

                new FileTask(b.jarPath()).copyToDir(_bbHome + "lib/ext", true);
            }

            return project;
        }
        catch (Exception e)
        {
            throw new Error("Unable to load project.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<Project> discoverProject(String packageDir, ClassLoader loader) throws Exception
    {
        Builder b = project();

        String[] fileNames = new File(b.layout().targetMainPath(), packageDir).list();

        if (fileNames == null)
            throw new Error("Unable to find project class.");

        for (String fileName : fileNames)
        {
            File file = new File(b.layout().targetMainPath(), packageDir + '/' + fileName);

            if (file.isDirectory())
            {
                Class<Project> buildProject = discoverProject(packageDir + '/' + fileName, loader);

                if (buildProject != null)
                    return buildProject;
            }
            else if (fileName.endsWith(".class"))
            {
                String className = Utils.trim(packageDir.replace('/', '.'), ".") + '.'
                    + fileName.substring(0, fileName.length() - 6);

                Class<?> prjClass = loader.loadClass(className);

                ProjectInfo projectAnn = prjClass.getAnnotation(ProjectInfo.class);

                if (projectAnn == null)
                    continue;

                if (Project.class.isAssignableFrom(prjClass))
                    return (Class<Project>) prjClass;

                throw new Error("Project class must extend " + Project.class);
            }
        }

        throw new Error("Unable to find project class.\nHint: Make sure the project class is annotated with "
            + ProjectInfo.class);
    }
}
