// MOCKUP - App Control señal PARE (IoT)
// Versión con login real (Firebase Authentication) y sin íconos en la barra inferior.
package com.example.deteccion_pare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ---------- Colores y datos de ejemplo ----------
private val Verde = Color(0xFF1B7F4B)
private val Rojo = Color(0xFFB3261E)
private val Azul = Color(0xFF1F5FBF)

data class Evento(val fecha: String, val seDetuvo: Boolean, val velocidadCms: Double)

private val eventosEjemplo = listOf(
    Evento("03/11 · 12:36", false, 23.4),
    Evento("03/11 · 12:34", true, 0.0),
    Evento("03/11 · 12:31", true, 0.0),
    Evento("03/11 · 12:27", false, 18.1)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Azul)) {
                PareApp()
            }
        }
    }
}

// ---------- Raíz: Login -> lectura del rol -> pantalla principal ----------
@Composable
fun PareApp() {
    var sesion by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
    if (!sesion) {
        LoginScreen(onLogin = { sesion = true })
    } else {
        RolGate(onSalir = {
            FirebaseAuth.getInstance().signOut()
            sesion = false
        })
    }
}

// Lee el rol del usuario desde Firestore: colección "usuarios", documento = UID
@Composable
fun RolGate(onSalir: () -> Unit) {
    var rol by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var intento by remember { mutableIntStateOf(0) }

    LaunchedEffect(intento) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            error = "No hay una sesión activa."
            cargando = false
            return@LaunchedEffect
        }
        cargando = true
        error = null
        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                android.util.Log.d("Rol", "uid=$uid existe=${doc.exists()} rol=[${doc.getString("rol")}]")
                val r = doc.getString("rol")?.trim()?.lowercase()
                if (r == "admin" || r == "trabajador") {
                    rol = r
                } else {
                    error = "Tu cuenta no tiene un rol válido. Habla con el administrador."
                }
                cargando = false
            }
            .addOnFailureListener { e ->
                android.util.Log.w("Rol", "No se pudo leer el rol", e)
                error = "No se pudo leer tu perfil. Revisa tu conexión."
                cargando = false
            }
    }

    val rolActual = rol
    when {
        cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        rolActual != null -> MainScreen(rol = rolActual, onSalir = onSalir)
        else -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(error ?: "Ocurrió un error.", color = Rojo)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { intento++ }) { Text("Reintentar") }
            TextButton(onClick = onSalir) { Text("Salir") }
        }
    }
}

// ---------- 1. Login con Firebase Authentication ----------
@Composable
fun LoginScreen(onLogin: () -> Unit) {
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Control señal PARE", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "Inicia sesión para monitorear y controlar el dispositivo.",
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        OutlinedTextField(
            value = correo, onValueChange = { correo = it },
            label = { Text("Correo electrónico") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = clave, onValueChange = { clave = it },
            label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Text(it, color = Rojo, modifier = Modifier.padding(top = 8.dp))
        }
        TextButton(onClick = { /* TODO: recuperar contraseña */ }) {
            Text("¿Olvidaste tu contraseña?")
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (correo.isBlank() || clave.isBlank()) {
                    error = "Ingresa correo y contraseña."
                } else {
                    cargando = true
                    error = null
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(correo.trim(), clave)
                        .addOnCompleteListener { tarea ->
                            cargando = false
                            if (tarea.isSuccessful) {
                                onLogin()
                            } else {
                                android.util.Log.w("Login", "Falló el inicio de sesión", tarea.exception)
                                error = "Correo o contraseña incorrectos."
                            }
                        }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text(if (cargando) "Ingresando..." else "Iniciar sesión", fontSize = 16.sp) }
        Text(
            "Las cuentas las crea el administrador · Conexión cifrada (TLS)",
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 24.dp).align(Alignment.CenterHorizontally)
        )
    }
}

// ---------- Pantalla principal con barra inferior ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rol: String, onSalir: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    var ultimo by remember { mutableStateOf(eventosEjemplo.first()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Control PARE", fontWeight = FontWeight.Bold)
                        Text(if (rol == "admin") "Administrador" else "Trabajador", fontSize = 12.sp)
                    }
                },
                actions = { TextButton(onClick = onSalir) { Text("Salir") } }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = {},
                    label = { Text("Monitoreo") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = {},
                    label = { Text("Control") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = {},
                    label = { Text("Historial") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                0 -> MonitoreoScreen(
                    ultimo = ultimo,
                    // Solo para la demo del mockup: alterna entre los dos resultados posibles
                    onSimular = { ultimo = if (ultimo.seDetuvo) eventosEjemplo[0] else eventosEjemplo[1] }
                )
                1 -> ControlScreen()
                else -> HistorialScreen()
            }
        }
    }
}

