# Tienda Cordero - Android
Proyecto Android nativo (Kotlin), sin permisos de internet. Funciones: productos, inventario, ventas, historial y resumen de ingresos/ganancia bruta. Datos locales SQLite.

## Compilar APK
1. Abrir esta carpeta con Android Studio reciente y esperar la sincronización de Gradle.
2. Instalar Android SDK Platform 35 si se solicita.
3. Seleccionar Build > Build Bundle(s) / APK(s) > Build APK(s).
4. El APK de depuración aparecerá en `app/build/outputs/apk/debug/app-debug.apk`.

Nota: no hay autenticación, copia de seguridad ni sincronización en esta primera versión. No borres los datos de la aplicación sin respaldarlos. Para distribuirla, configura firma de release y realiza pruebas en dispositivo.

## Crear APK desde GitHub sin computadora
1. Crea un repositorio nuevo en GitHub y sube **el contenido** de esta carpeta (incluida `.github/workflows/android-apk.yml`).
2. Abre la pestaña **Actions** del repositorio, selecciona **Crear APK Tienda Cordero** y pulsa **Run workflow** (si no se inició con el primer push).
3. Cuando termine exitosamente, abre la ejecución y descarga el artefacto **TiendaCordero-APK**.
4. Descomprime el ZIP descargado: dentro estará `app-debug.apk`, que podrás instalar en Android autorizando la instalación desde tu navegador/administrador de archivos.

El APK es de depuración, para uso personal y pruebas. El workflow requiere que GitHub tenga acceso a las dependencias de Android y Gradle. Aún no se ha ejecutado ni probado en un dispositivo.
