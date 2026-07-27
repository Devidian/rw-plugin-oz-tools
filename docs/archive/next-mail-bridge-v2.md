# Mail bridge API v2

## Objective

Add reusable reflection-only mailbox-capacity and trusted attachment methods
while preserving the v1 text-mail contract.

## Checklist

- [x] Add bridge attachment request/value records.
- [x] Preserve v1 method behavior.
- [x] Add focused bridge tests and documentation.
- [x] Run Maven test/package and consumer compatibility checks.

## Risks and rollback

The bridge remains optional and reflection-only. Missing v2 Mail endpoints
return `MAIL_UNAVAILABLE`; text mail remains unaffected.
