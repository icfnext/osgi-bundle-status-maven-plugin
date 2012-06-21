package com.citytechinc.cqlibrary.bundlestatusplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Check the status of bundle(s) in an OSGi container.
 *
 * @goal status
 * @phase verify
 */
public class OsgiBundleStatusPluginMojo extends AbstractMojo {

    /**
     * Symbolic names of OSGi bundles to check.
     *
     * @parameter
     * @required
     */
    private String[] bundleNames;

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
     * Required status for bundle(s) being checked. Defaults to "Active".
     *
     * @parameter default-value="Active"
     */
    private String requiredStatus;

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
     * Skip execution of the plugin.
     *
     * @parameter expression="${osgi.bundle.status.skip}" default-value="false"
     */
    private boolean skip;

    /**
     * Felix container user name.
     *
     * @parameter default-value="admin"
     */
    private String user;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execute per configuration.");
        } else {
            // TODO: move to factory class if additional container
            // implementations are added
            final BundleStatusChecker checker = FelixBundleStatusChecker.createFromMojo(this);

            for (final String bundleName : bundleNames) {
                checker.checkStatus(bundleName);
            }
        }
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

    public String getRequiredStatus() {
        return requiredStatus;
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
