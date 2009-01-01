package buildBlocks.java.jee.gwt;

import buildBlocks.java.jee.JEELayout;

/**
 * @author hkrishna
 */
public class GWTLayout extends JEELayout
{
    private String _gwt = "gwt";
    private String _out = "out";
    private String _gen = "gen";

    private String _gwtPath;
    private String _gwtOutPath;
    private String _gwtGenPath;

    public void gwt(String gwt)
    {
        _gwt = gwt;
    }

    public void out(String out)
    {
        _out = out;
    }

    public void gen(String gen)
    {
        _gen = gen;
    }

    /**
     * @return target/gwt/ by default.
     */
    public String targetGwtPath()
    {
        if (_gwtPath == null)
            _gwtPath = new StringBuilder(targetPath()).append(_gwt).append('/').toString();

        return _gwtPath;
    }

    /**
     * @return target/gwt/out by default.
     */
    public String targetGwtOutPath()
    {
        if (_gwtOutPath == null)
            _gwtOutPath = new StringBuilder(targetGwtPath()).append(_out).toString();

        return _gwtOutPath;
    }

    /**
     * @return target/gwt/gen by default.
     */
    public String targetGwtGenPath()
    {
        if (_gwtGenPath == null)
            _gwtGenPath = new StringBuilder(targetGwtPath()).append(_gen).toString();

        return _gwtGenPath;
    }
}
