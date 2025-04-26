# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build/Run/Test Commands
- Build: `./gradlew build`
- Run client: `./gradlew runClient`
- Run server: `./gradlew runServer`
- Run datagen: `./gradlew runDatagen`
- Clean: `./gradlew clean`
- Test: `./gradlew test`

## Code Style Guidelines
- **Kotlin** is the primary language, Java is used for mixins
- Use **object** for singleton classes (Kotlin)
- Follow Fabric modding conventions for mixins and entrypoints
- Kotlin target JVM 21, Java compatibility level 21
- Use proper package structure: `net.nekocraft`
- Use SLF4J for logging via LoggerFactory
- Create separate client/server implementations when appropriate
- Keep files organized in client/main source sets
- Maintain proper mixin configuration in resources