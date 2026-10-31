package legaltech;

public final class MatterIntakeTest {
    public static void main(String[] args) {
        LegalNotice notice = new MatterIntake().notice("MAT-1042", "deadline_follow_up", "counsel@example.com");
        if (!notice.subject().equals("Deadline follow-up for matter MAT-1042")) throw new AssertionError(notice);
        if (!notice.body().contains("next deadline")) throw new AssertionError(notice);
        System.out.println("MatterIntakeTest passed");
    }
}
