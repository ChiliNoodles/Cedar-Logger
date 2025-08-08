package io.github.chilinoodles.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class InMemoryLogTree : LogTree {
    private val _logs = mutableStateListOf<LogEntry>()
    val logs: List<LogEntry> get() = _logs

    @OptIn(ExperimentalTime::class)
    override fun log(priority: LogPriority, tag: String, message: String, throwable: Throwable?) {
        val entry = LogEntry(
            priority = priority,
            tag = tag,
            message = message,
            throwable = throwable,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        _logs.add(0, entry)

        if (_logs.size > 100) {
            _logs.removeRange(100, _logs.size)
        }
    }
}

data class LogEntry(
    val priority: LogPriority,
    val tag: String,
    val message: String,
    val throwable: Throwable?,
    val timestamp: Long
)

@Composable
fun App() {
    val inMemoryTree = remember { InMemoryLogTree() }

    LaunchedEffect(Unit) {
        Cedar.plant(ConsoleTree())
        Cedar.plant(inMemoryTree)
    }

    MaterialTheme {
        Scaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Cedar Logger Sample",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Demonstrating Cedar's multiplatform logging capabilities",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                LoggingControls()

                Spacer(modifier = Modifier.height(8.dp))

                LogDisplay(
                    logs = inMemoryTree.logs,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun LoggingControls() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Test Logging",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LogButton(
                    text = "Verbose",
                    color = Color(0xFF9E9E9E),
                    onClick = {
                        Cedar.v("This is a verbose message", null)
                    }
                )
                LogButton(
                    text = "Debug",
                    color = Color(0xFF2196F3),
                    onClick = {
                        Cedar.d("Debug information here", null)
                    }
                )
                LogButton(
                    text = "Info",
                    color = Color(0xFF4CAF50),
                    onClick = {
                        Cedar.i("Informational message", null)
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LogButton(
                    text = "Warning",
                    color = Color(0xFFFF9800),
                    onClick = {
                        Cedar.w("This is a warning message", null)
                    }
                )
                LogButton(
                    text = "Error",
                    color = Color(0xFFF44336),
                    onClick = {
                        Cedar.e("An error occurred", RuntimeException("Sample error"))
                    }
                )
                LogButton(
                    text = "Multiple",
                    color = Color(0xFF9C27B0),
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            Cedar.d("Starting batch logging...")
                            delay(100)
                            Cedar.i("Processing item 1")
                            delay(100)
                            Cedar.i("Processing item 2")
                            delay(100)
                            Cedar.w("Warning: item 3 has issues")
                            delay(100)
                            Cedar.i("Processing item 4")
                            delay(100)
                            Cedar.d("Batch logging completed")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RowScope.LogButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LogDisplay(
    logs: List<LogEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Text(
                text = "Live Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
            )

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No logs yet. Click the buttons above to generate logs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { logEntry ->
                        LogEntryItem(logEntry)
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LogEntryItem(logEntry: LogEntry) {
    val priorityColor = when (logEntry.priority) {
        LogPriority.VERBOSE -> Color(0xFF9E9E9E)
        LogPriority.DEBUG -> Color(0xFF2196F3)
        LogPriority.INFO -> Color(0xFF4CAF50)
        LogPriority.WARNING -> Color(0xFFFF9800)
        LogPriority.ERROR -> Color(0xFFF44336)
    }

    val priorityText = when (logEntry.priority) {
        LogPriority.VERBOSE -> "V"
        LogPriority.DEBUG -> "D"
        LogPriority.INFO -> "I"
        LogPriority.WARNING -> "W"
        LogPriority.ERROR -> "E"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = priorityColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = priorityColor,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = priorityText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "[${logEntry.tag}] ${logEntry.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (logEntry.throwable != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = logEntry.throwable.message ?: "Unknown error",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTimestamp(logEntry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
fun formatTimestamp(timestamp: Long): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - timestamp

    return when {
        diff < 1000 -> "Just now"
        diff < 60000 -> "${diff / 1000}s ago"
        diff < 3600000 -> "${diff / 60000}m ago"
        else -> "${diff / 3600000}h ago"
    }
}