package com.example.uptodo

import TaskViewModel
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uptodo.databinding.ActivityMainBinding
import com.example.uptodo.databinding.AddTaskLayoutBinding
import com.example.uptodo.databinding.DialogCategoryBinding
import com.example.uptodo.databinding.DialogPriorityBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity(), TaskAdapter.TaskClickListener {

    private lateinit var binding: ActivityMainBinding
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(application, TaskRepository(TaskDatabase.getDatabase(application).taskDao()))
    }

    private lateinit var taskAdapter: TaskAdapter
    private var selectedPriority: Int = 1 // Default priority
    private var selectedCategory: String = "Work" // Default category
    private var selectedDateTime: String = "" // Default datetime
    private var allTasks: List<TaskEntity> = listOf() // List to hold all tasks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        binding.taskRecyclerView.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(this)
        binding.taskRecyclerView.adapter = taskAdapter

        // Set up spinner
        setupSpinner()

        // Fetch tasks and update UI
        fetchTasks()

        // Set up click listeners
        clickListener()

        // Set up search functionality
        setupSearch()
    }

    private fun fetchTasks() {
        taskViewModel.getAllTasks().observe(this) { taskList ->
            allTasks = taskList // Save all tasks for search filtering
            val hasTasks = taskList.isNotEmpty()
            binding.emptyStateLayout.isVisible = !hasTasks
            binding.taskRecyclerView.isVisible = hasTasks
            binding.searchBar.isVisible = hasTasks
            taskAdapter.submitList(taskList)
        }
    }

    private fun setupSearch() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTasks(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterTasks(query: String) {
        val filteredTasks = allTasks.filter { task ->
            task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true) ||
                    task.category.contains(query, ignoreCase = true)
        }
        updateRecyclerView(filteredTasks)
    }


    private fun updateRecyclerView(tasks: List<TaskEntity>) {
        val hasTasks = tasks.isNotEmpty()
        binding.emptyStateLayout.isVisible = !hasTasks
        binding.taskRecyclerView.isVisible = hasTasks
        taskAdapter.submitList(tasks)
    }

    private fun clickListener() {
        binding.fab.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    @SuppressLint("InflateParams")
    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bindingSheet = AddTaskLayoutBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bindingSheet.root)
        bottomSheetDialog.show()

        // Handle the submit button click
        bindingSheet.submitTaskButton.setOnClickListener {
            val title = bindingSheet.taskTitleEditText.text.toString()
            val description = bindingSheet.taskDescriptionEditText.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty() && selectedDateTime.isNotEmpty()) {
                val task = TaskEntity(
                    title = title,
                    description = description,
                    priority = selectedPriority,
                    category = selectedCategory,
                    dateTime = selectedDateTime
                )
                lifecycleScope.launch {
                    taskViewModel.insert(task) // Insert the task using ViewModel
                    fetchTasks() // Refresh the task list
                }
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle time button click
        bindingSheet.timeButton.setOnClickListener {
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

        bindingSheet.category.setOnClickListener {
            showCategoryDialog()
        }

        bindingSheet.priority.setOnClickListener {
            showPriorityDialog()
        }
    }
    private fun setupSpinner() {
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortSpinner.adapter = spinnerAdapter

        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> sortTasksByPriority()
                    1 -> sortTasksByDate()
                    2 -> sortTasksByCategory()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun sortTasksByPriority() {
        val sortedTasks = allTasks.sortedBy { it.priority }
        updateRecyclerView(sortedTasks)
    }

    private fun sortTasksByDate() {
        val sortedTasks = allTasks.sortedBy { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(it.dateTime) }
        updateRecyclerView(sortedTasks)
    }

    private fun sortTasksByCategory() {
        val sortedTasks = allTasks.sortedBy { it.category }
        updateRecyclerView(sortedTasks)
    }


    private fun showPriorityDialog() {
        val dialog = Dialog(this)
        val bindingDialog = DialogPriorityBinding.inflate(layoutInflater)
        dialog.setContentView(bindingDialog.root)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // Refactor repetitive OnClickListener code
        val priorityButtons = listOf(
            bindingDialog.p1, bindingDialog.p2, bindingDialog.p3, bindingDialog.p4,
            bindingDialog.p5, bindingDialog.p6, bindingDialog.p7, bindingDialog.p8,
            bindingDialog.p9, bindingDialog.p10
        )

        priorityButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedPriority = index + 1
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

        // Map category buttons to their respective values
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

        // Set a single click listener for all category buttons
        categoryMap.forEach { (button, category) ->
            button.setOnClickListener {
                selectedCategory = category
                Toast.makeText(this, "Category: $selectedCategory", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    // Implementing the TaskClickListener interface method
    override fun onTaskClick(task: TaskEntity) {
        // Handle task click here, e.g., open TaskDetail activity
        val intent = Intent(this, TaskDetail::class.java)
        intent.putExtra("TASK_ID", task.id)
        startActivity(intent)
    }
}
