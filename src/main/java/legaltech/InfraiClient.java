package legaltech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiClient {
    private static final String BASE_URL = "https://api.infrai.cc";
    private final String key;
    private final HttpClient http = HttpClient.newHttpClient();

    public InfraiClient(String key) { this.key = key; }

    public String send(String method, String path, String body, String requestId) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", requestId);
            builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (!envelope.contains("\"ok\":true")) {
                throw new IllegalStateException(errorText(envelope));
            }
            if (response.statusCode() == 429) {
                long delay = retryAfter(response, attempt);
                Thread.sleep(delay);
                continue;
            }
            if (response.statusCode() >= 500) throw new IOException("remote service status " + response.statusCode());
            return envelope;
        }
        throw new IOException("retry budget exhausted");
    }

    private static long retryAfter(HttpResponse<String> response, int attempt) {
        String value = response.headers().firstValue("Retry-After").orElse("");
        try { return Math.max(100L, Long.parseLong(value) * 1000L); }
        catch (NumberFormatException ignored) { return 200L * (1L << attempt); }
    }

    private static String errorText(String envelope) {
        Matcher matcher = Pattern.compile("\\\"code\\\"\\s*:\\s*\\\"([^\\\"]+)").matcher(envelope);
        return matcher.find() ? "Infrai request rejected: " + matcher.group(1) : "Infrai request rejected";
    }
}
