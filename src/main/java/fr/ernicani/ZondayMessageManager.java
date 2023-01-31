package fr.ernicani;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public final class ZondayMessageManager extends Plugin {

    private ServerSocket serverSocket;
    private Thread listenThread;

    public void onEnable() {
        try {
            getProxy().registerChannel("zonday:main");
            run();
            Logger.getLogger("Minecraft").info("§b[WebSocket] Plugin activé !");
        }
        catch (Exception e) {
            Logger.getLogger("Minecraft").info("§c[WebSocket] Erreur survenue lors de l'activation du plugin : " + e);
        }

    }

    @Override
    public void onDisable() {
        try {
            serverSocket.close();
            listenThread.interrupt();
        }catch (IOException e) {
            Logger.getLogger("Minecraft").info("§c[WebSocket] Erreur survenue lors de la fermeture du serveur de socket : " + e);
        }
    }

    private void run() {
        try {
            serverSocket = new ServerSocket(1234);
        } catch (IOException e) {
            Logger.getLogger("Minecraft").info("§c[WebSocket] Erreur survenue lors de la création du serveur de socket : " + e);
            return;
        }
        Logger.getLogger("Minecraft").info("§b[WebSocket] Serveur de socket créé !");

        try {
            listenThread = new Thread(() -> {
                if (Thread.interrupted()) {
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logger.getLogger("Minecraft").info("§c[WebSocket] Erreur survenue lors de la mise en veille du thread d'écoute : " + e);
                    Thread.currentThread().interrupt();
                }
                try {
                while (!Thread.interrupted()) {
                        Socket socket = serverSocket.accept();
                        BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String request = socketReader.readLine();
                        String[] parts = request.split("\\?");
                        String[] params = parts[1].split("&");
                        String server = "";
                        String command = "";
                        String player = "";

                        for (String param : params) {
                            if (param.startsWith("server=")) {
                                server = param.substring(7).replaceAll("%3A", ":").replaceAll("%2F", "/");
                            } else if (param.startsWith("command=")) {
                                command = param.substring(8).replaceAll("%20", " ").replaceAll("%2F", "/").replaceAll("%3A", ":").replaceAll("%3D", "=");
                            } else if (param.startsWith("player=")) {
                                player = param.substring(7).replaceAll("%20", " ");
                            }
                        }
                        Logger.getLogger("Minecraft").info("§b[WebSocket] " + server + " : " + command + " pour le joueur " + player);

                        String data = "command=" + command + "&player=" + player;

                        boolean sended = sendCustomDataToServer(server, data);
                        if (sended) {
                            Logger.getLogger("Minecraft").info("§b[WebSocket] Commande envoyée au serveur " + server + " : " + command);
                        } else {
                            Logger.getLogger("Minecraft").info("§c[WebSocket] Erreur survenue lors de l'envoi de la commande au serveur " + server + " : " + command);
                        }

                        socket.close();

                    }
                } catch (IOException e) {
                    Logger.getLogger("Minecraft").info("§c[WebSocket] Erreur survenue lors de l'écoute du serveur de socket : " + e);
                }
            });
            listenThread.start();
        }
        catch (Exception e) {
            Logger.getLogger("Minecraft").info("§c[WebSocket] Erreur survenue lors de la création du thread d'écoute : " + e);
        }
    }

    public boolean sendCustomDataToServer(String serverTarget, String data) {
        ServerInfo targetServer = ProxyServer.getInstance().getServerInfo(serverTarget);
        if (targetServer == null) {
            return false;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("zonday:main");
        out.writeUTF(data);

        targetServer.sendData("zonday:main", out.toByteArray());
        return true;
    }


}
