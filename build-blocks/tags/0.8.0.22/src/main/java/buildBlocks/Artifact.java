package buildBlocks;

import static buildBlocks.Context.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hkrishna
 */
public class Artifact
{
    private static final List<String>          _remoteRepositories = new ArrayList<String>();
    private static String                      _localRepository    = System.getProperty("user.home")
                                                                       + "/.m2/repository/";

    private static final Map<String, Artifact> _artifacts          = new HashMap<String, Artifact>();

    static
    {
        _remoteRepositories.add("http://repo1.maven.org/maven2/");
        _remoteRepositories.add("http://mirrors.ibiblio.org/pub/mirrors/maven2/");
    }

    public static void localRepository(String path)
    {
        _localRepository = path;
    }

    public static void remoteRepositories(String... urls)
    {
        _remoteRepositories.clear();

        for (String url : urls)
            _remoteRepositories.add(url);
    }

    public static void install(String spec, String srcPath)
    {
        new FileTask(srcPath).copyToFile(_localRepository + toPath(spec), false);
        System.out.println("Installed " + spec + " from " + srcPath);
    }

    /**
     * @return String of the form group/project/version/project-version-classifier.type when the input spec is of the
     *         form group:project:type:classifier:version
     */
    private static String toPath(String spec)
    {
        String[] parts = spec.split(":");

        if (parts.length > 5)
            throw new IllegalArgumentException("Invalid artifact specification: " + spec);

        String group = parts[0], name = parts[1], type = parts[2], version = parts[parts.length - 1];

        StringBuilder path = new StringBuilder(group.replaceAll("\\.", "/")).append('/').append(name);
        path.append('/').append(version).append('/').append(name).append('-').append(version);

        if (parts.length > 4)
            path.append('-').append(parts[3]); // classifier

        path.append('.').append(type);

        return path.toString();
    }

    public static Artifact find(String spec)
    {
        Artifact artifact = _artifacts.get(spec);

        if (artifact == null)
        {
            artifact = new Artifact(spec);
            _artifacts.put(spec, artifact);
        }

        return artifact;
    }

    public static String toSpec(Project<?> p, String type, String classifier)
    {
        StringBuilder b = new StringBuilder(p.group()).append(':').append(p.name()).append(':').append(type);

        if (classifier != null && classifier.trim().length() > 0)
            b.append(':').append(classifier);

        return b.append(':').append(p.version()).toString();
    }

    public static String toName(Project<?> p, String type, String classifier)
    {
        StringBuilder b = new StringBuilder(p.name()).append('-').append(p.version());

        if (classifier != null && classifier.trim().length() > 0)
            b.append('-').append(classifier);

        return b.append('.').append(type).toString();
    }

    private String _spec;
    private String _path;

    private Artifact(String spec)
    {
        _spec = spec;
    }

    public String getPath()
    {
        if (_path != null)
            return _path;

        String path = toPath(_spec);

        _path = _localRepository + path;

        File artifact = new File(_path);

        if (artifact.exists())
            return artifact.getAbsolutePath();

        for (String url : _remoteRepositories)
        {
            try
            {
                Utils.download(url + path, _path, null);

                return _path;
            }
            catch (Exception e)
            {
                if (ctx().traceOn())
                    e.printStackTrace();
                else
                    System.out.println(String.format(
                        "Unable to download %s from %s because %s.%nUse -t option to see the stack trace.%n", path,
                        url, e));
            }
        }

        throw new Error("Unable to download artifact from any registered repository: " + this);
    }

    @Override
    public String toString()
    {
        return _spec;
    }
}
