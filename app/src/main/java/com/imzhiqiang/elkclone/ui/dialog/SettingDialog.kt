package com.imzhiqiang.elkclone.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imzhiqiang.elkclone.CurrencyRate
import com.imzhiqiang.elkclone.R
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDialog(
    onDismissRequest: () -> Unit,
    onSwitchCurrencyClick: () -> Unit,
    onSelectCurrencyClick: () -> Unit,
    currencyRate: CurrencyRate,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {

        Text(
            text = stringResource(
                R.string.current_rate,
                currencyRate.rate,
                currencyRate.source,
                currencyRate.rate,
                currencyRate.target
            ),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(R.string.last_update_time) + currencyRate.updateAt.format(
                DateTimeFormatter.ofPattern("MM-dd HH:mm")
            ),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .clickable {
                    onSwitchCurrencyClick()
                    onDismissRequest()
                }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_switch),
                contentDescription = "Switch"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(R.string.exchange))
        }

        Row(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .clickable {
                    onSelectCurrencyClick()
                    onDismissRequest()
                }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_list),
                contentDescription = "List"
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(R.string.select_other_currency))
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}