// ---------- 2. Monitoreo (datos en vivo) ----------
@Composable
fun MonitoreoScreen(ultimo: Evento, onSimular: () -> Unit) {
    val color = if (ultimo.seDetuvo) Verde else Rojo
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Último resultado · hace 12 s", fontSize = 12.sp)
                    Text(
                        if (ultimo.seDetuvo) "SE DETUVO" else "NO SE DETUVO",
                        color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold
                    )
                    Text("Velocidad estimada: ${ultimo.velocidadCms} cm/s")
                    Text(
                        "Semáforo: ${if (ultimo.seDetuvo) "verde" else "rojo"} · Bocina: ${if (ultimo.seDetuvo) "apagada" else "activada"}",
                        fontSize = 13.sp
                    )
                }
                Semaforo(rojo = !ultimo.seDetuvo, verde = ultimo.seDetuvo)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DatoCard("Distancia (HC-SR04)", if (ultimo.seDetuvo) "6 cm" else "12 cm", "Auto en zona: Sí", Modifier.weight(1f))
            DatoCard("Sensor touch", if (ultimo.seDetuvo) "Contacto" else "Sin contacto", "Línea de PARE", Modifier.weight(1f))
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Dispositivo", fontSize = 12.sp)
                Text("Arduino UNO R4 WiFi · IP [192.168.x.x]", fontWeight = FontWeight.Bold)
                Text("Conectado · actualización cada 1 s", fontSize = 13.sp, color = Verde)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("8 se detuvieron", color = Verde, fontWeight = FontWeight.Bold)
                Text("3 infracciones", color = Rojo, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedButton(onClick = onSimular, modifier = Modifier.fillMaxWidth()) {
            Text("Simular otra lectura (solo mockup)")
        }
    }
}

@Composable
fun DatoCard(titulo: String, valor: String, detalle: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, fontSize = 12.sp)
            Text(valor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(detalle, fontSize = 13.sp)
        }
    }
}

@Composable
fun Semaforo(rojo: Boolean, verde: Boolean) {
    Column(
        modifier = Modifier.background(Color(0xFF12202B), RoundedCornerShape(10.dp)).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Luz(if (rojo) Color(0xFFE5483F) else Color(0xFF3A4650))
        Luz(Color(0xFF3A4650)) // amarillo: sin uso en tu circuito
        Luz(if (verde) Color(0xFF3DDC84) else Color(0xFF3A4650))
    }
}

@Composable
private fun Luz(color: Color) {
    Box(Modifier.size(20.dp).background(color, CircleShape))
}

// ---------- 3. Control de dispositivo ----------
@Composable
fun ControlScreen() {
    var estado by remember { mutableStateOf("Apagado") }
    var respuesta by remember { mutableStateOf("Sin comandos enviados") }
    var confirmar by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Estado actual del semáforo", fontSize = 12.sp)
                Text(estado, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Modo prueba: no afecta el registro de infracciones", fontSize = 13.sp)
            }
        }

        Button(
            onClick = {
                estado = "Verde"; respuesta = "Acción confirmada: GET /test/verde"
            },
            colors = ButtonDefaults.buttonColors(containerColor = Verde),
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)
        ) { Text("Semáforo en verde", fontSize = 16.sp) }

        Button(
            onClick = { confirmar = true },
            colors = ButtonDefaults.buttonColors(containerColor = Rojo),
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)
        ) { Text("Semáforo en rojo + bocina", fontSize = 16.sp) }

        OutlinedButton(
            onClick = {
                estado = "Apagado"; respuesta = "Acción confirmada: GET /test/apagar"
            },
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)
        ) { Text("Apagar todo", fontSize = 16.sp) }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Respuesta del dispositivo", fontSize = 12.sp)
                Text(respuesta)
            }
        }
        Text("Solo el rol Administrador puede enviar comandos.", fontSize = 13.sp)
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Activar bocina") },
            text = { Text("Se encenderá el semáforo en rojo y sonará la bocina. ¿Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    estado = "Rojo + bocina"; respuesta = "Acción confirmada: GET /test/rojo"
                    confirmar = false
                }) { Text("Activar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}

// ---------- 4. Historial ----------
@Composable
fun HistorialScreen() {
    var filtro by remember { mutableIntStateOf(0) } // 0 todos, 1 se detuvo, 2 no se detuvo
    val lista = when (filtro) {
        1 -> eventosEjemplo.filter { it.seDetuvo }
        2 -> eventosEjemplo.filter { !it.seDetuvo }
        else -> eventosEjemplo
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filtro == 0, onClick = { filtro = 0 }, label = { Text("Todos") })
            FilterChip(selected = filtro == 1, onClick = { filtro = 1 }, label = { Text("Se detuvo") })
            FilterChip(selected = filtro == 2, onClick = { filtro = 2 }, label = { Text("No se detuvo") })
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(lista) { e ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                if (e.seDetuvo) "SE DETUVO" else "NO SE DETUVO",
                                color = if (e.seDetuvo) Verde else Rojo,
                                fontWeight = FontWeight.Bold
                            )
                            Text(e.fecha, fontSize = 13.sp)
                        }
                        Text("${e.velocidadCms} cm/s", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}