package com.test.coccoc.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.test.coccoc.databinding.ItemAudioBinding

data class AudioItem(
    val url: String,
    val isSelected: Boolean = false
) {
    val fileName: String
        get() = url.substringAfterLast("/").substringBefore("?").take(40)

    val fileType: String
        get() = when {
            url.contains(".mp3") -> "MP3"
            url.contains(".m4a") -> "M4A"
            url.contains(".m3u8") -> "HLS Stream"
            url.contains(".wav") -> "WAV"
            else -> "Audio"
        }
}

class AudioAdapter(
    private val onPlayClick: (AudioItem) -> Unit,
    private val onDownloadClick: (AudioItem) -> Unit,
    private val onItemClick: (AudioItem) -> Unit
) : ListAdapter<AudioItem, AudioAdapter.AudioViewHolder>(AudioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AudioViewHolder(
        private val binding: ItemAudioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AudioItem) {
            binding.apply {
                audioFileName.text = item.fileName
                audioFileType.text = item.fileType
                audioSelectedIcon.isVisible = item.isSelected

                root.setOnClickListener { onItemClick(item) }
                audioItemPlayButton.setOnClickListener { onPlayClick(item) }
                audioItemDownloadButton.setOnClickListener { onDownloadClick(item) }
            }
        }
    }

    private class AudioDiffCallback : DiffUtil.ItemCallback<AudioItem>() {
        override fun areItemsTheSame(oldItem: AudioItem, newItem: AudioItem): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: AudioItem, newItem: AudioItem): Boolean {
            return oldItem == newItem
        }
    }
}
