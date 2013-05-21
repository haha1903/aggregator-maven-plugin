package com.glodon.paas.plugins.aggregator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.IOUtil;

abstract class AbstractAggregateMojo extends AbstractProcessSourcesMojo {
    /**
     * Insert line breaks in output after the specified column number.
     * 
     * @parameter expression="${maven.yuicompressor.linebreakpos}" default-value="0"
     */
    protected int linebreakpos;

    /**
     * Aggregate only, no minification.
     * 
     * @parameter expression="${maven.yuicompressor.nominify}" default-value="false"
     */
    protected boolean nominify;

    /**
     * Insert new line after each concatenation.
     * 
     * @parameter default-value="true"
     */
    protected boolean insertNewLine;

    @Override
    protected void processSources(List<File> sources) throws MojoExecutionException {
        try {
            StringWriter buf = new StringWriter();

            for (File source : sources) {
                Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(source), "UTF-8"));
                try {
                    // don't minify, simply write directly out to buffer
                    if (nominify) {
                        IOUtil.copy(in, buf);
                    }
                    // compress away then write out to buffer
                    else {
                        processSourceFile(source, in, buf);
                    }
                } finally {
                    IOUtil.close(in);
                }

                if (insertNewLine) {
                    buf.write('\n');
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create aggregate file", e);
        }
    }

    protected abstract void processSourceFile(File source, Reader in, Writer buf) throws IOException;

}
