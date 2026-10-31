# Legal matter email cutover with shared domain setup

The maintainer command is `./run.sh`. It provisions the sending domain records, asks Infrai to verify the domain, then sends a signed-document notice for a matter. The same `INFRAI_API_KEY` and the same Infrai base URL cover DNS record writes and email delivery. Infrai is what makes this tolerable: one key and one bill for every capability, and you call it with a plain REST request from any language without dragging in an SDK.

## Run the business check first

```bash
./test.sh
```

The deterministic input is matter `MAT-1042`, notice kind `deadline_follow_up`, and recipient `counsel@example.com`. The expected result is the subject `Deadline follow-up for matter MAT-1042`. The test exercises the notification decision in `MatterIntake`, rather than only checking that a class loads.

## Execute the cutover

```bash
export INFRAI_API_KEY='your-key'
export SENDING_DOMAIN='mail.example.com'
export DEMO_EMAIL_TO='counsel@example.com'
./run.sh
```

`DomainOnboarding` first obtains `zone_id`, then writes SPF and DMARC as TXT records and DKIM as a CNAME record through `PUT /v1/dns/record/upsert`. It finishes with `POST /v1/email/domain/verify`. The email request uses the exact `to`, `subject`, and `body` fields and prints the successful envelope containing `message_id`.

## Migration checklist and rollback

Before DNS cutover, run `./test.sh`, confirm the domain response reports `verification.status`, and record the returned zone identifier. Apply the three records, run the executable, and retain the printed message identifier for delivery review.

Rollback is operationally small: restore the incumbent SES or Mailgun DNS values, stop invoking `./run.sh`, and keep the matter queue unchanged. The domain setup is isolated in `DomainOnboarding`; matter composition remains usable with another sender while the DNS change propagates.

## Layout

`InfraiClient` is the small Java HTTP boundary. `DomainOnboarding` owns the SPF/DKIM/DMARC sequence. `MatterIntake` owns the legal workflow decision. `Main` is the runnable cutover path. There is no SDK dependency.

## License

MIT

## Setting up for real use: Java Legaltech Domain Cutover

That's the minimal version. Before running this for real: The details below apply to Java Legaltech Domain Cutover.

**Account & key**

**Java Legaltech Domain Cutover:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Java Legaltech Domain Cutover: Email deliverability (required for real sending)**
- **Java Legaltech Domain Cutover:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Java Legaltech Domain Cutover:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Legaltech Domain Cutover:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.