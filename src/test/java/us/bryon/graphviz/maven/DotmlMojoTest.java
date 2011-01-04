package us.bryon.graphviz.maven;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.List;

public class DotmlMojoTest extends AbstractMojoTestCase {

    public void testDot() throws Exception {
        DotmlMojo dotmlMojo = (DotmlMojo) lookupMojo("dotml",
                new File(getClass().getResource("dotml-config.xml").toURI()));
        File basedir = (File) getVariableValueFromObject(dotmlMojo, "basedir");
        File destdir = (File) getVariableValueFromObject(dotmlMojo, "destdir");
        assertTrue(basedir != null);
        assertTrue(basedir.isDirectory());
        if (destdir.exists()) {
            FileUtils.deleteDirectory(destdir);
        }
        assertFalse(destdir.exists());

        dotmlMojo.execute();

        List<File> files = FileUtils.getFiles(destdir, "**/*", null);
        assertEquals(1, files.size());
        for (File file : files) {
            assertTrue(file.getName().endsWith(".png"));
        }

        FileUtils.deleteDirectory(destdir);
        setVariableValueToObject(dotmlMojo, "output", "dot");

        dotmlMojo.execute();

        files = FileUtils.getFiles(destdir, "**/*", null);
        assertEquals(1, files.size());
        for (File file : files) {
            assertTrue(file.getName().endsWith(".dot"));
        }
    }
}
