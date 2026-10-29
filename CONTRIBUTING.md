# Contributing to NeaBows

Thank you for your interest in contributing to NeaBows. We welcome bug reports, feature suggestions, particle physics optimizations, and pull requests.

## Development Standards

1. **Java Version:** Target Java 21 LTS (`<release>21</release>`).
2. **Platform Target:** Paper / Purpur 1.21.1+ using official Paperweight and Adventure APIs.
3. **Coding Standards:**
   - Adhere to Google Java Style Conventions.
   - Use immutable data structures where practical.
   - Profile performance using Spark or async profiling tools; bows and particle tasks must never degrade the main tick loop (20.0 TPS baseline).
   - Clean separation between projectile physics listeners, particle effect renderers, and inventory handlers.

## Pull Request Guidelines

- Branch naming: `feature/<feature-name>`, `fix/<bug-name>`, or `refactor/<target>`.
- Commit messages: Follow [Conventional Commits](https://www.conventionalcommits.org/) (e.g. `feat: add void bow pull-in mechanics`).
- All tests and `mvn clean package` must pass without warnings before submitting PRs.
