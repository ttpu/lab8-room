package uz.student.roomnotes.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val vm: NoteViewModel = viewModel(factory = NoteViewModel.Factory)
                    // 11-savol: ekranni aylantirib hashCode ni tekshiramiz
                    Log.d("VMtest", "viewModel hashCode = " + vm.hashCode())
                    NoteScreen(vm)
                }
            }
        }
    }
}
