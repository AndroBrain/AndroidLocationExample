package com.androbrain.androidgpsexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var screens by remember { mutableStateOf(listOf(Screen.MENU)) }
            BackHandler {
                screens = screens.toMutableList().apply { removeLastOrNull() }
            }
            MaterialTheme {
                Surface {
                    Crossfade(targetState = screens.lastOrNull(), label = "ScreenCrossfade") {
                        when (it) {
                            Screen.MENU -> MenuScreen(
                                onPermission = {
                                    screens =
                                        screens.toMutableList().apply { add(Screen.PERMISSION) }
                                },
                                onLocation = {
                                    screens = screens.toMutableList().apply { add(Screen.LOCATION) }
                                }
                            )

                            Screen.PERMISSION -> PermissionScreen()
                            Screen.LOCATION -> LocationScreen()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}
