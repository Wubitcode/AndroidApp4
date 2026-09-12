package com.wubitcode.androidapp4.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wubitcode.androidapp4.R
import com.wubitcode.androidapp4.model.Podcast

/**
 * RecyclerView adapter that displays podcast search results.
 *
 * Each podcast is shown using item_podcast.xml.
 * When a podcast is selected, the app opens the details screen.
 */
class PodcastAdapter(
    private var podcasts: List<Podcast> = emptyList()
) : RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder>() {

    /**
     * Holds references to the views used for one podcast result.
     */
    class PodcastViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val podcastName: TextView =
            itemView.findViewById(R.id.tvPodcastName)

        val artistName: TextView =
            itemView.findViewById(R.id.tvArtistName)

        val genre: TextView =
            itemView.findViewById(R.id.tvGenre)
    }

    /**
     * Creates a new ViewHolder when RecyclerView needs
     * another podcast item on screen.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PodcastViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_podcast, parent, false)

        return PodcastViewHolder(view)
    }

    /**
     * Displays podcast information and handles item selection.
     */
    override fun onBindViewHolder(
        holder: PodcastViewHolder,
        position: Int
    ) {
        val podcast = podcasts[position]

        holder.podcastName.text =
            podcast.collectionName ?: "Unknown Podcast"

        holder.artistName.text =
            podcast.artistName ?: "Unknown Artist"

        holder.genre.text =
            podcast.primaryGenreName ?: "Unknown Genre"

        // Opens the podcast details screen when the user taps a result.
        holder.itemView.setOnClickListener {

            val context = holder.itemView.context

            val intent = Intent(
                context,
                PodcastDetailsActivity::class.java
            ).apply {
                putExtra(
                    PodcastDetailsActivity.EXTRA_PODCAST_ID,
                    podcast.collectionId
                )
                putExtra(
                    PodcastDetailsActivity.EXTRA_PODCAST_NAME,
                    podcast.collectionName
                )

                putExtra(
                    PodcastDetailsActivity.EXTRA_ARTIST_NAME,
                    podcast.artistName
                )

                putExtra(
                    PodcastDetailsActivity.EXTRA_GENRE,
                    podcast.primaryGenreName
                )

                putExtra(
                    PodcastDetailsActivity.EXTRA_FEED_URL,
                    podcast.feedUrl
                )
            }

            context.startActivity(intent)
        }
    }

    /**
     * Returns the total number of podcast results.
     */
    override fun getItemCount(): Int {
        return podcasts.size
    }

    /**
     * Replaces the current podcast list with new search results
     * and refreshes the RecyclerView.
     */
    fun updatePodcasts(newPodcasts: List<Podcast>) {
        podcasts = newPodcasts
        notifyDataSetChanged()
    }
}