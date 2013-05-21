package com.glodon.paas.plugins.aggregator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * @goal aggregate
 * @phase process-resources
 */
public class AggregateMojo extends AbstractAggregateMojo {
    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;
    public static final String[] DEFAULT_INCLUDES = { "**/*.merge.js" };

    /**
     * @parameter default-value="${project.build.outputDirectory}"
     */
    private File sourceDirectory;

    @Override
    protected void processSourceFile(File source, Reader in, Writer buf) throws IOException {
        String content = IOUtils.toString(new FileInputStream(source));
        OutputStream out = new FileOutputStream(source);
        Pattern p = Pattern.compile("<script src=.*?(/.*?\\.js).*?</script>");
        Matcher m = p.matcher(content);
        while (m.find()) {
            File js = new File(outputDirectory, m.group(1));
            if (js.exists()) {
                FileInputStream jsIn = new FileInputStream(js);
                IOUtils.copy(jsIn, out);
                out.write(";".getBytes());
                IOUtils.closeQuietly(jsIn);
            } else {
                getLog().warn("js file not found: " + js.getPath());
            }
        }
        IOUtils.closeQuietly(out);
    }

    @Override
    protected String[] getDefaultIncludes() {
        return DEFAULT_INCLUDES;
    }

    @Override
    protected File getSourceDirectory() {
        return sourceDirectory;
    }
}
