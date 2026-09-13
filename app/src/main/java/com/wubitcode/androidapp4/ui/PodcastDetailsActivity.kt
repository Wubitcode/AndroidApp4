package com.wubitcode.androidapp4.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wubitcode.androidapp4.R
import com.wubitcode.androidapp4.model.Episode
import com.wubitcode.androidapp4.model.SubscribedPodcast
import com.wubitcode.androidapp4.model.SubscriptionManager
import com.wubitcode.androidapp4.network.RetrofitClient
import com.wubitcode.androidapp4.network.RssParser
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Displays detailed information about a selected podcast.
 *
 * Features:
 * - Persistent podcast subscriptions.
 * - RSS episode retrieval and parsing.
 * - Episode display using RecyclerView.
 * - Streaming podcast playback.
 * - Pause, resume, and stop controls.
 * - Playback progress and seeking.
 * - Adjustable playback speed.
 */
class PodcastDetailsActivity : AppCompatActivity() {

    private lateinit var tvDetailsTitle: TextView
    private lateinit var tvDetailsArtist: TextView
    private lateinit var tvDetailsGenre: TextView
    private lateinit var tvDetailsFeed: TextView
    private lateinit var btnSubscribe: Button

    private lateinit var tvEpisodeStatus: TextView
    private lateinit var episodeProgressBar: ProgressBar
    private lateinit var recyclerViewEpisodes: RecyclerView

    private lateinit var btnPauseResume: Button
    private lateinit var btnStop: Button

    private lateinit var playbackSeekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvDuration: TextView

    private lateinit var spinnerPlaybackSpeed: Spinner

    private lateinit var episodeAdapter: EpisodeAdapter

    private var podcastId: Long = -1L
    private var isSubscribed: Boolean = false

    /*
     * Podcast information is stored at Activity level because
     * it is also required when saving a subscription.
     */
    private var podcastName: String = "Unknown Podcast"
    private var artistName: String = "Unknown Artist"
    private var genre: String = "Unknown Genre"
    private var feedUrl: String = ""

    /**
     * Streams podcast episode audio.
     */
    private var mediaPlayer: MediaPlayer? = null

    private var isPaused: Boolean = false
    private var currentEpisodeTitle: String? = null

    /**
     * Stores the playback speed selected by the user.
     * Normal playback speed is 1.0.
     */
    private var currentPlaybackSpeed: Float = 1.0f

    /**
     * Human-readable values displayed in the Spinner.
     */
    private val playbackSpeedLabels =
        listOf(
            "0.75×",
            "1.00×",
            "1.25×",
            "1.50×",
            "2.00×"
        )

    /**
     * Numeric speeds corresponding to the Spinner labels.
     */
    private val playbackSpeedValues =
        listOf(
            0.75f,
            1.0f,
            1.25f,
            1.5f,
            2.0f
        )

    /*
     * Updates the SeekBar and elapsed playback time
     * approximately once each second.
     */
    private val progressHandler =
        Handler(Looper.getMainLooper())

    private var isUserSeeking = false

    private val progressRunnable =
        object : Runnable {

            override fun run() {

                val player =
                    mediaPlayer ?: return

                try {

                    if (!isUserSeeking) {

                        val currentPosition =
                            player.currentPosition

                        playbackSeekBar.progress =
                            currentPosition

                        tvCurrentTime.text =
                            formatTime(currentPosition)
                    }

                    if (player.isPlaying) {

                        progressHandler.postDelayed(
                            this,
                            1000
                        )
                    }

                } catch (_: IllegalStateException) {

                    // MediaPlayer may have already been released.
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_podcast_details
        )

        setupToolbar()
        connectViews()
        setupEpisodeRecyclerView()
        setupPlaybackControls()
        setupSeekBar()
        setupPlaybackSpeed()

        /*
         * Retrieve podcast information passed from either
         * PodcastAdapter or SubscriptionAdapter.
         */
        podcastId =
            intent.getLongExtra(
                EXTRA_PODCAST_ID,
                -1L
            )

        podcastName =
            intent.getStringExtra(
                EXTRA_PODCAST_NAME
            ) ?: "Unknown Podcast"

        artistName =
            intent.getStringExtra(
                EXTRA_ARTIST_NAME
            ) ?: "Unknown Artist"

        genre =
            intent.getStringExtra(
                EXTRA_GENRE
            ) ?: "Unknown Genre"

        feedUrl =
            intent.getStringExtra(
                EXTRA_FEED_URL
            ) ?: ""

        // Display podcast information.
        tvDetailsTitle.text =
            podcastName

        tvDetailsArtist.text =
            artistName

        tvDetailsGenre.text =
            genre

        tvDetailsFeed.text =
            if (feedUrl.isBlank()) {
                "No feed URL available"
            } else {
                feedUrl
            }

        setupSubscription()

        // Retrieve episodes from the podcast RSS feed.
        if (feedUrl.isNotBlank()) {

            loadEpisodes(feedUrl)

        } else {

            tvEpisodeStatus.text =
                "Episodes are unavailable for this podcast."
        }
    }

