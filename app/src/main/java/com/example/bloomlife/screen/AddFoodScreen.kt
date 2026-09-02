package com.example.bloomlife.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bloomlife.model.FoodItem

@Composable
fun AddFoodScreen(
    mealName: String,
    foods: List<FoodItem>,
    onFoodSelected: (FoodItem) -> Unit,
    onBack: () -> Unit
) {

    var searchText by remember {
        mutableStateOf("")
    }

    val filteredFoods = foods.filter {
        it.name.contains(
            searchText,
            ignoreCase = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Add Food to $mealName",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            label = {
                Text("Search food")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(filteredFoods) { food ->

                FoodCard(
                    food = food,
                    onAdd = {
                        onFoodSelected(food)
                    }
                )
            }
        }
    }
}

@Composable
fun FoodCard(
    food: FoodItem,
    onAdd: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${food.calories} kcal"
                )

                Text(
                    text = "Protein: ${food.protein}g"
                )

                Text(
                    text = "Carbs: ${food.carbohydrates}g"
                )

                Text(
                    text = "Fat: ${food.fat}g"
                )
            }

            Button(
                onClick = onAdd
            ) {

                Text("Add")
            }
        }
    }
}