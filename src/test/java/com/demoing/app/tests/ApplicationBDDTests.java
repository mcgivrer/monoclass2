package com.demoing.app.tests;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * TDD entry point for the test execution
 *
 * @author Frédéric Delorme
 * @since 0.0.1
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = { "com.demoing.app.tests.features" },
    plugin = {
        "html:target/cucumber-html-report",
        "json:target/cucumber.json",
        "pretty:target/cucumber-pretty.txt",
        "usage:target/cucumber-usage.json",
        "junit:target/cucumber-results.xml" },
    tags = "not @ignore")
class ApplicationBDDTests {
}