package com.imzhiqiang.elkclone.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.imzhiqiang.elkclone.MainViewModel
import com.imzhiqiang.elkclone.R
import com.imzhiqiang.elkclone.data.Currency
import com.imzhiqiang.elkclone.ui.theme.ElkCloneTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CurrencyListScreen(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = viewModel()
) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        var query by rememberSaveable { mutableStateOf("") }
        val queryResult = viewModel.queryResult.collectAsState(initial = emptyList()).value
        val softwareKeyboardController = LocalSoftwareKeyboardController.current

        SearchBar(searchText = query,
            onSearchTextChange = {
                query = it
                viewModel.setQuery(it)
            },
            onCancelClick = {
                navController.popBackStack()
            },
            onSearchAction = {
            })

        if (query.isEmpty()) {
            FullCurrencyList(viewModel.fullCurrencyList) {
                viewModel.setTargetCurrency(it.currencyCode)
                softwareKeyboardController?.hide()
                navController.popBackStack()
            }
        } else {
            SearchResultList(queryResult) {
                viewModel.setTargetCurrency(it.currencyCode)
                softwareKeyboardController?.hide()
                navController.popBackStack()
            }
        }
    }

}

@Composable
fun SearchResultList(currencyList: List<Currency>, onCurrencyItemClick: (Currency) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(currencyList, key = { it.currencyCode }) { currency ->
            CurrencyRow(currency) {
                onCurrencyItemClick(currency)
            }
            Divider()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullCurrencyList(
    currencyListMap: Map<String, List<Currency>>,
    onCurrencyItemClick: (Currency) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        currencyListMap.forEach { (groupKey, currencyList) ->
            stickyHeader {
                CurrencyHeader(groupKey)
            }
            items(currencyList, key = { groupKey + it.currencyCode }) { currency ->
                CurrencyRow(currency) {
                    onCurrencyItemClick(currency)
                }
                Divider()
            }
        }
    }
}

@Composable
fun CurrencyHeader(key: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(color = MaterialTheme.colorScheme.primary)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (key == "hot") stringResource(id = R.string.hot) else key,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CurrencyRow(currency: Currency, onItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clickable {
                onItemClick()
            }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = currency.currencyCode, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = currency.getDisplayName())
    }
}

@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearchAction: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        Spacer(modifier = Modifier.width(10.dp))
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            value = TextFieldValue(
                text = searchText,
                selection = TextRange(searchText.length)
            ),
            onValueChange = {
                onSearchTextChange(it.text)
            },
            textStyle = TextStyle(fontSize = 16.sp),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                onSearchAction()
                focusManager.clearFocus()
            }),
            decorationBox = { innerTextField ->
                // Because the decorationBox is used, the whole Row gets the same behaviour as the
                // internal input field would have otherwise. For example, there is no need to add a
                // Modifier.clickable to the Row anymore to bring the text field into focus when user
                // taps on a larger text field area which includes paddings and the icon areas.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(30)
                        )
                        .padding(horizontal = 10.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search"
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()
                    }
                    if (searchText.isNotEmpty()) {
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            Icons.Filled.Clear,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    onSearchTextChange("")
                                },
                            contentDescription = "Clear"
                        )
                    }
                }
            })
        TextButton(onClick = {
            onCancelClick()
            focusManager.clearFocus()
        }) {
            Text(
                text = stringResource(id = R.string.cancel),
                fontSize = 17.sp
            )
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyListScreenPreview() {
    ElkCloneTheme {
        CurrencyListScreen()
    }
}