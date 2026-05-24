# front-mobile

Minimal notes for local development.

## Requirements
- Android Studio
- JDK 17

## Run tests
```powershell
.\gradlew :app:testDebugUnitTest
```

## API base URL
The app uses a Retrofit client in `app/src/main/java/ru/zagrebin/front_mobile/data/AppContainer.kt`.
Update `BASE_URL` to point to your backend.

