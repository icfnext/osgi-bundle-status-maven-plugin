package com.citytechinc.maven.plugins.osgibundlestatus.checker

import com.citytechinc.maven.plugins.osgibundlestatus.OsgiBundleStatusPluginMojo
import com.citytechinc.maven.plugins.osgibundlestatus.models.Bundle
import com.citytechinc.maven.plugins.osgibundlestatus.models.BundleStatus
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientHandlerException
import com.sun.jersey.api.client.UniformInterfaceException
import com.sun.jersey.api.client.WebResource
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.shared.osgi.DefaultMaven2OsgiConverter
import org.apache.maven.shared.osgi.Maven2OsgiConverter
import org.osgi.framework.Version

class FelixBundleStatusChecker implements BundleStatusChecker {

    private static final ObjectMapper MAPPER = new ObjectMapper()

    private final Maven2OsgiConverter maven2OsgiConverter = new DefaultMaven2OsgiConverter()

    private final OsgiBundleStatusPluginMojo mojo

    private final WebResource resource

    private List<Bundle> bundles

    FelixBundleStatusChecker(OsgiBundleStatusPluginMojo mojo) {
        this.mojo = mojo

        def scheme = mojo.secure ? "https" : "http"

        resource = client.resource("$scheme://${mojo.host}:${mojo.port}").path(mojo.bundlesJsonPath)
    }

    @Override
    void checkStatus(String bundleName) throws MojoExecutionException, MojoFailureException {
        def expectedBundle = Bundle.fromSymbolicName(bundleName)

        if (!mojo.quiet) {
            mojo.log.info("Checking OSGi bundle status: ${expectedBundle.symbolicName}")
        }

        def remoteBundle = checkBundleStatus(expectedBundle)

        if (expectedBundle.version) {
            checkBundleVersion(expectedBundle, remoteBundle)
        }
    }

    private Bundle checkBundleStatus(Bundle bundle) {
        def remoteBundle = getBundle(bundle.symbolicName)

        if (remoteBundle) {
            def status = remoteBundle.state

            if (mojo.requiredStatus == status) {
                if (!mojo.quiet) {
                    mojo.log.info("${bundle.symbolicName} is $status")
                }
            } else {
                throw new MojoFailureException("${bundle.symbolicName} bundle status required to be " +
                    "${mojo.requiredStatus} but is $status")
            }
        } else {
            throw new MojoFailureException("Bundle not found: ${bundle.symbolicName}")
        }

        remoteBundle
    }

    private void checkBundleVersion(Bundle expected, Bundle actual) {
        if (!mojo.quiet) {
            mojo.log.info("Checking for expected version ${expected.version} of OSGi bundle ${expected.symbolicName}")
        }

        def expectedVersion = Version.parseVersion(maven2OsgiConverter.getVersion(expected.version))
        def actualVersion = Version.parseVersion(actual.version)

        if (expectedVersion != actualVersion) {
            throw new MojoFailureException(
                "Expected version ${expected.version} of ${expected.symbolicName} does not match actual version " +
                    "$actualVersion")
        }
    }

    private Bundle getBundle(String bundleSymbolicName) {
        def bundle = getRemoteBundle(bundleSymbolicName, false)

        def retryCount = 0

        def requiredStatus = mojo.requiredStatus
        def retryLimit = mojo.retryLimit
        def retryDelay = mojo.retryDelay

        while ((!bundle || requiredStatus != bundle.state) && retryCount < retryLimit) {
            if (!mojo.quiet) {
                if (bundle) {
                    mojo.log.info("Bundle is ${bundle.state}, retrying...")
                } else {
                    mojo.log.info("Bundle not found, retrying...")
                }
            }

            try {
                bundle = getRemoteBundle(bundleSymbolicName, true)
            } catch (Exception e) {
                if (retryCount == retryLimit - 1) {
                    throw e
                } else {
                    mojo.log.info("Error getting bundle status from Felix Console", e)
                }
            }

            Thread.sleep(retryDelay)

            retryCount++
        }

        bundle
    }

    private Bundle getRemoteBundle(String bundleSymbolicName, boolean force) {
        def bundle = null

        try {
            if (!bundles || force) {
                bundles = remoteBundles
            }

            bundle = bundles.find { it.symbolicName == bundleSymbolicName }
        } catch (MojoExecutionException ex) {
            mojo.log.info("Failed to get remote status, retrying...")
            mojo.log.debug(ex)
        }

        bundle
    }

    private List<Bundle> getRemoteBundles() throws MojoExecutionException {
        def bundles

        try {
            def bundleStatus = resource.get(BundleStatus)

            bundles = bundleStatus.bundles
        } catch (IOException e) {
            throw new MojoExecutionException("Error getting JSON response from Felix Console", e)
        } catch (UniformInterfaceException e) {
            throw new MojoExecutionException("Error getting JSON response from Felix Console", e)
        } catch (ClientHandlerException e) {
            throw new MojoExecutionException("Error getting JSON response from Felix Console", e)
        }

        bundles
    }

    private Client getClient() {
        def clientConfig = new DefaultClientConfig()

        clientConfig.singletons.add(new JacksonJsonProvider(MAPPER))

        def client = Client.create(clientConfig)

        client.addFilter(new HTTPBasicAuthFilter(mojo.username, mojo.password))
        client.connectTimeout = mojo.connectTimeout
        client.readTimeout = mojo.readTimeout

        client
    }
}