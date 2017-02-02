package com.company;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.company.Main.CLIENT_VERSION;
import static com.company.Main.randomNum;

public class Main {
    static final String CLIENT_VERSION = "0.02";
    private static final int PORT = 8901;
    static int randomNum = ThreadLocalRandom.current().nextInt(100, 999 + 1);
    static ArrayList<String> names = new ArrayList<>();
    static ArrayList<PrintWriter> writers = new ArrayList<>();
    static ArrayList<Player> freePlayer = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("Server is Running");
        try {
            while (true) {
                new Player(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
}

class Player extends Thread {
    String name;
    public Deck deck;
    private Player opponent;
    private Socket socket;
    BufferedReader input;
    PrintWriter output;
    String deckName;
    ArrayList<String> deckList;
    boolean endMuligan = false;

    boolean isFirstPlayer(String name1, String name2) {
        byte[] b = (name1 + randomNum).getBytes();
        byte[] b2 = (name2 + randomNum).getBytes();
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            String a = DatatypeConverter.printHexBinary(hash);
            byte[] hash2 = MessageDigest.getInstance("MD5").digest(b2);
            String a2 = DatatypeConverter.printHexBinary(hash2);
            return a.compareTo(a2) >= 0;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    Player(Socket socket) {
        this.socket = socket;
    }

    private ArrayList<String> getDeckList() throws IOException {
        ArrayList<String> result = new ArrayList<>();
        String card;
        while (!(card = input.readLine()).equals("$ENDDECK")) {
            result.add(card);
        }
        return result;
    }

    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "windows-1251"));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "windows-1251"), true);

            while (true) {
                String command = input.readLine();
                if (command.contains("$IAM")) {
                    ArrayList<String> parameter = getTextBetween(command);
                    String ver = parameter.get(2);
                    if (ver.equals(CLIENT_VERSION)) {
                        name = parameter.get(0);
                        System.out.println(name + " connected.");

                        boolean nameCorrect = false;
                        synchronized (Main.names) {
                            if (!Main.names.contains(name)) {
                                Main.names.add(name);
                                nameCorrect = true;
                            } else {
                                System.out.println("Name already exist.");
                                //Other name?
                            }
                        }
                        deckName = parameter.get(1);
                        deckList = getDeckList();
                        output.println("Hello, " + name + ", you going to play " + deckName + " deck.");
                        output.println("Waiting for opponent to connect");
                        //Const for shuffle
                        output.println("$YOUAREOK(" + Main.randomNum + ")");
                        if (nameCorrect) break;
                    } else {
                        output.println("Your client version is depricated! Update it.");
                        output.println("$YOUARENOTOK(" + "Your client version is depricated! Update it." + ")");
                        //Do something with it!
                    }
                }
            }

            Main.writers.add(output);
            Main.freePlayer.add(this);

            boolean pairFounded = false;

            while (true) {
                //If player disconnected before it take pair
                String a = input.readLine();
                System.out.println(name + " wait.");
                if (!a.equals("wait")) {
                    System.out.println("Player " + name + " disconnected before.");
                    Main.freePlayer.remove(this);
                }

                for (int i = 0; i < Main.freePlayer.size(); i++) {
                    if (!Main.freePlayer.get(i).name.equals(name) && Main.freePlayer.get(i).name != null) {
                        System.out.println("Pair found: " + name + "/" + Main.freePlayer.get(i).name);
                        opponent = Main.freePlayer.get(i);
                        Main.freePlayer.remove(Main.freePlayer.get(i));
                        pairFounded = true;
                    }
                }
                if (pairFounded) break;

                output.println("wait");
            }
            output.println("ok");

            //Get shuffled deck and send to opponent
            opponent.output.println("Your opponent " + name + ", play " + deckName + " deck.");
            opponent.output.println("$OPPONENTCONNECTED(" + name + "," + deckName + ")");
            System.out.println("Sen opCon for " + opponent.name);
            for (String card : deckList) {
                //May be send with command? For safety.
                opponent.output.println(card);
            }
            opponent.output.println("$ENDDECK");

            // Repeatedly get commands from the client and process them.
            while (true) {
                String command = input.readLine();
                output.println(command);
                opponent.output.println(command);

                if (command.contains("$DISCONNECT")) {
                    System.out.println(name + " normal disconnected.");
                    opponent.output.println("$DISCONNECT");
                    // This client is going down!  Remove it
                    if (name != null) {
                        Main.names.remove(name);
                    }
                    if (output != null) {
                        Main.writers.remove(output);
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                    break;
                }
                if (command.contains("$MULLIGANEND")) {
                    endMuligan = true;
                    if (opponent.endMuligan) {
                        //START
                        System.out.println("Game for " + name + " and " + opponent.name + " started.");
                        //Choice, who first
                        //Today at random
                        if (isFirstPlayer(name, opponent.name)) {
                            output.println("$NEWTURN(" + name + ")");
                            opponent.output.println("$NEWTURN(" + name + ")");
                        } else {
                            output.println("$NEWTURN(" + opponent.name + ")");
                            opponent.output.println("$NEWTURN(" + opponent.name + ")");
                        }
                        Main.randomNum = ThreadLocalRandom.current().nextInt(100, 999 + 1);//reroll for next
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Player disconnected: " + name);
            //Reconnect?
        } finally {
            System.out.println("Finaly " + name);
            Main.freePlayer.remove(this);
            if (opponent != null) opponent.output.println("$DISCONNECT");
            // This client is going down!  Remove it.
            if (name != null) {
                Main.names.remove(name);
            }
            if (output != null) {
                Main.writers.remove(output);
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

    }

    public static ArrayList<String> getTextBetween(String fromText) {
        ArrayList<String> rtrn = new ArrayList<>();
        String beforeText = "(";
        fromText = fromText.substring(fromText.indexOf(beforeText) + 1, fromText.length() - 1);
        String[] par = fromText.split(",");
        for (int i = 0; i < par.length; i++)
            rtrn.add(par[i]);
        return rtrn;
    }
}