Actualmente solo es la plantilla inicial creada por android studio- "main" esta en app/src/main/java/com/example/redsocialaio, pero para android studio el main o "mainActivity" es mas que nada para programar UI por lo que entiendo.

Algunas clases ocupadas usualmente para otras cosas serian:

ViewModel - Para manejar los datos relacionados con la UI
Repository - Para manejar informacion obtenida por otras bases de datos y variados
Services/WorkManager - Para actividades que se necesiten ejecutar de fondo.
Estas se deben crear por uno mismo por lo que entiendo y obviamente son aparte de cualquier clase que decidamos crear.

Android studio igualmente ocupa Gradle por si acaso- no Maven (no confundir con MavenCentral igual.)

Actualmente esta implementada las dependencias de Misskey y Mastodon con las API de Kmisskey y BigBone respectivamente.



Interfaz Grafica

  La interfaz grafica esta compuesta por varios archivos en el proyecto estas estan todas juntas en la carpeta
  llamada "res" donde la carpeta "drawable" estan todos las imagenes o iconos utilizados para poder visualizar mejor 
  la aplicacion.
  La carpeta "layout" es donde se puede visualizar el como se veria la aplicacion, esta esta compuesta por varios Xml 

  -activity_main : es la pantalla inicial de la aplicacion donde uno puede acceder al "home" de la aplicacion
  
  -activity_permissions : es la pantalla donde se muestran los permisos que la aplicacion necesita del sistema
  los cuales pueden ser la camara, acceder a la galeria, etc. Tambien muestra los permisos de las distintas redes sociales
  que componen la aplicacion
  
  -activity_recovery : es la pantalla inicial donde le permite al Usuario recibir un codigo para poder recuperar su cuenta
  
  -activity_select_instances : aqui se muestran las instancias en donde el Usuario puede elegir las distintas redes sociales 
  que va a utilizar en la aplicacion

  -activity_settings : permite acceder a la configuracion de la aplicacion ya sea para cerrar sesion o hacer cambios de seguridad,
  notificaciones, entre otros.

  -app_bar_main : es la pantalla de inicio donde el Usuario puede Iniciar Sesion y recuperar la cuenta.

  -fragment_gallery : permite al Usuario crear su cuenta si es la primera vez que accede a la aplicacion

  -item_post : es la imagen donde el Usuario ve una publicacion donde se muestra el nombre del Usuario, fecha, instancia, y el como
  puede interactuar con la publicacion (dar like, compartir, etc.)