    /**
     * Configures toolbar title and back navigation.
     */
    private fun setupToolbar() {

        val toolbar: Toolbar =
            findViewById(
                R.id.detailsToolbar
            )

        setSupportActionBar(toolbar)

        supportActionBar?.apply {

            title =
                "Podcast Details"

            setDisplayHomeAsUpEnabled(
                true
            )
        }

        toolbar.navigationIcon?.setTint(
            getColor(
                android.R.color.white
            )
        )
    }

    /**
     * Connects Kotlin properties to XML views.
     */
    private fun connectViews() {

        tvDetailsTitle =
            findViewById(
                R.id.tvDetailsTitle
            )

        tvDetailsArtist =
            findViewById(
                R.id.tvDetailsArtist
            )

        tvDetailsGenre =
            findViewById(
                R.id.tvDetailsGenre
            )

        tvDetailsFeed =
            findViewById(
                R.id.tvDetailsFeed
            )

        btnSubscribe =
            findViewById(
                R.id.btnSubscribe
            )

        tvEpisodeStatus =
            findViewById(
                R.id.tvEpisodeStatus
            )

        episodeProgressBar =
            findViewById(
                R.id.episodeProgressBar
            )

        recyclerViewEpisodes =
            findViewById(
                R.id.recyclerViewEpisodes
            )

        btnPauseResume =
            findViewById(
                R.id.btnPauseResume
            )

        btnStop =
            findViewById(
                R.id.btnStop
            )

        playbackSeekBar =
            findViewById(
                R.id.playbackSeekBar
            )

        tvCurrentTime =
            findViewById(
                R.id.tvCurrentTime
            )

        tvDuration =
            findViewById(
                R.id.tvDuration
            )

        spinnerPlaybackSpeed =
            findViewById(
                R.id.spinnerPlaybackSpeed
            )
    }

    /**
     * Configures the RecyclerView containing podcast episodes.
     */
    private fun setupEpisodeRecyclerView() {

        episodeAdapter =
            EpisodeAdapter { episode ->

                playEpisode(
                    episode
                )
            }

        recyclerViewEpisodes.apply {

            layoutManager =
                LinearLayoutManager(
                    this@PodcastDetailsActivity
                )

            adapter =
                episodeAdapter
        }
    }

    /**
     * Configures playback-speed choices.
     *
     * The default selection is 1.00x normal speed.
     */
    private fun setupPlaybackSpeed() {

        val adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                playbackSpeedLabels
            )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerPlaybackSpeed.adapter =
            adapter

        // 1.00x is the second item in the list.
        spinnerPlaybackSpeed.setSelection(
            1
        )

        spinnerPlaybackSpeed.onItemSelectedListener =
            object :
                AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    currentPlaybackSpeed =
                        playbackSpeedValues[position]

