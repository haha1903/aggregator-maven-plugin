package com.glodon.paas.plugins.aggregator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @goal aggregate-css
 * @phase process-resources
 */
public class AggregateCssMojo extends AbstractAggregateMojo {
    public static final String[] DEFAULT_INCLUDES = { "**/*.merge.css" };

    /**
     * @parameter default-value="${project.build.outputDirectory}/resources"
     */
    private File sourceDirectory;

    @Override
    protected void processSourceFile(File source, Reader in, Writer buf) throws IOException {
        URI basedir = sourceDirectory.toURI();
        URI relativize = basedir.relativize(source.toURI());
        System.out.println(relativize);
        String sourceParentPath = StringUtils.repeat("../", StringUtils.countMatches(relativize.toString(), "/"));
        String content = IOUtils.toString(new FileInputStream(source));
        OutputStream out = new FileOutputStream(source);
        Pattern p = Pattern.compile("@import\\s+?url\\(\"(.*?\\.css)\"\\)");
        Matcher m = p.matcher(content);
        while (m.find()) {
            File css = new File(source.getParent(), m.group(1));
            if (css.exists()) {
                URI r = basedir.relativize(css.toURI());
                FileInputStream cssIn = new FileInputStream(css);
                String cssContent = IOUtils.toString(cssIn, "UTF-8");
                cssContent = cssContent.replaceAll("url\\((\"|\')?(.*?)(\"|\')?\\)", "url($1" + sourceParentPath + new File(r.getPath()).getParent() + "/$2$3)");
                IOUtils.copy(new StringReader(cssContent), out, "UTF-8");
                IOUtils.closeQuietly(cssIn);
            } else {
                getLog().warn("css file not found: " + css.getPath());
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
