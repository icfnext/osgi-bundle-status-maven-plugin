package com.citytechinc.cqlibrary.bundlestatusplugin

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log

import spock.lang.Specification

class FelixBundleStatusCheckerTest extends Specification {

    static def JSON = [data: [[symbolicName: 'foo', state:'Active'], [symbolicName: 'bar', state:'Resolved']]]

    def mojo

    def setup() {
        mojo = Mock(OsgiBundleStatusPluginMojo)
        mojo.host >> 'localhost'
        mojo.port >> '4502'
        mojo.user >> 'admin'
        mojo.password >> 'admin'
        mojo.requiredStatus >> 'Active'
        mojo.retryDelay >> 100
        mojo.retryLimit >> 5
        mojo.log >> Mock(Log)
    }

    def "active bundle"() {
        setup:
        def restClient = mockRestClient(0, JSON)

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('foo')

        then:
        notThrown(MojoFailureException)
    }

    def "resolved bundle"() {
        setup:
        def restClient = mockRestClient(5, JSON)

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('bar')

        then:
        thrown(MojoFailureException)
    }

    def "nonexistent bundle"() {
        setup:
        def restClient = mockRestClient(0, JSON)

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('other')

        then:
        thrown(MojoFailureException)
    }

    def "empty response"() {
        setup:
        def restClient = mockRestClient(0, [:])

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('other')

        then:
        thrown(MojoExecutionException)
    }

    def mockRestClient(expectedRetryCount, json) {
        def restClient = Mock(RESTClient)

        def response = Mock(HttpResponseDecorator)

        (1 + expectedRetryCount) * restClient.get(_, _) >> {
            it[1].call(response, json)
        }

        restClient
    }
}