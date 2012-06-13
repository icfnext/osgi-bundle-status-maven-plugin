package com.citytechinc.cqlibrary.bundlestatusplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal status
 * @phase verify
 */
public class OsgiBundleStatusPluginMojo extends AbstractMojo {

    /**
     * Symbolic name of OSGi bundle.
     *
     * @parameter
     * @required
     */
    private String bundleSymbolicName;

    /**
     * Felix container host name.
     *
     * @parameter default-value="localhost"
     */
    private String host;

    /**
     * Felix container password.
     *
     * @parameter default-value="admin"
     */
    private String password;

    /**
     * Felix container port number.
     *
     * @parameter default-value="4502"
     */
    private String port;

    /**
     * Delay in milliseconds before retrying bundle status check.
     *
     * @parameter default-value="1000"
     */
    private Integer retryDelay;

    /**
     * Number of times to retry checking bundle status before aborting.
     *
     * @parameter default-value="5"
     */
    private Integer retryLimit;

    /**
     * Felix container user name.
     *
     * @parameter default-value="admin"
     */
    private String user;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final BundleStatusChecker checker = FelixBundleStatusChecker.createFromMojo(this);

        checker.checkStatus(bundleSymbolicName);
    }

    public String getBundleSymbolicName() {
        return bundleSymbolicName;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getPort() {
        return port;
    }

    public Integer getRetryDelay() {
        return retryDelay;
    }

    public Integer getRetryLimit() {
        return retryLimit;
    }

    public String getUser() {
        return user;
    }
}
