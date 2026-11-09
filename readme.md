# Control señal PARE (IoT) – App Android

Aplicación móvil Android para monitorear y controlar un dispositivo IoT que detecta si un vehículo se detiene ante una señal PARE.
Proyecto de la asignatura **TI3042 – Aplicaciones Móviles para IoT** (Unidad 2).

**Autor:** Danitza Ojeda Isla · **Estado:** en desarrollo

## Problemática

Detectar si un auto se detiene en la línea de una señal PARE, avisar con un semáforo y una bocina cuando no lo hace, y permitir que personas autorizadas vean y consulten los resultados desde su teléfono.

## Cómo funciona

```
Arduino UNO R4 WiFi  <--HTTP por WiFi-->  App Android  <--TLS-->  Firebase (Auth + Firestore)
 (HC-SR04, touch,                          (admin / trabajador)
  semáforo, bocina)
```

- **Placa:** expone un servidor HTTP con `/estado`, `/test/verde`, `/test/rojo` y `/test/apagar`.
- **App:** consulta el estado cada 1 s, envía comandos y guarda los resultados.
- **Firebase:** autenticación con correo y contraseña, roles y historial compartido entre teléfonos.

## Roles

| Rol | Monitoreo | Control | Historial | Crear cuentas |
|---|---|---|---|---|
| Administrador | Sí | Sí | Sí | Sí |
| Trabajador | Sí | Sí | Sí | No |

## Tecnologías

Android Studio · Kotlin · Jetpack Compose (Material 3) · Firebase Authentication · Cloud Firestore · WiFi (HTTP)

## Documentación

- Avances por etapa: [`docs/AVANCES.md`](docs/AVANCES.md)
- Capturas del mockup: `docs/img/`

## Cómo ejecutar

1. Clona el repositorio y ábrelo con Android Studio.
2. Agrega tu archivo `google-services.json` dentro de `app/` (no se sube al repositorio; se obtiene desde la consola de Firebase).
3. Sincroniza Gradle y ejecuta en un emulador o dispositivo con Android 8.0 (API 26) o superior.
4. Para el monitoreo en vivo, conecta el teléfono a la misma red WiFi que la placa.

## Seguridad (ISO 27400)

Se documenta en `docs/AVANCES.md` a medida que se implementa: cifrado de credenciales (Firebase Auth), control de acceso por rol (reglas de Firestore) y conexiones seguras (TLS hacia Firebase).