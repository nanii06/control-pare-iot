# Bitácora de avances

Cada parte se cierra con un commit y una etiqueta (`parte-0`, `parte-1`, ...). Al terminar una parte, completa su sección.

## Resumen

| Parte | Tema | Estado |
|---|---|---|
| 0 | Mockup de la app (pantallas y navegación) | Hecho |
| 1 | Proyecto base y Firebase | En curso |
| 2 | Login con Firebase y roles | Pendiente |
| 3 | Conexión con la placa (monitoreo y control) | Pendiente |
| 4 | Historial en Firestore en tiempo real | Pendiente |
| 5 | Seguridad (ISO 27400) y pruebas | Pendiente |
| 6 | Demostración y entrega | Pendiente |

---

## Parte 0 – Mockup

**Fecha:** [fecha]

**Qué se hizo**
- Se diseñaron las pantallas Login, Monitoreo, Control, Historial y Usuarios.
- Hay dos versiones por rol: el administrador tiene 4 pestañas y el trabajador 3 (sin Usuarios).
- Se armó un esqueleto en Jetpack Compose con datos de ejemplo.

**Evidencia:** capturas en `docs/img/`.

---

## Parte 1 – Proyecto base y Firebase

**Fecha:** 10/11/2026

**Qué se hizo**
- [ ] Proyecto creado en Android Studio (Empty Activity, Compose, Kotlin).
- [ ] Proyecto creado en Firebase y app Android registrada.
- [ ] `google-services.json` agregado en `app/` (no se sube al repositorio).
- [ ] Plugin de Google Services y dependencias de Firebase (Auth y Firestore) agregados.
- [ ] Authentication con correo y contraseña activado.
- [ ] Firestore creado y cuenta de administrador inicial registrada en la colección `usuarios`.

**Decisiones y justificación**
- Firebase Authentication: [justificación breve: seguridad, escalabilidad, facilidad].
- Firestore: [justificación: consulta desde varios teléfonos y en tiempo real].

**Cómo se probó:** [qué se verificó y resultado].

**Problemas encontrados:** [si hubo errores y cómo se resolvieron].