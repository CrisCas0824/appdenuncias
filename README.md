# App de denuncias

Aplicación Android (Kotlin y Jetpack Compose) que consume la [API de denuncias](https://github.com/CrisCas0824/apiDenuncias). Permite registrar, buscar por DNI, consultar detalles, atender denuncias y recorrer páginas de cinco registros.

## Ejecutar

Abra este proyecto en Android Studio y ejecute la API Spring Boot. Configure `BASE_URL` en `app/src/main/java/com/castrobarrios/denunciasapp/data/remote/RetrofitClient.kt` antes de instalar la app:

- Celular físico: `http://IP_DE_TU_PC:8080/` (teléfono y PC deben poder comunicarse por la red local; el servicio debe seguir ejecutándose).
- Emulador: `http://10.0.2.2:8080/` si puede conectarse; en caso contrario, use `http://127.0.0.1:8080/` y ejecute `adb reverse tcp:8080 tcp:8080`.
- Servicio publicado: URL pública HTTPS del servicio, terminada en `/`.

La dirección configurada actualmente corresponde a la IP local usada para probar la app y puede variar al cambiar de red. El permiso de Internet y la autorización de HTTP local están configurados en `AndroidManifest.xml`.

Para generar el APK de prueba, ejecute `./gradlew assembleDebug`; estará en `app/build/outputs/apk/debug/app-debug.apk`.
