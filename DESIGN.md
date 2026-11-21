---
name: Shelf
description: Native Android book search with readable metadata and purposeful navigation.
colors:
  light-primary: "#2946C6"
  light-on-primary: "#FFFFFF"
  light-primary-container: "#DFE6FF"
  light-on-primary-container: "#14266E"
  light-secondary: "#505E86"
  light-on-secondary: "#FFFFFF"
  light-secondary-container: "#E6EAF7"
  light-on-secondary-container: "#27345B"
  light-background: "#F7F8FC"
  light-on-background: "#171E31"
  light-surface: "#F7F8FC"
  light-on-surface: "#171E31"
  light-surface-variant: "#E8ECF7"
  light-on-surface-variant: "#515D76"
  light-surface-container: "#EDF0F9"
  light-surface-container-low: "#F0F2FA"
  light-outline: "#6B7892"
  light-outline-variant: "#D7DDEC"
  dark-primary: "#BBC8FF"
  dark-on-primary: "#10236D"
  dark-primary-container: "#253B83"
  dark-on-primary-container: "#E0E7FF"
  dark-secondary: "#C0C9E7"
  dark-on-secondary: "#243052"
  dark-secondary-container: "#344163"
  dark-on-secondary-container: "#E1E7FA"
  dark-background: "#101526"
  dark-on-background: "#E5EAFA"
  dark-surface: "#101526"
  dark-on-surface: "#E5EAFA"
  dark-surface-variant: "#303A53"
  dark-on-surface-variant: "#BEC8E2"
  dark-surface-container: "#1B2338"
  dark-surface-container-low: "#161D30"
  dark-outline: "#8895B3"
  dark-outline-variant: "#3A4560"
typography:
  displaySmall:
    fontFamily: "Manrope"
    fontSize: "32sp"
    fontWeight: 700
    lineHeight: "40sp"
    letterSpacing: "-0.8sp"
  headlineLarge:
    fontFamily: "Manrope"
    fontSize: "30sp"
    fontWeight: 700
    lineHeight: "38sp"
    letterSpacing: "-0.7sp"
  headlineMedium:
    fontFamily: "Manrope"
    fontSize: "26sp"
    fontWeight: 700
    lineHeight: "34sp"
    letterSpacing: "-0.5sp"
  titleLarge:
    fontFamily: "Manrope"
    fontSize: "22sp"
    fontWeight: 600
    lineHeight: "30sp"
  titleMedium:
    fontFamily: "Manrope"
    fontSize: "18sp"
    fontWeight: 600
    lineHeight: "26sp"
  titleSmall:
    fontFamily: "Manrope"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "24sp"
  bodyLarge:
    fontFamily: "Manrope"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "25sp"
  bodyMedium:
    fontFamily: "Manrope"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "22sp"
  bodySmall:
    fontFamily: "Manrope"
    fontSize: "12sp"
    fontWeight: 400
    lineHeight: "18sp"
  labelLarge:
    fontFamily: "Manrope"
    fontSize: "14sp"
    fontWeight: 600
    lineHeight: "20sp"
  labelMedium:
    fontFamily: "Manrope"
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "18sp"
  labelSmall:
    fontFamily: "Manrope"
    fontSize: "11sp"
    fontWeight: 500
    lineHeight: "16sp"
rounded:
  reading-sheet: "16dp"
spacing:
  compact: "8dp"
  identity: "12dp"
  content: "16dp"
  section: "20dp"
  page: "24dp"
  detail-section: "28dp"
components:
  search-field:
    width: "fillMaxWidth"
  suggestion-chip:
    height: "min 48dp"
  retry-button:
    height: "min 48dp"
  result-row-light:
    backgroundColor: "{colors.light-surface}"
    padding: "16dp vertical"
  result-row-dark:
    backgroundColor: "{colors.dark-surface}"
    padding: "16dp vertical"
  reading-sheet-light:
    backgroundColor: "{colors.light-primary-container}"
    rounded: "{rounded.reading-sheet}"
    padding: "{spacing.page}"
  reading-sheet-dark:
    backgroundColor: "{colors.dark-primary-container}"
    rounded: "{rounded.reading-sheet}"
    padding: "{spacing.page}"
  book-title:
    typography: "{typography.headlineMedium}"
  book-author:
    typography: "{typography.bodyLarge}"
---

# Design System: Shelf

## Overview

**A readable, immediately usable catalogue**

