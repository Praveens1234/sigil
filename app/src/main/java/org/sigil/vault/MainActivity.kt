package org.sigil.vault

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sigil.vault.security.LockManager

/** Single activity and Compose navigation boundary. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blank the recents snapshot: no vault content is exposed.
        setTaskDescription(android.app.ActivityManager.TaskDescription(null, null))
        setContent {
            SigilTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SigilRoot()
                }
            }
        }
    }

    override fun onStop() {
        LockManager.get(this).onStop()
        super.onStop()
    }
}

@Composable
private fun SigilRoot() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SIGIL", color = Color(0xFF2DD4BF), fontSize = 34.sp, letterSpacing = 6.sp)
            Text(
                "Offline. Sealed. Yours.",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
            Button(
                onClick = { /* Authentication and vault shell follow after CI gates. */ },
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text("Get started")
            }
        }
    }
}

@Composable
private fun SigilTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFF2DD4BF),
            background = Color.Black,
            surface = Color(0xFF0C0C0E),
            onPrimary = Color(0xFF00110E),
            onBackground = Color(0xFFF2F3F5),
            onSurface = Color(0xFFF2F3F5)
        ),
        content = content
    )
}
