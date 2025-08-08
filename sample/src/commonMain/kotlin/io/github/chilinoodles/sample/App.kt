package io.github.chilinoodles.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chilinoodles.cedar.logging.Cedar
import com.chilinoodles.cedar.logging.LogPriority
import com.chilinoodles.cedar.logging.LogTree
import com.chilinoodles.cedar.logging.trees.ConsoleTree
import com.chilinoodles.cedar.logging.trees.PlatformLogTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun App() {
    var logMessages by remember { mutableStateOf(listOf<LogMessage>()) }
    val uiTree = remember { UITree { logMessages = logMessages + it } }
    val scope = rememberCoroutineScope()

    // FIXME: Adjust landscape detection for wasmJs
//    val configuration = LocalConfiguration.current
//    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val isLandscape = false
    LaunchedEffect(Unit) {
        Cedar.plant(PlatformLogTree().configureForPlatform {
            iosSubsystem = "CedarLogger"
            enableEmojis = true
        })
        Cedar.plant(uiTree)
        Cedar.i("Cedar Logger initialized!")
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1976D2),
            surface = Color(0xFFF8F9FA),
            background = Color(0xFFFFFFFF),
            onPrimary = Color.White,
            onSurface = Color(0xFF1C1C1C),
            onBackground = Color(0xFF1C1C1C)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Simple Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌲",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Cedar Logger",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlsPanel(
                        modifier = Modifier.weight(1f),
                        scope = scope,
                        onClearLogs = { logMessages = emptyList() }
                    )

                    LogDisplayPanel(
                        modifier = Modifier.weight(1f),
                        logMessages = logMessages
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlsPanel(
                        modifier = Modifier.fillMaxWidth(),
                        scope = scope,
                        onClearLogs = { logMessages = emptyList() }
                    )

                    LogDisplayPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        logMessages = logMessages
                    )
                }
            }
        }
    }
}

@Composable
fun ControlsPanel(
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    onClearLogs: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SimpleButton(
                    text = "🔍 Verbose",
                    color = Color(0xFF757575),
                    modifier = Modifier.weight(1f)
                ) {
                    Cedar.v("Verbose message")
                }
                SimpleButton(
                    text = "🐛 Debug",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                ) {
                    Cedar.d("Debug message")
                }
                SimpleButton(
                    text = "ℹ️ Info",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                ) {
                    Cedar.i("Info message")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SimpleButton(
                    text = "⚠️ Warning",
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                ) {
                    Cedar.w("Warning message")
                }
                SimpleButton(
                    text = "❌ Error",
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                ) {
                    Cedar.e("Error message")
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SimpleButton(
                    text = "🌐 Network",
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                ) {
                    Cedar.tag("Network").i("API call successful")
                }
                SimpleButton(
                    text = "💾 Database",
                    color = Color(0xFF009688),
                    modifier = Modifier.weight(1f)
                ) {
                    Cedar.tag("Database").d("Query executed")
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            Cedar.tag("Performance").scope(LogPriority.DEBUG, "Async operation").use {
                                delay(800)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF795548)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("⏱️ Timer", color = Color.White, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onClearLogs,
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE53E3E)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🗑️ Clear", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun LogDisplayPanel(
    modifier: Modifier = Modifier,
    logMessages: List<LogMessage>
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Console",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF2A2A2A)
                ) {
                    Text(
                        text = "${logMessages.size}",
                        fontSize = 11.sp,
                        color = Color(0xFFBBBBBB),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (logMessages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🌟",
                            fontSize = 36.sp
                        )
                        Text(
                            text = "Ready to log!",
                            fontSize = 14.sp,
                            color = Color(0xFFBBBBBB),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logMessages.takeLast(MAX_LOG_MESSAGES_DISPLAYED).reversed()) { log ->
                        LogCard(log)
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LogCard(log: LogMessage) {
    val backgroundColor = when (log.priority) {
        LogPriority.VERBOSE -> Color(0xFF616161)
        LogPriority.DEBUG -> Color(0xFF1976D2)
        LogPriority.INFO -> Color(0xFF388E3C)
        LogPriority.WARNING -> Color(0xFFF57C00)
        LogPriority.ERROR -> Color(0xFFD32F2F)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = log.icon,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 1.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.message,
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )

                if (log.tag != "AppLogger") {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = backgroundColor.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = log.tag,
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Text(
                            text = log.priority.name,
                            fontSize = 8.sp,
                            color = Color(0xFFBBBBBB),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

data class LogMessage(
    val priority: LogPriority,
    val tag: String,
    val message: String,
    val icon: String
)

class UITree(private val onLog: (LogMessage) -> Unit) : LogTree {
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        val icon = when (priority) {
            LogPriority.VERBOSE -> "🔍"
            LogPriority.DEBUG -> "🐛"
            LogPriority.INFO -> "ℹ️"
            LogPriority.WARNING -> "⚠️"
            LogPriority.ERROR -> "❌"
        }

        val fullMessage = if (throwable != null) {
            "$message (${throwable.message})"
        } else {
            message
        }

        onLog(LogMessage(priority, tag, fullMessage, icon))
    }
}