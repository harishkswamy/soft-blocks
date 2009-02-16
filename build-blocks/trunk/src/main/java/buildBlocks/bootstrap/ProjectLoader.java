package buildBlocks.bootstrap;

import static buildBlocks.Context.*;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import buildBlocks.Context;
import buildBlocks.ModuleInfo;
import buildBlocks.Project;
import buildBlocks.ProjectInfo;
import buildBlocks.Utils;
import buildBlocks.java.JavaModule;

/**
 * @author hkrishna
 */
@ModuleInfo(id = "_bbbm_")
public class ProjectLoader extends JavaModule<ProjectBuilder>
{
    private BuilderCtx _builderCtx;

    public ProjectLoader(ProjectBuilder projectBuilder, BuilderCtx builderCtx)
    {
        super("1.5", projectBuilder);

        _builderCtx = builderCtx;
    }

    Project<?> loadProject(boolean exportBuilder)
    {
        ProjectBuilder b = project();

        String projectPath = b.layout().projectPath();
        b.layout().projectPath(projectPath + ctx().property("build.dir", "build"));

        if (!new File(b.layout().mainJavaPath()).exists())
            throw new Error("Project not found.");

        compile();

        try
        {
            URLClassLoader classLoader = new URLClassLoader(new URL[] { new File(b.layout().targetMainBinPath())
                .toURL() }, _builderCtx.bbClassLoader());

            Project<?> project = discoverProject(".", classLoader).newInstance();
            project.layout().projectPath(projectPath);

            String buildNum = Context.ctx().property("build#");

            if (buildNum != null)
                project.buildNum(buildNum);

            // b.classifier(project.id() + '-' + project.version());
            b.classifier(project.id());

            if (exportBuilder)
            {
                jar();
                _builderCtx.exportBuilder(jarPath());
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
        ProjectBuilder b = project();

        String[] fileNames = new File(b.layout().targetMainBinPath(), packageDir).list();

        if (fileNames == null)
            throw new Error("Unable to find project class.");

        for (String fileName : fileNames)
        {
            File file = new File(b.layout().targetMainBinPath(), packageDir + '/' + fileName);

            if (file.isDirectory())
            {
                Class<Project> buildProject = discoverProject(packageDir + '/' + fileName, loader);

                if (buildProject != null)
                    return buildProject;
            }
            else if (fileName.endsWith(".class"))
            {
                if (fileName.contains("$"))
                    continue;

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

    @Override
    public String mainClasspath()
    {
        return _builderCtx.bbClasspath();
    }
}
