package com.citytechinc.maven.plugins.osgibundlestatus.models

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class Bundle {

    static Bundle fromSymbolicName(String symbolicName) {
        def tokens = symbolicName.tokenize(";")
        def version = null

        if (tokens.size() > 1) {
            version = tokens[1]
        }

        new Bundle(symbolicName: tokens[0], version: version)
    }

    Integer id

    String name

    String state

    String version

    String symbolicName


}
