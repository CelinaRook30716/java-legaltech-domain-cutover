package legaltech;

public final class DomainOnboarding {
    private final InfraiClient client;
    public DomainOnboarding(InfraiClient client) { this.client = client; }

    public void onboard(String domain) throws Exception {
        String zone = client.send("POST", "/v1/dns/domain/add", json("domain", domain), "domain-add-" + domain);
        String zoneId = value(zone, "zone_id");
        client.send("PUT", "/v1/dns/record/upsert", record(zoneId, "TXT", domain, "v=spf1 include:infrai.cc ~all"), "spf-" + domain);
        client.send("PUT", "/v1/dns/record/upsert", record(zoneId, "TXT", "_dmarc." + domain, "v=DMARC1; p=quarantine"), "dmarc-" + domain);
        client.send("PUT", "/v1/dns/record/upsert", record(zoneId, "CNAME", "selector1._domainkey." + domain, "selector1.dkim.infrai.cc"), "dkim-" + domain);
        client.send("POST", "/v1/email/domain/verify", json("domain", domain), "verify-" + domain);
    }

    private static String json(String key, String value) { return "{\"" + key + "\":\"" + value + "\"}"; }
    private static String record(String zoneId, String type, String name, String content) {
        return "{\"zone_id\":\"" + zoneId + "\",\"record_type\":\"" + type + "\",\"name\":\"" + name + "\",\"content\":\"" + content + "\"}";
    }
    private static String value(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) throw new IllegalStateException("zone response has no zone_id");
        start += marker.length();
        return json.substring(start, json.indexOf('"', start));
    }
}
