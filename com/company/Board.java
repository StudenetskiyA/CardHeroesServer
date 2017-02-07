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

    void addCreatureToBoard(Card _creature, Player _player) {
        Creature summonCreature = new Creature(_creature, _player);

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
}
