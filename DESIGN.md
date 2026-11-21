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
  displayMedium:
    fontFamily: "Manrope"
    fontSize: "42sp"
    fontWeight: 700
    lineHeight: "50sp"
    letterSpacing: "-1sp"
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
  medium: "12dp"
  large: "16dp"
spacing:
  compact: "8dp"
  identity: "12dp"
  content: "16dp"
  section: "20dp"
  page: "24dp"
  detail-section: "28dp"
  pane: "48dp"
components:
  search-panel-light:
    backgroundColor: "{colors.light-primary-container}"
    textColor: "{colors.light-on-primary-container}"
    rounded: "{rounded.large}"
    padding: "{spacing.page}"
  search-panel-dark:
    backgroundColor: "{colors.dark-primary-container}"
    textColor: "{colors.dark-on-primary-container}"
    rounded: "{rounded.large}"
    padding: "{spacing.page}"
  search-field:
    width: "fillMaxWidth"
    rounded: "{rounded.medium}"
  suggestion-button:
    height: "min 56dp"
    padding: "12dp vertical"
    typography: "{typography.titleMedium}"
  clear-button:
    size: "48dp"
  back-button:
    size: "48dp"
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
    rounded: "{rounded.large}"
    padding: "{spacing.page}"
  reading-sheet-dark:
    backgroundColor: "{colors.dark-primary-container}"
    rounded: "{rounded.large}"
    padding: "{spacing.page}"
  book-title:
    typography: "{typography.headlineMedium}"
  book-author:
    typography: "{typography.bodyLarge}"
---

# Design System: Shelf

## Overview

**A readable, immediately usable catalogue**

Shelf is a native Compose Material 3 catalogue with cool mineral surfaces, one cobalt family and locally bundled Manrope. A tonal search panel makes the task immediately legible; open bibliographic rows support scanning and one tonal reading sheet marks the selected work.

Movement connects the same title/author group across search and detail. Native controls, system Back and readable scrollable content remain the basis of interaction. `Theme.kt`, `SearchScreen.kt`, `DetailScreen.kt` and `BookIdentity.kt` define the implementation described here.

**Key Characteristics:**

- Cool mineral light and dark themes with cobalt emphasis.
- A tonal search panel, open bibliographic rows and a selected-work reading sheet.
- Shared title/author identity with system-aware motion.
- Native controls, local type and drawn vector icons.

Layout dimensions use Android `dp`; typography uses `sp`. Component entries record application overrides, with Material defaults for properties the app does not set.

## Colors

The palette is mineral and cool, with cobalt directing attention rather than filling every surface. Frontmatter keys preserve both explicit theme modes; `ShelfTheme` follows `isSystemInDarkTheme()`.

### Primary

- **Cobalt / pale cobalt** (`light-primary`, `dark-primary`): Shelf catalogue mark and wordmark, result provenance and forward affordances; also feeds native primary controls.
- **Cobalt container tone** (`*-primary-container`): the opening search panel and selected-work sheet. The panel uses `onPrimaryContainer` for its copy and unfocused field content/border. In the reading sheet, the title and author explicitly retain `onSurface` and `onSurfaceVariant` to match the list.
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

- `displayMedium`: opening search task heading.
- `titleLarge`: compact search heading, empty-state title and detail section heading.
- `headlineMedium`: Shelf app-bar wordmark and the shared work title.
- `titleMedium`: suggestion actions, detail app-bar title and metadata values.
- `titleSmall`: suggestion heading.
- `bodyLarge`: introduction, shared author, failure message and empty-state explanation.
- `bodyMedium`: loading and suggestion explanation.
- `bodySmall`: result limit and detail note.
- `labelLarge`: result provenance and metadata labels.
- `labelMedium`: Open Library attribution and first-publication year.
- `displaySmall`, `headlineLarge`, `labelSmall`: defined theme roles; no direct screen use in the current implementation.

**The Identity Rule.** List and detail use the same `BookIdentity`: `headlineMedium` title, `bodyLarge` author and `spacing.identity` gap. The text group has a maximum width of 264 dp at both ends. Preserve wrapping and available width; attach shared motion to the inner text group, not its flexible parent cell, and do not scale one endpoint independently. Missing authors use the localized fallback rather than invented metadata.

## Layout

`ShelfLayout` centralizes the two-pane threshold (800 dp), side-pane width (320 dp), expanded content cap (1120 dp), compact content cap (720 dp) and inter-pane gap (48 dp). `BoxWithConstraints` uses the available width after `Scaffold` insets; these are native layout constraints, not web breakpoints.

Below the threshold, search is one centered `LazyColumn` with page padding and page spacing between items. Its stable `controls` item precedes suggestions, loading, failure or results. The opening panel contains heading, input and supporting sentence in that order, separated by section spacing and surrounded by page padding. It compacts to a `titleLarge` heading and unpadded surface when search leaves idle, or when the compact-width content height is below 480 dp. This preserves the editing node rather than swapping whole headers.

At and above the threshold, search places a scrollable side pane beside the catalogue. The outer row has page padding; the catalogue retains its own page content padding and IME padding. The side pane also follows the IME. In detail, the identity sheet and metadata stack on compact widths with detail-section spacing; at expanded widths they form a side pane and a flexible metadata column. Detail remains a single vertically scrollable content area in both arrangements.

Result identities occupy a flexible parent cell, but their inner text group is bounded to 264 dp. A page-sized gap separates that cell from the trailing native-size vector. The selected sheet supplies page padding around the same bounded group. Titles and authors have no fixed line limit. Result year, where present, has section spacing below it before the divider. Metadata uses compact label/value gaps and a compact spacer before its divider.

