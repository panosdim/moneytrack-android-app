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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun EditCategorySheet(
    categoryItem: Category,
    categories: List<Category>,
    bottomSheetState: SheetState,
) {
    if (bottomSheetState.isVisible) {
        val context = LocalContext.current
        val viewModel: CategoriesViewModel = viewModel()
        val categoryName = remember { FieldState(categoryItem.category) }
        val scope = rememberCoroutineScope()
        val openDeleteDialog = remember { mutableStateOf(false) }
        var isLoading by remember {
            mutableStateOf(false)
        }

        if (openDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDeleteDialog.value = false
                },
                title = {
                    Text(text = stringResource(id = R.string.delete_category_dialog_title))
                },
                text = {
                    Text(
                        stringResource(id = R.string.delete_category_dialog_description)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false

                            scope.launch {
                                viewModel.removeCategory(categoryItem).collect {
                                    when (it) {
                                        is Response.Success -> {
                                            isLoading = false

                                            Toast.makeText(
                                                context, R.string.delete_toast,
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
                        }
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                        }
                    ) {
                        Text(stringResource(id = R.string.dismiss))
                    }
                }
            )
        }

        fun validateCategory() {
            if (categoryName.value != categoryItem.category) {
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
        }

        fun isFormValid(): Boolean {
            // Check if has something changed in category name
            if (categoryName.value == categoryItem.category) {
                return false
            }
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
                    stringResource(
                        id = R.string.edit_category
                    ),
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { openDeleteDialog.value = true },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.delete))
                    }

                    Button(
                        enabled = isFormValid(),
                        onClick = {
                            categoryItem.category = categoryName.value

                            scope.launch {
                                viewModel.updateCategory(categoryItem).collect {
                                    when (it) {
                                        is Response.Success -> {
                                            isLoading = false

                                            Toast.makeText(
                                                context, R.string.category_updated,
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
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.update))
                    }
                }
            }
        }
    }
}