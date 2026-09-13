package com.wubitcode.androidapp4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wubitcode.androidapp4.model.Podcast
import com.wubitcode.androidapp4.model.PodcastSearchResponse
import com.wubitcode.androidapp4.network.RetrofitClient
import com.wubitcode.androidapp4.ui.PodcastAdapter
import com.wubitcode.androidapp4.ui.SubscriptionsActivity
import com.wubitcode.androidapp4.worker.PodcastUpdateScheduler
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Main screen for the SuperPodcast application.
 *
 * Users can:
 * - Search the iTunes podcast directory.
 * - Apply an optional minimum-title-word filter.
 * - Open podcast details.
 * - Open the My Subscriptions screen.
 * - Receive background update checks for subscribed podcasts.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var etMinimumWords: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnSubscriptions: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var recyclerViewPodcasts: RecyclerView

    private lateinit var podcastAdapter: PodcastAdapter

    /**
     * Handles the Android 13+ notification permission request.
     *
     * The app still works normally if the user declines.
     * Notifications simply cannot be displayed until permission
     * is granted through Android settings.
     */
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {

                tvStatus.text =
                    "Podcast update notifications are enabled."

            } else {

                tvStatus.text =
                    "Notifications are disabled. Podcast search still works normally."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Connect Kotlin variables to the main screen views.
        connectViews()

        // Configure the podcast search results list.
        setupRecyclerView()

        /*
         * Schedule periodic background checks for newly
         * published episodes from subscribed podcasts.
         */
        PodcastUpdateScheduler.schedule(this)

        /*
         * Android 13 and later require the user to approve
         * notification permission at runtime.
         */
        requestNotificationPermissionIfNeeded()

        // Start a podcast search when the Search button is selected.
        btnSearch.setOnClickListener {

            val searchTerm =
                etSearch.text
                    .toString()
                    .trim()

            if (searchTerm.isEmpty()) {

                tvStatus.text =
                    "Please enter a podcast search term."

                return@setOnClickListener
            }

            /*
             * The advanced filter is optional.
             * A blank or invalid value means no filter is applied.
             */
            val minimumWords =
                etMinimumWords.text
                    .toString()
                    .trim()
                    .toIntOrNull()

            searchPodcasts(
                searchTerm,
                minimumWords
            )
        }

        /*
         * Opens the My Subscriptions screen so users
         * can view podcasts they have saved.
         */
        btnSubscriptions.setOnClickListener {

            val intent =
                Intent(
                    this,
                    SubscriptionsActivity::class.java
                )

            startActivity(intent)
        }
    }

    /**
     * Connects Activity properties to their XML views.
     */
    private fun connectViews() {

        etSearch =
            findViewById(
                R.id.etSearch
            )

        etMinimumWords =
            findViewById(
                R.id.etMinimumWords
            )

        btnSearch =
            findViewById(
                R.id.btnSearch
            )

        btnSubscriptions =
            findViewById(
                R.id.btnSubscriptions
            )

        progressBar =
            findViewById(
                R.id.progressBar
            )

        tvStatus =
            findViewById(
                R.id.tvStatus
            )

        recyclerViewPodcasts =
            findViewById(
                R.id.recyclerViewPodcasts
            )
    }

    /**
     * Requests notification permission on Android 13
     * and newer when it has not already been granted.
     */
    private fun requestNotificationPermissionIfNeeded() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val permissionStatus =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )

            if (
                permissionStatus !=
                PackageManager.PERMISSION_GRANTED
            ) {

                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    /**
     * Configures the RecyclerView used to display
     * podcast search results.
     */
    private fun setupRecyclerView() {

        podcastAdapter =
            PodcastAdapter()

        recyclerViewPodcasts.apply {

            layoutManager =
                LinearLayoutManager(
                    this@MainActivity
                )

            adapter =
                podcastAdapter
        }
    }

    /**
     * Sends a podcast search request to the iTunes Search API.
     *
     * If a minimum word count is supplied, the returned
     * podcasts are filtered before being displayed.
     */
    private fun searchPodcasts(
        searchTerm: String,
        minimumWords: Int?
    ) {

        showLoading(true)

        tvStatus.text =
            "Searching for podcasts..."

        RetrofitClient.apiService
            .searchPodcasts(searchTerm)
            .enqueue(
                object :
                    Callback<PodcastSearchResponse> {

                    override fun onResponse(
                        call: Call<PodcastSearchResponse>,
                        response: Response<PodcastSearchResponse>
                    ) {

                        showLoading(false)

                        if (!response.isSuccessful) {

                            podcastAdapter.updatePodcasts(
                                emptyList()
                            )

                            tvStatus.text =
                                "Search failed. Error code: ${response.code()}"

                            return
                        }

                        val podcasts =
                            response.body()
                                ?.results
                                .orEmpty()

                        /*
                         * Apply the optional minimum-title-word
                         * filter before displaying results.
                         */
                        val filteredPodcasts =
                            applyMinimumWordFilter(
                                podcasts,
                                minimumWords
                            )

                        podcastAdapter.updatePodcasts(
                            filteredPodcasts
                        )

                        updateSearchStatus(
                            originalCount = podcasts.size,
                            displayedCount = filteredPodcasts.size,
                            minimumWords = minimumWords
                        )
                    }

                    override fun onFailure(
                        call: Call<PodcastSearchResponse>,
                        throwable: Throwable
                    ) {

                        showLoading(false)

                        podcastAdapter.updatePodcasts(
                            emptyList()
                        )

                        tvStatus.text =
                            "Network error: " +
                                    (
                                            throwable.localizedMessage
                                                ?: "Unknown error"
                                            )
                    }
                }
            )
    }

    /**
     * Filters podcast results according to the minimum
     * number of words required in the podcast title.
     */
    private fun applyMinimumWordFilter(
        podcasts: List<Podcast>,
        minimumWords: Int?
    ): List<Podcast> {

        if (
            minimumWords == null ||
            minimumWords <= 1
        ) {

            return podcasts
        }

        return podcasts.filter { podcast ->

            val title =
                podcast.collectionName
                    ?.trim()
                    .orEmpty()

            if (title.isEmpty()) {

                false

            } else {

                val wordCount =
                    title.split(
                        Regex("\\s+")
                    ).size

                wordCount >=
                        minimumWords
            }
        }
    }

    /**
     * Displays useful search and filter result information.
     */
    private fun updateSearchStatus(
        originalCount: Int,
        displayedCount: Int,
        minimumWords: Int?
    ) {

        if (displayedCount == 0) {

            tvStatus.text =
                if (
                    minimumWords != null &&
                    minimumWords > 1
                ) {

                    "No podcasts matched the minimum " +
                            "$minimumWords-word title filter."

                } else {

                    "No podcasts found."
                }

            return
        }

        tvStatus.text =
            if (
                minimumWords != null &&
                minimumWords > 1
            ) {

                "$displayedCount of $originalCount podcast(s) " +
                        "matched the minimum $minimumWords-word title filter."

            } else {

                "$displayedCount podcast(s) found."
            }
    }

    /**
     * Shows or hides the loading indicator while
     * a network request is running.
     */
    private fun showLoading(
        isLoading: Boolean
    ) {

        progressBar.visibility =
            if (isLoading) {

                View.VISIBLE

            } else {

                View.GONE
            }

        btnSearch.isEnabled =
            !isLoading
    }
}