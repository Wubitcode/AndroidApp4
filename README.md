# SuperPodcast

SuperPodcast is an Android podcast application developed for MWD3B Android Development Assignment 7.

The project applies Android networking concepts explored in the PodPlay tutorial.
It connects to the iTunes Search API to find podcasts, retrieves podcast RSS feeds, 
displays real episodes, supports subscriptions, and streams podcast audio.

## Features

### Podcast Search

- Search the iTunes podcast directory
- Retrieve live podcast data using Retrofit
- Parse JSON responses into Kotlin data classes
- Display search results using RecyclerView
- Show podcast title, creator, and genre

### Advanced Search Filter

SuperPodcast includes an additional search criterion
that allows users to specify the minimum number of words required in a podcast title.

Example:


Search term: cybersecurity
Minimum title words: 4
Podcast Details

Users can select a podcast from the search results to view:

Podcast title
Creator
Genre
RSS feed URL
Subscription status
Available episodes
Podcast Subscriptions

Users can:-

Subscribe to a podcast
Unsubscribe from a podcast
Keep subscription state after navigating away or restarting the app

Subscription information is stored locally using SharedPreferences.

RSS Episode Retrieval

SuperPodcast retrieves the selected podcast's RSS/XML feed and parses episode information including:

Episode title
Publication date
Description
Audio URL

Episodes are displayed using a RecyclerView.

Audio Playback

Podcast episodes can be streamed directly from their RSS audio URLs.

Playback controls include:

Play
Pause
Resume
Stop

Android MediaPlayer is used for streaming playback.

Technologies Used
Kotlin
Android Studio
Android SDK
Retrofit
Gson
RecyclerView
SharedPreferences
XmlPullParser
MediaPlayer
iTunes Search API
Podcast RSS feeds
Git
GitHub
Project Structure
com.wubitcode.androidapp4
│
├── MainActivity.kt
│
├── model
│   ├── Episode.kt
│   ├── Podcast.kt
│   ├── PodcastSearchResponse.kt
│   └── SubscriptionManager.kt
│
├── network
│   ├── ITunesApiService.kt
│   ├── RetrofitClient.kt
│   └── RssParser.kt
│
└── ui
├── EpisodeAdapter.kt
├── PodcastAdapter.kt
└── PodcastDetailsActivity.kt
Networking Flow
User Search
↓
iTunes Search API
↓
JSON Response
↓
Kotlin Models
↓
RecyclerView
↓
Podcast Details
↓
RSS Feed
↓
XML Parsing
↓
Episode List
↓
MediaPlayer
↓
Audio Playback
Requirements
Android Studio
Android 7.0 (API 24) or later
Internet connection
Running the Application
Clone the repository.
Open the project in Android Studio.
Sync Gradle dependencies.
Start an Android emulator or connect an Android device.
Run the application.
Enter a podcast search term.
Optionally enter a minimum number of words for podcast titles.
Select a podcast to view its episodes.
Select Play Episode to stream podcast audio.
Assignment Information

Course: MWD3B - Android Development
Assignment: Assignment 7
Application: SuperPodcast
Repository: AndroidApp4

Author

Wubit 


