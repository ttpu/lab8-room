package uz.student.roomnotes.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoteScreen(viewModel: NoteViewModel) {
    val notes by viewModel.notes.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Eslatmalar", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Sarlavha") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Matn") },
            modifier = Modifier.fillMaxWidth()
        )

        if (error != "") {
            Text(error, color = Color.Red)
        }

        Row {
            Button(onClick = {
                viewModel.add(title, body)
                title = ""
                body = ""
            }) {
                Text("Qo'shish")
            }
            Spacer(modifier = Modifier.width(8.dp))
            // 8-savol: main thread da bloklovchi so'rov
            Button(onClick = {
                val natija = viewModel.blockingCall()
                Toast.makeText(context, natija, Toast.LENGTH_LONG).show()
            }) {
                Text("Blocking test")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                viewModel.search(it)
            },
            label = { Text("Qidirish") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.padding(top = 8.dp)) {
            Button(onClick = { viewModel.changeSort(SortOrder.ByDate) }) { Text("Sana") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.changeSort(SortOrder.ByTitle) }) { Text("Nomi") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.changeSort(SortOrder.PinnedFirst) }) { Text("Pinned") }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Jami: " + notes.size + " ta")
        HorizontalDivider()

        LazyColumn {
            items(notes, key = { it.id }) { note ->
                NoteItem(note, onDelete = { viewModel.delete(note.id) })
            }
        }
    }
}

@Composable
fun NoteItem(note: NoteUi, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(note.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(note.preview)
            Text(note.dateText, fontSize = 12.sp, color = Color.Gray)
            Button(onClick = onDelete) {
                Text("O'chirish")
            }
        }
    }
}
