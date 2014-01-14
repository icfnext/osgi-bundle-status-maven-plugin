package com.citytechinc.maven.plugins.osgibundlestatus.checker

import com.citytechinc.maven.plugins.osgibundlestatus.OsgiBundleStatusPluginMojo
import groovy.json.JsonBuilder
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import spock.lang.Specification

import static net.jadler.Jadler.closeJadler
import static net.jadler.Jadler.initJadlerListeningOn
import static net.jadler.Jadler.onRequest
import static net.jadler.Jadler.verifyThatRequest

class FelixBundleStatusCheckerSpec extends Specification {

    static def JSON = [data: [[symbolicName: "foo", state: "Active"], [symbolicName: "bar", state: "Resolved"]]]

    def setup() {
        initJadlerListeningOn(4502)
    }

    def cleanup() {
        closeJadler()
    }

    def "active bundle"() {
        setup:
        setupMockServer(JSON)

        def checker = setupChecker()

        when:
        checker.checkStatus("foo")

        then:
        notThrown(MojoFailureException)
        verifyRequests(1)
    }

    def "custom bundle status success"() {
        setup:
        def json = [data: [[symbolicName: "foo", state: "Custom"]]]

        setupMockServer(json)

        def checker = setupChecker("Custom", 5)

        when:
        checker.checkStatus("foo")

        then:
        notThrown(MojoFailureException)
        verifyRequests(1)
    }

    def "custom bundle status failure"() {
        setup:
        setupMockServer(JSON)

        def checker = setupChecker("Custom", 5)

        when:
        checker.checkStatus("foo")

        then:
        thrown(MojoFailureException)
        verifyRequests(6)
    }

    def "resolved bundle 5 retries"() {
        setup:
        setupMockServer(JSON)

        def checker = setupChecker()

        when:
        checker.checkStatus("bar")

        then:
        thrown(MojoFailureException)
        verifyRequests(6)
    }

    def "resolved bundle 10 retries"() {
        setup:
        setupMockServer(JSON)

        def checker = setupChecker("Active", 10)

        when:
        checker.checkStatus("bar")

        then:
        thrown(MojoFailureException)
        verifyRequests(11)
    }

    def "nonexistent bundle"() {
        setup:
        setupMockServer(JSON)

        def checker = setupChecker()

        when:
        checker.checkStatus("other")

        then:
        thrown(MojoFailureException)
        verifyRequests(6)
    }

    def "multiple bundles"() {
        setup:
        def json = [data: [[symbolicName: "foo", state: "Active"], [symbolicName: "bar", state: "Active"]]]

        setupMockServer(json)

        def checker = setupChecker()

        when:
        checker.checkStatus("foo")
        checker.checkStatus("bar")

        then:
        notThrown(MojoFailureException)
        verifyRequests(1)
    }

    def "multiple bundles, fails on first"() {
        setup:
        setupMockServer(JSON)

        def checker = setupChecker()

        when:
        checker.checkStatus("bar")
        checker.checkStatus("foo")

        then:
        thrown(MojoFailureException)
        verifyRequests(6)
    }

    def "multiple bundles, fails on second"() {
        setup:
        setupMockServer(JSON)

        def checker = setupChecker()

        when:
        checker.checkStatus("foo")
        checker.checkStatus("bar")

        then:
        thrown(MojoFailureException)
        verifyRequests(6)
    }

    def "empty response"() {
        setup:
        setupMockServer([:])

        def checker = setupChecker()

        when:
        checker.checkStatus("other")

        then:
        thrown(MojoExecutionException)
        verifyRequests(1)
    }

    def setupMockServer(json) {
        onRequest()
            .havingMethodEqualTo("GET")
            .havingPathEqualTo("/system/console/bundles/.json")
            .respond()
            .withStatus(200)
            .withContentType("application/json")
            .withBody(new JsonBuilder(json).toString())
    }

    def setupChecker() {
        setupChecker("Active", 5)
    }

    def setupChecker(requiredStatus, retryLimit) {
        def mojo = Mock(OsgiBundleStatusPluginMojo)

        mojo.host >> "localhost"
        mojo.port >> 4502
        mojo.contextPath >> ""
        mojo.path >> "/system/console"
        mojo.username >> "admin"
        mojo.password >> "admin"
        mojo.requiredStatus >> requiredStatus
        mojo.retryDelay >> 1
        mojo.retryLimit >> retryLimit
        mojo.log >> Mock(Log)

        new FelixBundleStatusChecker(mojo)
    }

    void verifyRequests(int times) {
        verifyThatRequest()
            .havingPathEqualTo("/system/console/bundles/.json")
            .receivedTimes(times)
    }
}