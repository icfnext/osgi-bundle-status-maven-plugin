package com.citytechinc.maven.plugins.osgibundlestatus.checker

import groovyx.net.http.RESTClient
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException

class FelixBundleStatusChecker implements BundleStatusChecker {

    final def host

    final def port

    final def username

    final def password

    final def retryDelay

    final def retryLimit

    final def requiredStatus

    final def log

    final def restClient

    def json

    FelixBundleStatusChecker(mojo) {
        host = mojo.host
        port = mojo.port
        username = mojo.username
        password = mojo.password
        retryDelay = mojo.retryDelay
        retryLimit = mojo.retryLimit
        requiredStatus = mojo.requiredStatus
        log = mojo.log

        restClient = new RESTClient("http://$host:$port")

        restClient.client.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            void process(HttpRequest httpRequest, HttpContext httpContext) {
                httpRequest.addHeader("Authorization", "Basic " + "$username:$password".toString().bytes.encodeBase64().toString())
            }
        })
    }

    FelixBundleStatusChecker(mojo, restClient) {
        host = mojo.host
        port = mojo.port
        username = mojo.username
        password = mojo.password
        retryDelay = mojo.retryDelay
        retryLimit = mojo.retryLimit
        requiredStatus = mojo.requiredStatus
        log = mojo.log

        this.restClient = restClient
    }

    @Override
    void checkStatus(String bundleSymbolicName) throws MojoExecutionException, MojoFailureException {
        log.info "Checking OSGi bundle status: $bundleSymbolicName"

        try {
            def status = getStatus(bundleSymbolicName)

            if (requiredStatus == status) {
                log.info "$bundleSymbolicName is $status"
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

    def getStatus(String bundleSymbolicName) {
        def status = getRemoteBundleStatus(bundleSymbolicName, false)

        def retryCount = 0

        while (requiredStatus != status && retryCount < retryLimit) {
            if (status) {
                log.info "Bundle is $status, retrying..."
            } else {
                log.info "Bundle not found, retrying..."
            }

            status = getRemoteBundleStatus(bundleSymbolicName, true)

            Thread.sleep(retryDelay)

            retryCount++
        }

        status
    }

    def getRemoteBundleStatus(String bundleSymbolicName, boolean force) {
        def status = null

        if (!json || force) {
            json = getBundleStatusJson()
        }

        def bundle = json.find { it.symbolicName == bundleSymbolicName }

        if (bundle) {
            status = bundle.state
        }

        status
    }

    def getBundleStatusJson() {
        def bundleStatusJson = null

        restClient.get(path: "/system/console/bundles/.json") { response, json ->
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

        bundleStatusJson
    }
}