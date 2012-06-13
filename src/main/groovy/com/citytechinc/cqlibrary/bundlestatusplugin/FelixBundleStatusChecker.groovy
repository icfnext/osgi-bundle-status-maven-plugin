package com.citytechinc.cqlibrary.bundlestatusplugin

import groovy.json.JsonSlurper

import groovyx.net.http.RESTClient

import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException

final class FelixBundleStatusChecker implements BundleStatusChecker {

    private final def mojo

    private final def restClient

    private final def log

    static FelixBundleStatusChecker createFromMojo(mojo) {
        def host = mojo.host
        def port = mojo.port

        mojo.log.info "Connecting to Felix Console @ $host:$port"

        def restClient = new RESTClient("http://$host:$port")

        restClient.client.addRequestInterceptor(new HttpRequestInterceptor() {
            void process(HttpRequest httpRequest, HttpContext httpContext) {
                httpRequest.addHeader("Authorization", "Basic " + "${mojo.user}:${mojo.password}".toString().bytes.encodeBase64().toString())
            }
        })

        new FelixBundleStatusChecker(mojo, restClient)
    }

    FelixBundleStatusChecker(mojo, restClient) {
        this.mojo = mojo
        this.restClient = restClient

        log = mojo.log
    }

    @Override
    void checkStatus(bundleSymbolicName) throws MojoExecutionException, MojoFailureException {
        log.info "Checking OSGi bundle status: $bundleSymbolicName"

        def requiredStatus = mojo.requiredStatus
        def delay = mojo.retryDelay
        def limit = mojo.retryLimit

        try {
            def status = ''

            def retryCount = 0

            while (requiredStatus != status && retryCount <= limit) {
                if (retryCount > 0) {
                    log.info "Bundle is $status, retrying..."
                }

                status = getStatus(bundleSymbolicName)

                Thread.sleep(delay)

                retryCount++
            }

            if (requiredStatus == status) {
                log.info "$bundleSymbolicName is $status"
            } else {
                throw new MojoFailureException("$bundleSymbolicName bundle status required to be $requiredStatus but is $status")
            }
        } catch (IOException ioe) {
            throw new MojoExecutionException("Error getting bundle status from Felix Console", ioe)
        }
    }

    private String getStatus(bundleSymbolicName) throws MojoExecutionException, MojoFailureException, IOException {
        def status = ""

        restClient.get(path: "/system/console/bundles/.json") { response, json ->
            if (json) {
                def data = json.data

                def bundle = data.find { it.symbolicName == bundleSymbolicName }

                if (bundle) {
                    status = bundle.state
                } else {
                    throw new MojoFailureException("Bundle not found : $bundleSymbolicName")
                }
            } else {
                throw new MojoExecutionException("Error getting JSON response from Felix Console")
            }
        }

        status
    }
}