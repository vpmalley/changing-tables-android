package fr.vpm.changingtables.ui.saved

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.vpm.changingtables.R
import fr.vpm.changingtables.databinding.ItemSavedBusinessBinding
import fr.vpm.changingtables.models.Business

class SavedBusinessesAdapter : ListAdapter<Business, SavedBusinessesAdapter.ViewHolder>(BusinessDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedBusinessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemSavedBusinessBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(business: Business) {
            binding.businessName.text = business.name

            val iconRes = when (business.type) {
                "coffee", "cafe" -> R.drawable.ic_coffee_24
                "restaurant" -> R.drawable.ic_restaurant_24
                "activity" -> R.drawable.ic_local_activity_24
                else -> R.drawable.ic_coffee_24
            }
            binding.businessTypeIcon.setImageResource(iconRes)

            binding.changingTableIcon.visibility = if (business.hasChangingTable != "no") View.VISIBLE else View.GONE
        }
    }

    class BusinessDiffCallback : DiffUtil.ItemCallback<Business>() {
        override fun areItemsTheSame(oldItem: Business, newItem: Business): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Business, newItem: Business): Boolean {
            return oldItem.name == newItem.name &&
                   oldItem.savedOnDevice == newItem.savedOnDevice &&
                   oldItem.hasChangingTable == newItem.hasChangingTable &&
                   oldItem.type == newItem.type
        }
    }
}
