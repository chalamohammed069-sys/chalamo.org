package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.R
import com.example.ui.AuthState
import com.example.ui.BananaViewModel
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BananaYellow
import com.example.ui.theme.CardBg
import com.example.ui.theme.LightGray
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: BananaViewModel,
    modifier: Modifier = Modifier
) {
    var isSignUp by remember { mutableStateOf(false) }
    var isForgotPassword by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        // Overlay background graphics
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BananaYellow.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo
            Text(
                text = "🍌",
                fontSize = 72.sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "BANANA",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BananaYellow,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "The Premium AMOLED Social Platform",
                fontSize = 14.sp,
                color = NeonCyan,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Auth State Feedback
            when (val state = authState) {
                is AuthState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                is AuthState.Loading -> {
                    CircularProgressIndicator(
                        color = BananaYellow,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(bottom = 16.dp)
                    )
                }
                else -> {
                    statusMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = if (isSuccessMessage) NeonCyan else Color.Red,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }

            // Input Fields with Beautiful Theme Accents
            if (isForgotPassword) {
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    icon = Icons.Default.Email,
                    testTag = "auth_email_input"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.resetPassword(
                            email = email,
                            onSuccess = {
                                isSuccessMessage = true
                                statusMessage = "Password reset link sent to $email! Please check your spam folder."
                            },
                            onError = { err ->
                                isSuccessMessage = false
                                statusMessage = err
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BananaYellow, contentColor = AmoledBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_reset_btn")
                ) {
                    Text("Send Reset Link", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Back to Login",
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { isForgotPassword = false }
                        .padding(8.dp)
                )
            } else {
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    icon = Icons.Default.Email,
                    testTag = "auth_email_input"
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isSignUp) {
                    AuthTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username",
                        icon = Icons.Default.Person,
                        testTag = "auth_username_input"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AuthTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = "Full Name",
                        icon = Icons.Default.Badge,
                        testTag = "auth_displayname_input"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AuthTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = "Short Bio",
                        icon = Icons.Default.Edit,
                        testTag = "auth_bio_input"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = LightGray) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan) },
                    trailingIcon = {
                        val icon = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(icon, contentDescription = null, tint = LightGray)
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BananaYellow,
                        unfocusedBorderColor = CardBg,
                        focusedLabelColor = BananaYellow,
                        cursorColor = BananaYellow,
                        focusedTextColor = White,
                        unfocusedTextColor = White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input")
                )

                if (!isSignUp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = LightGray,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable { isForgotPassword = true }
                                .padding(4.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (isSignUp) {
                            viewModel.register(email, username, displayName, bio)
                        } else {
                            viewModel.login(email, password)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BananaYellow, contentColor = AmoledBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_submit_btn")
                ) {
                    Text(
                        text = if (isSignUp) "Create Account" else "Sign In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Authentication UI Simulation
                OutlinedButton(
                    onClick = {
                        // Google Authentication flow simulation
                        viewModel.login("google_user@gmail.com", "google_secure_123456")
                    },
                    border = BorderStroke(1.dp, NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_google_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = "Google",
                        tint = NeonCyan,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Continue with Google", color = White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Toggle Auth Mode
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                        color = LightGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isSignUp) "Sign In" else "Sign Up",
                        color = BananaYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { 
                                isSignUp = !isSignUp
                                statusMessage = null 
                            }
                            .padding(4.dp)
                            .testTag("auth_mode_toggle")
                    )
                }
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = LightGray) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = NeonCyan) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BananaYellow,
            unfocusedBorderColor = CardBg,
            focusedLabelColor = BananaYellow,
            cursorColor = BananaYellow,
            focusedTextColor = White,
            unfocusedTextColor = White
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
