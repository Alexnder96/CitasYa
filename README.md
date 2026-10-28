# CitasYa

Aplicación móvil Android para programar citas médicas en la Red de Hospitales MINSA de Lima. Proyecto académico de Ingeniería de Sistemas e Informática (UTP).

**Equipo:** Pino Acuña, Willy Alexander · Pumayalla Miranda, Breynoll

## Qué hace
- Registro de pacientes validando el DNI contra el padrón del hospital, inicio de sesión, recuperación y cambio de contraseña, cierre de sesión.
- Agendar citas por especialidad, médico y horario libre (la sede es la del paciente); consultar citas activas, historial y cancelar.
- Recordatorio local 2 horas antes de cada cita (AlarmManager + notificación).

## Arquitectura
App Android (Kotlin, Retrofit) → API REST en PHP → base de datos MySQL/MariaDB. Ver `docs/diagramas/arquitectura_solucion.png`.

| Carpeta | Contenido |
|---|---|
| `android/` | Proyecto de Android Studio (Kotlin, minSdk 26, targetSdk 34) |
| `backend/Api_citas/` | API REST en PHP (paciente, cita, doctor, especialidad, sede) |
| `database/` | Script SQL de la base `citas_medicas` con datos de prueba ficticios |
| `docs/` | Diagramas, capturas e informe |

## Requisitos
- Windows 10 o superior, XAMPP (Apache, PHP 8.x y MySQL/MariaDB).
- Android Studio con **Gradle JDK 17 o 21** (*Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*). Las versiones más nuevas, como JDK 25, no son compatibles con Gradle 8.11.1.
- Emulador Android 8.0 (API 26) o superior, o un teléfono físico.

## Cómo ejecutarlo (entorno local)
1. Inicia **Apache** y **MySQL** desde el panel de XAMPP.
2. Copia `backend/Api_citas` dentro de `htdocs/`.
3. En phpMyAdmin importa `database/citas_medicas.sql` (crea la base `citas_medicas` con sus seis tablas).
4. Abre `backend/Api_citas/db_connection.php` en `htdocs` y verifica el **puerto** de MySQL (el proyecto usa `3307`; el valor por defecto de XAMPP es `3306`), el usuario y la contraseña.
5. Comprueba la API en el navegador: `http://localhost/Api_citas/sede.php?estado=1` debe mostrar las sedes en JSON.
6. Abre la carpeta `android/` en Android Studio y espera la sincronización de Gradle.
7. En `Controller/RetrofitClient.kt` revisa `BASE_URL`:
   - Emulador: `http://10.0.2.2/Api_citas/` (valor por defecto).
   - Teléfono físico: la URL de un túnel (por ejemplo `cloudflared tunnel --url http://localhost:80`) o la IP de tu PC, siempre terminada en `/`.
8. Ejecuta la app. Cuando Android lo pida, **permite las notificaciones**.

### Cuentas de prueba
- Correos `paciente01@example.com` a `paciente10@example.com`, contraseña `Prueba123` (`paciente02` está inactiva a propósito).
- Para probar el registro usa un DNI del padrón que aún no tenga cuenta, por ejemplo `70000011`.

## Problemas conocidos
- **Error `#1467 Failed to read auto-increment value from storage engine`** al reservar citas (se vio en MariaDB 10.4 de XAMPP). Se corrige ejecutando `ALTER TABLE cita AUTO_INCREMENT = 300;` en phpMyAdmin. Se previene deteniendo MySQL con «Stop» antes de apagar el equipo.
- **`Incompatible Gradle JVM version`**: elige un JDK 17 o 21 como indica la sección de requisitos.
- La aplicación identifica al paciente solo con su `idPaciente` (sin token); se propone JWT como mejora futura.

## Metodología
Scrum en dos sprints: Sprint 1 (autenticación) y Sprint 2 (citas, historial y notificaciones). Ver `docs/diagramas/avance_general.png`, los burndown de cada sprint y la sección 3.3 del informe.

## Contribuciones
Cada integrante trabaja en su propia rama y abre Pull Requests hacia `main`. Ver `CONTRIBUTING.md`.
