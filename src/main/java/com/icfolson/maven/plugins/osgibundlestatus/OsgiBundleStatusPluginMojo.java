package com.icfolson.maven.plugins.osgibundlestatus;

import com.icfolson.maven.plugins.osgibundlestatus.checker.BundleStatusChecker;
import com.icfolson.maven.plugins.osgibundlestatus.checker.FelixBundleStatusChecker;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Check the status of bundle(s) in an OSGi container.
 */
@Mojo(name = "status", defaultPhase = LifecyclePhase.INSTALL)
public class OsgiBundleStatusPluginMojo extends AbstractMojo {

    private static final String PATH_SUFFIX = "/bundles/.json";

    /**
     * Root path to the OSGi Management Console.
     */
    @Parameter(defaultValue = "/system/console")
    private String path;

    /**
     * Symbolic names of OSGi bundles to check.  Names can include a version number delimited with a semicolon, e.g.
     * "bundle-name;1.0.0".
     */
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    @Parameter(required = true)
    private String[] bundleNames;

    /**
     * OSGi container host name.
     */
    @Parameter(defaultValue = "localhost")
    private String host;

    /**
     * OSGi container port number.
     */
    @Parameter(defaultValue = "4502")
    private Integer port;

    /**
     * OSGi container context path.
     */
    @Parameter
    private String contextPath;

    /**
     * OSGi container user name.
     */
    @Parameter(defaultValue = "admin")
    private String username;

    /**
     * OSGi container password.
     */
    @Parameter(defaultValue = "admin")
    private String password;

    /**
     * Delay in milliseconds before retrying bundle status check.
     */
    @Parameter(defaultValue = "1000")
    private Integer retryDelay;

    /**
     * Number of times to retry checking bundle status before aborting.
     */
    @Parameter(defaultValue = "5")
    private Integer retryLimit;

    /**
     * Required status for bundle(s) being checked.
     */
    @Parameter(defaultValue = "Active")
    private String requiredStatus;

    /**
     * Use 'https' scheme for status check.
     */
    @Parameter(defaultValue = "false")
    private boolean secure;

    /**
     * Skip execution of the plugin.
     */
    @Parameter(property = "osgi.bundle.status.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Quiet logging when checking bundle status.
     */
    @Parameter(property = "osgi.bundle.status.quiet", defaultValue = "false")
    private boolean quiet;

    /**
     * Read timeout in milliseconds.  Defaults to 0 (infinite).
     */
    @Parameter(defaultValue = "0")
    private Integer readTimeout;

    /**
     * Connection timeout in milliseconds.  Defaults to 0 (infinite).
     */
    @Parameter(defaultValue = "0")
    private Integer connectTimeout;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execution per configuration.");
        } else {
            final BundleStatusChecker checker = new FelixBundleStatusChecker(this);

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

    public Integer getPort() {
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

    public String getUsername() {
        return username;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public boolean isSecure() {
        return secure;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public String getBundlesJsonPath() {
        return (contextPath == null ? "" : contextPath) + path + PATH_SUFFIX;
    }
}
