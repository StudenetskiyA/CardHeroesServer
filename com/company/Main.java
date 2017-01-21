package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.company.Main.CLIENT_VERSION;

public class Main {
    public static final String CLIENT_VERSION="0.01";
    private static final int PORT=8901;

    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("Server is Running");
        try {
            while (true) {
                Game game = new Game();
                Game.Player playerX = game.new Player(listener.accept());
                Game.Player playerO = game.new Player(listener.accept());
                playerX.start();
                playerO.start();
              //  Game game2 = new Game();
               // Game.Player playerX2 = game2.new Player(listener.accept());
               // Game.Player playerO2 = game2.new Player(listener.accept());
              //  playerX2.start();
              //  playerO2.start();
            }
        } finally {
            listener.close();
        }
    }
}

class Game {
    private static ArrayList<PrintWriter> writers = new ArrayList<>();
    private static ArrayList<String> players = new ArrayList<>();
    private static String mulliganEnds = "";
    private static int playerConnected=0;
    int randomNum = ThreadLocalRandom.current().nextInt(100, 999 + 1);

    class Player extends Thread {
        String name;
        String opponentName;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        PrintWriter outputOpponent;
        String deckName;
        ArrayList<String> deckList;

        public Player(Socket socket) {
            this.socket = socket;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream(),"windows-1251"));
                output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "windows-1251"), true);

                while (true) {
                    String command = input.readLine();
                    System.out.println(command);
                    //TODO BUG! I player connected and before got pair disconnected, next player think will play with it.
                    if (command.contains("$IAM")) {
                        ArrayList<String> parameter = Card.getTextBetween(command);
                        String ver=parameter.get(2);
                        if (ver.equals(CLIENT_VERSION)) {
                            name = parameter.get(0);
                            playerConnected++;
                            System.out.println(name + " connected.");
                            writers.add(output);
                            players.add(name);
                            deckName = parameter.get(1);
                            deckList = getDeckList();
                           // System.out.print(deckList.toString());
                            output.println("Hello, " + name + ", you going to play " + deckName + " deck.");
                            output.println("Waiting for opponent to connect");
                            //Const for shuffle
                            output.println("$YOUAREOK("+randomNum+")");
                           // start();
                            break;
                        }
                        else {
                            output.println("Your client version is depricated! Update it.");
                            output.println("$YOUARENOTOK("+"Your client version is depricated! Update it."+")");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Player disconnected: "+name);
                //Reconnect?
            }
        }

        private ArrayList<String> getDeckList() throws IOException {
            ArrayList<String> result= new ArrayList<>();
            String card;
            while (!(card = input.readLine()).equals("$ENDDECK")){
               // System.out.println("Card get "+card);
                result.add(card);
            }
           return result;
        }
        /**
         * The run method of this thread.
         */
        public void run() {
            try {
                // The thread is only started after pair connects.
                System.out.println("run called, playerConnected = "+playerConnected);
                //Get shuffled deck and send to client?
                for (int i=playerConnected-2;i<writers.size();i++){
                    if (writers.get(i) != output) {
                        outputOpponent=writers.get(i);
                        opponentName=players.get(i);
                        writers.get(i).println("Your opponent " + name+ ", play "+deckName+ " deck.");
                        writers.get(i).println("$OPPONENTCONNECTED("+name+","+deckName+")");
                        //Send deck
                        for (String card:deckList){
                            writers.get(i).println(card);
                        }
                        writers.get(i).println("$ENDDECK");
                    }
                }
                // Repeatedly get commands from the client and process them.
                while (true) {
                    String command = input.readLine();
                    output.println(command);
                    outputOpponent.println(command);

                    //System.out.println("2"+ name+"/"+command);
                    //System.out.println("2" + opponentName+"/"+command);
                    if (command.contains("$DISCONNECT")) {
                        System.out.println(name+ " normal disconnected.");
                        mulliganEnds = "";
                      //  playerConnected--;
                      //  writers.remove(output);
                      //  players.remove(name);
                      //  System.out.println("playerConnected = "+playerConnected);
                    }
                    if (command.contains("$MULLIGANEND")) {
                        //mulliganEnds++;
                        ArrayList<String> parameter = Card.getTextBetween(command);
                        //String ver=parameter.get(2);

                        if (mulliganEnds.equals("")){
                            mulliganEnds=parameter.get(0);
                            System.out.println("Mullends write at ("+parameter.get(0));
                        }
                        if (mulliganEnds.equals(opponentName)) {
                            //START
                            mulliganEnds = "";
                            System.out.println(name+"/"+"All mulligan ends.");
                            output.println("$NEWTURN(" + players.get(playerConnected-2) + ")");
                            outputOpponent.println("$NEWTURN(" + players.get(playerConnected-2) + ")");
                            randomNum = ThreadLocalRandom.current().nextInt(100, 999 + 1);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Player disconnect: " + name+ " :"+e);
//                playerConnected--;
//                writers.remove(output);
//                players.remove(name);
              //  writers.remove(this.output);
                //reset connection for all player?
             //   output.println("$DISCONNECT");
             //   outputOpponent.println("$DISCONNECT");
            //    System.out.println("Opponent disconnect");
//                for (PrintWriter writer : writers) {
//                    System.out.println("Opponent disconnect");
//                    writer.println("$DISCONNECT");
//                }
            } finally {
//                try {
//                  //  socket.close();
//                  //  writers.clear();
//                   // players.clear();
//                } catch (IOException e) {
//                }
            }
        }
    }
}