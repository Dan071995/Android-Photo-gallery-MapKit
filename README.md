# About Android_Photo_gallery_MapKit App:
This project is a continuation of https://github.com/Dan071995/Android_Photo-gallery

Added a new button that opens a new fragment. In which the map is automatically loaded (Yandex Map Kit) and the user's position on the map is marked. Once the user's position is obtained, the user's coordinates are passed to the OpenTripMap API (https://opentripmap.io/docs). This API returns a list of objects (landmarks) within a certain radius (I have 3000 m). Further, the received sights are marked on the map!
Thus, I display the user's position on the map and the nearest places of interest to him!

App icon: <a href="https://www.flaticon.com/free-icons/gps" title="gps icons">Gps icons created by Flat-icons-com - Flaticon</a>

# Technology stack:
Coroutines / Flows / MVVM / Room / SQLLite / RecyclerView / ResultAPI / Retrofit / Yandex MapKit

# Video:


https://user-images.githubusercontent.com/104363713/235672053-b3aecb63-64e8-4b1d-9dfc-f33913e33f34.mp4

