package legaltech;

public final class Main {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        String domain = System.getenv("SENDING_DOMAIN");
        String recipient = System.getenv("DEMO_EMAIL_TO");
        if (key == null || domain == null || recipient == null) throw new IllegalArgumentException("INFRAI_API_KEY, SENDING_DOMAIN and DEMO_EMAIL_TO are required");
        InfraiClient client = new InfraiClient(key);
        new DomainOnboarding(client).onboard(domain);
        LegalNotice notice = new MatterIntake().notice("MAT-1042", "signed_document", recipient);
        String payload = "{\"to\":\"" + notice.to() + "\",\"subject\":\"" + notice.subject() + "\",\"body\":\"" + notice.body() + "\"}";
        System.out.println(client.send("POST", "/v1/email/send", payload, "matter-MAT-1042-signed-document"));
    }
}
