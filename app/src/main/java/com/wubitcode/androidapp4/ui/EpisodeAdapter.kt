package com.wubitcode.androidapp4.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.wubitcode.androidapp4.R
import com.wubitcode.androidapp4.model.Episode

/**
 * RecyclerView adapter used to display podcast episodes
 * retrieved from a podcast RSS feed.
 *
 * The adapter also reports when the user selects
 * the Play Episode button.
 */
class EpisodeAdapter(
    private var episodes: List<Episode> = emptyList(),
    private val onPlayClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    /**
     * Holds references to the views used for one episode row.
     */
    class EpisodeViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val episodeTitle: TextView =
            itemView.findViewById(R.id.tvEpisodeTitle)

        val episodeDate: TextView =
            itemView.findViewById(R.id.tvEpisodeDate)

        val episodeDescription: TextView =
            itemView.findViewById(R.id.tvEpisodeDescription)

        val playButton: Button =
            itemView.findViewById(R.id.btnPlayEpisode)
    }

    /**
     * Creates a new episode row using item_episode.xml.
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EpisodeViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_episode,
                parent,
                false
            )

        return EpisodeViewHolder(view)
    }

    /**
     * Displays episode information and handles
     * the Play Episode button.
     */
    override fun onBindViewHolder(
        holder: EpisodeViewHolder,
        position: Int
    ) {

        val episode = episodes[position]

        holder.episodeTitle.text =
            episode.title

        holder.episodeDate.text =
            episode.publicationDate
                ?: "Date unavailable"

        /*
         * Podcast RSS descriptions often contain HTML.
         * HtmlCompat converts basic HTML into readable text.
         */
        val description =
            episode.description
                ?: "No description available."

        holder.episodeDescription.text =
            HtmlCompat.fromHtml(
                description,
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

        /*
         * Only enable playback when the RSS feed
         * provides a valid episode audio URL.
         */
        holder.playButton.isEnabled =
            !episode.audioUrl.isNullOrBlank()

        holder.playButton.setOnClickListener {

            if (!episode.audioUrl.isNullOrBlank()) {
                onPlayClick(episode)
            }
        }
    }

    /**
     * Returns the number of episodes currently displayed.
     */
    override fun getItemCount(): Int {
        return episodes.size
    }

    /**
     * Replaces the current episode list with newly
     * retrieved RSS episodes.
     */
    fun updateEpisodes(
        newEpisodes: List<Episode>
    ) {
        episodes = newEpisodes
        notifyDataSetChanged()
    }
}