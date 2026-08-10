# Grocery Shop Management System --- Development Rules

## 1. Purpose

These rules define how the project should be developed. They are
intended to keep AI-assisted development consistent, maintainable,
secure, and aligned with the Java Full Stack learning goal.

## 2. Primary Technology Rules

Use:

-   Java.
-   Spring Boot.
-   Spring Web.
-   Spring Data JPA.
-   Hibernate.
-   PostgreSQL.
-   Maven.
-   React.
-   Git/GitHub.

Do not replace the backend with Node.js, Python, PHP, or another backend
language.

Do not replace Spring Boot with another Java backend framework unless
explicitly decided later.

## 3. Frontend Rules

Use React for the frontend.

Prefer:

-   Functional components.
-   Hooks.
-   Reusable components where there is a genuine reuse case.
-   Simple state management initially.
-   Native browser APIs where sufficient.

Do not add Redux or another large state-management library unless the
application actually needs it.

Do not create excessively complicated component abstractions.

## 4. Backend Rules

Use the layered architecture:

``` text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Controllers must not contain substantial business logic.

Services should own business rules.

Repositories should handle persistence and database queries.

Do not put SQL/database operations directly inside controllers.

## 5. Java Rules

Use modern Java features appropriately.

Prefer:

-   Meaningful class and method names.
-   `BigDecimal` for money.
-   `Optional` where it improves clarity.
-   Streams when they improve readability.
-   Enums for fixed states/categories.
-   Immutable values where practical.

Do not use overly clever Java code merely to demonstrate language
features.

Code should be easy for a junior Java developer to understand.

## 6. Spring Rules

Use dependency injection.

Prefer constructor injection.

Avoid field injection unless there is a specific reason.

Use Spring annotations appropriately:

-   `@RestController`
-   `@Service`
-   `@Repository`
-   `@Entity`
-   `@Transactional`
-   Validation annotations
-   Security annotations when security is implemented

Do not add annotations without understanding why they are needed.

## 7. Database Rules

Use JPA/Hibernate for normal persistence.

Use custom queries only when necessary.

Money fields must use decimal types.

Do not store passwords as plain text.

Use foreign keys and appropriate constraints.

Avoid unnecessary database duplication.

Do not delete historical business records merely because a product is no
longer sold.

Prefer product deactivation where historical references exist.

## 8. API Rules

Use REST-style endpoints.

Use HTTP methods correctly:

``` text
GET     Read
POST    Create
PUT/PATCH Update
DELETE  Delete/deactivate
```

Use appropriate HTTP status codes.

Validate all client input on the backend.

Never trust frontend validation alone.

Return consistent error responses.

Do not expose stack traces, SQL errors, passwords, secrets, or internal
implementation details.

## 9. Error Handling

Use centralized exception handling.

Expected errors should be converted into useful API responses.

Examples:

-   Product not found.
-   Insufficient stock.
-   Invalid quantity.
-   Duplicate product/SKU when uniqueness is required.
-   Validation failure.
-   Unauthorized access.

Avoid:

``` java
catch (Exception e) {
    // ignore
}
```

Never silently swallow important exceptions.

Do not use empty catch blocks.

## 10. Transaction Rules

Operations that must succeed together should use transactions.

A sale must not result in:

``` text
Sale created
but stock not reduced
```

or:

``` text
Stock reduced
but sale not created
```

These operations should be atomic.

## 11. Security Rules

-   Never hardcode secrets.
-   Never commit `.env` files containing real credentials.
-   Never store plain-text passwords.
-   Validate authorization on the backend.
-   Do not trust role information sent by the frontend.
-   Use HTTPS in production.
-   Do not expose database ports publicly unless absolutely necessary.
-   Use environment variables for production secrets.

## 12. Git Rules

Commit frequently with meaningful messages.

Good:

``` text
feat: add product CRUD APIs
feat: implement stock-in workflow
fix: prevent sale from exceeding available stock
test: add product service tests
```

Avoid:

``` text
update
changes
final
final2
done
```

Do not commit:

-   Passwords.
-   API keys.
-   Build output.
-   IDE-specific files when unnecessary.
-   Large generated files.

## 13. Testing Rules

New business logic should have tests when practical.

Important areas to test:

-   Product creation.
-   Product validation.
-   Stock updates.
-   Insufficient stock.
-   Sale creation.
-   Sale total calculation.
-   Stock reduction.
-   Transaction behavior.
-   Authorization.

Do not claim a feature is complete without testing its important paths.

## 14. AI Coding Rules

When AI is working on this project:

1.  Read the relevant existing files before modifying them.
2.  Do not invent files or classes that already exist.
3.  Do not rewrite unrelated working code.
4.  Make the smallest reasonable change.
5.  Preserve existing functionality.
6.  Explain important architectural decisions briefly.
7.  After changes, identify how the change should be tested.
8.  Do not mark a phase complete without verifying it.
9.  If requirements conflict, stop and ask for clarification.
10. Do not silently change the technology stack.

## 15. Dependency Rules

Before adding a library:

-   Check whether the existing stack already solves the problem.
-   Prefer established and actively maintained libraries.
-   Avoid libraries that solve trivial problems.
-   Keep dependency count reasonable.
-   Record important dependency decisions.

Do not add a library only because it is popular.

## 16. UI Rules

The UI should prioritize usability for a grocery shop owner.

-   Buttons must be clear.
-   Important actions must be obvious.
-   Forms should be short.
-   Tables should remain readable on mobile.
-   Errors should explain what the user needs to fix.
-   Destructive actions require confirmation where appropriate.
-   Do not overload the dashboard with unnecessary charts.

## 17. Scope Rules

Build only what the current phase requires.

Do not jump ahead to:

-   AI features.
-   Multi-store support.
-   Advanced analytics.
-   Complex accounting.
-   Microservices.
-   Kubernetes.
-   Event-driven architecture.

The application is initially for one small grocery shop.

## 18. Architecture Restraint

Do not introduce microservices.

Use a modular monolith architecture initially.

Do not create abstractions before there is a real need.

Do not optimize prematurely.

Prefer:

``` text
Simple + correct + maintainable
```

over:

``` text
Complex + impressive + unnecessary
```

## 19. Documentation Rules

Update relevant documentation when major architectural or product
decisions change.

Keep documentation concise but useful.

Memory.md, once created, should record actual implementation progress
and decisions rather than duplicating the entire codebase.

## 20. Definition of Done

A feature is done only when:

-   Code is implemented.
-   It builds successfully.
-   Relevant tests pass.
-   Main success path works.
-   Important failure paths are handled.
-   Existing functionality has not been unnecessarily broken.
-   Documentation/progress is updated when appropriate.
