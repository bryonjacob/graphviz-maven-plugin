package us.bryon.graphviz.maven;

import java.io.File;

/**
 * use Graphviz to render .dot files to images.
 *
 * This mojo uses <a href="http://www.graphviz.org/">Graphviz</a> to convert files in the "dot"
 * file format.  By default, this mojo assumes you have DOT files with the extension .dot in a
 * directory in your maven project named <code>src/main/dot</code> - it will call out to the
 * Graphviz binary and convert the files to PNG files and place them in
 * <code>target/graphviz</code>, using the default "dot" layout for Graphviz.
 *
 * You can customize the source and target locations, the file format, or the layout by setting
 * plugin configuration parameters.
 *
 * @goal dot
 * @phase generate-sources
 */
public class DotMojo extends AbstractDotMojo {

    /**
     * the base directory in which .dot files can be found.
     *
     * @parameter expression="${project.basedir}/src/main/dot"
     * @required
     */
    protected File basedir;
    /**
     * pattern of files to include (separate multiple patterns with commas).
     *
     * @parameter default-value="**\/*.dot"
     * @required
     */
    protected String includes;

    protected File getBasedir() {
        return basedir;
    }

    protected String getIncludes() {
        return includes;
    }
}
