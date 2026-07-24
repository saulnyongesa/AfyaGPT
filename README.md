# AfyaGPT

AfyaGPT is an offline-first clinical decision-support application for Community Health Workers, Nurses, and Clinical Officers in Kenya and Sub-Saharan Africa.

The app follows the World Health Organization Integrated Management of Childhood Illness guidelines and Kenya Ministry of Health protocols. It allows frontline health workers to triage pediatric patients, screen for general danger signs, track routine immunizations, and access clinical guidance, even in clinics with no network connectivity.

## Key Features

### IMCI Triage and Assessment

- General Danger Signs: screens for inability to drink or breastfeed, vomiting everything, convulsions, and lethargy.
- Vitals and Respiratory Timer: an integrated timer measures age adjusted respiratory rate and flags the fast breathing threshold.
- Multi system symptom assessment, covering:
  - Respiratory and cough, classifying into Severe Pneumonia, Pneumonia, or No Pneumonia.
  - Diarrhea and dehydration, classifying into Severe Dehydration (Plan C), Some Dehydration (Plan B), or No Dehydration (Plan A), with ORS and zinc dosing guidance.
  - Fever and malaria, including interpretation of malaria rapid diagnostic tests and checks for stiff neck or bulging fontanelle.
  - Ear problems, evaluating discharge and tender swelling behind the ear.
  - Nutrition and anemia, calculating MUAC thresholds, bilateral oedema, and palmar pallor.

### AI Decision Support Assistant

Chat conversations are stored locally and linked to individual patient profiles in the Room database. The assistant provides guidance on IMCI protocol classifications, first dose antibiotic calculations, and hospital referral criteria.

### Immunization Tracker

When a newborn or infant is registered, the app generates the official Kenya Expanded Programme on Immunization schedule automatically, covering BCG, OPV, Penta, PCV, Rota, Vitamin A, MR, and Yellow Fever doses. Administered dates, status, and batch numbers are logged and persisted locally.

### Offline Clinical Library

A reference library is available fully offline, covering:

- Severe pneumonia and acute respiratory infections
- Uncomplicated and severe malaria treatment
- Acute diarrhea and dehydration management plans
- Severe acute malnutrition and RUTF guidance
- Acute otitis media and mastoiditis
- Neonatal sepsis and jaundice

## Tech Stack

The app is built with Clean Architecture and MVVM.

- Language: Kotlin
- UI: Jetpack Compose with Material 3
- Dependency injection: Dagger Hilt
- Local database: Room
- Preferences and session state: Jetpack DataStore
- Annotation processing: Kotlin Symbol Processing (KSP)
- Async and reactive state: Kotlin Coroutines, StateFlow, and SharedFlow
- Build system: Gradle, JDK 17

Data flows from the Compose UI layer down through Hilt scoped ViewModels, into repositories, and finally into either Room (patients, users, chat, immunization records) or DataStore (session and theme preferences).

## Project Structure

```
com.example.afyagpt/
├── AfyaGPTApp.kt              Application entry point
├── data/
│   ├── local/                 Room database, entities, and DAOs
│   ├── preferences/           UserPreferences (DataStore)
│   └── repository/            AuthRepository, PatientRepository, VaccinationRepository, ChatRepository
├── di/                        Hilt modules
├── domain/                    Domain models
├── ui/
│   ├── components/            Shared UI components
│   ├── navigation/            App routes and navigation graph
│   ├── screens/                Screen composables and ViewModels, organized by feature
│   └── theme/                  Color, typography, and shape definitions
└── util/                      Helper utilities (ID generation, date handling, EPI schedule logic)
```

## Getting Started

### Requirements

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17 or higher
- Android SDK, minimum API 26, target API 34 or 35

### Building locally

Clone the repository:

```bash
git clone https://github.com/your-org/AfyaGPT.git
cd AfyaGPT
```

On Linux or macOS, make the Gradle wrapper executable:

```bash
chmod +x gradlew
```

Build the debug APK:

```bash
./gradlew assembleDebug
```

The generated APK is placed at `app/build/outputs/apk/debug/app-debug.apk`.

## CI/CD

The project uses GitHub Actions for continuous integration. On every push or pull request to main, the workflow compiles the project, runs annotation processing, builds the APK, and uploads it as a build artifact. The workflow file is located at `.github/workflows/android-ci.yml`.

## License

Copyright 2026 AfyaGPT Project Team. Distributed under the Apache License, Version 2.0.
