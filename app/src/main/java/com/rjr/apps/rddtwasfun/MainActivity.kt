package com.rjr.apps.rddtwasfun

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rjr.apps.rddtwasfun.ui.theme.RddtWasFunTheme

class MainActivity : ComponentActivity() {

    companion object {
        var developerModeEnabled = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RddtWasFunTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content()
                }
            }
        }
    }

    @Composable
    fun Content(modifier: Modifier = Modifier) {
        var clicks by remember { mutableStateOf(0) }

        LazyColumn(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                item {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontSize = 42.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .clickable {
                                if (clicks == 2) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        R.string.developer_mode_enabled,
                                        Toast.LENGTH_LONG
                                    ).show()

                                    developerModeEnabled = true
                                } else if (clicks < 2) {
                                    clicks++
                                }
                            }
                    )
                }

                item {
                    Text(
                        text = stringResource(id = R.string.app_settings_instructions),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(R.drawable.pixel_settings_1),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(
                                    start = 16.dp,
                                    end = 4.dp
                                )
                                .weight(1f)
                        )

                        Image(
                            painter = painterResource(R.drawable.pixel_settings_2),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(
                                    start = 4.dp,
                                    end = 16.dp
                                )
                                .weight(1f)
                        )
                    }
                }

                item {
                    Button(
                        modifier = Modifier.padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.orange)
                        ),
                        onClick = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:$packageName")
                                ).apply {
                                    addCategory(Intent.CATEGORY_DEFAULT)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.app_settings_button),
                            color = Color.White
                        )
                    }
                }
            }
        )
    }

    @Preview(name = "Dark mode", showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Preview(name = "Light mode", showBackground = true, showSystemUi = true)
    @Composable
    private fun Preview() {
        RddtWasFunTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Content()
            }
        }
    }
}
