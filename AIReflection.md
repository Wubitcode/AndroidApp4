AI Reflection – SuperPodcast

1. How did you use AI during this assignment?

   I used AI as a learning and development assistant throughout the build of my SuperPodcast application. 
   It helped me plan out the project structure, connect the app to the iTunes podcast API using Retrofit, 
   retrieve and parse RSS feeds, display podcast episodes, and implement audio playback with MediaPlayer.
   As I added new features  such as  podcast subscriptions, the My Subscriptions screen, playback controls, 
   the SeekBar, playback speed adjustment, background checking with WorkManager, and podcast update notifications 
   I continued using on AI for guidance. When I ran into errors, I used it to help decode the error messages and
   pinpoint where in my code the problem originated. Along the way I researched and picked up several new concepts: 
   Retrofit networking, RSS parsing, SharedPreferences, Gson, MediaPlayer, WorkManager, notification permissions,
   and Android background tasks.

2. How did you understand, verify, and adapt the AI-generated suggestions?

   I didn't just copy the code. I tested the app in the Android emulator after every major change. 
   For example, I ran podcast searches across different topics (cybersecurity, education, music, comedy) 
   to confirm the search functionality worked generally rather than for just one term.
   I also tested the full user flow: opening podcast details, loading real RSS episodes, subscribing and unsubscribing, 
   opening My Subscriptions, playing episodes, pausing and resuming audio, stopping playback, scrubbing the SeekBar, 
   and changing playback speed.
   When something broke, I read the Android Studio build output and cross-checked it against my project structure. 
   One example was a PodcastUpdateWorker redeclaration error  I traced it to two copies of the same class,
   removed the duplicate, and kept the correct file inside the worker package. Throughout, 
  I adapted AI-generated examples to fit my own package name, project structure, XML layouts, models, and existing SuperPodcast features. Testing each feature individually helped me understand how the different parts of the app connected to one another.

3. What did you learn or get better at during this assignment?

   This assignment sharpened my understanding of Android development, particularly around networking 
   and working with external data. I learned how Retrofit communicates with an API, how RSS podcast 
   feeds get parsed into episode objects, and how RecyclerView displays dynamic podcast and episode data.
   My understanding of MediaPlayer also grew a lot streaming podcast audio, pausing and resuming playback, 
   stopping audio, tracking playback position with a SeekBar, and adjusting playback speed.
   I picked up new ground on the persistence and background side too: saving subscription data with 
   SharedPreferences and Gson, scheduling background tasks with WorkManager, and handling Android's 
   runtime notification permissions, which newer Android versions require before an app can send notifications.
   What worked best for me was building and testing one feature at a time. Not everything worked on the first try. 
   Activity registration issues and duplicate worker files both tripped me up  but debugging those problems made me-
   noticeably better at reading build errors and navigating the Android project structure. Overall, 
   this assignment left me a lot more confident working with APIs, Android components, debugging, 
   persistent data, media playback, and background processing.

