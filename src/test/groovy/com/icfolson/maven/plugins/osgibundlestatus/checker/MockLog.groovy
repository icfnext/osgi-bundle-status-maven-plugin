package com.icfolson.maven.plugins.osgibundlestatus.checker

import com.icfolson.maven.plugins.osgibundlestatus.OsgiBundleStatusPluginMojo
import org.apache.maven.plugin.logging.Log
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MockLog implements Log {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiBundleStatusPluginMojo)

    @Override
    boolean isDebugEnabled() {
        true
    }

    @Override
    void debug(CharSequence content) {
        LOG.debug(content as String)
    }

    @Override
    void debug(CharSequence content, Throwable error) {
        LOG.debug(content as String, error)
    }

    @Override
    void debug(Throwable error) {
        LOG.debug("", error)
    }

    @Override
    boolean isInfoEnabled() {
        true
    }

    @Override
    void info(CharSequence content) {
        LOG.info(content as String)
    }

    @Override
    void info(CharSequence content, Throwable error) {
        LOG.info(content as String, error)
    }

    @Override
    void info(Throwable error) {
        LOG.info("", error)
    }

    @Override
    boolean isWarnEnabled() {
        true
    }

    @Override
    void warn(CharSequence content) {
        LOG.warn(content as String)
    }

    @Override
    void warn(CharSequence content, Throwable error) {
        LOG.warn(content as String, error)
    }

    @Override
    void warn(Throwable error) {
        LOG.warn("", error)
    }

    @Override
    boolean isErrorEnabled() {
        true
    }

    @Override
    void error(CharSequence content) {
        LOG.error(content as String)
    }

    @Override
    void error(CharSequence content, Throwable error) {
        LOG.error(content as String, error)
    }

    @Override
    void error(Throwable error) {
        LOG.error("", error)
    }
}