                    /*
                     * If an episode is already prepared,
                     * immediately apply the newly selected speed.
                     */
                    applyPlaybackSpeed()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    // Keep the previously selected speed.
                }
            }
    }

    /**
     * Applies the currently selected speed to MediaPlayer.
     */
    private fun applyPlaybackSpeed() {

        val player =
            mediaPlayer ?: return

        try {

            val playbackParams =
                player.playbackParams

            playbackParams.speed =
                currentPlaybackSpeed

            player.playbackParams =
                playbackParams

        } catch (_: IllegalStateException) {

            /*
             * The player may still be preparing.
             * The speed will also be applied when
             * onPrepared() is called.
             */
        }
    }

    /**
     * Initializes saved subscription state.
     */
    private fun setupSubscription() {

        if (podcastId != -1L) {

            isSubscribed =
                SubscriptionManager
                    .isSubscribed(
                        this,
                        podcastId
                    )

            /*
             * Convert older Assignment 7 ID-only subscriptions
             * into Assignment 8 complete subscription records.
             */
            if (isSubscribed) {

                val alreadyFullySaved =
                    SubscriptionManager
                        .getSubscriptions(
                            this
                        )
                        .any {

                            it.podcastId ==
                                    podcastId
                        }

                if (!alreadyFullySaved) {

                    SubscriptionManager
                        .subscribe(
                            this,
                            createSubscribedPodcast()
                        )
                }
            }

            updateSubscriptionButton()

        } else {

            btnSubscribe.isEnabled =
                false

            btnSubscribe.text =
                "Subscription Unavailable"
        }

        btnSubscribe.setOnClickListener {

            if (podcastId == -1L) {
                return@setOnClickListener
            }

            if (isSubscribed) {

                SubscriptionManager
                    .unsubscribe(
                        this,
                        podcastId
                    )

                isSubscribed =
                    false

            } else {

                SubscriptionManager
                    .subscribe(
                        this,
                        createSubscribedPodcast()
                    )

                isSubscribed =
                    true
            }

            updateSubscriptionButton()
        }
    }

    /**
     * Creates a complete subscription object.
     */
    private fun createSubscribedPodcast():
            SubscribedPodcast {

        return SubscribedPodcast(
            podcastId = podcastId,
            podcastName = podcastName,
            artistName = artistName,
            genre = genre,
            feedUrl = feedUrl
        )
    }

    /**
     * Configures Pause / Resume and Stop controls.
     */
    private fun setupPlaybackControls() {

        btnPauseResume
            .setOnClickListener {

                val player =
                    mediaPlayer
                        ?: return@setOnClickListener

                try {

                    if (player.isPlaying) {

                        player.pause()

                        isPaused =
                            true

                        btnPauseResume.text =
                            "Resume"

                        tvEpisodeStatus.text =
                            "Paused: " +
                                    (
                                            currentEpisodeTitle
                                                ?: "Episode"
                                            )

                        stopProgressUpdates()

                    } else if (isPaused) {

                        player.start()

                        isPaused =
                            false

                        btnPauseResume.text =
                            "Pause"

                        tvEpisodeStatus.text =
                            "Playing: " +
                                    (
                                            currentEpisodeTitle
                                                ?: "Episode"
                                            )

                        startProgressUpdates()
                    }

                } catch (_: IllegalStateException) {

                    tvEpisodeStatus.text =
                        "Playback is unavailable."
                }
            }

        btnStop.setOnClickListener {

            stopPlayback()
        }
    }

    /**
     * Configures seeking through the current episode.
     */
    private fun setupSeekBar() {

        playbackSeekBar
            .setOnSeekBarChangeListener(
                object :
                    SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {

                        if (fromUser) {

                            tvCurrentTime.text =
                                formatTime(
                                    progress
                                )
                        }
                    }

                    override fun onStartTrackingTouch(
                        seekBar: SeekBar?
                    ) {

                        isUserSeeking =
                            true

                        stopProgressUpdates()
                    }

                    override fun onStopTrackingTouch(
                        seekBar: SeekBar?
                    ) {

                        val player =
                            mediaPlayer

                        if (
                            player != null &&
                            seekBar != null
                        ) {

                            try {

                                player.seekTo(
                                    seekBar.progress
                                )

                                tvCurrentTime.text =
                                    formatTime(
                                        seekBar.progress
                                    )

                            } catch (
                                _: IllegalStateException
                            ) {

                                // Player is not currently seekable.
                            }
                        }

                        isUserSeeking =
                            false

                        if (
                            mediaPlayer
                                ?.isPlaying == true
                        ) {

                            startProgressUpdates()
                        }
                    }
                }
            )
    }

    /**
     * Streams the selected podcast episode.
     */
    private fun playEpisode(
        episode: Episode
    ) {

        val audioUrl =
            episode.audioUrl

        if (audioUrl.isNullOrBlank()) {

            Toast.makeText(
                this,
                "Audio is unavailable for this episode.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        releaseMediaPlayer()
        resetPlaybackControls()

        currentEpisodeTitle =
            episode.title

        isPaused =
            false

        btnStop.isEnabled =
            true

        tvEpisodeStatus.text =
            "Preparing: ${episode.title}"

        mediaPlayer =
            MediaPlayer().apply {

                try {

                    setDataSource(
                        audioUrl
                    )

                    /*
                     * Start playback after the remote
                     * audio stream has been prepared.
                     */
                    setOnPreparedListener { player ->

                        /*
                         * Apply the current Spinner speed
                         * before playback begins.
                         */
                        try {

                            val playbackParams =
                                player.playbackParams

                            playbackParams.speed =
                                currentPlaybackSpeed

                            player.playbackParams =
                                playbackParams

                        } catch (_: Exception) {

                            // Continue using normal speed if unsupported.
                        }

                        player.start()

                        val duration =
                            player.duration

                        playbackSeekBar.max =
                            duration

                        playbackSeekBar.progress =
                            0

                        playbackSeekBar.isEnabled =
                            true

                        tvCurrentTime.text =
                            "00:00"

                        tvDuration.text =
                            formatTime(
                                duration
                            )

                        btnPauseResume.isEnabled =
                            true

                        btnPauseResume.text =
                            "Pause"

                        btnStop.isEnabled =
                            true

                        tvEpisodeStatus.text =
                            "Playing: ${episode.title}"

                        startProgressUpdates()

                        Toast.makeText(
                            this@PodcastDetailsActivity,
                            "Playing at ${playbackSpeedLabels[spinnerPlaybackSpeed.selectedItemPosition]}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    /*
                     * Reset playback controls when the
                     * episode finishes.
                     */
                    setOnCompletionListener {

                        tvEpisodeStatus.text =
                            "Playback finished."

                        resetPlaybackControls()
                        releaseMediaPlayer()
                    }

                    /*
                     * Handle streaming errors safely.
                     */
                    setOnErrorListener { _, _, _ ->

                        tvEpisodeStatus.text =
                            "Unable to play this episode."

                        resetPlaybackControls()
                        releaseMediaPlayer()

                        true
                    }

                    prepareAsync()

                } catch (
                    exception: Exception
                ) {

                    tvEpisodeStatus.text =
                        "Playback error: " +
                                (
                                        exception.localizedMessage
                                            ?: "Unknown error"
                                        )

                    resetPlaybackControls()
                    releaseMediaPlayer()
                }
            }
    }

    /**
     * Starts updating the SeekBar and elapsed time.
     */
    private fun startProgressUpdates() {

        progressHandler.removeCallbacks(
            progressRunnable
        )

        progressHandler.post(
            progressRunnable
        )
    }

    /**
     * Stops automatic playback progress updates.
     */
    private fun stopProgressUpdates() {

        progressHandler.removeCallbacks(
            progressRunnable
        )
    }

    /**
     * Stops the currently playing episode.
     */
    private fun stopPlayback() {

        val player =
            mediaPlayer

        if (player != null) {

            try {

                if (
                    player.isPlaying ||
                    isPaused
                ) {

                    player.stop()
                }

            } catch (
                _: IllegalStateException
            ) {

                // Player may already have stopped.
            }
        }

        tvEpisodeStatus.text =
            "Playback stopped."

        resetPlaybackControls()
        releaseMediaPlayer()
    }

    /**
     * Downloads and parses the selected podcast RSS feed.
     */
    private fun loadEpisodes(
        feedUrl: String
    ) {

        showEpisodeLoading(
            true
        )

        tvEpisodeStatus.text =
            "Loading episodes..."

        RetrofitClient.apiService
            .getPodcastFeed(
                feedUrl
            )
            .enqueue(
                object :
                    Callback<ResponseBody> {

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response:
                        Response<ResponseBody>
                    ) {

                        if (!response.isSuccessful) {

                            showEpisodeLoading(
                                false
                            )

                            tvEpisodeStatus.text =
                                "Unable to load episodes. " +
                                        "Error code: ${response.code()}"

                            return
                        }

                        val responseBody =
                            response.body()

                        if (responseBody == null) {

                            showEpisodeLoading(
                                false
                            )

                            tvEpisodeStatus.text =
                                "The podcast feed was empty."

                            return
                        }

                        Thread {

                            try {

                                val episodes =
                                    responseBody
                                        .use { body ->

                                            body
                                                .byteStream()
                                                .use {
                                                        inputStream ->

                                                    RssParser
                                                        .parse(
                                                            inputStream
                                                        )
                                                }
                                        }

                                runOnUiThread {

                                    showEpisodeLoading(
                                        false
                                    )

                                    episodeAdapter
                                        .updateEpisodes(
                                            episodes
                                        )

                                    tvEpisodeStatus.text =
                                        if (
                                            episodes.isEmpty()
                                        ) {

                                            "No episodes were found."

                                        } else {

                                            "${episodes.size} episode(s) found."
                                        }
                                }

                            } catch (
                                exception:
                                Exception
                            ) {

                                runOnUiThread {

                                    showEpisodeLoading(
                                        false
                                    )

                                    tvEpisodeStatus.text =
                                        "Could not read episodes: " +
                                                (
                                                        exception
                                                            .localizedMessage
                                                            ?: "Unknown error"
                                                        )
                                }
                            }

                        }.start()
                    }

                    override fun onFailure(
                        call: Call<ResponseBody>,
                        throwable: Throwable
                    ) {

                        showEpisodeLoading(
                            false
                        )

                        tvEpisodeStatus.text =
                            "Episode network error: " +
                                    (
                                            throwable
                                                .localizedMessage
                                                ?: "Unknown error"
                                            )
                    }
                }
            )
    }

    /**
     * Formats milliseconds as MM:SS or H:MM:SS.
     */
    private fun formatTime(
        milliseconds: Int
    ): String {

        val totalSeconds =
            milliseconds / 1000

        val seconds =
            totalSeconds % 60

        val totalMinutes =
            totalSeconds / 60

        val minutes =
            totalMinutes % 60

        val hours =
            totalMinutes / 60

        return if (hours > 0) {

            String.format(
                "%d:%02d:%02d",
                hours,
                minutes,
                seconds
            )

        } else {

            String.format(
                "%02d:%02d",
                minutes,
                seconds
            )
        }
    }

    /**
     * Shows or hides RSS loading progress.
     */
    private fun showEpisodeLoading(
        isLoading: Boolean
    ) {

        episodeProgressBar.visibility =
            if (isLoading) {

                View.VISIBLE

            } else {

                View.GONE
            }
    }

    /**
     * Updates Subscribe / Unsubscribe button text.
     */
    private fun updateSubscriptionButton() {

        btnSubscribe.text =
            if (isSubscribed) {

                "Unsubscribe"

            } else {

                "Subscribe"
            }
    }

    /**
     * Returns playback controls to their idle state.
     */
    private fun resetPlaybackControls() {

        stopProgressUpdates()

        btnPauseResume.isEnabled =
            false

        btnPauseResume.text =
            "Pause"

        btnStop.isEnabled =
            false

        playbackSeekBar.isEnabled =
            false

        playbackSeekBar.progress =
            0

        tvCurrentTime.text =
            "00:00"

        tvDuration.text =
            "00:00"

        isPaused =
            false

        currentEpisodeTitle =
            null
    }

    /**
     * Safely releases MediaPlayer resources.
     */
    private fun releaseMediaPlayer() {

        stopProgressUpdates()

        try {

            mediaPlayer?.release()

        } catch (_: Exception) {

            // Ignore MediaPlayer cleanup errors.
        }

        mediaPlayer =
            null
    }

    /**
     * Handles toolbar back navigation.
     */
    override fun onSupportNavigateUp():
            Boolean {

        finish()

        return true
    }

    /**
     * Releases playback resources when the Activity closes.
     */
    override fun onDestroy() {

        stopProgressUpdates()
        releaseMediaPlayer()

        super.onDestroy()
    }

    companion object {

        const val EXTRA_PODCAST_ID =
            "podcast_id"

        const val EXTRA_PODCAST_NAME =
            "podcast_name"

        const val EXTRA_ARTIST_NAME =
            "artist_name"

        const val EXTRA_GENRE =
            "genre"

        const val EXTRA_FEED_URL =
            "feed_url"
    }
}