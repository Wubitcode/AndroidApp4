package com.wubitcode.androidapp4

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wubitcode.androidapp4.model.Podcast
import com.wubitcode.androidapp4.model.PodcastSearchResponse
import com.wubitcode.androidapp4.network.RetrofitClient
import com.wubitcode.androidapp4.ui.PodcastAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Main screen for the SuperPodcast application.
 *
 * Users can:
 * - Search the iTunes podcast directory.
 * - Optionally filter results by the minimum number
 *   of words contained in the podcast title.
 * - Open a podcast to view details and episodes.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var etMinimumWords: EditText
    private lateinit var btnSearch: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var recyclerViewPodcasts: RecyclerView

    private lateinit var podcastAdapter: PodcastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect Kotlin variables to the main screen views.
        etSearch = findViewById(R.id.etSearch)
        etMinimumWords = findViewById(R.id.etMinimumWords)
        btnSearch = findViewById(R.id.btnSearch)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        recyclerViewPodcasts = findViewById(R.id.recyclerViewPodcasts)

        setupRecyclerView()

        // Start the podcast search when the Search button is selected.
        btnSearch.setOnClickListener {

            val searchTerm =
                etSearch.text.toString().trim()

            if (searchTerm.isEmpty()) {

                tvStatus.text =
                    "Please enter a podcast search term."

                return@setOnClickListener
            }

            /*
             * The advanced filter is optional.
             * If the user leaves it blank, no minimum-word
             * restriction is applied.
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
    }

    /**
     * Configures the RecyclerView used for podcast search results.
     */
    private fun setupRecyclerView() {

        podcastAdapter =
            PodcastAdapter()

        recyclerViewPodcasts.apply {

            layoutManager =
                LinearLayoutManager(this@MainActivity)

            adapter =
                podcastAdapter
        }
    }

    /**
     * Sends a podcast search request to the iTunes Search API.
     *
     * If a minimum word count is supplied, the returned podcasts
     * are filtered before being displayed.
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
                object : Callback<PodcastSearchResponse> {

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
                         * Apply the advanced title-word filter
                         * only when the user enters a value.
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
                                    (throwable.localizedMessage
                                        ?: "Unknown error")
                    }
                }
            )
    }

    /**
     * Filters podcasts by the number of words in the title.
     *
     * Example:
     * Minimum = 4
     *
     * "Cybersecurity Today" -> 2 words -> excluded
     * "Inside the World of Cybersecurity" -> 5 words -> included
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

                wordCount >= minimumWords
            }
        }
    }

    /**
     * Displays useful information about the search and filter results.
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
     * Shows or hides the loading indicator while a network
     * request is running.
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