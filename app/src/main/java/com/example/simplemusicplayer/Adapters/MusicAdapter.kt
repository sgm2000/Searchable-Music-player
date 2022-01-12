package com.example.simplemusicplayer.Adapters

import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simplemusicplayer.MainActivity
import com.example.simplemusicplayer.Modes.SongModel
import com.example.simplemusicplayer.R
import com.example.simplemusicplayer.`interface`.onSongSelect
import com.example.simplemusicplayer.currentSong
import kotlinx.android.synthetic.main.music_layout.view.*
import java.lang.Exception

class MusicAdapter(
    var songList: ArrayList<SongModel>,
    var context: Context,
    var onSongSelect: onSongSelect
) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var song_title = itemView.song_title
        var song_artist = itemView.song_artist
        var song_image = itemView.song_image

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.music_layout,parent,false)

        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.song_title.text=songList[position].song_title
        holder.song_artist.text=songList[position].artist

        var bitmap: Bitmap? = null
        try{
            bitmap=MediaStore.Images.Media.getBitmap(context.contentResolver,songList[position].image)
            holder.song_image.setImageBitmap(bitmap)
        }
        catch (e :Exception)
        {

        }

        holder.itemView.setOnClickListener{
            currentSong=position
            onSongSelect.onSelect(songList[position])
        }


    }

    override fun getItemCount(): Int {
        return songList.size

    }

    public fun updateList(list:ArrayList<SongModel>)
    {
        songList= list
        notifyDataSetChanged()
    }



}