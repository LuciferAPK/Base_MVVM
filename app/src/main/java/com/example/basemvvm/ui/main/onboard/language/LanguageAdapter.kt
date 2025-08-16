package com.example.basemvvm.ui.main.onboard.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.basemvvm.R
import com.example.basemvvm.data.model.LanguageModel
import com.example.basemvvm.databinding.ItemLanguageBinding

class LanguageAdapter(private val onClickItem: (String) -> Unit) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION
    private var listData = listOf<LanguageModel>()

    fun setData(newList: List<LanguageModel>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = listData.size
            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                listData[oldItemPosition].languageCode == newList[newItemPosition].languageCode

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                listData[oldItemPosition] == newList[newItemPosition]
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listData = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding =
            ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = listData[position]
        with(holder.binding) {
            txtCountryName.text = language.countryName
            imgFlag.setImageResource(language.imgFlag)
            root.setBackgroundResource(
                if (position == selectedPosition) R.drawable.bg_item_language_selected else R.drawable.bg_item_language
            )

            root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                if (previousPosition != RecyclerView.NO_POSITION) notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onClickItem(language.languageCode)
            }
        }
    }

    override fun getItemCount(): Int = listData.size

    class LanguageViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root)
}