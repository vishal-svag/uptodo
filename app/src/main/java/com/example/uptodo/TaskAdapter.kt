package com.example.uptodo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uptodo.databinding.ItemTaskBinding

class TaskAdapter(private val taskClickListener: TaskClickListener) : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding, taskClickListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    class TaskViewHolder(private val binding: ItemTaskBinding, private val taskClickListener: TaskClickListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskEntity) {
            binding.taskTitleTextView.text = task.title
            binding.taskCategoryTextView.text = task.category
            binding.taskDateTimeTextView.text = task.dateTime
            binding.priorityText.text = task.priority.toString()

            binding.root.setOnClickListener {
                taskClickListener.onTaskClick(task)
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem == newItem
        }
    }

    interface TaskClickListener {
        fun onTaskClick(task: TaskEntity)
    }
}


