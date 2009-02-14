package buildBlocks.repos;

/**
 * @author hkrishna
 */
public interface SourceRepository
{
    void init(String url, String userName, String password);

    long checkout(String revision, String path, String target);
}
