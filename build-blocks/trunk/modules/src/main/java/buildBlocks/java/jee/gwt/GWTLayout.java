package buildBlocks.java.jee.gwt;

import buildBlocks.Layout;
import buildBlocks.java.jee.JEELayout;

/**
 * @author hkrishna
 */
public class GWTLayout extends JEELayout
{
    private String _gwt = "gwt/";
    private String _out = "out/";
    private String _gen = "gen/";

    /**
     * @param path
     *            relative to {@link Layout#targetPath()}.
     */
    public GWTLayout gwt(String path)
    {
        _gwt = path;
        return this;
    }

    /**
     * @param path
     *            relative to {@link #targetGWTPath()}.
     */
    public GWTLayout out(String path)
    {
        _out = path;
        return this;
    }

    /**
     * @param path
     *            relative to {@link #targetGWTPath()}.
     */
    public GWTLayout gen(String path)
    {
        _gen = path;
        return this;
    }

    private String _gwtPath;
    private String _gwtOutPath;
    private String _gwtGenPath;

    /**
     * @return target/gwt/ by default.
     */
    public String targetGWTPath()
    {
        if (_gwtPath == null)
            _gwtPath = new StringBuilder(targetPath()).append(_gwt).append('/').toString();

        return _gwtPath;
    }

    /**
     * @return target/gwt/out/ by default.
     */
    public String targetGWTOutPath()
    {
        if (_gwtOutPath == null)
            _gwtOutPath = new StringBuilder(targetGWTPath()).append(_out).toString();

        return _gwtOutPath;
    }

    /**
     * @return target/gwt/gen/ by default.
     */
    public String targetGWTGenPath()
    {
        if (_gwtGenPath == null)
            _gwtGenPath = new StringBuilder(targetGWTPath()).append(_gen).toString();

        return _gwtGenPath;
    }
}
