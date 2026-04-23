package com.example.hl7poc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "fhir.client")
public class FhirClientProperties {

    public enum Mode {
        LOCAL,
        AWS
    }

    private Mode mode = Mode.LOCAL;
    private String localBaseUrl = "http://localhost:8080/fhir";
    private String awsBaseUrl = "";
    private String awsRegion = "us-east-1";
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getLocalBaseUrl() {
        return localBaseUrl;
    }

    public void setLocalBaseUrl(String localBaseUrl) {
        this.localBaseUrl = localBaseUrl;
    }

    public String getAwsBaseUrl() {
        return awsBaseUrl;
    }

    public void setAwsBaseUrl(String awsBaseUrl) {
        this.awsBaseUrl = awsBaseUrl;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String activeBaseUrl() {
        if (mode == Mode.AWS) {
            return awsBaseUrl;
        }
        return localBaseUrl;
    }
}
