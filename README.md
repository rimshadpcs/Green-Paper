# Exchange Calculator

A native Android exchange calculator built with Kotlin and Jetpack Compose.

The app converts between USDc and supported Latin American currencies using a remote ticker API. I approached it as a small production-style fintech feature, with focus on clean architecture, reliable state handling, edge cases, and polished user experience.

## Demo

| Conversion | Currency Selection | Dark Mode | Rotation & Compact Layout | Offline State |
|---|---|---|---|---|
| ![Conversion demo](https://github.com/user-attachments/assets/76efe909-fa2a-4024-a2bb-490186e4aea8) | ![Currency selection demo](https://github.com/user-attachments/assets/ab52c538-2c15-49f8-9031-58895c713a82) | ![Dark mode demo](https://github.com/user-attachments/assets/1327ae4d-2036-4e37-90cf-c5f05de81295) | ![Landscape rotation demo](https://github.com/user-attachments/assets/eda17981-3b67-428a-9199-19257e7360a1) | ![Offline state demo](https://github.com/user-attachments/assets/942a26f4-3b3d-4b00-b221-dbd04de3552f) |
## What the App Does

Users can:

- Convert between USDc and supported quote currencies.
- Enter an amount in either field and see the other field update automatically.
- Select a different currency from a bottom sheet.
- Swap the currency positions.
- Refresh exchange rates.
- Continue using the calculator with the last available rates when the network is unavailable.
- Reopen the app and restore the last selected currency.

## Product & UX Details
Beyond the main conversion flow, I added several details to make the app feel closer to a real fintech experience:

- Last available rate handling when fresh rates cannot be loaded.
- Automatic refresh when the device comes back online.
- Clear refresh and retry states.
- Persisted last selected currency after app restart.
- Animated rate info chip showing rate and update information.
- Conversion value feedback shimmer while typing, inspired by seen in fintech app Wise.
- Swap animation with native click sound and haptic feedback.
- Dark mode support.
- Country flag assets for supported currencies.
- Input handling for decimals, pasted text, invalid characters, large values, and repeated separators.
- Rotation-friendly layout handling, so the screen remains usable after orientation changes.
- Scroll support for smaller screens, landscape orientation, compact heights, and keyboard-visible states.

I looked at apps like Wise and other fintech products for inspiration around rate visibility, conversion feedback, refresh states, and error messaging. I also checked practical Android edge cases such as rotation, compact screen heights, keyboard visibility, and dark mode so the app behaves more like a real mobile product rather than only a happy-path demo.


## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Clean Architecture-inspired layering
- SOLID Principles
- Coroutines / Flow
- Hilt
- Retrofit / OkHttp
- DataStore
- JUnit
- Compose UI testing


## Project Structure

```text
 app/src/main/java/com/rimapps/arqtest/

  core/
    Shared platform utilities such as network monitoring.

  data/
    Remote API access, local persistence, DTOs, mappers, fallback data providers,
    and repository implementations.

  di/
    Hilt dependency injection modules.

  domain/
    Business models, repository contracts, use cases, and domain result types.

  presentation/
    Compose UI, ViewModels, UI state, UI events, UI models, and presentation-specific logic.
