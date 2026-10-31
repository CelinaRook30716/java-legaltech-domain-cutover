package legaltech;

public final class MatterIntake {
    public LegalNotice notice(String matterId, String kind, String recipient) {
        String subject;
        String body;
        switch (kind) {
            case "signed_document" -> { subject = "Signed document for matter " + matterId; body = "The signed document is ready for matter " + matterId + "."; }
            case "deadline_follow_up" -> { subject = "Deadline follow-up for matter " + matterId; body = "Please review the next deadline for matter " + matterId + "."; }
            default -> { subject = "Matter intake received: " + matterId; body = "We received the intake for matter " + matterId + "."; }
        }
        return new LegalNotice(recipient, subject, body);
    }
}
