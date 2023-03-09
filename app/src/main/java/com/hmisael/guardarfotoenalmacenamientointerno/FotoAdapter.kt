package com.hmisael.guardarfotoenalmacenamientointerno

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hmisael.guardarfotoenalmacenamientointerno.databinding.ItemFotoBinding


class FotoAdapter(private val onPhotoClick: (Foto) -> Unit) :
                    ListAdapter<Foto, FotoAdapter.FotoViewHolder>(Companion) {

    inner class FotoViewHolder(val binding: ItemFotoBinding):
        RecyclerView.ViewHolder(binding.root)

    companion object : DiffUtil.ItemCallback<Foto>() {
        override fun areItemsTheSame(oldItem: Foto, newItem: Foto): Boolean {
            return oldItem.nombre == newItem.nombre
        }

        override fun areContentsTheSame(oldItem: Foto, newItem: Foto): Boolean {
            return oldItem.nombre == newItem.nombre && oldItem.bitmap.sameAs(newItem.bitmap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        return FotoViewHolder(
            ItemFotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val photo = currentList[position]
        holder.binding.apply {
            ivFoto.setImageBitmap(photo.bitmap)

            ivFoto.setOnLongClickListener {
                onPhotoClick(photo)
                true
            }
        }
    }
}