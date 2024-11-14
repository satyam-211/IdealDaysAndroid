package com.example.myapplication.view.presentation.taskList.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.model.Task
import com.example.myapplication.viewmodel.TaskListViewModel

@Composable
fun TaskView(task: Task) {
    val viewModel: TaskListViewModel = hiltViewModel()
    val angerMode by viewModel.angerMode.collectAsState()
    val scheduleTasks by viewModel.scheduleNowTasks
    val isChecked by remember {
        derivedStateOf {
            scheduleTasks.contains(task)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            elevation = CardDefaults.elevatedCardElevation(),
            colors = if (angerMode) CardColors(
                Color(0xFFFFCCCC),
                Color(0xFFFF9696),
                Color(0xFFFF6464),
                Color(0xFFFF3232)
            ) else CardDefaults.cardColors()
        ) {
            when (task) {
                is Task.BinaryTask -> BinaryTaskView(task)
                is Task.PartialTask -> PartialTaskView(task)
            }
        }
        if(angerMode)
            Checkbox(
                modifier = Modifier.padding(start = 10.dp),
                checked = isChecked,
                onCheckedChange = { checked ->
                       viewModel.toggleSchedule(task, checked)
                }
            )
    }

}

@Composable
@Preview(showBackground = true)
fun TaskViewPreview(){
    Button(
        modifier = Modifier.padding(start = 4.dp),
        onClick = { },
        colors = ButtonColors(Color.Red, Color.Red, Color.Red, Color.Red)
    ) {
        Text(text = "Schedule Now", color = Color.White)
    }
}






