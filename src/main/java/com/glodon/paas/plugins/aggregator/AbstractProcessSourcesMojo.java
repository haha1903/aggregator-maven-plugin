package com.glodon.paas.plugins.aggregator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

abstract class AbstractProcessSourcesMojo extends AbstractMojo {

    /**
     * List of sources includes patterns.
     * 
     * @parameter
     */
    protected String[] includes;

    /**
     * List of sources excludes patterns.
     * 
     * @parameter
     */
    protected String[] excludes;

    /**
     * Whether sources are required (the default) or optional. The build will fail if sources are not present and value
     * of this parameter is <code>true</code>. Setting to <code>false</code> can be useful when mojo is configured in
     * parent pom.xml and applies to multiple modules.
     * 
     * @parameter default-value="true"
     */
    protected boolean required;

    /** @component */
    protected BuildContext buildContext;

    public void execute() throws MojoExecutionException {
        List<File> sources = new ArrayList<File>();

        File sourceDirectory = getSourceDirectory();
        if (sourceDirectory.exists()) {
            if (includes == null || includes.length <= 0) {
                Scanner scanner = buildContext.newScanner(sourceDirectory, true);
                scanner.setIncludes(getDefaultIncludes());
                scanner.setExcludes(excludes);
                scanTo(sources, scanner);
            } else {
                // honour <includes> order
                // TODO maybe do something about same file matching multiple includes
                for (String include : includes) {
                    Scanner scanner = buildContext.newScanner(sourceDirectory, true);
                    scanner.setIncludes(new String[] { include });
                    scanner.setExcludes(excludes);
                    scanTo(sources, scanner);
                }
            }
        } else {
            getLog().warn("Source directory " + sourceDirectory + " does not exist.");
        }

        if (sources.isEmpty() && required) {
            throw new MojoExecutionException("No sources to process");
        }

        if (!sources.isEmpty()) {
            processSources(sources);
        }
    }

    private void scanTo(List<File> sources, Scanner scanner) {
        scanner.addDefaultExcludes();
        scanner.scan();
        for (String relPath : scanner.getIncludedFiles()) {
            sources.add(new File(scanner.getBasedir(), relPath));
        }
    }

    protected abstract void processSources(List<File> sources) throws MojoExecutionException;

    protected abstract String[] getDefaultIncludes();

    protected abstract File getSourceDirectory();

}
