# Third-party notices

Original Shelf source and vector icon: Manuel Naval, MIT (see LICENSE). No third-party images are bundled. Manrope is bundled under SIL OFL 1.1, separately from the MIT application source.

Gradle Wrapper scripts/JAR are from Gradle 8.14.6, Apache-2.0; its LICENSE and NOTICE are preserved in `gradle/licenses/`. The wrapper distribution is pinned with its SHA-256. Dependencies are fetched by Gradle, not copied into source:

| Component | Pinned version | License |
| --- | --- | --- |
| Kotlin and Compose compiler plugin | 2.2.10 | Apache-2.0 |
| Kotlin coroutines | 1.10.2 | Apache-2.0 |
| Android Gradle Plugin | 8.13.2 | Apache-2.0 |
| AndroidX Activity | 1.10.1 | Apache-2.0 |
| AndroidX Lifecycle | 2.9.2 | Apache-2.0 |
| AndroidX Navigation | 2.9.3 | Apache-2.0 |
| AndroidX Compose BOM | 2025.08.00 | Apache-2.0 |
| Retrofit and Gson converter | 3.0.0 | Apache-2.0 |
| OkHttp / MockWebServer | 4.12.0 | Apache-2.0 |
| Gson | 2.13.2 | Apache-2.0 |
| JUnit | 4.13.2 | EPL-1.0 |
| AndroidX Test / Espresso | 1.3.0 / 1.7.0 / 3.7.0 | Apache-2.0 |

[Open Library](https://openlibrary.org/) supplies bibliographic metadata at runtime, not code or bundled datasets. See its [API usage guidance](https://openlibrary.org/developers/api) and [licensing statement](https://openlibrary.org/developers/licensing). The app displays attribution; this repository's MIT license does not relicense external service content. Fictional test fixtures were written for this exercise.

## Manrope

[Manrope, Google Fonts source](https://github.com/google/fonts/tree/main/ofl/manrope): variable TTF blob `75274da58537d6123b14f2cd0c355ad4681fc2b3`, OFL blob `e271172a9ed0cddc895aabb6509f1c7d880b492d`. Static 400/500/600/700 instances were generated with fontTools 4.68.0 and no outline redesign; `app/src/main/assets/licenses/Manrope-OFL.txt` preserves the license. Copyright belongs to The Manrope Project Authors.

