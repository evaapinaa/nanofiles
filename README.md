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
 ┃ ┃ ┣ 📜 NanoFiles.java
 ┃ ┣ 📂 logic
 ┃ ┃ ┣ 📜 NFController.java
 ┃ ┃ ┣ 📜 NFControllerLogicDir.java
 ┃ ┃ ┣ 📜 NFControllerLogicP2P.java
 ┃ ┃ ┣ 📂 shell
 ┃ ┃ ┃ ┣ 📜 NFCommands.java
 ┃ ┃ ┃ ┣ 📜 NFShell.java
 ┃ ┣ 📂 tcp
 ┃ ┃ ┣ 📂 client
 ┃ ┃ ┃ ┣ 📜 NFConnector.java
 ┃ ┃ ┣ 📂 message
 ┃ ┃ ┃ ┣ 📜 PeerMessage.java
 ┃ ┃ ┃ ┣ 📜 PeerMessageOps.java
 ┃ ┃ ┣ 📂 server
 ┃ ┃ ┃ ┣ 📜 NFServer.java
 ┃ ┃ ┃ ┣ 📜 NFServerComm.java
 ┃ ┃ ┃ ┣ 📜 NFServerSimple.java
 ┃ ┃ ┃ ┣ 📜 NFServerThread.java
 ┃ ┣ 📂 udp
 ┃ ┃ ┣ 📂 client
 ┃ ┃ ┃ ┣ 📜 DirectoryConnector.java
 ┃ ┃ ┣ 📂 message
 ┃ ┃ ┃ ┣ 📜 DirMessage.java
 ┃ ┃ ┃ ┣ 📜 DirMessageOps.java
 ┃ ┃ ┣ 📂 server
 ┃ ┃ ┃ ┣ 📜 NFDirectoryServer.java
 ┃ ┣ 📂 util
 ┃ ┃ ┃ ┣ 📜 FileDatabase.java
 ┃ ┃ ┃ ┣ 📜 FileDigest.java
 ┃ ┃ ┃ ┣ 📜 FileInfo.java
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

