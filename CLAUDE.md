# Restlet Framework - project instructions

## Changes log

Keep `changes.md` up to date as part of every task. Add an entry under the current development
version (the first section of the file) in the same commit as the change when it is significant:

- **Breaking changes**: API changes (removed, renamed or behavior-changing public classes,
  methods or fields), changed output formats, raised minimum Java or Maven versions.
- **Bugs fixed**: user-visible fixes.
- **Misc**: dependency and Maven plugin upgrades (one line per library with the new version),
  notable build, CI or quality-tooling changes.

Skip purely internal refactorings, test-only changes and formatting. Follow the existing wording
style, e.g. "Upgraded Jetty library to version 12.0.39.".

## Build

- Use the Maven wrapper: `./mvnw -B verify -P default,test`. This is what CI runs and it includes
  the Spotless, JaCoCo (80% per module) and SpotBugs checks.
- SpotBugs only tolerates known findings (`spotbugs.max.violations`): lower it when fixing some.