Shelf is a native Compose Material 3 catalogue with cool mineral surfaces, one cobalt family and locally bundled Manrope. Book metadata supplies the visual hierarchy: open rows support scanning; one tonal reading sheet marks the selected work.

Movement connects the same title/author group across search and detail. Native controls, system Back and readable scrollable content remain the basis of interaction. `Theme.kt`, `SearchScreen.kt`, `DetailScreen.kt` and `BookIdentity.kt` define the implementation described here.

**Key Characteristics:**

- Cool mineral light and dark themes with cobalt emphasis.
- Open bibliographic rows and a single tonal reading sheet.
- Shared title/author identity with system-aware motion.
- Native controls, local type and drawn vector icons.

Layout dimensions use Android `dp`; typography uses `sp`. Component entries record application overrides, with Material defaults for properties the app does not set.

## Colors

The palette is mineral and cool, with cobalt directing attention rather than filling every surface. Frontmatter keys preserve both explicit theme modes; `ShelfTheme` follows `isSystemInDarkTheme()`.

### Primary

- **Cobalt / pale cobalt** (`light-primary`, `dark-primary`): Shelf wordmark, result provenance and forward affordances; also feeds native primary controls.
- **Cobalt reading tone** (`*-primary-container`): the selected-work sheet. Its inherited `onPrimaryContainer` is defined, but the title and author explicitly retain `onSurface` and `onSurfaceVariant` to match the list.
- **Primary contrast roles** (`*-on-primary`, `*-on-primary-container`): paired foregrounds available to Material components; do not substitute them for the explicit identity foregrounds.

### Secondary

- **Slate blue** (`*-secondary`, `*-secondary-container` and paired `on-*` roles): supporting Material theme roles, not a second decorative accent.

### Neutral

- **Mineral canvas** (`*-background`, `*-surface`): page and open rows; their matching colors avoid an unnecessary card silhouette.
- **Mineral layers** (`*-surface-variant`, `*-surface-container`, `*-surface-container-low`): supplied native component tones.
- **Ink and slate text** (`*-on-background`, `*-on-surface`, `*-on-surface-variant`): primary copy and quieter authors, labels and explanations.
- **Slate outlines** (`*-outline`, `*-outline-variant`): native field boundary roles and subtle row/metadata separators.

The app does not override Material's error palette. Failure text uses `colorScheme.error`; its concrete color is library-owned, not a newly extracted Shelf token.

## Typography

Manrope Regular, Medium, SemiBold and Bold are bundled locally in `res/font`; the OFL is retained at `app/src/main/assets/licenses/Manrope-OFL.txt`. The app declares no alternative font stack. Frontmatter records every custom `ShelfType` role, including reserve roles not currently selected directly by a screen.

- `headlineLarge`: search task heading.
- `headlineMedium`: Shelf app-bar wordmark and the shared work title.
- `titleLarge`: empty-state title and detail section heading.
- `titleMedium`: detail app-bar title and metadata values.
- `titleSmall`: suggestion heading.
- `bodyLarge`: introduction, shared author, failure message and empty-state explanation.
- `bodyMedium`: loading and suggestion explanation.
- `bodySmall`: result limit and detail note.
- `labelLarge`: result provenance and metadata labels.
- `labelMedium`: Open Library attribution and first-publication year.
- `displaySmall`, `labelSmall`: defined theme roles; no direct screen use in the current implementation.

**The Identity Rule.** List and detail use the same `BookIdentity`: `headlineMedium` title, `bodyLarge` author and `spacing.identity` gap. Preserve wrapping and available width on both ends; do not scale one endpoint independently. Missing authors use the localized fallback rather than invented metadata.

## Layout

Both screens center their content within a maximum width of 720 dp. The search `LazyColumn` has page spacing on all sides, section spacing between items and IME padding. Detail scrolls vertically with page padding and detail-section spacing. There is no breakpoint switch or alternate desktop composition in the source.

Suggestions wrap with compact horizontal and vertical gaps. Search copy uses a compact heading-to-introduction gap. Each result has content spacing vertically around its title/author row, page spacing before the trailing icon, optional year with section spacing below, then a divider. Its identity width is the content width less 48 dp: 24 dp gap plus the 24 dp icon. Detail reserves the same 48 dp as two inner page insets inside its sheet, keeping identity text width equal.

Metadata fields use compact label/value gaps and a compact spacer before the divider. Suggestions and retry have an explicit minimum height of 48 dp; other targets retain native Material sizing. Content grows and scrolls rather than truncating titles to a fixed line count. Insets are supplied by `Scaffold` and the keyboard-aware search container.

