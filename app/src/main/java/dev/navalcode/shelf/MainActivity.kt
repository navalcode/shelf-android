package dev.navalcode.shelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.navalcode.shelf.presentation.SearchViewModel
import dev.navalcode.shelf.presentation.ShelfApp
import dev.navalcode.shelf.presentation.ShelfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val searchBooks = (application as ShelfApplication).searchBooks
        setContent {
            ShelfTheme {
                val model: SearchViewModel =
                    viewModel(
                        factory =
                            viewModelFactory {
                                initializer {
                                    SearchViewModel(searchBooks, createSavedStateHandle())
                                }
                            },
                    )
                ShelfApp(model)
            }
        }
    }
}
