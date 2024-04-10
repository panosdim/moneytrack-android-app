package com.panosdim.moneytrack.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.paddingExtraLarge
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.prefs
import com.panosdim.moneytrack.ui.AppLogo
import com.panosdim.moneytrack.ui.theme.MoneyTrackTheme
import com.panosdim.moneytrack.viewmodels.LoginViewModel
import kotlinx.coroutines.launch


class LoginActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val loginViewModel: LoginViewModel = viewModel()
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var isLoading by remember {
                mutableStateOf(false)
            }

            fun isEmailValid(): Boolean {
                if (email.isEmpty()) {
                    return false
                }
                return Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }

            fun isPasswordValid(): Boolean {
                if (password.isEmpty() || password.length < 5) {
                    return false
                }
                return true
            }

            fun isFormValid(): Boolean {
                return isEmailValid() && isPasswordValid()
            }

            fun login() {
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        loginViewModel.login(email, password).collect { response ->
                            when (response) {
                                is Response.Success -> {
                                    isLoading = false
                                    prefs.email = email
                                    prefs.password = password
                                    prefs.token = response.data.token

                                    val intent =
                                        Intent(this@LoginActivity, MainActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)

                                    //Complete and destroy login activity once successful
                                    finish()
                                }

                                is Response.Error -> {
                                    Toast.makeText(
                                        applicationContext,
                                        response.errorMessage,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                                    isLoading = false
                                }

                                is Response.Loading -> {
                                    isLoading = true
                                }
                            }
                        }
                    }
                }
            }

            MoneyTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .imePadding()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(paddingLarge)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = paddingLarge)
                                    .padding(bottom = paddingExtraLarge)
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(top = paddingLarge)
                                        .fillMaxWidth(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    text = stringResource(id = R.string.app_name),
                                    textAlign = TextAlign.Center
                                )

                                AppLogo()

                                Text(
                                    modifier = Modifier
                                        .padding(top = paddingLarge)
                                        .fillMaxWidth(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center,
                                    text = stringResource(id = R.string.login)
                                )

                                OutlinedTextField(
                                    value = email,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email"
                                        )
                                    },
                                    isError = !isEmailValid(),
                                    supportingText = {
                                        if (!isEmailValid()) {
                                            Text(
                                                modifier = Modifier.fillMaxWidth(),
                                                text = "Email is not valid.",
                                                textAlign = TextAlign.End,
                                            )
                                        }
                                    },
                                    onValueChange = { email = it },
                                    label = { Text(stringResource(id = R.string.email)) },
                                    modifier = Modifier
                                        .padding(top = paddingLarge)
                                        .fillMaxWidth()
                                )

                                val keyboardController = LocalSoftwareKeyboardController.current

                                var isPasswordVisible by remember {
                                    mutableStateOf(false)
                                }

                                OutlinedTextField(
                                    modifier = Modifier
                                        .padding(top = paddingLarge)
                                        .fillMaxWidth(),
                                    value = password,
                                    onValueChange = { password = it },
                                    label = {
                                        Text(stringResource(id = R.string.password))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Password,
                                            contentDescription = "Password"
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            isPasswordVisible = !isPasswordVisible
                                        }) {

                                            val visibleIconAndText = Pair(
                                                first = Icons.Outlined.Visibility,
                                                second = stringResource(id = R.string.icon_password_visible)
                                            )

                                            val hiddenIconAndText = Pair(
                                                first = Icons.Outlined.VisibilityOff,
                                                second = stringResource(id = R.string.icon_password_hidden)
                                            )

                                            val passwordVisibilityIconAndText =
                                                if (isPasswordVisible) visibleIconAndText else hiddenIconAndText

                                            // Render Icon
                                            Icon(
                                                imageVector = passwordVisibilityIconAndText.first,
                                                contentDescription = passwordVisibilityIconAndText.second
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        keyboardController?.hide()
                                        if (isFormValid() && !isLoading) {
                                            login()
                                        }
                                    }),
                                    isError = !isPasswordValid(),
                                    supportingText = {
                                        if (!isPasswordValid()) {
                                            Text(
                                                modifier = Modifier.fillMaxWidth(),
                                                text = "Password length must be more than 5.",
                                                textAlign = TextAlign.End,
                                            )
                                        }
                                    },
                                )

                                if (isLoading) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = paddingLarge)
                                    )
                                }

                                Button(
                                    modifier = Modifier.padding(top = paddingLarge),
                                    enabled = isFormValid() && !isLoading,
                                    onClick = { login() },
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text(stringResource(id = R.string.login))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}