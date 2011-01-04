package us.bryon.graphviz.maven;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.List;

public class DotMojoTest extends AbstractMojoTestCase {

    public void testDot() throws Exception {
        DotMojo dotMojo = (DotMojo) lookupMojo("dot",
                new File(getClass().getResource("dot-config.xml").toURI()));
        File basedir = (File) getVariableValueFromObject(dotMojo, "basedir");
        File destdir = (File) getVariableValueFromObject(dotMojo, "destdir");
        assertTrue(basedir != null);
        assertTrue(basedir.isDirectory());
        if (destdir.exists()) {
            FileUtils.deleteDirectory(destdir);
        }
        assertFalse(destdir.exists());

        dotMojo.execute();

        List<File> files = FileUtils.getFiles(destdir, "**/*", null);
        assertEquals(3, files.size());
        for (File file : files) {
            assertTrue(file.getName().endsWith(".svg"));
        }
    }
}
