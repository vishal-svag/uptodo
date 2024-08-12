package com.example.uptodo

import TaskViewModel
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.uptodo.databinding.ActivityTaskDetailBinding
import com.example.uptodo.databinding.DialogCategoryBinding
import com.example.uptodo.databinding.DialogPriorityBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskDetail : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(application, TaskRepository(TaskDatabase.getDatabase(application).taskDao()))
    }

    private var taskId: Int = -1
    private var isEditing: Boolean = false
    private var selectedPriority: Int = 1 // Default priority
    private var selectedCategory: String = "Work" // Default category
    private var selectedDateTime: String = "" // Default datetime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the task ID from the intent
        taskId = intent.getIntExtra("TASK_ID", -1)

        if (taskId != -1) {
            loadTaskDetails(taskId)
        } else {
            showErrorAndExit("Task ID not found")
        }

        setupClickListeners()
    }

    private fun loadTaskDetails(taskId: Int) {
        taskViewModel.getTaskById(taskId).observe(this) { task ->
            if (task != null) {
                binding.taskTitle.text = task.title
                binding.taskDescription.text = task.description
                binding.taskTimeLabel.text = task.dateTime
                binding.taskCategoryLabel.text = task.category
                binding.taskPriorityLabel.text = task.priority.toString()
                setEditMode(false)
            } else {
                showErrorAndExit("Task not found")
            }
        }
    }


    private fun setupClickListeners() {
        binding.edit.setOnClickListener {
            isEditing = !isEditing
            setEditMode(isEditing)
        }

        binding.SaveBtn.setOnClickListener {
            if (isEditing) {
                saveTask()
            }
        }

        binding.btnDeleteTask.setOnClickListener {
            deleteTask()
        }

        binding.closeIcon.setOnClickListener {
            onBackPressed()
        }
        binding.taskTimeLabel.setOnClickListener {
            if (isEditing) openDateTimePicker()
        }

        binding.taskCategoryLabel.setOnClickListener {
            if (isEditing) showCategoryDialog()
        }

        binding.taskPriorityLabel.setOnClickListener {
            if (isEditing) showPriorityDialog()
        }
    }

    private fun setEditMode(editMode: Boolean) {
        binding.taskTitle.isEnabled = editMode
        binding.taskDescription.isEnabled = editMode
        binding.taskTimeLabel.isEnabled = editMode
        binding.taskCategoryLabel.isEnabled = editMode
        binding.taskPriorityLabel.isEnabled = editMode

        binding.SaveBtn.isVisible = editMode // Show Save button only in edit mode
    }

    private fun saveTask() {
        val updatedTask = TaskEntity(
            id = taskId,
            title = binding.taskTitle.text.toString(),
            description = binding.taskDescription.text.toString(),
            dateTime = binding.taskTimeLabel.text.toString(),
            category = binding.taskCategoryLabel.text.toString(),
            priority = binding.taskPriorityLabel.text.toString().toIntOrNull() ?: 0
        )

        lifecycleScope.launch {
            try {
                taskViewModel.updateTask(updatedTask)
                Toast.makeText(this@TaskDetail, "Task updated", Toast.LENGTH_SHORT).show()
                setEditMode(false)
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetail, "Error updating task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTask() {
        lifecycleScope.launch {
            try {
                taskViewModel.deleteTask(taskId)
                Toast.makeText(this@TaskDetail, "Task deleted", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetail, "Error deleting task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun openDateTimePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePicker = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        selectedDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.time)
                        binding.taskTimeLabel.text = selectedDateTime
                        Toast.makeText(this, "Date and Time set: $selectedDateTime", Toast.LENGTH_SHORT).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showPriorityDialog() {
        val dialog = Dialog(this)
        val bindingDialog = DialogPriorityBinding.inflate(layoutInflater)
        dialog.setContentView(bindingDialog.root)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val priorityButtons = listOf(
            bindingDialog.p1, bindingDialog.p2, bindingDialog.p3, bindingDialog.p4,
            bindingDialog.p5, bindingDialog.p6, bindingDialog.p7, bindingDialog.p8,
            bindingDialog.p9, bindingDialog.p10
        )

        priorityButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedPriority = index + 1
                binding.taskPriorityLabel.text = selectedPriority.toString()
                Toast.makeText(this, "Priority: $selectedPriority", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        bindingDialog.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCategoryDialog() {
        val dialog = Dialog(this)
        val bindingDialog = DialogCategoryBinding.inflate(layoutInflater)
        dialog.setContentView(bindingDialog.root)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val categoryMap = mapOf(
            bindingDialog.p1 to "Grocery",
            bindingDialog.p2 to "Work",
            bindingDialog.p3 to "Sports",
            bindingDialog.p4 to "Design",
            bindingDialog.p5 to "University",
            bindingDialog.p6 to "Social",
            bindingDialog.p7 to "Music",
            bindingDialog.p8 to "Health",
            bindingDialog.p9 to "Movie",
            bindingDialog.p10 to "Home"
        )

        categoryMap.forEach { (button, category) ->
            button.setOnClickListener {
                selectedCategory = category
                binding.taskCategoryLabel.text = selectedCategory
                Toast.makeText(this, "Category: $selectedCategory", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showErrorAndExit(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish() // Close the activity on error
    }

}
