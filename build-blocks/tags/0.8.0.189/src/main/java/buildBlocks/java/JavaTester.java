package buildBlocks.java;

/**
 * @author hkrishna
 */
public abstract class JavaTester
{
    private JavaModule<?> _javaModule;

    public JavaTester(JavaModule<?> javaModule)
    {
        _javaModule = javaModule;
    }

    protected JavaModule<?> javaModule()
    {
        return _javaModule;
    }

    public void test()
    {
        if (!run())
        {
            System.out.println();
            throw new Error("Java tests failed!");
        }
    }

    protected abstract boolean run();
}
