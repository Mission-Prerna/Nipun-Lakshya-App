package com.samagra.parent.ui.assessmenthome

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.samagra.parent.R
import com.samagra.parent.databinding.DrawerItemBinding

class DrawerAdapter(
    private val items: List<DrawerItem>,
    val context: Context,
    private val onItemClick: (item: DrawerItem) -> Unit
) :
    RecyclerView.Adapter<DrawerAdapter.ViewHolder>() {

    class ViewHolder(val binding: DrawerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DrawerItem, context: Context) {
            if (!data.enabled) {
                binding.drawerItem.isEnabled = false
                binding.drawerItem.isClickable = false
                binding.drawerItem.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.gray_disabled_light
                    )
                )
                binding.constraintLayout.alpha = 0.4F
            }else{
                binding.drawerItem.isEnabled = true
                binding.drawerItem.isClickable = true
                binding.drawerItem.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
                binding.constraintLayout.alpha = 1.0F
            }
            binding.textView8.text = data.name
            binding.imageView3.setImageResource(data.icon)
        }

        companion object {
            fun create(parent: ViewGroup): ViewHolder {
                val binding = DrawerItemBinding.inflate(LayoutInflater.from(parent.context))
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], context)

        holder.binding.drawerItem.setOnClickListener {
            onItemClick(items[position])
        }
    }

    override fun getItemCount() = items.size

}