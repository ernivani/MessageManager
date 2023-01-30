
# Zonday Message Manager

Ce plugin bungeecord permet de partager a des serveurs de déja parametré des websocket traduit et via un canal privé
## Comment l'utiliser :
en premier temps il faut créer un générateur de websocket en javascript
```javascript
const { io } = require("socket.io-client");

function callSocket(server, command, player) {
  const socket = io("ws://localhost:1234/", {
    reconnectionDelayMax: 1000,
    reconnection: false,
    query: {
      server,
      command,
      player
    }
  });
  
  socket.on("connect", () => {
    setTimeout(() => {
        socket.disconnect();
    }, 5000);
  });
};

let pseudo = "ernicani";

callSocket("lobby", "msg player World!", pseudo);
```
## Explication :
La création d'une websocket comme celle présenté ci-dessus vas permettre :
- En premier temps de définir le serveur cible (dans notre cas lobby) 
- En second temps de créer une commande envoyer au serveur avec comme attribut "player" 
- En troisième temps l'argument "pseudo" ci-dessus est traduit dans le plugin pour être attribuer a la place de "player" dans la commande
## Authors

- [@ernivani](https://www.github.com/ernivani)

## Update :

les mises a jours seront affiché ici avec une date et une explication courte 
