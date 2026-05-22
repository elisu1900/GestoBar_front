# GestoBar — Frontend

Aplicación de escritorio para la gestión de un bar/restaurante. Desarrollada con **JavaFX 21** como cliente gráfico que consume una API REST (backend Spring Boot separado).

## Tecnologías

| Herramienta | Versión |
|---|---|
| Java | 21 |
| JavaFX | 21.0.6 |
| Gradle | 8.x (Kotlin DSL) |
| Jackson | 2.17.1 |
| JUnit Jupiter | 5.12.1 |

## Requisitos previos

- **JDK 21** instalado y en el `PATH`.
- El **backend** (`GestoBar_back`) levantado en `http://localhost:8080` (o la IP configurada).

## Configuración

Toda la configuración de la aplicación vive en `src/main/resources/app.properties`:

```properties
# URL base de la API
api.base.url=http://localhost:8080/api

# Para otra máquina en red, descomenta y ajusta:
# api.base.url=http://192.168.1.50:8080/api

# Timeouts (segundos)
api.timeout.connect=10
api.timeout.read=15

# Ventana
app.window.width=1280
app.window.height=800
app.window.maximized=false
```

Edita `api.base.url` si el backend corre en una máquina diferente.

## Descarga e instalación (Windows)

No necesitas tener Java instalado. El instalador incluye su propio runtime.

1. Descarga el instalador haciendo clic aquí: **[⬇️ Descargar GestoBar-1.0.0.exe](https://github.com/elisu1900/GestoBar/raw/develop/build/jpackage/GestoBar/GestoBar-1.0.0.exe)**
2. Ejecuta el instalador y sigue los pasos (puedes elegir el directorio de instalación y crear accesos directos en el escritorio y el menú inicio).
3. Abre **GestoBar** desde el acceso directo o desde el directorio de instalación.

> Antes de arrancar la app asegúrate de que el backend esté levantado y de que la URL en `app.properties` apunte a la máquina correcta (ver sección [Configuración](#configuración)).

---

## Ejecutar la aplicación

```bash
# Windows
gradlew.bat run

# Linux / macOS
./gradlew run
```

## Construir un ejecutable distribuible (jlink)

```bash
gradlew jlink
# El resultado queda en build/image/
```

## Estructura del proyecto

```
src/main/
├── java/com/elias/gestobar/
│   ├── Launcher.java               # Punto de entrada JavaFX
│   ├── config/
│   │   ├── AppConfig.java          # Lee app.properties
│   │   └── SessionManager.java     # Sesión del usuario activo (singleton)
│   ├── controllers/
│   │   ├── Router.java             # Navegación entre pantallas
│   │   ├── LoginController.java
│   │   ├── MainController.java
│   │   ├── NavbarController.java
│   │   ├── TablePanelController.java
│   │   ├── OrderPanelController.java
│   │   ├── ProductPanelController.java
│   │   ├── AdministrationController.java
│   │   ├── DailyBalanceController.java
│   │   ├── UsersController.java
│   │   └── ...
│   ├── service/
│   │   ├── ApiClient.java          # Cliente HTTP singleton (java.net.http)
│   │   ├── AuthService.java
│   │   ├── TableApiService.java
│   │   ├── TicketApiService.java
│   │   ├── ProductApiService.java
│   │   ├── BalanceApiService.java
│   │   └── UserApiService.java
│   ├── model/
│   │   ├── dto/                    # Records de transferencia de datos
│   │   └── enums/Role.java         # ADMIN | WAITER
│   └── util/
│       ├── ApiException.java
│       ├── JsonMapper.java
│       └── AlertHelper.java
└── resources/
    ├── app.properties
    └── com/elias/gestobar/
        ├── view/                   # Ficheros FXML (pantallas)
        ├── css/styles.css
        └── images/
```

## Flujo de la aplicación

```
Splash  →  Login  →  Main
                       ├── Panel de mesas    (ADMIN + WAITER)
                       ├── Panel de pedidos  (ADMIN + WAITER)
                       ├── Panel de productos(ADMIN + WAITER)
                       ├── Balance diario    (solo ADMIN)
                       ├── Administración    (solo ADMIN)
                       └── Gestión de usuarios (solo ADMIN)
```

1. **Splash**: pantalla de carga inicial.
2. **Login**: autentica contra `POST /auth/login`. La cookie de sesión se mantiene automáticamente.
3. **Main**: layout principal con navbar lateral. Carga los paneles según el rol del usuario:
   - **WAITER**: puede ver mesas, gestionar pedidos y añadir productos al ticket abierto.
   - **ADMIN**: acceso completo, incluyendo balance diario, administración de productos/categorías y gestión de usuarios.

## Roles

| Rol | Permisos |
|---|---|
| `WAITER` | Ver mesas, abrir/gestionar tickets, añadir/quitar productos |
| `ADMIN` | Todo lo anterior + balance diario, administración, gestión de usuarios |

## Comunicación con el backend

`ApiClient` (singleton) usa `java.net.http.HttpClient` con gestión automática de cookies (sesión). Todos los servicios (`*ApiService`) delegan en él.

Los errores HTTP se traducen a `ApiException` y se muestran al usuario mediante `AlertHelper`.

## Variables de entorno / propiedades clave

| Propiedad | Descripción | Default |
|---|---|---|
| `api.base.url` | URL base del backend | `http://localhost:8080/api` |
| `api.timeout.connect` | Timeout de conexión (s) | `10` |
| `api.timeout.read` | Timeout de lectura (s) | `15` |
| `app.env` | Entorno (`dev` / `prod`) | `dev` |
| `app.ui.refresh.interval` | Intervalo de refresco UI (s) | `30` |
