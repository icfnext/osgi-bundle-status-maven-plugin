package com.icfolson.maven.plugins.osgibundlestatus.checker

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException

interface BundleStatusChecker {

    void checkStatus(String bundleSymbolicName) throws MojoExecutionException, MojoFailureException
}