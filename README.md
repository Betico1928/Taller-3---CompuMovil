# Taller de Autenticación y Localización de Usuarios para Android Studio

Este taller se trata de una aplicación para Android que permite el registro y la autenticación de usuarios, muestra las ubicaciones de los usuarios y puntos de interés en un mapa, y envía notificaciones a los usuarios cuando el estado de disponibilidad de los demás cambia.

## Descripción de la Aplicación

1. **Registro y Autenticación:** La aplicación comienza permitiendo el registro de nuevos usuarios y la autenticación de usuarios existentes. Los datos del usuario, incluyendo su nombre, apellido, email, contraseña, imagen de perfil, número de identificación, y localización (latitud y longitud) son recopilados y almacenados utilizando Firebase Authentication y una base de datos dinámica. La imagen de perfil del usuario se almacena utilizando Firebase Storage.

2. **Visualización de Mapa:** Una vez el usuario ha iniciado sesión, la pantalla principal de la aplicación muestra un mapa con marcadores en cinco localizaciones de interés, que se cargan desde un archivo JSON. También se muestra un marcador con la localización actual del usuario.

3. **Opciones de Menú:** El menú de la aplicación incluye opciones para cerrar la sesión del usuario y para establecer el estado del usuario como "disponible".

4. **Lista de Usuarios y Seguimiento de Ubicación:** Una opción adicional en el menú lanza una actividad que lista todos los usuarios disponibles en la aplicación. Para cada usuario se muestra su imagen de perfil, su nombre, y un botón que permite ver su ubicación actual. Al seleccionar este botón, se muestra un mapa con la ubicación en tiempo real del usuario seleccionado, y la distancia a la posición del usuario que está realizando el seguimiento. Si el usuario que se está siguiendo se mueve, su marcador en el mapa se actualiza y la distancia mostrada cambia en tiempo real.

5. **Servicio de Notificación:** La aplicación incluye un servicio que escucha por cambios en la lista de usuarios disponibles. Cuando un usuario cambia su estado a "disponible", el servicio envía una notificación a los demás usuarios. Al seleccionar la notificación, se lanza una actividad que permite seguir la ubicación del usuario que cambió su estado.

## Tecnologías Usadas

- Android Studio
- Firebase Authentication
- Firebase Database
- Firebase Storage
- Google Maps API

## Configuración y Uso

Para utilizar esta aplicación, necesitarás tener Android Studio y una cuenta de Firebase. Por favor, sigue las instrucciones de configuración de Firebase proporcionadas en la documentación oficial para conectar la aplicación a tu proyecto de Firebase. Una vez que hayas configurado Firebase, puedes compilar y ejecutar la aplicación en un emulador o dispositivo Android utilizando Android Studio.
