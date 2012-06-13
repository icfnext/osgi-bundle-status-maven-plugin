package com.citytechinc.cqlibrary.bundlestatusplugin

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log

import spock.lang.Specification

class FelixBundleStatusCheckerTest extends Specification {

    static def JSON = [data: [[symbolicName: 'foo', state:'Active'], [symbolicName: 'bar', state:'Resolved']]]

    def "check active bundle"() {
        setup:
        def mojo = mockMojo(5)
        def restClient = mockRestClient(0, JSON)

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('foo')

        then:
        notThrown(MojoFailureException)
    }

    def "check resolved bundle"() {
        setup:
        def mojo = mockMojo(5)
        def restClient = mockRestClient(5, JSON)

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('bar')

        then:
        thrown(MojoFailureException)
    }

    def "check nonexistent bundle"() {
        setup:
        def mojo = mockMojo(5)
        def restClient = mockRestClient(0, JSON)

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('other')

        then:
        thrown(MojoFailureException)
    }

    def mockMojo(retryLimit) {
        def mojo = Mock(OsgiBundleStatusPluginMojo)

        mojo.host >> 'localhost'
        mojo.port >> '4502'
        mojo.user >> 'admin'
        mojo.password >> 'admin'
        mojo.retryDelay >> 100
        mojo.retryLimit >> retryLimit
        mojo.log >> Mock(Log)

        mojo
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