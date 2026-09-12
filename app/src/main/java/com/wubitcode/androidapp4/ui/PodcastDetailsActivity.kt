package com.wubitcode.androidapp4.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wubitcode.androidapp4.R
import com.wubitcode.androidapp4.model.Episode
import com.wubitcode.androidapp4.model.SubscriptionManager
import com.wubitcode.androidapp4.network.RetrofitClient
import com.wubitcode.androidapp4.network.RssParser
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Displays information about a selected podcast.
 *
 * Features:
 * - Persistent podcast subscription management.
 * - RSS feed downloading and parsing.
 * - Episode display using RecyclerView.
 * - Streaming podcast playback.
 * - Pause, resume, and stop playback controls.
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

    private lateinit var episodeAdapter: EpisodeAdapter

    private var podcastId: Long = -1L
    private var isSubscribed: Boolean = false

    /**
     * Handles streaming playback of the selected podcast episode.
     */
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Tracks whether playback is currently paused.
     */
    private var isPaused: Boolean = false

    /**
     * Stores the title of the episode currently being played.
     */
    private var currentEpisodeTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast_details)

        setupToolbar()
        connectViews()
        setupEpisodeRecyclerView()

        /*
         * Retrieve podcast information passed from PodcastAdapter.
         */
        podcastId =
            intent.getLongExtra(
                EXTRA_PODCAST_ID,
                -1L
            )

        val podcastName =
            intent.getStringExtra(EXTRA_PODCAST_NAME)
                ?: "Unknown Podcast"

        val artistName =
            intent.getStringExtra(EXTRA_ARTIST_NAME)
                ?: "Unknown Artist"

        val genre =
            intent.getStringExtra(EXTRA_GENRE)
                ?: "Unknown Genre"

        val feedUrl =
            intent.getStringExtra(EXTRA_FEED_URL)

        // Display selected podcast information.
        tvDetailsTitle.text = podcastName
        tvDetailsArtist.text = artistName
        tvDetailsGenre.text = genre

        tvDetailsFeed.text =
            feedUrl ?: "No feed URL available"

        setupSubscription()
        setupPlaybackControls()

        /*
         * Load episodes when the selected podcast
         * provides a valid RSS feed URL.
         */
        if (!feedUrl.isNullOrBlank()) {
            loadEpisodes(feedUrl)
        } else {
            tvEpisodeStatus.text =
                "Episodes are unavailable for this podcast."
        }
    }

    /**
     * Configures the toolbar and back navigation.
     */
    private fun setupToolbar() {

        val toolbar: Toolbar =
            findViewById(R.id.detailsToolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Podcast Details"
            setDisplayHomeAsUpEnabled(true)
        }

        // Make the back arrow visible on the purple toolbar.
        toolbar.navigationIcon?.setTint(
            getColor(android.R.color.white)
        )
    }

    /**
     * Connects Kotlin variables to the XML views.
     */
    private fun connectViews() {

        tvDetailsTitle =
            findViewById(R.id.tvDetailsTitle)

        tvDetailsArtist =
            findViewById(R.id.tvDetailsArtist)

        tvDetailsGenre =
            findViewById(R.id.tvDetailsGenre)

        tvDetailsFeed =
            findViewById(R.id.tvDetailsFeed)

        btnSubscribe =
            findViewById(R.id.btnSubscribe)

        tvEpisodeStatus =
            findViewById(R.id.tvEpisodeStatus)

        episodeProgressBar =
            findViewById(R.id.episodeProgressBar)

        recyclerViewEpisodes =
            findViewById(R.id.recyclerViewEpisodes)

        btnPauseResume =
            findViewById(R.id.btnPauseResume)

        btnStop =
            findViewById(R.id.btnStop)
    }

    /**
     * Configures the episode RecyclerView.
     *
     * EpisodeAdapter reports Play Episode selections
     * through the callback.
     */
    private fun setupEpisodeRecyclerView() {

        episodeAdapter = EpisodeAdapter { episode ->
            playEpisode(episode)
        }

        recyclerViewEpisodes.apply {

            layoutManager =
                LinearLayoutManager(
                    this@PodcastDetailsActivity
                )

            adapter = episodeAdapter
        }
    }

    /**
     * Configures Pause / Resume and Stop buttons.
     */
    private fun setupPlaybackControls() {

        btnPauseResume.setOnClickListener {

            val player = mediaPlayer ?: return@setOnClickListener

            if (player.isPlaying) {

                player.pause()
                isPaused = true

                btnPauseResume.text = "Resume"

                tvEpisodeStatus.text =
                    "Paused: ${currentEpisodeTitle ?: "Episode"}"

            } else if (isPaused) {

                player.start()
                isPaused = false

                btnPauseResume.text = "Pause"

                tvEpisodeStatus.text =
                    "Playing: ${currentEpisodeTitle ?: "Episode"}"
            }
        }

        btnStop.setOnClickListener {
            stopPlayback()
        }
    }

    /**
     * Streams a selected podcast episode.
     */
    private fun playEpisode(
        episode: Episode
    ) {

        val audioUrl = episode.audioUrl

        if (audioUrl.isNullOrBlank()) {

            Toast.makeText(
                this,
                "Audio is unavailable for this episode.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        /*
         * Stop and release any previously playing episode.
         */
        releaseMediaPlayer()

        currentEpisodeTitle = episode.title
        isPaused = false

        btnPauseResume.text = "Pause"
        btnPauseResume.isEnabled = false
        btnStop.isEnabled = true

        tvEpisodeStatus.text =
            "Preparing: ${episode.title}"

        mediaPlayer = MediaPlayer().apply {

            try {

                setDataSource(audioUrl)

                /*
                 * Start playback after the remote audio
                 * stream has finished preparing.
                 */
                setOnPreparedListener { player ->

                    player.start()

                    btnPauseResume.isEnabled = true
                    btnStop.isEnabled = true

                    tvEpisodeStatus.text =
                        "Playing: ${episode.title}"

                    Toast.makeText(
                        this@PodcastDetailsActivity,
                        "Playing episode",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                /*
                 * Reset the controls when playback finishes.
                 */
                setOnCompletionListener {

                    tvEpisodeStatus.text =
                        "Playback finished."

                    resetPlaybackControls()
                    releaseMediaPlayer()
                }

                /*
                 * Handle playback errors safely.
                 */
                setOnErrorListener { _, _, _ ->

                    tvEpisodeStatus.text =
                        "Unable to play this episode."

                    resetPlaybackControls()
                    releaseMediaPlayer()

                    true
                }

                prepareAsync()

            } catch (exception: Exception) {

                tvEpisodeStatus.text =
                    "Playback error: " +
                            (exception.localizedMessage
                                ?: "Unknown error")

                resetPlaybackControls()
                releaseMediaPlayer()
            }
        }
    }

    /**
     * Stops the current podcast episode.
     */
    private fun stopPlayback() {

        val player = mediaPlayer

        if (player != null) {

            try {

                if (player.isPlaying || isPaused) {
                    player.stop()
                }

            } catch (_: IllegalStateException) {
                // Player may already be stopped or released.
            }
        }

        tvEpisodeStatus.text =
            "Playback stopped."

        resetPlaybackControls()
        releaseMediaPlayer()
    }

    /**
     * Initializes saved podcast subscription state.
     */
    private fun setupSubscription() {

        if (podcastId != -1L) {

            isSubscribed =
                SubscriptionManager.isSubscribed(
                    this,
                    podcastId
                )

            updateSubscriptionButton()

        } else {

            btnSubscribe.isEnabled = false

            btnSubscribe.text =
                "Subscription Unavailable"
        }

        btnSubscribe.setOnClickListener {

            if (podcastId == -1L) {
                return@setOnClickListener
            }

            if (isSubscribed) {

                SubscriptionManager.unsubscribe(
                    this,
                    podcastId
                )

                isSubscribed = false

            } else {

                SubscriptionManager.subscribe(
                    this,
                    podcastId
                )

                isSubscribed = true
            }

            updateSubscriptionButton()
        }
    }

    /**
     * Downloads and parses a podcast RSS feed.
     */
    private fun loadEpisodes(
        feedUrl: String
    ) {

        showEpisodeLoading(true)

        tvEpisodeStatus.text =
            "Loading episodes..."

        RetrofitClient.apiService
            .getPodcastFeed(feedUrl)
            .enqueue(
                object : Callback<ResponseBody> {

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {

                        if (!response.isSuccessful) {

                            showEpisodeLoading(false)

                            tvEpisodeStatus.text =
                                "Unable to load episodes. " +
                                        "Error code: ${response.code()}"

                            return
                        }

                        val responseBody =
                            response.body()

                        if (responseBody == null) {

                            showEpisodeLoading(false)

                            tvEpisodeStatus.text =
                                "The podcast feed was empty."

                            return
                        }

                        /*
                         * RSS parsing is performed away from
                         * the main UI thread.
                         */
                        Thread {

                            try {

                                val episodes =
                                    responseBody.use { body ->

                                        body.byteStream().use { inputStream ->

                                            RssParser.parse(
                                                inputStream
                                            )
                                        }
                                    }

                                runOnUiThread {

                                    showEpisodeLoading(false)

                                    episodeAdapter.updateEpisodes(
                                        episodes
                                    )

                                    if (episodes.isEmpty()) {

                                        tvEpisodeStatus.text =
                                            "No episodes were found."

                                    } else {

                                        tvEpisodeStatus.text =
                                            "${episodes.size} episode(s) found."
                                    }
                                }

                            } catch (
                                exception: Exception
                            ) {

                                runOnUiThread {

                                    showEpisodeLoading(false)

                                    tvEpisodeStatus.text =
                                        "Could not read episodes: " +
                                                (exception.localizedMessage
                                                    ?: "Unknown error")
                                }
                            }

                        }.start()
                    }

                    override fun onFailure(
                        call: Call<ResponseBody>,
                        throwable: Throwable
                    ) {

                        showEpisodeLoading(false)

                        tvEpisodeStatus.text =
                            "Episode network error: " +
                                    (throwable.localizedMessage
                                        ?: "Unknown error")
                    }
                }
            )
    }

    /**
     * Shows or hides the RSS loading indicator.
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
     * Updates the subscription button based on
     * the currently saved state.
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
     * Resets playback buttons to their idle state.
     */
    private fun resetPlaybackControls() {

        btnPauseResume.isEnabled = false
        btnPauseResume.text = "Pause"

        btnStop.isEnabled = false

        isPaused = false
        currentEpisodeTitle = null
    }

    /**
     * Releases MediaPlayer resources safely.
     */
    private fun releaseMediaPlayer() {

        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
            // Ignore release errors during cleanup.
        }

        mediaPlayer = null
    }

    /**
     * Returns to the podcast search screen.
     */
    override fun onSupportNavigateUp(): Boolean {

        finish()

        return true
    }

    /**
     * Releases playback resources when the Activity closes.
     */
    override fun onDestroy() {

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