package com.example.uptodo

import androidx.lifecycle.LiveData

class TaskRepository(private val taskDao: TaskDAO) {

    // Insert a task into the database
    suspend fun insertTask(task: TaskEntity) {
        taskDao.insert(task)
    }

    // Update a task in the database
    suspend fun updateTask(task: TaskEntity) {
        taskDao.update(task)
    }

    // Delete a task from the database
    suspend fun deleteTask(taskId: Int) {
        taskDao.delete(taskId)
    }

    // Get a task by ID
    fun getTaskById(taskId: Int): LiveData<TaskEntity?> {
        return taskDao.getTaskById(taskId)
    }

    // Get all tasks
    fun getAllTasks(): LiveData<List<TaskEntity>> {
        return taskDao.getAllTasks()
    }
}
