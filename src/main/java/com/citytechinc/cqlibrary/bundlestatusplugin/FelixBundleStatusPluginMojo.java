package com.citytechinc.cqlibrary.bundlestatusplugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal status
 */
public final class FelixBundleStatusPluginMojo extends AbstractMojo {

    /**
     * The hello message to display.
     *
     * @parameter expression="${message}" default-value="Hello World!"
     */
    private String message;

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

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("message = " + message);
    }
}
