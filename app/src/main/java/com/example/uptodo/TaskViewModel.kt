import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.uptodo.TaskEntity
import com.example.uptodo.TaskRepository
import kotlinx.coroutines.launch

// TaskViewModel
class TaskViewModel(application: Application, private val taskRepository: TaskRepository) : AndroidViewModel(application) {

    // Insert a new task
    fun insert(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
        }
    }

    // Update an existing task
    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }

    // Method to get a single task by ID
    fun getTaskById(taskId: Int): LiveData<TaskEntity?> {
        return taskRepository.getTaskById(taskId)
    }

    // Method to get all tasks
    fun getAllTasks(): LiveData<List<TaskEntity>> {
        return taskRepository.getAllTasks()
    }
}
