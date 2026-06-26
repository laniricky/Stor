# Stor - Expense & Loan Tracker

This project is a complete production-ready backend and the scaffolded architecture for the Stor Android app.

## Project Structure
- `stor-backend/` - Ktor + Exposed + PostgreSQL REST API. Fully implemented.
- `stor-android/` - Jetpack Compose Android App. Scaffolded with Room, Retrofit, Hilt, and Compose Navigation structure.

## Backend Setup (stor-backend)
1. Navigate to the `stor-backend` directory.
2. Ensure you have Java 17+ installed.
3. Configure the `.env` file from `.env.example`.
4. Run locally:
   ```bash
   ./gradlew run
   ```

### Database Migrations
The backend uses Ktor and Exposed. Migrations are automatically run on startup using SchemaUtils or you can run `migrations/V1__init.sql` against your Neon.tech database.

### API Documentation
See `stor-backend/docs/API.md` for full API endpoint documentation.

## Android App Setup (stor-android)
1. Open the `stor-android` directory in Android Studio.
2. Sync Gradle.
3. The app is set up with Clean Architecture:
    - **Presentation**: `app/src/main/kotlin/com/stor/presentation/`
    - **Domain**: `app/src/main/kotlin/com/stor/domain/`
    - **Data**: `app/src/main/kotlin/com/stor/data/`
4. Core DI, Networking, and Database modules are implemented.
5. Create UI screens inside the `presentation/screens/` directory following the design system in `presentation/theme/`.

## Deployment
The backend includes a `Dockerfile` and `render.yaml` for 1-click deployment to Render.com.

1. Connect your repo to Render.
2. Use the `render.yaml` blueprint.
3. Set your `DATABASE_URL` and `JWT_SECRET` in the Render dashboard.
