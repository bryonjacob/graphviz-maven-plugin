package us.bryon.graphviz.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.io.filemappers.FileExtensionMapper;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * provides common functionality for the various mojos that ultimately use graphviz to render
 * images.
 */
public abstract class AbstractDotMojo extends AbstractMojo {
    /**
     * pattern of files to exclude (separate multiple patterns with commas).
     * @parameter
     */
    protected String excludes;
    /**
     * layout to use.
     *
     * this can be any value that can be provided to the -K option of the graphviz binary - at
     * time of writing this is <code>dot|neato|twopi|circo|fdp|sfdp</code>. see the man pages
     * (http://www.graphviz.org/pdf/dot.1.pdf) for descriptions of the formats.
     * @parameter default-value="dot"
     * @required
     */
    protected String layout;
    /**
     * output format.
     *
     * this can be any value that can be provided to the -T option of the graphviz binary - the
     * list of all known options is here:
     * <a href="http://www.graphviz.org/doc/info/output.html">http://www.graphviz.org/doc/info/output.html</a>.
     * the set of options available on your maching may vary - run <code>dot -T?</code> for a list
     * of output formats supported by your installation.
     * @parameter default-value="png"
     * @required
     */
    protected String output;
    /**
     * destination directory into which rendered output will be written.
     * @parameter expression="${project.build.directory}/graphviz"
     * @required
     */
    protected File destdir;
    /**
     * the graphviz binary to use.
     *
     * by default, it is assumed that "dot" is on your PATH - if not, this value should be set to
     * the full path to the dot binary.
     * @parameter default-value="dot"
     * @required
     */
    protected String dot;
    /**
     * file mapper to use.
     *
     * provide a custom mapper used to transform input filenames into output - by default, the
     * mapper used will be a FileExtensionMapper, using the value of <code>output</code> as the
     * extension on the output file.
     * @parameter
     */
    protected FileMapper mapper;

    // the basedir and includes vary across the concrete implementations, so we need to access
    // these values through getters.
    protected abstract File getBasedir();
    protected abstract String getIncludes();

    // by default, we do an identity transform and just pass the original input file to graphviz -
    // subclasses can override this where the original file must be somehow transformed before
    // passing it to graphviz.
    protected File transformInputFile(File input) throws MojoExecutionException { return input; }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // find matching files
            List<String> filenames = FileUtils.getFileNames(
                    getBasedir(), getIncludes(), excludes, false);
            // get the configured mapper, or create a default one if none is specified
            FileMapper mapper = this.mapper == null ? createDefaultMapper() : this.mapper;
            // for each file...
            for (String filename : filenames) {
                // compute the "from" and "to" files
                File from = new File(getBasedir(), filename);
                File to = new File(destdir, mapper.getMappedFileName(filename));
                getLog().debug(String.format("converting %s to %s", from, to));
                // make sure the directory for the "to" file exists
                to.getParentFile().mkdirs();
                // call graphviz to convert the "from" file
                Commandline cli = new Commandline();
                cli.setExecutable(dot);
                cli.addArguments(new String[] {
                        String.format("-K%s", layout),
                        String.format("-T%s", output),
                        transformInputFile(from).getAbsolutePath()
                });
                try {
                    // pipe the output stream from graphviz to the "to" file
                    Process process = cli.execute();
                    IOUtil.copy(process.getInputStream(), new FileOutputStream(to));
                } catch (CommandLineException e) {
                    throw new MojoExecutionException("error performing conversion", e);
                }
            }

        } catch (IOException e) {
            throw new MojoExecutionException(
                    String.format("error finding files from %s (includes = %s, excludes = %s)",
                            getBasedir(), getIncludes(), excludes), e);
        }
    }

    // by default, map files by using the "output" format as the extension
    protected FileExtensionMapper createDefaultMapper() {
        FileExtensionMapper fileExtensionMapper = new FileExtensionMapper();
        fileExtensionMapper.setTargetExtension(output);
        return fileExtensionMapper;
    }
}
