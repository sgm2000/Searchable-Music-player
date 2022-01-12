package com.example.simplemusicplayer.Services

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.TextView
import com.example.simplemusicplayer.Modes.SongModel
import com.example.simplemusicplayer.`interface`.OnsongComplete
import java.sql.Time
import java.util.concurrent.TimeUnit

class MusicService : Service(),MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    lateinit var player: MediaPlayer
    lateinit var songs: SongModel
    lateinit var onsongComplete: OnsongComplete
    private val musicBind = MusicBinder()
    lateinit var seekBar: SeekBar
    private val interval = 1000
    lateinit var start_point:TextView
    lateinit var end_point:TextView
    var playerState= STOPPED
    override fun onBind(p0: Intent?): IBinder? {
        return musicBind

    }

    override fun onCreate() {
        super.onCreate()

        player = MediaPlayer()
        initMusic()
    }
    fun setListner(onsongComplete: OnsongComplete)
    {
        this.onsongComplete = onsongComplete

    }

    fun initMusic() {
        player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        player.stop()
        player.reset()
        player.release()
        return false
    }

    inner class MusicBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    companion object {
        const val STOPPED = 0
        const val PAUSED = 1
        const val PLAYING = 2
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp!!.start()
        val duration = mp.duration
        seekBar.max = duration
        seekBar.postDelayed(progressRunner, interval.toLong())

        end_point.text = String.format("%d:%d",TimeUnit.MICROSECONDS.toMinutes(duration.toLong()),
        TimeUnit.MILLISECONDS.toSeconds(duration.toLong())-
        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration.toLong()))
        )
    }

    public fun pauseSong()
    {
        player.pause()
        playerState = PAUSED
        seekBar.removeCallbacks(progressRunner)
    }

    public fun resumeSong()
    {
        player.start()
        playerState= PLAYING
        progressRunner.run()
    }
    

    private fun playSong()
    {
        player.reset()

        val playSong = songs
        val current_songId = playSong.song_id
        val trackUri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,current_songId)

        player.setDataSource(applicationContext,trackUri)
        player.prepareAsync()
        progressRunner.run()
    }

    fun setUI(seekBar: SeekBar,start_int :TextView,end_int:TextView)
    {
        this.seekBar=seekBar
        start_point=start_int
        end_point=end_int
        seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser)
                {
                    player.seekTo(progress)
                }
                start_point.text = String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(progress.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(progress.toLong())-
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress.toLong()))
                )

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }
    private val progressRunner:Runnable = object: Runnable {

        override fun run() {

            if (seekBar != null) {
                seekBar.progress = player.currentPosition
                if (player.isPlaying) {
                    seekBar.postDelayed(this, interval.toLong())
                }
            }
        }
    }


    fun setSong(songmodel:SongModel)
    {
        songs=songmodel
        playerState= PLAYING
        playSong()
    }
    override fun onCompletion(mp: MediaPlayer?) {
        onsongComplete.onSongComplete()

    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return false
    }
}