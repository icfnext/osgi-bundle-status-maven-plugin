package com.citytechinc.cqlibrary.bundlestatusplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal status
 */
public final class FelixBundleStatusPluginMojo extends AbstractMojo {

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
     */
    private String bundleSymbolicName;

    @Override
    public void execute() throws MojoExecutionException {
        if (bundleSymbolicName == null) {

        } else {
            final String status = new FelixBundleStatusChecker(this).getStatus(host, port, user, password,
                "com.sigmaaldrich.sigma-aldrich-core");

        }
    }
}
