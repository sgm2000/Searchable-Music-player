 package com.example.simplemusicplayer

import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplemusicplayer.Adapters.MusicAdapter
import com.example.simplemusicplayer.Modes.SongModel
import com.example.simplemusicplayer.Services.MusicService
import com.example.simplemusicplayer.`interface`.OnsongComplete
import com.example.simplemusicplayer.`interface`.onSongSelect
import com.google.android.material.bottomsheet.BottomSheetBehavior

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.music_layout.view.*
import kotlinx.android.synthetic.main.playmusic_layout.*
import kotlinx.android.synthetic.main.playmusic_layout.view.*
import java.lang.Exception


class MainActivity : AppCompatActivity(), onSongSelect, View.OnClickListener,OnsongComplete {

    lateinit var list: ArrayList<SongModel>
    lateinit var adapter: MusicAdapter
    lateinit var musicService: MusicService
    lateinit var seekBar: SeekBar
    var playintent: Intent? = null

    private lateinit var bottomSheetBehaviour:BottomSheetBehavior<LinearLayout>

    lateinit var songmodel:SongModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list = ArrayList()
        var manager= LinearLayoutManager(applicationContext)

        recyclerview.layoutManager=manager
        adapter=MusicAdapter(list,applicationContext,this)
        recyclerview.adapter = adapter
        playpause_btn.setOnClickListener(this)
        btn_previous.setOnClickListener(this)
        btn_next.setOnClickListener(this)
        collapse_btn.setOnClickListener(this)

        bottomSheetBehaviour = BottomSheetBehavior.from(bottomLayout)
        searach_edittext.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                searchSongs(p0.toString())
            }

        })
        getSongs()

        
    }


    /* Fetch mp3 files from device*/

    private fun getSongs()
    {
        list.clear()
        val contentResolver: ContentResolver = this.contentResolver
        var songUri : Uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI    /* detect the mp3 audio files*/
        var cursor : Cursor?=contentResolver.query(songUri,null,null,null,null)
        if(cursor != null && cursor.moveToFirst())
        {
            val songId:Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist : Int = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val date:Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            val albumColumn: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext())
            {
                val currentId: Long = cursor.getLong(songId)
                val song_title :String= cursor.getString(songTitle)
                val song_artist :String = cursor.getString(songArtist)
                val song_data :String = cursor.getString(songData)
                val song_date :Long = cursor.getLong(date)
                val albumId : Long = cursor.getLong(albumColumn)

                    val IMAGE_URI :Uri = Uri.parse("content : //media/external/audio/albumart")
                val album_uri : Uri= ContentUris.withAppendedId(IMAGE_URI,albumId)

                if(!song_artist.equals("<unknown>") )
                {
                    list.add(SongModel(currentId,song_title,song_artist,song_data,song_date,album_uri))
                }

            }
            adapter.notifyDataSetChanged()

        }
    }
    // Function to Search Songs
    private fun searchSongs(value: String)
    {
        var songList = ArrayList<SongModel>()
        for (song : SongModel in list)
        {
            var islist_added=false
            if(song.song_title.toLowerCase().contains(value.toLowerCase())){
                songList.add(song)
                islist_added = true
            }
            if(song.artist.toLowerCase().contains(value.toLowerCase()))
            {
                if(!islist_added)
                songList.add(song)
            }
        }
        adapter.updateList(songList)
    }

    override fun onStart() {
        super.onStart()
        if (playintent == null)
        {
            playintent= Intent(this,MusicService::class.java)
            bindService(playintent,musicConnection,Context.BIND_AUTO_CREATE)
            startService(playintent)
        }
    }

    override fun onDestroy() {
        stopService(playintent)
        unbindService(musicConnection)
        super.onDestroy()
    }

    private fun updateUI()
    {
        bottomLayout.song_title_bar.text=songmodel.song_title
        bottomLayout.song_artist_name.text=songmodel.artist

        var bitmap: Bitmap? = null
        try {
            bitmap=MediaStore.Images.Media.getBitmap(contentResolver,songmodel.image)
            bottomLayout.song_artist_name.song_image.setImageBitmap(bitmap)
        }catch (e:Exception){

        }
    }

    private var musicConnection :ServiceConnection= object :ServiceConnection
    {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder:MusicService.MusicBinder=service as MusicService.MusicBinder

            musicService=binder.service
            musicService.setUI(bottomLayout.seekBar,bottomLayout.start_text,bottomLayout.end_text)
            musicService.setListner(this@MainActivity)



        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }

    }

    override fun onSelect(song: SongModel) {
        musicService.setSong(song)
        songmodel=song
        updateUI()
    }

    override fun onClick(v: View?) {
        when(v)
        {
            playpause_btn->
            {
                if(musicService.playerState == 2)
                {
                    playpause_btn.setBackgroundResource(R.drawable.ic_play)      //pause song
                    musicService.pauseSong()
                }
                else if(musicService.playerState == 1)
                {
                    playpause_btn.setBackgroundResource(R.drawable.ic_pause)       //resume song
                    musicService.resumeSong()

                }
            }
            btn_next->
            {
                if(list.size>0)
                {

                    if(currentSong != -1)
                    {
                        if (list.size-1 == currentSong)
                        {
                            currentSong = 0
                            musicService.setSong(list[currentSong])
                            songmodel=list[currentSong]
                            updateUI()

                        }
                        else
                        {
                            ++currentSong
                            musicService.setSong(list[currentSong])
                            songmodel=list[currentSong]
                            updateUI()

                        }

                    }

                }

            }
            btn_previous->
            {
                if(currentSong != -1)
                {
                    if(currentSong == 0)
                    {
                        currentSong = list.size -1
                        musicService.setSong(list[currentSong])
                        songmodel=list[currentSong]
                        updateUI()

                    }
                    else
                    {
                        currentSong--
                        musicService.setSong(list[currentSong])
                        songmodel=list[currentSong]
                        updateUI()

                    }


                }

            }
            collapse_btn->
            {
                if(BottomSheetBehavior.STATE_EXPANDED == bottomSheetBehaviour.state)
                {
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                    collapse_btn.setImageResource(R.drawable.ic_up_arrow)

                }
                else
                {
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
                    collapse_btn.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                }
            }


        }
    }

    override fun onSongComplete() {
        if(currentSong != -1)
        {
            if (list.size-1 == currentSong)
            {
                currentSong = 0
                musicService.setSong(list[currentSong])
                songmodel=list[currentSong]
                updateUI()

            }
            else
            {
                ++currentSong
                musicService.setSong(list[currentSong])
                songmodel=list[currentSong]
                updateUI()

            }

        }
    }



}