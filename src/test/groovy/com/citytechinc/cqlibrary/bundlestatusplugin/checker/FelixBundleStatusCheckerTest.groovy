package com.citytechinc.cqlibrary.bundlestatusplugin.checker

import com.citytechinc.cqlibrary.bundlestatusplugin.OsgiBundleStatusPluginMojo

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log

import spock.lang.Specification

class FelixBundleStatusCheckerTest extends Specification {

    static def JSON = [data: [[symbolicName: 'foo', state:'Active'], [symbolicName: 'bar', state:'Resolved']]]

    def "active bundle"() {
        setup:
        def restClient = createMockRestClient(0, JSON)
        def mojo = createMockMojo()

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('foo')

        then:
        notThrown(MojoFailureException)
    }

    def "custom bundle status success"() {
        setup:
        def json = [data: [[symbolicName: 'foo', state:'Custom']]]

        def restClient = createMockRestClient(0, json)
        def mojo = createMockMojo('Custom', 5)

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('foo')

        then:
        notThrown(MojoFailureException)
    }

    def "custom bundle status failure"() {
        setup:
        def restClient = createMockRestClient(5, JSON)
        def mojo = createMockMojo('Custom', 5)

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('foo')

        then:
        thrown(MojoFailureException)
    }

    def "resolved bundle 5 retries"() {
        setup:
        def restClient = createMockRestClient(5, JSON)
        def mojo = createMockMojo()

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('bar')

        then:
        thrown(MojoFailureException)
    }

    def "resolved bundle 10 retries"() {
        setup:
        def restClient = createMockRestClient(10, JSON)
        def mojo = createMockMojo('Active', 10)

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('bar')

        then:
        thrown(MojoFailureException)
    }

    def "nonexistent bundle"() {
        setup:
        def restClient = createMockRestClient(5, JSON)
        def mojo = createMockMojo()

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('other')

        then:
        thrown(MojoFailureException)
    }

    def "multiple bundles, fails on first"() {
        setup:
        def restClient = createMockRestClient(5, JSON)
        def mojo = createMockMojo()

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('bar')
        checker.checkStatus('foo')

        then:
        thrown(MojoFailureException)
    }

    def "multiple bundles, fails on second"() {
        setup:
        def restClient = createMockRestClient(6, JSON)
        def mojo = createMockMojo()

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('foo')
        checker.checkStatus('bar')

        then:
        thrown(MojoFailureException)
    }

    def "empty response"() {
        setup:
        def restClient = createMockRestClient(0, [:])
        def mojo = createMockMojo()

        def checker = new FelixBundleStatusChecker(mojo, restClient)

        when:
        checker.checkStatus('other')

        then:
        thrown(MojoExecutionException)
    }

    def createMockRestClient(expectedRetryCount, json) {
        def restClient = Mock(RESTClient)

        def response = Mock(HttpResponseDecorator)

        (1 + expectedRetryCount) * restClient.get(_, _) >> { url, closure ->
            closure.call(response, json)
        }

        restClient
    }

    def createMockMojo() {
        createMockMojo('Active', 5)
    }

    def createMockMojo(requiredStatus, retryLimit) {
        def mojo = Mock(OsgiBundleStatusPluginMojo)

        mojo.host >> 'localhost'
        mojo.port >> '4502'
        mojo.user >> 'admin'
        mojo.password >> 'admin'
        mojo.requiredStatus >> requiredStatus
        mojo.retryDelay >> 1
        mojo.retryLimit >> retryLimit
        mojo.log >> Mock(Log)

        mojo
    }
}