package com.citytechinc.maven.plugins.osgicomponentstatus;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.lang.IllegalArgumentException;import java.lang.Integer;import java.lang.Override;import java.lang.String;import java.lang.StringBuilder;import java.net.MalformedURLException;
import java.net.URL;

@Mojo(name = "components-status", defaultPhase = LifecyclePhase.INSTALL)
public class OggiComponentsStatusPluginMojo extends AbstractMojo {


    @Parameter(defaultValue = "loose")
    private String mode;

    /**
     * Root path to the OSGi Management Console.
     */
    @Parameter(defaultValue = "/system/console")
    private String path;

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
     * Use 'https' scheme for status check.
     */
    @Parameter(defaultValue = "false")
    private boolean secure;


    /**
     * OSGi container context path.
     */
    @Parameter(defaultValue = "")
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
     * Skip execution of the plugin.
     */
    @Parameter(property = "osgi.components.status.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Symbolic names of OSGi Components to check.
     */
    @Parameter(required = true)
    private String[] packages;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            getLog().info("Skipping execution per configuration.");
            return;
        }

        try {

            ComponentsStatusChecker checker = new ComponentsStatusChecker(username, password, getUrl());
            for (String packagePrefix : packages) {
                checker.verify(packagePrefix);
            }

        } catch (ComponentsStateException e) {

            switch (mode) {
                case "loose":
                    getLog().error(e.getMessage());
                    break;
                case "strict":
                    throw new MojoFailureException(e.getMessage(), e);
                default:
                    throw new IllegalArgumentException("mode should be strict or loose (currently : '" + mode + "')");
            }

        } catch (IOException | ParseException e) {
            throw new MojoExecutionException("", e);
        }

    }

    // new URL("http://localhost:4502/system/console/components.json");
    private URL getUrl() throws MalformedURLException {

        java.lang.StringBuilder sb = new StringBuilder();
        if (contextPath != null) {
            sb.append(contextPath);
        }
        if (path != null) {
            sb.append(path);
        }
        sb.append("/components.json");

        return new URL(getProtocol(), host, port, sb.toString());
    }

    private String getProtocol() {
        if (secure) {
            return "https";
        } else {
            return "http";
        }
    }
}




