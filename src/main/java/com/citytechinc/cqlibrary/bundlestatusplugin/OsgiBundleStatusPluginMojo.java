package com.citytechinc.cqlibrary.bundlestatusplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal status
 * @phase verify
 */
public final class OsgiBundleStatusPluginMojo extends AbstractMojo {

    /**
     * Felix container host name.
     *
     * @parameter default-value="localhost"
     */
    private String host;

    /**
     * Felix container port number.
     *
     * @parameter default-value="4502"
     */
    private String port;

    /**
     * Felix container user name.
     *
     * @parameter default-value="admin"
     */
    private String user;

    /**
     * Felix container password.
     *
     * @parameter default-value="admin"
     */
    private String password;

    /**
     * Symbolic name of OSGi bundle.
     *
     * @parameter
     * @required
     */
    private String bundleSymbolicName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final BundleStatusChecker checker = new FelixBundleStatusChecker(this, host, port, user, password);

        checker.checkStatus(bundleSymbolicName);
    }
}
