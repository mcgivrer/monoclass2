package com.demoing.app.tests;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * TDD entry point for the test execution
 *
 * @author Frédéric Delorme
 * @since 0.0.1
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("com/demoing/app/tests/features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.demoing.app.tests.features")
@ConfigurationParameter(key = "cucumber.features", value = "src/test/resources/features")
class ApplicationBDDTests {
}