## Elevation & Depth

Depth is conveyed primarily by tone and spacing. Result surfaces use the page surface color with divider separation. The selected-work sheet uses the primary-container tone. Neither surface supplies a shadow elevation; there is no custom shadow token or lifted-card vocabulary. Native app bars and controls keep Material 3 behavior where no override is present.

## Shapes

The reading sheet has gently rounded corners via `RoundedCornerShape(rounded.reading-sheet)`. Results retain the plain `Surface` shape and no enclosing stroke. The app supplies no global `Shapes` override: field, button and chip corners remain Material 3 defaults, not custom Shelf radii.

Action vectors use a 24 dp viewport and a two-unit rounded stroke. Search, clear, forward and back are native drawable resources, tinted by their Compose context; suggestion arrows render at 18 dp. The separate 48 dp launcher vector is cobalt with white geometric book spines. There are no shipping raster book covers.

## Components

### Search field

A full-width, single-line `OutlinedTextField` with a localized label, search leading icon and conditional clear `IconButton`. It uses the native outlined-field focus, disabled, text and outline defaults; no custom hover or browser behavior exists. The IME action is Done. Clearing calls the same query-edit path as typing.

### Suggestions

Native `AssistChip` actions in a wrapping `FlowRow`, each with the explicit minimum touch height and a small forward vector. They populate a real query; they are not selected filters or preloaded catalogue results. Shape, outline, feedback and type otherwise remain native defaults.

### Retry button and state messages

Native filled `Button` with explicit minimum height; it appears for recoverable failures and uses the theme's primary roles. Invalid-query failures have no retry button. Loading shows text and a full-width native linear progress indicator. Failure/loading/provenance announcements use polite live-region semantics. Empty content uses a title and explanatory body, without a decorative illustration.

### Open result row

A full-width clickable `Surface`, title/author identity, cobalt forward arrow, optional first-publication year and outline-variant divider. It has native touch feedback and no custom elevation or boxed-card boundary. Stable work IDs identify both lazy items and the shared transition. Whole rows navigate; decorative arrows have no independent announcement.

### Reading sheet and metadata fields

One primary-container sheet holds the selected identity. The title is a semantic heading in detail; colors remain the same semantic roles as the list. Below it, a section heading, labeled values and subtle dividers form a scrollable reading view. Missing year/edition values remain explicit. The sheet is not a modal bottom sheet.

### Navigation and shared identity

The search top bar presents Shelf and Open Library attribution; detail presents a localized title and native Back icon button. Navigation Compose owns search/detail transitions and normal Back behavior.

A single `sharedElement` keyed by `book:<id>` moves the identically typeset title/author group with `FastOutSlowInEasing`. The focal movement lasts 240 ms. Destination screen content fades in for 100 ms after 180 ms; departing content fades out for 80 ms. Detail metadata adds its own 120 ms fade after 220 ms and uses the same exit duration. These are the actual local specs, not a promise that independently nested fades sum to a particular frame timeline. Forward and pop navigation use the same screen fade specs. Query edits have no result exit fade.

`rememberMotionEnabled()` observes Android `Settings.Global.ANIMATOR_DURATION_SCALE` and `TRANSITION_ANIMATION_SCALE` with a `ContentObserver`. If either value is zero, shared-element and metadata modifiers are omitted and navigation enter/exit transitions are `None`; their animation delays are removed. This explicit preference path is independent of a Compose test clock's duration scaling. There are no custom decorative infinite motion loops; loading retains the native progress component. No frame-rate or hardware performance certification is asserted.

## Do's and Don'ts

### Do:
- **Do** keep the title and author identically typeset and equally wide in list and detail.
- **Do** use semantic Material color roles so system light/dark selection remains coherent.
- **Do** preserve scrollable content, explicit missing metadata and Spanish resources with English fallback.
- **Do** use native vector icons and retain the Manrope OFL notice.
- **Do** remove spatial modifiers, transition specs and delays when either observed Android animation scale is zero.

### Don't:
- **Don't** add raster book covers, decorative statistics, a marketing splash or repeated boxed result cards.
- **Don't** crossfade two title/author copies or introduce identity clipping as an animation style.
- **Don't** keep old search results visible for an exit animation after the query changes.
- **Don't** infer physical-device performance, TalkBack coverage or store approval from this design record.
