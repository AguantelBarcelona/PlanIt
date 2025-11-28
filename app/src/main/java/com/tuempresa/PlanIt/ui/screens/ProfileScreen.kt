package com.tuempresa.PlanIt.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tuempresa.PlanIt.R
import com.tuempresa.PlanIt.ui.viewmodel.AuthViewModel
import com.tuempresa.PlanIt.ui.viewmodel.UserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val userState by viewModel.userState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Launcher for picking an image from the gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // viewModel.updateProfilePicture(it.toString())
            }
        }
    )

    // Dialog states
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            userState?.let {
                ProfileHeader(it, onEditClick = { galleryLauncher.launch("image/*") })
                Spacer(modifier = Modifier.height(32.dp))
                ProfileStats(it)
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            ProfileOptions(viewModel, onLogoutClick = { showLogoutDialog = true }, onResetClick = { showResetDialog = true })
        }

        if (showLogoutDialog) {
            ConfirmDialog(
                title = "Cerrar Sesión",
                text = "¿Estás seguro de que quieres cerrar sesión?",
                icon = Icons.Default.ExitToApp,
                onConfirm = { 
                    viewModel.logout()
                    showLogoutDialog = false
                },
                onDismiss = { showLogoutDialog = false }
            )
        }

        if (showResetDialog) {
            ConfirmDialog(
                title = "Restablecer Aplicación",
                text = "Esta acción es irreversible y eliminará todas tus tareas y datos de la cuenta. ¿Estás seguro?",
                icon = Icons.Default.Warning,
                confirmButtonColor = MaterialTheme.colorScheme.error,
                onConfirm = { 
                    // viewModel.resetApp()
                    showResetDialog = false
                },
                onDismiss = { showResetDialog = false }
            )
        }
    }
}

@Composable
fun ProfileHeader(user: UserState, onEditClick: () -> Unit) {
    Box {
        AsyncImage(
            model = user.profilePictureUrl ?: R.drawable.ic_launcher_background, // Use placeholder if no image
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onEditClick() },
            contentScale = ContentScale.Crop
        )
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Editar foto",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(user.displayName ?: user.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun ProfileStats(user: UserState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(user.totalTasks.toString(), style = MaterialTheme.typography.headlineMedium)
            Text("Tareas Creadas", style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(user.completedTasks.toString(), style = MaterialTheme.typography.headlineMedium)
            Text("Tareas Completadas", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ProfileOptions(viewModel: AuthViewModel, onLogoutClick: () -> Unit, onResetClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider()
        ProfileOptionItem(icon = Icons.Default.Settings, text = "Editar Perfil", onClick = { /* Navigate to edit screen */ })
        ProfileOptionItem(icon = Icons.Default.Info, text = "Restablecer Datos", onClick = onResetClick)
        Divider()
        ProfileOptionItem(icon = Icons.Default.ExitToApp, text = "Cerrar Sesión", onClick = onLogoutClick, isDestructive = true)
    }
}

@Composable
fun ProfileOptionItem(icon: Any, text: String, onClick: () -> Unit, isDestructive: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()

            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon( 
            imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector, 
            contentDescription = null, 
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text, 
            color = if (isDestructive) MaterialTheme.colorScheme.error else LocalContentColor.current,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    icon: Any,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonColor: androidx.compose.ui.graphics.Color = ButtonDefaults.textButtonColors().contentColor
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon as androidx.compose.ui.graphics.vector.ImageVector, contentDescription = null) },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = confirmButtonColor)
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}