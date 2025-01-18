#  NanoFiles - Redes de Comunicaciones


## 📌 Descripción
NanoFiles es un sistema de compartición y transferencia de ficheros desarrollado en **Java**. Implementa un protocolo de comunicación basado en **UDP** para interactuar con el directorio y un protocolo confiable **TCP** para la transferencia de archivos entre pares (*peers*).

## 📜 Características
- Arquitectura **Cliente-Servidor** para comunicación con el directorio.
- Comunicación **Peer-to-Peer** para transferencia de archivos.
- **Protocolo de comunicación** definido con mensajes textuales.
- **Protocolo de transferencia de ficheros** con mensajes binarios multiformato.
- Implementación en **Java** con soporte para múltiples hilos.

## 📁 Estructura del Proyecto
```
📦 nanoFiles
 ┣ 📂 src/es/um/redes/nanoFiles
 ┃ ┣ 📂 application
 ┃ ┃ ┣ 📜 Directory.java
 ┃ ┃ ┣ 📜 Directory.class
 ┃ ┃ ┣ 📜 NanoFiles.java
 ┃ ┃ ┣ 📜 NanoFiles.class
 ┃ ┣ 📂 logic
 ┃ ┃ ┣ 📜 NFController.java
 ┃ ┃ ┣ 📜 NFController.class
 ┃ ┃ ┣ 📜 NFControllerLogicDir.java
 ┃ ┃ ┣ 📜 NFControllerLogicDir.class
 ┃ ┃ ┣ 📜 NFControllerLogicP2P.java
 ┃ ┃ ┣ 📜 NFControllerLogicP2P.class
 ┃ ┃ ┣ 📂 shell
 ┃ ┃ ┃ ┣ 📜 NFCommands.java
 ┃ ┃ ┃ ┣ 📜 NFCommands.class
 ┃ ┃ ┃ ┣ 📜 NFShell.java
 ┃ ┃ ┃ ┣ 📜 NFShell.class
 ┃ ┣ 📂 tcp
 ┃ ┃ ┣ 📂 client
 ┃ ┃ ┃ ┣ 📜 NFConnector.java
 ┃ ┃ ┃ ┣ 📜 NFConnector.class
 ┃ ┃ ┣ 📂 message
 ┃ ┃ ┃ ┣ 📜 PeerMessage.java
 ┃ ┃ ┃ ┣ 📜 PeerMessage.class
 ┃ ┃ ┃ ┣ 📜 PeerMessageOps.java
 ┃ ┃ ┃ ┣ 📜 PeerMessageOps.class
 ┃ ┃ ┣ 📂 server
 ┃ ┃ ┃ ┣ 📜 NFServer.java
 ┃ ┃ ┃ ┣ 📜 NFServer.class
 ┃ ┃ ┃ ┣ 📜 NFServerComm.java
 ┃ ┃ ┃ ┣ 📜 NFServerComm.class
 ┃ ┃ ┃ ┣ 📜 NFServerSimple.java
 ┃ ┃ ┃ ┣ 📜 NFServerSimple.class
 ┃ ┃ ┃ ┣ 📜 NFServerThread.java
 ┃ ┃ ┃ ┣ 📜 NFServerThread.class
 ┃ ┣ 📂 udp
 ┃ ┃ ┣ 📂 client
 ┃ ┃ ┃ ┣ 📜 DirectoryConnector.java
 ┃ ┃ ┃ ┣ 📜 DirectoryConnector.class
 ┃ ┃ ┣ 📂 message
 ┃ ┃ ┃ ┣ 📜 DirMessage.java
 ┃ ┃ ┃ ┣ 📜 DirMessage.class
 ┃ ┃ ┃ ┣ 📜 DirMessageOps.java
 ┃ ┃ ┃ ┣ 📜 DirMessageOps.class
 ┃ ┃ ┣ 📂 server
 ┃ ┃ ┃ ┣ 📜 NFDirectoryServer.java
 ┃ ┃ ┃ ┣ 📜 NFDirectoryServer.class
 ┃ ┣ 📂 util
 ┃ ┃ ┃ ┣ 📜 FileDatabase.java
 ┃ ┃ ┃ ┣ 📜 FileDatabase.class
 ┃ ┃ ┃ ┣ 📜 FileDigest.java
 ┃ ┃ ┃ ┣ 📜 FileDigest.class
 ┃ ┃ ┃ ┣ 📜 FileInfo.java
 ┃ ┃ ┃ ┣ 📜 FileInfo.class
```


## 📡 Formato de Mensajes
NanoFiles utiliza un protocolo textual para la comunicación con el directorio y un protocolo binario para la transferencia de archivos. Algunos ejemplos:

**Solicitud de login:**
```
operation: login
nickname: usuario123
```

**Respuesta exitosa:**
```
operation: loginOk
sessionkey: 5211
```

**Solicitud de transferencia de archivos:**
```
OpCode: 1 (DOWNLOAD_REQUEST)
Hash: 81h238
```


## 🛠 Mejoras Implementadas
- **Fgserve** con puerto variable
- **Bgserve** con soporte para múltiples hilos
- **Downloadfrom** usando el **nickname**
- **Userlist** ampliado con servidores activos
- **Stopserver** para detener la compartición de archivos

## 👨‍💻 Autores
- @evaapinaa
- @OkeV2

