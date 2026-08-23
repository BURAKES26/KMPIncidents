# KMP Incidents

KMP Incidents is a Kotlin Multiplatform application for reporting, tracking, and managing local incidents (e.g.
hazards, damages, or issues reported by citizens). It ships a single shared codebase (business logic + Compose
Multiplatform UI) that runs on **Android, iOS, Desktop (JVM), and Web**, backed by a **Ktor server** with JWT
authentication, an incident/user management API, and real-time push notifications via Server-Sent Events (SSE).

## What the project can do

- **Report incidents**: any user (or anonymously) can create an incident with a description, photo, location, and
  license plate (when applicable).
- **Track your own incidents**: registered users can see the list and detail of incidents they reported, and get a
  live status update the moment an official changes the status.
- **Manage incidents (officials/admins)**: browse all incidents (with pagination), view them on a map, inspect
  details, change status/priority, and delete incidents.
- **User management (admins)**: manage registered users and their roles.
- **Statistics**: charts/insights about reported incidents (status/priority distribution over time).
- **Authentication**: register/login with JWT-based sessions, role-based access (regular user vs. official/admin).
- **Real-time notifications**: the server emits `IncidentEvent`s (created/updated/deleted) over SSE; the reporting
  user is notified whenever an official/admin updates or changes the status of their incident.

## Project structure

This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop (JVM), and a Ktor Server.

- [`/app/shared`](./app/shared/src) contains the code shared across all Compose Multiplatform apps: UI screens,
  view models, networking (Ktor client), data/repositories, and platform-specific (`expect`/`actual`)
  implementations.
    - [`commonMain`](./app/shared/src/commonMain/kotlin) — shared UI (`ui/screens/auth`, `ui/screens/incidents`,
      `ui/screens/management`, `ui/screens/stats`), navigation, view models, and API/repository layer.
    - [`androidMain`](./app/shared/src/androidMain/kotlin) — Android-specific implementations, including the
      foreground service that keeps listening for incident-update push notifications while the app is closed.
    - [`iosMain`](./app/shared/src/iosMain/kotlin) — iOS-specific implementations (location, photo picker, maps,
      etc.).
    - [`jvmMain`](./app/shared/src/jvmMain/kotlin) — Desktop (JVM) specific implementations.
    - [`webMain`](./app/shared/src/webMain/kotlin) — Web (JS/Wasm) specific implementations.
- [`/app/androidApp`](./app/androidApp) — the Android application entry point.
- [`/app/desktopApp`](./app/desktopApp) — the Desktop (JVM) application entry point.
- [`/app/webApp`](./app/webApp) — the Web application entry point (JS and Wasm targets).
- [`/app/iosApp`](./app/iosApp/iosApp) — the iOS application entry point. Even though the UI is shared via Compose
  Multiplatform, this is where the native iOS entry point (and any SwiftUI code) lives.
- [`/core`](./core/src) — code shared between *all* targets in the project (both `app` and `server`), most notably
  [`commonMain`](./core/src/commonMain/kotlin).
- [`/server`](./server/src/main/kotlin) — the Ktor server application: authentication (`auth`), incidents
  (`incidents`), users (`users`), plugins, and utilities.

## Per-app overview

### Android app

The most complete client. Users can log in/register, report new incidents (with photo and location), browse and
track their own reported incidents, and — depending on their role — manage all incidents, view them on a map, manage
users, and see statistics. It also runs a foreground service that keeps an SSE connection open to the server so the
user still receives a system notification whenever one of their reported incidents is updated or its status
changes, even while the app is in the background or fully closed. Tapping the notification opens/brings the app to
the foreground.

### Desktop app (JVM)

Runs the same shared management-oriented UI and feature set as the Android app (browsing/managing incidents, maps,
statistics), targeted at desktop platforms via Compose Multiplatform for Desktop. **This app is intended only for
officials/admins** — it is designed as an "Official Desktop" client for managing incidents, not for regular users to
report incidents.

### Web app

Runs the shared UI in the browser (both a Wasm target for modern browsers and a JS target for broader compatibility),
covering the same incident reporting/tracking/management features as the other clients.

### iOS app

Shares the same Compose Multiplatform UI and feature set as the other apps (reporting, tracking, management, maps,
statistics). **Note: the iOS build is experimental and has not been tested on real devices/simulators** — it is
included primarily to demonstrate that the shared codebase compiles and links for iOS targets, but its behavior is
not verified and it may contain bugs or incomplete platform-specific implementations.

### Server

A Ktor-based backend providing:

- JWT authentication (login/register) and role-based authorization (regular users vs. officials/admins).
- REST endpoints to create, update, delete, and query incidents (including pagination and per-user "my incidents"),
  and to manage users.
- Server-Sent Events (SSE) endpoints that stream incident change events in real time, including a dedicated
  authenticated endpoint that notifies a registered user specifically when an official/admin updates or changes the
  status of an incident they reported.

## Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and
options:

- Android app: `./gradlew :app:androidApp:assembleDebug`
- Desktop app:
    - Hot reload: `./gradlew :app:desktopApp:hotRun --auto`
    - Standard run: `./gradlew :app:desktopApp:run`
- Server: `./gradlew :server:run`
- Web app:
    - Wasm target (faster, modern browsers): `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
    - JS target (slower, supports older browsers): `./gradlew :app:webApp:jsBrowserDevelopmentRun`
- iOS app: open the [/app/iosApp](./app/iosApp) directory in Xcode and run it from there. **This target is
  experimental and untested** — expect possible build/runtime issues.

## Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :app:shared:testAndroidHostTest`
- Desktop tests: `./gradlew :app:shared:jvmTest`
- Server tests: `./gradlew :server:test`
- Web tests:
    - Wasm target: `./gradlew :app:shared:wasmJsTest`
    - JS target: `./gradlew :app:shared:jsTest`
- iOS tests: `./gradlew :app:shared:iosSimulatorArm64Test` (not verified — see the iOS note above)

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack
channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web). If you face any issues, please report them
on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).