Suggestion rows have an explicit minimum height of 56 dp; retry is at least 48 dp; clear and Back explicitly occupy 48 dp squares. Native Material feedback and remaining component defaults are retained. `MainActivity` enables edge-to-edge, `Scaffold` supplies content insets and search content applies keyboard insets. Source inspection records these constraints; it does not certify every font-scale/device combination.

## Elevation & Depth

Depth is conveyed primarily by tone and spacing. Result surfaces use the page surface color with divider separation. The opening search panel and selected-work sheet use the primary-container tone. Neither surface supplies a shadow elevation; there is no custom shadow token or lifted-card vocabulary. Native app bars and controls keep Material 3 behavior where no override is present.

## Shapes

The app explicitly themes `Shapes.medium` and `Shapes.large` with gently rounded corners. The outlined search field uses `rounded.medium`; opening search panel and reading sheet use `rounded.large`. The compact search header explicitly uses `RectangleShape` with zero padding, preserving the field outline at its lower corners; this is a component exception, not another radius token. Results retain the plain `Surface` shape and no enclosing stroke. Other shape slots and native control defaults remain Material-owned.

Search, clear, forward and Back use native 24 dp vector resources with a two-unit rounded stroke; directional arrows auto-mirror. Suggestions size their forward icon to 20 dp. The original catalogue mark is a filled 24 dp book-spine vector, tinted with the primary role beside the Shelf wordmark; it is decorative in semantics. The separate launcher vector has a 48 dp canvas with cobalt and white geometric book spines. There are no shipping raster book covers.

## Components

### Search panel and field

A stable `SearchHeader` contains a semantic heading, full-width single-line `OutlinedTextField` and, only in its opening state, explanatory body text. The opening container uses primary-container contrast roles and the large theme shape; the compact state returns to surface/onSurface with `RectangleShape` and zero padding so its container does not clip the outlined field. The field uses the medium theme shape. Focused/unfocused text, unfocused label and leading icon explicitly follow `LocalContentColor`; the unfocused border uses `onPrimaryContainer` when opening and `outline` otherwise. Remaining focus, outline and interaction states stay native Material defaults.

The IME action is Search: it invokes the supplied submission callback and hides the keyboard. Conditional clear is a localized 48 dp `IconButton` and calls the same edit path as typing. There are no custom web hover states.

### Suggestions

Full-width native `TextButton` rows present real author/title queries with start-aligned `titleMedium` text, vertical identity padding, a forward vector and outline-variant dividers. Each row has the explicit minimum touch height. A native button click edits the query; these are neither selected filters nor preloaded catalogue results. A `titleSmall` label and quieter body explanation frame the list.

### Retry button and state messages

Native filled `Button` with explicit minimum height; it appears for recoverable failures and uses the theme's primary roles. Invalid-query failures have no retry button. Loading shows text and a full-width native linear progress indicator. Failure, loading, empty-state and result-provenance announcements use polite live-region semantics. Result provenance and the localized plural work count merge into one heading announcement. Empty content uses a title and explanatory body, without a decorative illustration.

### Open result row

A full-width clickable `Surface`, title/author identity, cobalt forward arrow, optional first-publication year and outline-variant divider. It has native touch feedback and no custom elevation or boxed-card boundary. Stable work IDs identify both lazy items and the shared transition. Whole rows expose `Role.Button` and a localized “Open details for <title>” action label. The optional year has a localized first-publication description; decorative arrows have no independent announcement.

### Reading sheet and metadata fields

One primary-container sheet holds the selected identity. The title is a semantic heading in detail; colors remain the same semantic roles as the list. A section heading, labeled values and subtle dividers form the reading view beneath or beside it. The detail container has a localized `paneTitle`; its metadata heading also has heading semantics. Missing year/edition values remain explicit. The sheet is not a modal bottom sheet.

### Navigation and shared identity

The search top bar presents the drawn catalogue mark, Shelf wordmark and Open Library attribution; detail presents a localized title and native 48 dp Back icon button. Navigation Compose owns search/detail transitions and normal Back behavior.

A single `sharedElement` keyed by `book:<id>` attaches to the inner width-bounded title/author group and moves it with `FastOutSlowInEasing`. The focal movement lasts 240 ms. Destination screen content fades in for 100 ms after 180 ms; departing content fades out for 80 ms. Detail metadata adds its own 120 ms fade after 220 ms and uses the same exit duration. These are the actual local specs, not a promise that independently nested fades sum to a particular frame timeline. Forward and pop navigation use the same screen fade specs. Query edits have no result exit fade.

`rememberMotionEnabled()` observes Android `Settings.Global.ANIMATOR_DURATION_SCALE` and `TRANSITION_ANIMATION_SCALE` with a `ContentObserver`. If either value is zero, shared-element and metadata modifiers are omitted and navigation enter/exit transitions are `None`; their animation delays are removed. This explicit preference path is independent of a Compose test clock's duration scaling. There are no custom decorative infinite motion loops; loading retains the native progress component. No frame-rate or hardware performance certification is asserted.

## Do's and Don'ts

### Do:

- **Do** keep the title and author identically typeset and bounded to the same text width in list and detail.
- **Do** use semantic Material color roles so system light/dark selection remains coherent.
- **Do** preserve scrollable content, explicit missing metadata and Spanish resources with English fallback.
- **Do** use native vector icons and retain the Manrope OFL notice.
- **Do** preserve a stable input node, IME Search, localized action labels and native touch feedback.
- **Do** remove spatial modifiers, transition specs and delays when either observed Android animation scale is zero.

### Don't:

- **Don't** add raster book covers, decorative statistics, a marketing splash or repeated boxed result cards.
- **Don't** crossfade two title/author copies or introduce identity clipping as an animation style.
- **Don't** keep old search results visible for an exit animation after the query changes.
- **Don't** infer physical-device performance, TalkBack coverage or store approval from this design record.
