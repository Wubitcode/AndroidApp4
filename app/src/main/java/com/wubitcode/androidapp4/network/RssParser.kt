package com.wubitcode.androidapp4.network

import android.util.Xml
import com.wubitcode.androidapp4.model.Episode
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

/**
 * Parses a podcast RSS feed and converts each <item>
 * element into an Episode object used by SuperPodcast.
 */
object RssParser {

    /**
     * Reads podcast RSS XML from the supplied InputStream.
     *
     * Common RSS fields used:
     * - <title>       Episode title
     * - <description> Episode description
     * - <pubDate>     Publication date
     * - <enclosure>   Audio file URL
     */
    fun parse(inputStream: InputStream): List<Episode> {

        val episodes = mutableListOf<Episode>()

        val parser = Xml.newPullParser()

        // Podcast RSS feeds do not require namespace processing
        // for the basic fields used by this assignment.
        parser.setFeature(
            XmlPullParser.FEATURE_PROCESS_NAMESPACES,
            false
        )

        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var insideItem = false

        var title: String? = null
        var description: String? = null
        var publicationDate: String? = null
        var audioUrl: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {

            when (eventType) {

                XmlPullParser.START_TAG -> {

                    when (parser.name.lowercase()) {

                        "item" -> {
                            /*
                             * A new podcast episode has started.
                             * Reset temporary values before reading it.
                             */
                            insideItem = true
                            title = null
                            description = null
                            publicationDate = null
                            audioUrl = null
                        }

                        "title" -> {
                            if (insideItem) {
                                title = parser.nextText()
                            }
                        }

                        "description" -> {
                            if (insideItem) {
                                description = parser.nextText()
                            }
                        }

                        "pubdate" -> {
                            if (insideItem) {
                                publicationDate = parser.nextText()
                            }
                        }

                        "enclosure" -> {
                            if (insideItem) {

                                /*
                                 * The enclosure element normally contains
                                 * the URL of the episode's audio file.
                                 */
                                audioUrl =
                                    parser.getAttributeValue(
                                        null,
                                        "url"
                                    )
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {

                    if (
                        parser.name.equals(
                            "item",
                            ignoreCase = true
                        )
                    ) {

                        /*
                         * The episode is complete, so convert
                         * the collected RSS values into Episode.
                         */
                        episodes.add(
                            Episode(
                                title = title ?: "Untitled Episode",
                                description = description,
                                publicationDate = publicationDate,
                                audioUrl = audioUrl
                            )
                        )

                        insideItem = false
                    }
                }
            }

            eventType = parser.next()
        }

        return episodes
    }
}