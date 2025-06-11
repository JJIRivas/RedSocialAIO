Actualmente solo es la plantilla inicial creada por android studio- "main" esta en app/src/main/java/com/example/redsocialaio, pero para android studio el main o "mainActivity" es mas que nada para programar UI por lo que entiendo.

Algunas clases ocupadas usualmente para otras cosas serian:

ViewModel - Para manejar los datos relacionados con la UI
Repository - Para manejar informacion obtenida por otras bases de datos y variados
Services/WorkManager - Para actividades que se necesiten ejecutar de fondo.
Estas se deben crear por uno mismo por lo que entiendo y obviamente son aparte de cualquier clase que decidamos crear.

Android studio igualmente ocupa Gradle por si acaso- no Maven (no confundir con MavenCentral igual.)

Actualmente esta implementada las dependencias de Misskey y Mastodon con las API de Kmisskey y BigBone respectivamente.
