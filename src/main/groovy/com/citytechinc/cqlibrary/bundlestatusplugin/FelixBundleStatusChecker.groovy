package com.citytechinc.cqlibrary.bundlestatusplugin

import groovy.json.JsonSlurper

import groovyx.net.http.RESTClient

import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext

import org.apache.maven.plugin.Mojo

class FelixBundleStatusChecker {

    private final def log

    FelixBundleStatusChecker(Mojo mojo) {
        log = mojo.log
    }

    String getStatus(host, port, user, password, bundleSymbolicName) {
        def url = "http://${host}:${port}"

        log.info "Checking OSGi bundle status for $bundleSymbolicName"

        def http = new RESTClient("http://${host}:${port}")

        log.info "Connecting to Felix console at $host:$port"

        http.client.addRequestInterceptor(new HttpRequestInterceptor() {
            void process(HttpRequest httpRequest, HttpContext httpContext) {
                httpRequest.addHeader("Authorization", "Basic " + "${user}:${password}".toString().bytes.encodeBase64().toString())
            }
        })

        def status = ''

        http.get(path: "/system/console/bundles/.json") { response, json ->
            if (json) {
                def data = json.data

                def bundle = data.find { it.symbolicName == bundleSymbolicName }

                if (bundle) {
                    status = bundle.state

                    log.info "$bundleSymbolicName is $status"
                } else {
                    log.info "no bundle found with symbolic name = $bundleSymbolicName"
                }
            } else {
                log.info 'error getting response from felix console'
            }
        }

        status
    }
}