package buildBlocks.bootstrap;

import static buildBlocks.BuildCtx.*;

import jBlocks.server.Utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import buildBlocks.Project;
import buildBlocks.ProjectInfo;
import buildBlocks.java.JavaLayout;
import buildBlocks.java.JavaModule;

/**
 * @author hkrishna
 */
@ProjectInfo(group = "com.google.code.soft-blocks", version = "")
public class ProjectLoader extends Project<JavaLayout>
{
    private ProjectBuilder _builder;

    public ProjectLoader(ProjectBuilder builder)
    {
        super(new JavaLayout());
        version(Builder.version());

        _builder = builder;

        layout().projectPath(_builder.projectPath());

        String buildJavaVersion = ctx().getFromThread("build.java.version", "1.5");
        module(JavaModule.class, new JavaModule<ProjectLoader>(buildJavaVersion, this)
        {
            @Override
            public String mainClasspath()
            {
                return _builder.classpath();
            }
        });
    }

    Project<?> loadProject()
    {
        String projectPath = layout().projectPath();
        layout().projectPath(projectPath + ctx().getFromThread("build.dir", "build"));

        if (!new File(layout().mainJavaPath()).exists())
            throw new Error("Project not found under " + _builder.projectPath());

        JavaModule<?> jMod = module(JavaModule.class);

        jMod.compile();

        try
        {
            URLClassLoader classLoader = new URLClassLoader(
                new URL[] { new File(layout().targetMainBinPath()).toURL() }, _builder.classLoader());

            Project<?> project = discoverProject(".", classLoader).newInstance();

            project.layout().projectPath(projectPath);
            project.buildNum(_builder.buildNum());

            // b.classifier(project.id() + '-' + project.version());
            classifier(project.id());

            if (_builder.exportBuilder())
            {
                jMod.jar();
                _builder.exportBuilder(jMod.jarPath());
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
        String[] fileNames = new File(layout().targetMainBinPath(), packageDir).list();

        if (fileNames == null)
            throw new Error("Unable to find project class.");

        for (String fileName : fileNames)
        {
            File file = new File(layout().targetMainBinPath(), packageDir + '/' + fileName);

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
}
