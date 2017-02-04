package com.company;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by StudenetiskiyA on 30.12.2016.
 */

public class Board {

     String whichTurn="";
     int turnCount = 0;

    public Board() {
    }
//
//    static int getPlayerNumByName(String _name) {
//        if (_name.equals(Main.players[0].playerName)) return 0;
//        else if (_name.equals(Main.players[1].playerName)) return 1;
//        else {
//            System.out.println("Error - Unknown player.");
//            return -1;
//        }
//    }
//
//    static Player getPlayerByName(String _name) {
//        if (_name.equals(Main.players[0].playerName)) return Main.players[0];
//        else if (_name.equals(Main.players[1].playerName)) return Main.players[1];
//        else {
//            System.out.println("Error - Unknown player.");
//            return null;
//        }
//    }


    void addCreatureToBoard(Card _creature, Player _player) {
        Creature summonCreature = new Creature(_creature, _player);

      //  int np = _player.numberPlayer;
        _player.addCreatureToList(summonCreature);

        _player.owner.gameQueue.push(new GameQueue.QueueEvent("Summon",summonCreature,0));

        if (summonCreature.text.contains("Уникальность.")) {
            for (int i = _player.creatures.size() - 1; i >= 0; i--) {
                if (_player.creatures.get(i).name.equals(_creature.name) && _player.creatures.get(i)!=summonCreature){
                    System.out.println("Double uniqe creature, die.");
                    _player.owner.sendBoth("Double uniqe creature, die.");
                    _player.creatures.get(i).die();
                    break;
                }
            }
        }
        if (summonCreature.getTougness()<=0){
            System.out.println("Creature hp less 0, die.");
            summonCreature.die();
        }
    }

    static void putCardToGraveyard(Card _card, Player _owner) {
        _owner.addCardToGraveyard(_card);
    }
}
