package buildBlocks;

import java.io.File;

/**
 * @author hkrishna
 */
public class JarTask extends AbstractZipTask<JarTask>
{
    private StringBuilder _manifest = new StringBuilder("Manifest-Version: 1.0\n");

    public JarTask(String path, String version)
    {
        super(path);
        mfAttr("Implementation-Version", version);
    }

    public JarTask mfEntry(String name, String value)
    {
        _manifest.append('\n');
        return mfAttr(name, value);
    }

    public JarTask mfAttr(String key, String value)
    {
        _manifest.append(key).append(": ").append(value).append('\n');
        return this;
    }

    public JarTask create()
    {
        File manifest = Utils.writeFile(new File(System.getProperty("java.io.tmpdir"), "MANIFEST.MF"), _manifest.toString().getBytes());
        to("META-INF").from(manifest.getParent()).add(manifest.getPath());
        return create(true);
    }
}
