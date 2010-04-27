package buildBlocks;

/**
 * @author hkrishna
 */
public class ZipTask extends AbstractZipTask<ZipTask>
{
    public ZipTask(String path)
    {
        super(path);
    }

    public ZipTask create()
    {
        return create(false);
    }
}
