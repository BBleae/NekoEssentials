# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
NekoEssentials is a Fabric mod for Minecraft 1.21.5, written in Kotlin and Java.

## Build Commands
- `./gradlew build` - Build the mod
- `./gradlew runClient` - Run the client for testing
- `./gradlew runDatagen` - Generate data
- `./gradlew test` - Run all tests
- `./gradlew test --tests "net.nekocraft.TestName"` - Run a single test

## Code Style
- Use Kotlin for new code unless extending Java-specific Minecraft/Fabric APIs
- Follow Kotlin style conventions (https://kotlinlang.org/docs/coding-conventions.html)
- Indent with 4 spaces, not tabs
- Maximum line length: 120 characters
- Use `object` for singletons
- Prefer `val` over `var` when possible
- Use descriptive naming: `camelCase` for functions/variables, `PascalCase` for classes
- Log using SLF4J logger initialized in each class/object
- Handle exceptions with appropriate logging rather than empty catch blocks