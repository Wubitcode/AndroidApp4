AndroidApp4 – SuperPodcast

MWD3B Android Development – Assignment 8

This project is the completed and polished version of the SuperPodcast
application started in Assignment 7, as required for Assignment 8.

SuperPodcast is an Android podcast application developed in Kotlin for my Mobile Web Development course.

The application allows users to search for podcasts using the iTunes Search API, view podcast details, load episodes from RSS feeds, subscribe to podcasts, and stream podcast episodes directly inside the app.

For Assignment 8, the application was extended with improved subscription management, audio playback controls, playback progress, playback speed controls, background podcast checking, and notifications.


Features
Podcast Search

Users can search for podcasts using any topic or keyword.

Examples:

•	Cybersecurity
•	Technology
•	Education
•	Comedy
•	Business
•	Science
•	History
•	Sports

Podcast data is retrieved from the iTunes Search API using Retrofit.


Advanced Podcast Title Filter

SuperPodcast includes an optional minimum-title-word filter.

For example, if the user enters:
the application displays only podcast titles containing at least four words.
This feature was added to make the application different from the basic PodPlay tutorial implementation.

Podcast Details
Selecting a podcast opens a detailed screen showing:
•	Podcast title
•	Artist or publisher
•	Genre
•	RSS feed URL
•	Subscribe / Unsubscribe control
•	Available podcast episodes
RSS Episode Loading
Podcast episodes are retrieved from each podcast's RSS feed.
The application uses an XML parser to extract information including:
•	Episode title
•	Description
•	Publication date
•	Audio URL
Episodes are displayed using a RecyclerView.
Podcast Playback
Users can stream podcast episodes directly inside SuperPodcast using Android MediaPlayer.
Playback features include:
•	Play
•	Pause
•	Resume
•	Stop
•	Seek through an episode
•	Display elapsed playback time
•	Display total episode duration
Playback Speed
Users can change the playback speed while listening to a podcast episode.
Available speeds are:
0.75x
1.00x
1.25x
1.50x
2.00x
The speed can be changed while audio is playing.
Podcast Subscriptions
Users can subscribe to podcasts from the Podcast Details screen.
Subscriptions are stored locally using:
•	SharedPreferences
•	Gson
The application stores important podcast information including:
•	Podcast ID
•	Podcast name
•	Artist name
•	Genre
•	RSS feed URL
Users can also unsubscribe at any time.
My Subscriptions
The My Subscriptions screen displays all podcasts saved by the user.
Users can:
•	View saved podcasts
•	Open a subscribed podcast
•	Access its episodes
•	Play episodes
•	Remove the subscription
The subscriptions list automatically refreshes when the user returns to the screen.
Background Podcast Update Checks
SuperPodcast uses Android WorkManager to perform periodic background checks for subscribed podcasts.
The application:
1.	Reads the user's subscriptions.
2.	Downloads each podcast RSS feed.
3.	Checks the newest available episode.
4.	Compares it with the previously recorded episode.
5.	Detects when a newer episode becomes available.
      Background work runs only when a network connection is available.
      Podcast Notifications
      SuperPodcast can notify the user when a new episode is detected for a subscribed podcast.
      Android notification functionality includes:
      •	Notification channel
      •	New episode notification
      •	Podcast name
      •	Episode title
      •	Android 13+ notification permission handling
      Users can allow or deny notification permission without affecting the main podcast search and playback functionality.
      Technologies Used
      •	Kotlin
      •	Android Studio
      •	Android SDK
      •	Retrofit
      •	Gson
      •	RecyclerView
      •	MediaPlayer
      •	SharedPreferences
      •	WorkManager
      •	NotificationCompat
      •	Android XML layouts
      •	XML / RSS parsing
      •	iTunes Search API
      •	Git
      •	GitHub


Project Structure

com.wubitcode.androidapp4
│
├── MainActivity.kt
│
├── model
│   ├── Episode.kt
│   ├── Podcast.kt
│   ├── PodcastSearchResponse.kt
│   ├── SubscribedPodcast.kt
│   └── SubscriptionManager.kt
│
├── network
│   ├── ITunesApiService.kt
│   ├── RetrofitClient.kt
│   └── RssParser.kt
│
├── ui
│   ├── EpisodeAdapter.kt
│   ├── PodcastAdapter.kt
│   ├── PodcastDetailsActivity.kt
│   ├── SubscriptionAdapter.kt
│   └── SubscriptionsActivity.kt
│
└── worker
├── PodcastUpdateWorker.kt
└── PodcastUpdateScheduler.kt


Main Application Flow

Main Screen
↓
Search iTunes Podcast API
↓
Podcast Results
↓
Podcast Details
↓
RSS Feed
↓
Episode List
↓
Audio Playback


Subscription flow:


Podcast Details
↓
Subscribe
↓
SharedPreferences + Gson
↓
My Subscriptions


Background update flow:


WorkManager
↓
Subscribed Podcasts
↓
RSS Feed Check
↓
New Episode Detection
↓
Android Notification
