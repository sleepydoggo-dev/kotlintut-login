package com.example.kotlintut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlintut.ui.theme.Locales
import com.example.kotlintut.viewmodel.AuthUiState

@Composable
fun AuthGatewayScreen(
    language: String,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).pointerInput(Unit) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Fastfood,
            contentDescription = null,
            modifier = Modifier.size(140.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(translate("auth_title"), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(translate("auth_desc"), fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text(translate("login"), fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text(translate("register"), fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onCancelClick) {
            Text(translate("continue_as_guest"), color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authState: AuthUiState,
    language: String,
    onLogin: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(translate("login")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(translate("welcome_back"), fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            if (authState.error != null) {
                Text(authState.error, color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text(translate("username")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(translate("password")) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (authState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { onLogin(identifier, password) },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    enabled = identifier.isNotBlank() && password.isNotBlank()
                ) {
                    Text(translate("login"))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authState: AuthUiState,
    language: String,
    onRegister: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val translate = remember(language) { { key: String -> Locales.getString(key, language) } }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    // Password requirements logic
    val hasMinLength = password.length >= 8
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }
    val isPasswordValid = hasMinLength && hasUpperCase && hasLowerCase && hasDigit && hasSymbol
    val isEmailValid = email.contains("@") && email.contains(".")

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {},
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(translate("registration")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(translate("create_account"), fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            if (authState.error != null) {
                Text(authState.error, color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(translate("full_name")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(translate("username")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(translate("email")) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = email.isNotEmpty() && !isEmailValid
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(translate("password")) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = password.isNotEmpty() && !isPasswordValid
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Requirements Display
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(translate("password_requirements"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                RequirementItem(translate("req_length"), hasMinLength)
                RequirementItem(translate("req_upper"), hasUpperCase)
                RequirementItem(translate("req_lower"), hasLowerCase)
                RequirementItem(translate("req_digit"), hasDigit)
                RequirementItem(translate("req_symbol"), hasSymbol)
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            if (authState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { onRegister(username, email, password, fullName) },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    enabled = username.isNotBlank() && isEmailValid && isPasswordValid
                ) {
                    Text(translate("register"))
                }
            }
        }
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = if (isMet) Color(0xFF4CAF50) else Color.Gray)
    }
}
