package com.example.talktobook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.talktobook.ui.navigation.TalkToBookNavigation
import com.example.talktobook.ui.theme.TalkToBookTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TalkToBookTheme {
                TalkToBookNavigation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}