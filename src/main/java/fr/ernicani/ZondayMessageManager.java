package fr.ernicani;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.BufferedReader;
import java.io.File;
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
            Logger.getLogger("Minecraft").info("§b[WebSocket] Plugin activé !");
            run();
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
        listenThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.getLogger("Minecraft").info("§c[WebSocket] Erreur survenue lors de la mise en veille du thread d'écoute : " + e);
                Thread.currentThread().interrupt();
            }
            while (!Thread.interrupted()) {
                try {
                    Socket socket = serverSocket.accept();
                    BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String request = socketReader.readLine();
                    String[] parts = request.split("\\?");
                    String[] params = parts[1].split("&");
                    String server = "";
                    String command = "";
                    for (String param : params) {
                        if (param.startsWith("server=")) {
                            server = param.substring(3).replaceAll("%3A", ":");
                        } else if (param.startsWith("command=")) {
                            command = param.substring(8).replaceAll("%20", " ");
                        }
                    }

                    if (sendCustomDataToServer(server, command)) {
                        socket.getOutputStream().write("OK".getBytes());
                    } else {
                        socket.getOutputStream().write("KO".getBytes());
                    }

                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });
        listenThread.start();
    }

    public boolean sendCustomDataToServer(String serverTarget, String cmd) {
        ServerInfo targetServer = ProxyServer.getInstance().getServerInfo(serverTarget);
        if (targetServer == null) {
            return false;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("zonday:main");
        out.writeUTF(cmd);

        targetServer.sendData("zonday:main", out.toByteArray());
        return true;
    }


}