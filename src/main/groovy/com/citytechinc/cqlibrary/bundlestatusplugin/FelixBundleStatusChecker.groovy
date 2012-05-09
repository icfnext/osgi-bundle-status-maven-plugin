package com.citytechinc.cqlibrary.bundlestatusplugin

import groovy.json.JsonSlurper

import groovyx.net.http.RESTClient

import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException

final class FelixBundleStatusChecker {

    static final String STATUS_ACTIVE = "Active"

    private final def log

    private final def http

    FelixBundleStatusChecker(Mojo mojo, host, port, user, password) {
        log = mojo.log

        log.info "Connecting to Felix Console : $host:$port"

        http = new RESTClient("http://${host}:${port}")

        http.client.addRequestInterceptor(new HttpRequestInterceptor() {
            void process(HttpRequest httpRequest, HttpContext httpContext) {
                httpRequest.addHeader("Authorization", "Basic " + "${user}:${password}".toString().bytes.encodeBase64().toString())
            }
        })
    }

    void checkStatus(bundleSymbolicName) throws MojoExecutionException, MojoFailureException {
        try {
            def status = getStatus(bundleSymbolicName)

            if (STATUS_ACTIVE.equals(status)) {

            } else {
                throw new MojoFailureException("Bundle " + bundleSymbolicName + " is not active")
            }
        } catch (IOException ioe) {
            throw new MojoExecutionException("Error getting bundle status from Felix Console", ioe)
        }
    }

    private String getStatus(bundleSymbolicName) throws MojoExecutionException, MojoFailureException, IOException {
        def status = ''

        log.info "Checking OSGi bundle status : $bundleSymbolicName"

        http.get(path: "/system/console/bundles/.json") { response, json ->
            if (json) {
                def data = json.data

                def bundle = data.find { it.symbolicName == bundleSymbolicName }

                if (bundle) {
                    status = bundle.state

                    log.info "$bundleSymbolicName is $status"
                } else {
                    throw new MojoFailureException("Bundle not found : $bundleSymbolicName")
                }
            } else {
                throw new MojoExecutionException('Error getting JSON response from Felix Console')
            }
        }

        status
    }
}