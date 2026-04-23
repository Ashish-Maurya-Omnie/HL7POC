package com.example.hl7poc.client;

import com.example.hl7poc.config.FhirClientProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class FhirHttpClient {

    private static final String APPLICATION_FHIR_JSON = "application/fhir+json";

    private final HttpClient httpClient;
    private final FhirClientProperties properties;
    private final AwsCredentialsProvider credentialsProvider;
    private final Aws4Signer signer;

    public FhirHttpClient(
            HttpClient httpClient,
            FhirClientProperties properties,
            AwsCredentialsProvider credentialsProvider,
            Aws4Signer signer
    ) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.credentialsProvider = credentialsProvider;
        this.signer = signer;
    }

    public String get(String path, Map<String, String> queryParams) {
        return execute("GET", path, queryParams, null);
    }

    public String post(String path, String body) {
        return execute("POST", path, Map.of(), body);
    }

    private String execute(String method, String path, Map<String, String> queryParams, String body) {
        if (properties.activeBaseUrl() == null || properties.activeBaseUrl().isBlank()) {
            throw new IllegalArgumentException("FHIR base URL is not configured for mode: " + properties.getMode());
        }

        URI uri = buildUri(properties.activeBaseUrl(), path, queryParams);

        try {
            HttpRequest request = properties.getMode() == FhirClientProperties.Mode.AWS
                    ? buildSignedRequest(method, uri, body)
                    : buildLocalRequest(method, uri, body);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() / 100 != 2) {
                throw new FhirClientException(
                        "FHIR request failed with status " + response.statusCode() + ": " + response.body(),
                        response.statusCode()
                );
            }

            return response.body();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new FhirClientException("FHIR request failed: " + ex.getMessage(), 500, ex);
        } catch (IOException ex) {
            throw new FhirClientException("FHIR request failed: " + ex.getMessage(), 500, ex);
        }
    }

    private HttpRequest buildLocalRequest(String method, URI uri, String body) {
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);

        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(properties.getReadTimeout())
                .header("Accept", APPLICATION_FHIR_JSON)
                .method(method, publisher);

        if (body != null) {
            builder.header("Content-Type", APPLICATION_FHIR_JSON);
        }

        return builder.build();
    }

    private HttpRequest buildSignedRequest(String method, URI uri, String body) {
        SdkHttpMethod sdkMethod = SdkHttpMethod.fromValue(method);
        SdkHttpFullRequest.Builder requestBuilder = SdkHttpFullRequest.builder()
                .method(sdkMethod)
                .uri(uri)
                .putHeader("Accept", APPLICATION_FHIR_JSON);

        byte[] bytes = null;
        if (body != null) {
            bytes = body.getBytes(StandardCharsets.UTF_8);
            requestBuilder.putHeader("Content-Type", APPLICATION_FHIR_JSON);
            byte[] finalBytes = bytes;
            requestBuilder.contentStreamProvider(() -> new ByteArrayInputStream(finalBytes));
        }

        Aws4SignerParams params = Aws4SignerParams.builder()
                .awsCredentials(credentialsProvider.resolveCredentials())
                .signingName("healthlake")
                .signingRegion(Region.of(properties.getAwsRegion()))
                .build();

        SdkHttpFullRequest signedRequest = signer.sign(requestBuilder.build(), params);

        HttpRequest.BodyPublisher publisher = bytes == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(bytes);

        HttpRequest.Builder javaRequest = HttpRequest.newBuilder(signedRequest.getUri())
                .timeout(properties.getReadTimeout())
                .method(method, publisher);

        signedRequest.headers().forEach((name, values) -> {
            if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)) {
                return;
            }
            javaRequest.header(name, join(values));
        });

        return javaRequest.build();
    }

    private URI buildUri(String baseUrl, String path, Map<String, String> queryParams) {
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;

        StringBuilder builder = new StringBuilder(normalizedBase).append(normalizedPath);

        if (!queryParams.isEmpty()) {
            StringJoiner queryBuilder = new StringJoiner("&");
            queryParams.forEach((key, value) -> {
                if (value != null && !value.isBlank()) {
                    queryBuilder.add(encode(key) + "=" + encode(value));
                }
            });

            String queryString = queryBuilder.toString();
            if (!queryString.isBlank()) {
                builder.append("?").append(queryString);
            }
        }

        return URI.create(builder.toString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String join(List<String> values) {
        StringJoiner joiner = new StringJoiner(",");
        for (String value : values) {
            joiner.add(value);
        }
        return joiner.toString();
    }
}
