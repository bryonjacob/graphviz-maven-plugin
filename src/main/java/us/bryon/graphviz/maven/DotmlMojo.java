package us.bryon.graphviz.maven;

import org.apache.maven.plugin.MojoExecutionException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;

/**
 * use DOTML and GraphViz to render DOTML .xml files to images.
 *
 * This mojo does much the same as the <a href="/dot-mojo.html">graphviz:dot</a> mojo, except that
 * instead of standard Graphviz .dot files, it expects its input to be DOTML files, an XML dialect
 * for .dot files.
 *
 * This product includes DOTML developed by Martin Loetzsch
 * (<a href="http://www.martin-loetzsch.de/DOTML">http://www.martin-loetzsch.de/DOTML</a>).
 *
 * @goal dotml
 * @phase generate-sources
 */
public class DotmlMojo extends AbstractDotMojo {

    /**
     * the base directory in which .dot files can be found.
     *
     * @parameter expression="${project.basedir}/src/main/dotml"
     * @required
     */
    protected File basedir;
    /**
     * pattern of files to include (separate multiple patterns with commas).
     *
     * @parameter expression="**\/*.xml"
     * @required
     */
    protected String includes;

    protected File getBasedir() {
        return basedir;
    }

    protected String getIncludes() {
        return includes;
    }

    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    // when we are using DOTML files, we need to transform them to DOT files first.
    protected File transformInputFile(File from) throws MojoExecutionException {
        // create a temp file
        File tempFile;
        try {
            tempFile = File.createTempFile("dotml-tmp", ".xml");
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "error creating temp file to hold DOTML to DOT translation", e);
        }
        // perform an XSLT transform from the input file to the temp file
        Source xml = new StreamSource(from);
        Result result = new StreamResult(tempFile);
        try {
            Source xslt = new StreamSource(
                    getClass().getClassLoader().getResourceAsStream("dotml/dotml2dot.xsl"));
            transformerFactory.newTransformer(xslt).transform(xml, result);
        } catch (TransformerException e) {
            throw new MojoExecutionException(
                    String.format("error transforming %s from DOTML to DOT file", from), e);
        }
        // return the temp file
        return tempFile;
    }

}
