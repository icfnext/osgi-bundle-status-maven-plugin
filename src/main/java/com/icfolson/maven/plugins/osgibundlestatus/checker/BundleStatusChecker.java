package com.icfolson.maven.plugins.osgibundlestatus.checker;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * An instance of a bundle status checker may be used to check the status of one or more bundles in an OSGi container.
 */
public interface BundleStatusChecker {

    /**
     * Check the status of a bundle in an OSGi container as configured in the plugin mojo.
     *
     * @param bundleSymbolicName symbolic name of bundle, may optionally include the version number delimited by a
     * semicolon
     * @throws MojoExecutionException if exception occurs during the bundle status execution
     * @throws MojoFailureException if status check fails
     */
    void checkStatus(String bundleSymbolicName) throws MojoExecutionException, MojoFailureException;
}
