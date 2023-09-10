package com.imzhiqiang.elkclone.ui.dialog

import android.icu.util.Currency
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imzhiqiang.elkclone.MainViewModel
import com.imzhiqiang.elkclone.R
import com.imzhiqiang.elkclone.formatToStr
import com.imzhiqiang.elkclone.parseToDouble
import com.imzhiqiang.elkclone.ui.theme.ElkCloneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDialog(onDismissRequest: () -> Unit, viewModel: MainViewModel = viewModel()) {

    val currencyRate by viewModel.currencyRate.collectAsStateWithLifecycle()
    var sourceAmount by remember { mutableDoubleStateOf(1.0) }
    var targetAmount by remember { mutableDoubleStateOf(1.0) }
    var isTargetFocused by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismissRequest) {
        Card {

            val locale = LocalConfiguration.current.locales[0]

            Box {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text(
                            text = Currency.getInstance(currencyRate.source).getDisplayName(locale)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = if (isTargetFocused) (targetAmount / currencyRate.rate).formatToStr() else sourceAmount.formatToStr(),
                            onValueChange = { v ->
                                sourceAmount = v.parseToDouble()
                                targetAmount = sourceAmount * currencyRate.rate
                            },
                            prefix = {
                                Text(
                                    text = Currency.getInstance(currencyRate.source)
                                        .getSymbol(locale),
                                    fontSize = 14.sp,
                                )
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                        )
                    }

                    Icon(
                        painter = painterResource(id = R.drawable.ic_double_down),
                        contentDescription = "",
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text(
                            text = Currency.getInstance(currencyRate.target).getDisplayName(locale)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = if (isTargetFocused) targetAmount.formatToStr() else (sourceAmount * currencyRate.rate).formatToStr(),
                            onValueChange = { v ->
                                targetAmount = v.parseToDouble()
                                sourceAmount = targetAmount / currencyRate.rate
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                            prefix = {
                                Text(
                                    text = Currency.getInstance(currencyRate.target)
                                        .getSymbol(locale),
                                    fontSize = 14.sp,
                                )
                            },
                            modifier = Modifier.onFocusChanged {
                                isTargetFocused = it.isFocused
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                IconButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditDialogPreview() {
    ElkCloneTheme {
        EditDialog(onDismissRequest = {})
    }
}