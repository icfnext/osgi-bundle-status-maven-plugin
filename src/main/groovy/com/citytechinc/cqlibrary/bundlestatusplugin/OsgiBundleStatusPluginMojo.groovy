package com.citytechinc.cqlibrary.bundlestatusplugin

import com.citytechinc.cqlibrary.bundlestatusplugin.checker.FelixBundleStatusChecker

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter

/**
 * Check the status of bundle(s) in an OSGi container.
 */
@Mojo(name = "status", defaultPhase = LifecyclePhase.VERIFY)
class OsgiBundleStatusPluginMojo extends AbstractMojo {

    /**
     * Symbolic names of OSGi bundles to check.
     */
    @Parameter(required = true)
    String[] bundleNames

    /**
     * Felix container host name.
     */
    @Parameter(defaultValue = "localhost")
    String host

    /**
     * Felix container password.
     */
    @Parameter(defaultValue = "admin")
    String password

    /**
     * Felix container port number.
     */
    @Parameter(defaultValue = "4502")
    String port

    /**
     * Required status for bundle(s) being checked. Defaults to "Active".
     */
    @Parameter(defaultValue = "Active")
    String requiredStatus

    /**
     * Delay in milliseconds before retrying bundle status check.
     */
    @Parameter(defaultValue = "1000")
    Integer retryDelay

    /**
     * Number of times to retry checking bundle status before aborting.
     */
    @Parameter(defaultValue = "5")
    Integer retryLimit

    /**
     * Skip execution of the plugin.
     */
    @Parameter(property = "osgi.bundle.status.skip", defaultValue = "false")
    boolean skip

    /**
     * Felix container user name.
     */
    @Parameter(defaultValue = "admin")
    String user

    @Override
    void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            log.info("Skipping execute per configuration.")
        } else {
            // TODO: move to factory class if additional container
            // implementations are added
            def checker = FelixBundleStatusChecker.createFromMojo(this)

            bundleNames.each { bundleName ->
                checker.checkStatus(bundleName)
            }
        }
    }
}
