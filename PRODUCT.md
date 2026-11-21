# Product


## Platform

android

## Users

People searching for a book by title, author or subject. A second audience is a technical reviewer evaluating a small, runnable Kotlin/Compose exercise and its decisions.

## Product Purpose

Search the public Open Library catalogue, cancel replaced requests, reuse fresh per-query results and open a bibliographic detail. Success is an understandable, responsive task and code whose behavior is supported by tests.

## Stack

Existing Kotlin / Compose Android app; domain and data JVM modules, Retrofit, coroutines, memory cache, ViewModel and Navigation. Manual dependency wiring.

## Capabilities and Constraints

Two screens, up to 20 works per query, three-minute memory cache, immediate cancellation before debounce and no stale query rendering. No accounts, disk cache, backend credentials or store distribution. Spanish resources and English fallback. Existing automated failure tests remain authoritative during visual changes.

## Brand Commitments

Shelf uses native Android controls, readable typography and a single shared identity between list and detail.

## Evidence on Hand

Working source, deterministic JVM tests, Compose journeys and actual emulator captures. Public API smoke checks are separate from synthetic test fixtures. No performance benchmark, physical-device, TalkBack or store approval evidence.

## Product Principles

- Search and readable results precede presentation.
- Cancellation changes visible state immediately.
- Movement explains a relationship; system-disabled animations remain usable.
- Required and optional metadata stay distinguishable.
- Technical claims link to reproducible evidence.
