En esta versión 2.0 de Avioncito de Papel, se lo logró implementar un servidor TCP multihebra.

Este servidor también permite el acceso a múltiples clientes, los que pueden acceder al servicio de mensajería ingresando su nick desde el navegador.

Los clientes corresponden a servidores web, que se ejecutan en un navegador tras ocupar un predeterminado puerto que no esté siendo ocupado.

Para ejecutar la aplicación:

-Desde la línea de comando dirigirse a /Servidor y ejecutar los siguientes comandos:

>javac Server.java
>java Server

-Luego ubicarse en /Webserver y ejecutar:

>javac server.java
>java clienteServerTCP 

Tras hacer lo anterior, ejecutar el navegador en la dirección http://localhost:8000/chatnick.html y seguir las instrucciones.
