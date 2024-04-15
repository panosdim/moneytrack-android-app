package com.panosdim.moneytrack.ui.categories

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.FieldState
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.utils.removeEmojis
import com.panosdim.moneytrack.viewmodels.CategoriesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(categories: List<Category>, bottomSheetState: SheetState) {
    if (bottomSheetState.isVisible) {
        val context = LocalContext.current
        val viewModel: CategoriesViewModel = viewModel()
        val categoryName = remember { FieldState("") }
        val scope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            if (categoryName.value.isEmpty()) {
                focusRequester.requestFocus()
            }
        }

        var isLoading by remember {
            mutableStateOf(false)
        }

        fun validateCategory() {
            categoryName.removeError()

            if (categoryName.value.isEmpty()) {
                categoryName.setError(App.instance.getString(R.string.category_error_empty_name))
            } else {
                // Check if existing category has the same name
                if (categories.find {
                        removeEmojis(it.category).equals(categoryName.value, ignoreCase = true)
                    } != null) {
                    categoryName.setError(
                        App.instance.getString(R.string.category_error_already_exist)
                    )
                }
            }
        }

        fun isFormValid(): Boolean {
            return !categoryName.hasError
        }

        validateCategory()

        ModalBottomSheet(
            onDismissRequest = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingLarge)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(id = R.string.add_category),
                    style = MaterialTheme.typography.headlineMedium
                )

                OutlinedTextField(
                    value = categoryName.value,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    isError = categoryName.hasError,
                    supportingText = {
                        if (categoryName.hasError) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = categoryName.errorMessage,
                                textAlign = TextAlign.End,
                            )
                        }
                    },
                    onValueChange = {
                        categoryName.value = it
                        validateCategory()
                    },
                    label = { Text(stringResource(id = R.string.category_name)) },
                    modifier = Modifier
                        .padding(bottom = paddingLarge)
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = paddingLarge)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        enabled = isFormValid() && !isLoading,
                        onClick = {
                            val newCategory = Category(
                                null,
                                categoryName.value,
                                0
                            )

                            scope.launch {
                                viewModel.addCategory(newCategory).collect {
                                    when (it) {
                                        is Response.Success -> {
                                            isLoading = false

                                            Toast.makeText(
                                                context, R.string.category_added,
                                                Toast.LENGTH_LONG
                                            ).show()

                                            scope.launch {
                                                bottomSheetState.hide()
                                            }
                                        }

                                        is Response.Error -> {
                                            Toast.makeText(
                                                context,
                                                it.errorMessage,
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
                        },
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.create))
                    }
                }
            }
        }
    }
}