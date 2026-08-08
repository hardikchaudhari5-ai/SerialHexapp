package com.example.serialhex

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.serialhex.logic.DataProcessor
import com.example.serialhex.usb.UsbCommunicationHelper

class MainActivity : ComponentActivity() {
    private lateinit var usbHelper: UsbCommunicationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usbHelper = UsbCommunicationHelper(this)

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SerialHexDashboard(usbHelper)
                }
            }
        }
    }
}

@Composable
fun SerialHexDashboard(usbHelper: UsbCommunicationHelper) {
    var fileData by remember { mutableStateOf(byteArrayOf()) }
    var fileName by remember { mutableStateOf("No file loaded") }
    var statusMessage by remember { mutableStateOf("Ready") }

    // SAF Launchers
    val openFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { /* Read file logic here */ }
    }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Left Panel: Actions & Status
        Column(modifier = Modifier.weight(0.3f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            Text("SerialHex Utility", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            
            // File Info Card
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("File: $fileName", style = MaterialTheme.typography.bodySmall)
                    Text("Size: ${fileData.size} bytes", style = MaterialTheme.typography.bodySmall)
                    Text("Checksum: ${DataProcessor.calculateChecksum(fileData)}", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Buttons
            Button(onClick = { /* openFileLauncher.launch(arrayOf("*/*")) */ }, Modifier.fillMaxWidth()) {
                Text("Open File (.bin)")
            }
            OutlinedButton(onClick = { /* Save Logic */ }, Modifier.fillMaxWidth()) {
                Text("Save File")
            }
            
            Divider(Modifier.padding(vertical = 12.dp))

            // Hardware Communication Card
            Text("Hardware Control", style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { 
                        val dev = usbHelper.findDevice()
                        if (dev != null) usbHelper.connect(dev)
                    }, Modifier.fillMaxWidth()) {
                        Text("Connect USB")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { /* Read Block */ }, Modifier.weight(1f)) { Text("Read") }
                        Button(onClick = { /* Write Block */ }, Modifier.weight(1f)) { Text("Write") }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Text("Status: $statusMessage", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.width(16.dp))

        // Right Panel: Hex Viewer
        Card(modifier = Modifier.weight(0.7f).fillMaxHeight()) {
            HexViewerPanel(fileData)
        }
    }
}

@Composable
fun HexViewerPanel(data: ByteArray) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.Black).padding(8.dp)) {
        items(data.size / 16 + if (data.size % 16 > 0) 1 else 0) { rowIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Offset
                Text(
                    text = String.format("%08X | ", rowIndex * 16),
                    color = Color.Yellow,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                
                // Hex bytes
                var hexPart = ""
                var asciiPart = ""
                for (i in 0 until 16) {
                    val index = rowIndex * 16 + i
                    if (index < data.size) {
                        hexPart += DataProcessor.toHexString(data[index]) + " "
                        asciiPart += DataProcessor.toAscii(data[index])
                    } else {
                        hexPart += "   "
                        asciiPart += " "
                    }
                }
                
                Text(
                    text = hexPart,
                    color = Color.White,
                    modifier = Modifier.width(320.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                
                Text(
                    text = "| $asciiPart",
                    color = Color.Cyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}
