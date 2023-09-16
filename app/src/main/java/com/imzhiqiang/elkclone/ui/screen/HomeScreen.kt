package com.imzhiqiang.elkclone.ui.screen

import android.icu.text.NumberFormat
import android.icu.util.Currency
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.imzhiqiang.elkclone.MainViewModel
import com.imzhiqiang.elkclone.R
import com.imzhiqiang.elkclone.toast
import com.imzhiqiang.elkclone.ui.dialog.EditDialog
import com.imzhiqiang.elkclone.ui.dialog.SettingDialog
import com.imzhiqiang.elkclone.ui.theme.ElkCloneTheme
import java.lang.Integer.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = viewModel()
) {
    val locale = LocalConfiguration.current.locales[0]
    val numberFormat = NumberFormat.getInstance()
    val currencyRate by viewModel.currencyRate.collectAsStateWithLifecycle()
    val sourceCurrency by remember {
        derivedStateOf {
            Currency.getInstance(currencyRate.source).getDisplayName(locale)
        }
    }
    val targetCurrency by remember {
        derivedStateOf {
            Currency.getInstance(currencyRate.target).getDisplayName(locale)
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showSettingDialog by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$sourceCurrency -> $targetCurrency",
                            fontSize = 16.5.sp,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.current_rate,
                                currencyRate.rate,
                                currencyRate.source,
                                currencyRate.rate,
                                currencyRate.target
                            ),
                            fontSize = 10.sp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(onClick = {
                        showSettingDialog = true
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshCurrencyRate() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }) {
        val topPadding = it.calculateTopPadding()

        val hapticFeedback = LocalHapticFeedback.current

        var drag by remember { mutableFloatStateOf(0f) }
        var multiple by rememberSaveable { mutableIntStateOf(1) }
        var isExpanded by remember { mutableStateOf(false) }

        val amountValues by remember {
            derivedStateOf { AmountValue.get(multiple).toMutableStateList() }
        }

        Layout(
            content = {
                amountValues.forEach { amountItem ->
                    CurrencyAmountItem(
                        amountValue = amountItem,
                        rate = currencyRate.rate,
                        offset = drag,
                        numberFormat = numberFormat,
                        onItemClick = { current ->
                            val index = amountValues.indexOf(current)
                            val next = amountValues.getOrNull(index + 1)
                                ?: current.copy(amount = current.amount * 2)
                            isExpanded = !isExpanded
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (isExpanded) {
                                amountValues.clear()
                                amountValues.add(current)
                                amountValues.addAll(current.getMiddleValues(next))
                                amountValues.add(next)
                            } else {
                                amountValues.clear()
                                amountValues.addAll(AmountValue.get(multiple))
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(onDragStart = {
                        if (!isExpanded) {
                            drag = 0f
                        }
                    }, onDragEnd = {
                        if (!isExpanded) {
                            var hapticFeedbackAgain = false
                            if (drag > 50) {
                                multiple = (multiple / 10).coerceAtLeast(1)
                                hapticFeedbackAgain = multiple == 1
                            } else if (drag < -50) {
                                multiple = (multiple * 10).coerceAtMost(100_000)
                                hapticFeedbackAgain = multiple == 100_000
                            }
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (hapticFeedbackAgain) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            drag = 0f
                        }
                    }, onHorizontalDrag = { change, dragAmount ->
                        if (!isExpanded) {
                            change.consume()
                            drag += dragAmount
                        }
                    })
                }
        ) { measurables, constraints ->
            val dividedHeight = constraints.maxHeight / measurables.size
            val height = max(56.dp.roundToPx(), dividedHeight)
            val itemConstraints = constraints.copy(minHeight = height, maxHeight = height)
            val placeables = measurables.map { measurable ->
                // Measure each children
                measurable.measure(itemConstraints)
            }
            layout(constraints.maxWidth, constraints.maxHeight) {
                // Track the y co-ord we have placed children up to
                var yPosition = 0

                // Place children in the parent layout
                placeables.forEach { placeable ->
                    // Position item on the screen
                    placeable.place(x = 0, y = yPosition)

                    // Record the y co-ord placed up to
                    yPosition += placeable.height
                }
            }
        }
    }

    if (showEditDialog) {
        EditDialog(onDismissRequest = { showEditDialog = false })
    }

    if (showSettingDialog) {
        SettingDialog(
            onDismissRequest = {
                showSettingDialog = false
            },
            onSwitchCurrencyClick = {
                viewModel.switchCurrency()
            },
            onSelectCurrencyClick = {
                navController.navigate("currencylist")
            },
            currencyRate = currencyRate
        )
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(uiState) {
        if (!uiState.shouldUpdate) {
            context.toast(R.string.recent_updated)
        } else {
            if (uiState.success) {
                context.toast(R.string.update_rate_success)
            } else if (uiState.error != null) {
                context.toast(R.string.update_rate_failed, uiState.error?.localizedMessage)
            }
        }
        viewModel.consumeUiState()
    }
}

data class AmountValue(
    val amount: Double,
    val multiple: Int = 1,
    val isMiddleValue: Boolean = false
) {

    companion object {
        fun get(multiple: Int): List<AmountValue> {
            return (1..10).map { i -> AmountValue(i.toDouble(), multiple) }
        }
    }

    fun getValue() = amount * multiple

    fun getMiddleValues(next: AmountValue): List<AmountValue> {
        val step = (next.amount - this.amount) / 10
        return (1..9).map { AmountValue(amount + it * step, multiple, true) }
    }
}

@Composable
fun CurrencyAmountItem(
    amountValue: AmountValue,
    rate: Double,
    offset: Float,
    numberFormat: NumberFormat,
    modifier: Modifier = Modifier,
    onItemClick: (amountValue: AmountValue) -> Unit
) {
    val leftColor = MaterialTheme.colorScheme.secondaryContainer
    val rightColor = MaterialTheme.colorScheme.tertiaryContainer

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier
            .clickable { onItemClick(amountValue) }) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (amountValue.isMiddleValue) leftColor else leftColor.copy(
                            alpha = 0.7f
                        )
                    )
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = numberFormat.format(amountValue.getValue()),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 24.sp,
                    modifier = Modifier.offset {
                        IntOffset(
                            offset.roundToInt()
                                .coerceAtLeast(16.dp.roundToPx().unaryMinus())
                                .coerceAtMost(16.dp.roundToPx().unaryPlus()), 0
                        )
                    },
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (amountValue.isMiddleValue) rightColor else rightColor.copy(
                            alpha = 0.7f
                        )
                    )
                    .padding(start = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = numberFormat.format(amountValue.getValue() * rate),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontSize = 24.sp,
                    modifier = Modifier.offset {
                        IntOffset(
                            offset.roundToInt()
                                .coerceAtLeast(16.dp.roundToPx().unaryMinus())
                                .coerceAtMost(16.dp.roundToPx().unaryPlus()), 0
                        )
                    },
                )
            }
        }
        Divider(thickness = 0.3.dp)
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ElkCloneTheme {
        HomeScreen()
    }
}