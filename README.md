# RedSocialAIO - App de Red Social

Es una aplicación que se conecta a Mastodon (como Twitter pero descentralizado) para:
- Ver posts públicos de otras personas
- Publicar tus propios posts
- Dar likes y hacer reblogs
- Seguir a otros usuarios


### Carpetas principales:
- `app/src/main/java/` - Aquí está todo el código de la app
- `app/src/main/res/layout/` - Aquí están los diseños de las pantallas (XML)
- `app/src/main/res/values/` - Colores, textos y configuraciones

### Clases importantes:

**MainActivity.java** - La pantalla principal con el menú lateral
**LoginActivity.java** - Pantalla para iniciar sesión
**PostActivity.java** - Pantalla para escribir nuevos posts

**HomeFragment.java** - Muestra la lista de posts (como el feed de Twitter)
**StatusAdapter.java** - Se encarga de mostrar cada post individual en la lista
**ProfileFragment.java** - Muestra el perfil del usuario autenticado con sus datos y estadísticas

**MastodonViewModel.java** - El "cerebro" que maneja los datos de los posts
**MastodonRepository.java** - Se conecta a internet para traer los posts de Mastodon
**MastodonApiService.java** - Define cómo hablar con los servidores de Mastodon

### Funcionalidad de las clases
1. **HomeFragment** le dice al **ViewModel**: "Dame los posts"
2. **ViewModel** le dice al **Repository**: "Ve a buscar posts"
3. **Repository** usa **ApiService** para conectarse a internet
4. Los posts regresan por el mismo camino
5. **StatusAdapter** los muestra bonitos en la pantalla

### Funciones

**Menú lateral:
- Timeline: ver posts
- Crear Post: escribir algo nuevo
- Mi Perfil: muestra información del usuario (avatar, nombre, biografía, estadísticas)
- Cerrar Sesión: salir de la app

**Posts con mejor diseño**:
- Cada post está en una "tarjeta" (card) con bordes redondeados
- Se ve más moderno y fácil de leer
- Muestra avatar, nombre, contenido y botones de like/reblog

**Actualización automática**:
- Cuando se publica algo, automáticamente se actualiza la lista

**Perfil de usuario**:
- Muestra avatar, nombre de usuario y nombre para mostrar
- Biografía del usuario
- Estadísticas: número de posts, seguidores y seguidos
- Diseño con cards modernas y fácil de leer

### Tecnologías usadas:
- **Retrofit**: Para conectarse a internet y traer datos
- **Glide**: Para cargar las fotos de perfil
- **RecyclerView**: Para mostrar listas largas de posts

### API de Mastodon:
- Se uso la API oficial de Mastodon
- Nos conectamos principalmente a mastodon.social (Una URL de instancia)
- BigBone es una librería que facilita el uso (aunque no se uso de forma directa)


