package com.wubitcode.androidapp4.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wubitcode.androidapp4.R
import com.wubitcode.androidapp4.model.SubscribedPodcast

/**
 * RecyclerView adapter used to display podcasts saved
 * in the user's My Subscriptions list.
 *
 * Selecting a saved podcast reopens PodcastDetailsActivity
 * so the user can view episodes, play audio, or unsubscribe.
 */
class SubscriptionAdapter(
    private var subscriptions: List<SubscribedPodcast> = emptyList()
) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    /**
     * Holds references to the views used for one
     * subscribed podcast row.
     */
    class SubscriptionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val podcastName: TextView =
            itemView.findViewById(R.id.tvPodcastName)

        val artistName: TextView =
            itemView.findViewById(R.id.tvArtistName)

        val genre: TextView =
            itemView.findViewById(R.id.tvGenre)
    }

    /**
     * Creates a new subscription row using
     * the existing item_podcast.xml layout.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubscriptionViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_podcast,
                    parent,
                    false
                )

        return SubscriptionViewHolder(view)
    }

    /**
     * Displays the saved podcast information
     * and handles podcast selection.
     */
    override fun onBindViewHolder(
        holder: SubscriptionViewHolder,
        position: Int
    ) {

        val podcast =
            subscriptions[position]

        holder.podcastName.text =
            podcast.podcastName

        holder.artistName.text =
            podcast.artistName

        holder.genre.text =
            podcast.genre

        /*
         * Reopen the podcast details screen when the
         * user selects a saved subscription.
         */
        holder.itemView.setOnClickListener {

            val context =
                holder.itemView.context

            val intent =
                Intent(
                    context,
                    PodcastDetailsActivity::class.java
                ).apply {

                    putExtra(
                        PodcastDetailsActivity.EXTRA_PODCAST_ID,
                        podcast.podcastId
                    )

                    putExtra(
                        PodcastDetailsActivity.EXTRA_PODCAST_NAME,
                        podcast.podcastName
                    )

                    putExtra(
                        PodcastDetailsActivity.EXTRA_ARTIST_NAME,
                        podcast.artistName
                    )

                    putExtra(
                        PodcastDetailsActivity.EXTRA_GENRE,
                        podcast.genre
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
     * Returns the number of saved subscriptions.
     */
    override fun getItemCount(): Int {
        return subscriptions.size
    }

    /**
     * Replaces the current list of saved podcasts
     * and refreshes the RecyclerView.
     */
    fun updateSubscriptions(
        newSubscriptions: List<SubscribedPodcast>
    ) {

        subscriptions =
            newSubscriptions

        notifyDataSetChanged()
    }
}