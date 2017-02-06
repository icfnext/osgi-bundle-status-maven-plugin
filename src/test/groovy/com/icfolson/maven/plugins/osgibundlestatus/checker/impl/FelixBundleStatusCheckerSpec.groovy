package com.icfolson.maven.plugins.osgibundlestatus.checker.impl

import com.icfolson.maven.plugins.osgibundlestatus.OsgiBundleStatusPluginMojo
import groovy.json.JsonBuilder
import net.jadler.Jadler
import org.apache.maven.plugin.MojoFailureException
import spock.lang.Specification

class FelixBundleStatusCheckerSpec extends Specification {

    private static final def JSON = [
        data: [
            [symbolicName: "foo", state: "Active", version: "1.0.0"],
            [symbolicName: "bar", state: "Resolved"],
        ]
    ]

    def setup() {
        Jadler.initJadlerListeningOn(8888)
    }

    def cleanup() {
        Jadler.closeJadler()
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

    def "active bundle with version"() {
        setup:
        setupMockServer(JSON)

        def checker = setupChecker()

        when:
        checker.checkStatus("foo;1.0.0")

        then:
        notThrown(MojoFailureException)
        verifyRequests(1)
    }

    def "active bundle with invalid version"() {
        setup:
        setupMockServer(JSON)

        def checker = setupChecker()

        when:
        checker.checkStatus("foo;1.0.1")

        then:
        thrown(MojoFailureException)
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
        thrown(MojoFailureException)
        verifyRequests(6)
    }

    def setupMockServer(json) {
        Jadler.onRequest()
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

    def setupChecker(status, limit) {
        def mojo = new OsgiBundleStatusPluginMojo()

        mojo.with {
            host = "localhost"
            port = 8888
            contextPath = ""
            path = "/system/console"
            username = "admin"
            password = "admin"
            requiredStatus = status
            retryDelay = 1
            retryLimit = limit
            log = new MockLog()
        }

        new FelixBundleStatusChecker(mojo)
    }

    void verifyRequests(int times) {
        Jadler.verifyThatRequest()
            .havingPathEqualTo("/system/console/bundles/.json")
            .receivedTimes(times)
    }
}