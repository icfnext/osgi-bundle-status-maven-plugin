package com.citytechinc.maven.plugins.osgibundlestatus.checker

import com.citytechinc.maven.plugins.osgibundlestatus.OsgiBundleStatusPluginMojo
import groovyx.net.http.RESTClient
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException

class FelixBundleStatusChecker implements BundleStatusChecker {

    OsgiBundleStatusPluginMojo mojo

    RESTClient restClient

    def json

    FelixBundleStatusChecker(OsgiBundleStatusPluginMojo mojo) {
        this.mojo = mojo

        def scheme = mojo.secure ? "https" : "http"

        restClient = new RESTClient("$scheme://${mojo.host}:${mojo.port}")

        restClient.client.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            void process(HttpRequest httpRequest, HttpContext httpContext) {
                httpRequest.addHeader("Authorization",
                    "Basic " + "${mojo.username}:${mojo.password}".toString().bytes.encodeBase64().toString())
            }
        })
    }

    @Override
    void checkStatus(String bundleSymbolicName) throws MojoExecutionException, MojoFailureException {
        if (!mojo.quiet) {
            mojo.log.info "Checking OSGi bundle status: $bundleSymbolicName"
        }

        def requiredStatus = mojo.requiredStatus

        try {
            def status = getStatus(bundleSymbolicName)

            if (requiredStatus == status) {
                if (!mojo.quiet) {
                    mojo.log.info "$bundleSymbolicName is $status"
                }
            } else {
                def msg

                if (status) {
                    msg = "$bundleSymbolicName bundle status required to be $requiredStatus but is $status"
                } else {
                    msg = "Bundle not found: $bundleSymbolicName"
                }

                throw new MojoFailureException(msg)
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error getting bundle status from Felix Console", e)
        }
    }

    private def getStatus(String bundleSymbolicName) {
        def status = getRemoteBundleStatusQuiet(bundleSymbolicName, false)

        def retryCount = 0

        def requiredStatus = mojo.requiredStatus
        def retryLimit = mojo.retryLimit
        def retryDelay = mojo.retryDelay

        while (requiredStatus != status && retryCount < retryLimit) {
            if (!mojo.quiet) {
                if (status) {
                    mojo.log.info "Bundle is $status, retrying..."
                } else {
                    mojo.log.info "Bundle not found, retrying..."
                }
            }

            try{
				status = getRemoteBundleStatusQuiet(bundleSymbolicName, true);
            }catch (Exception e){
				if(retryCount == retryLimit - 1){
					throw e;
				}else{
					mojo.log.info("Error getting bundle status from Felix Console", e)
				}
            }
			
            Thread.sleep(retryDelay)

            retryCount++
        }

        status
    }

    private def getRemoteBundleStatusQuiet(String bundleSymbolicName, boolean force) {
        try {
            return getRemoteBundleStatus(bundleSymbolicName, force)
        } catch (MojoExecutionException ex) {
            mojo.log.info "Failed to get remote status, retrying..."
            mojo.log.debug ex
        }
    }

    private def getRemoteBundleStatus(String bundleSymbolicName, boolean force) {
        if (!json || force) {
            json = getBundleStatusJson()
        }

        def bundle = json.find { it.symbolicName == bundleSymbolicName }

        bundle?.state
    }

    private def getBundleStatusJson() throws MojoExecutionException {
        def bundleStatusJson = null

        try {
            restClient.get(path: mojo.bundlesJsonPath) { response, json ->
                if (json) {
                    def data = json.data

                    if (data) {
                        bundleStatusJson = data
                    } else {
                        throw new MojoExecutionException("Invalid JSON response from Felix Console")
                    }
                } else {
                    throw new MojoExecutionException("Error getting JSON response from Felix Console")
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Error getting JSON response from Felix Console", ex)
        }

        bundleStatusJson
    }
}