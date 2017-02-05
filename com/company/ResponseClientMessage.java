package com.company;

import java.util.ArrayList;

import static com.company.MyFunction.*;
import static com.company.MyFunction.ActivatedAbility.WhatAbility.nothing;


/**
 * Created by StudenetskiyA on 25.01.2017.
 */
public class ResponseClientMessage extends Thread {
    Gamer gamer;
      Player player;
    String fromServer = "";

    ResponseClientMessage(Gamer _gamer,String _fromServer) {
        gamer=_gamer;
        player=gamer.player;
        fromServer = _fromServer;
    }

    public synchronized void run() {
        boolean dontDoQueue=false;
        boolean freeMonitor=false;
        gamer.ready = false;

        System.out.println(fromServer);
        if (fromServer.contains("$ENDTURN(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            System.out.println("End turn " + parameter.get(0));
            if (gamer.player.playerName.equals(parameter.get(0))) {
                gamer.setPlayerGameStatus(MyFunction.PlayerStatus.EnemyTurn);
                gamer.opponent.setPlayerGameStatus(MyFunction.PlayerStatus.MyTurn);
                player.endTurn();
                gamer.opponent.sendUntapAll();
            } else if (gamer.opponent.player.playerName.equals(parameter.get(0))) {
               // player.endTurn();
            }
        }
//        else if (fromServer.contains("$NEWTURN(")) {
//            gamer.setPlayerGameStatus(MyFunction.PlayerStatus.MyTurn);
//            player.newTurn();
//        }
         else if (fromServer.contains("$CHOISEBLOCKER(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
           // if (players[0].playerName.equals(parameter.get(0))) {
                gamer.opponent.status = PlayerStatus.IChoiceBlocker;
                gamer.opponent.creatureWhoAttack = Integer.parseInt(parameter.get(1));
                gamer.opponent.creatureWhoAttackTarget = Integer.parseInt(parameter.get(2));
           // }
            dontDoQueue=true;
        }
        else if (fromServer.contains("$TAPNOTARGET(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            player.creatures.get(Integer.parseInt(parameter.get(1))).tapNoTargetAbility();
        } else if (fromServer.contains("$DISCARD(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            int n=Integer.parseInt(parameter.get(1));
            gamer.printToView(0, player.playerName + " сбрасывает "+player.cardInHand.get(n).name);
            Board.putCardToGraveyard(player.cardInHand.get(n), player);
            player.cardInHand.remove(n);
        }
        else if ((fromServer.contains("$CRYTARGET(")) || (fromServer.contains("$TAPTARGET("))) {
            // CRYTARGET also for DeathratleTarget and TapTarget
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            Creature cr;
            boolean death = false;
            if (parameter.get(1).equals("-1")) {
                //died creature ability.
                death = true;
                cr = new Creature(MyFunction.ActivatedAbility.creature);
            } else {
                cr = player.creatures.get(Integer.parseInt(parameter.get(1)));
            }
            Player tmpPlayer = (parameter.get(2).equals("1")) ? gamer.opponent.player:player;
            if (parameter.get(3).equals("-1")) {
                if (fromServer.contains("$CRYTARGET("))
                    if (death) cr.deathratle(null, tmpPlayer);
                    else cr.battlecryTarget(null, tmpPlayer);
                else
                    cr.tapTargetAbility(null, tmpPlayer);
            } else {
                int died = MyFunction.getNumDiedButNotRemovedYet(tmpPlayer.creatures);
                if (tmpPlayer.creatures.size() - 1 >= Integer.parseInt(parameter.get(3)) + died) {
                    if (fromServer.contains("$CRYTARGET("))
                        if (death)
                            cr.deathratle(tmpPlayer.creatures.get(Integer.parseInt(parameter.get(3))), null);
                        else
                            cr.battlecryTarget(tmpPlayer.creatures.get(Integer.parseInt(parameter.get(3))), null);
                    else
                        cr.tapTargetAbility(tmpPlayer.creatures.get(Integer.parseInt(parameter.get(3))), null);
                }
            }
            dontDoQueue=true;
            freeMonitor=true;
        }
        else if (fromServer.contains("$EQUIPTARGET(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            gamer.status=PlayerStatus.MyTurn;
            int equip = Integer.parseInt(parameter.get(1));
            if (parameter.get(2).equals("1")) {
                if (parameter.get(3).equals("-1"))
                    player.equpiment[equip].tapTargetAbility(null, gamer.opponent.player);
                else
                    player.equpiment[equip].tapTargetAbility(gamer.opponent.player.creatures.get(Integer.parseInt(parameter.get(3))), null);
            } else {
                if (parameter.get(3).equals("-1"))
                    player.equpiment[equip].tapTargetAbility(null, player);
                else
                    player.equpiment[equip].tapTargetAbility(player.creatures.get(Integer.parseInt(parameter.get(3))), null);
            }
        } else if (fromServer.contains("$HEROTARGET(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            player.untappedCoin -= Integer.parseInt(parameter.get(3));
            if (parameter.get(1).equals("1")) {
                if (parameter.get(2).equals("-1")) player.ability(null,gamer.opponent.player);
                else player.ability(gamer.opponent.player.creatures.get(Integer.parseInt(parameter.get(2))), null);
            } else {
                if (parameter.get(2).equals("-1")) player.ability(null, player);
                else player.ability(player.creatures.get(Integer.parseInt(parameter.get(2))), null);
            }
            dontDoQueue=true;
        } else if (fromServer.contains("$HERONOTARGET(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            player.tap();
            player.untappedCoin -= Integer.parseInt(parameter.get(1));
            player.abilityNoTarget();
        } else if (fromServer.contains("$BLOCKER(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);

            Creature cr = gamer.opponent.player.creatures.get(Integer.parseInt(parameter.get(1)));//Who attack
            if (Integer.parseInt(parameter.get(2)) == -1) {
                if (Integer.parseInt(parameter.get(3)) == -1) {
                    //Fight with hero
                    cr.fightPlayer(player);
                } else {
                    Creature block = player.creatures.get(Integer.parseInt(parameter.get(3)));
                    //Fight with bocker
                    block.blockThisTurn = true;
                    cr.fightCreature(block);
                    if (Integer.parseInt(parameter.get(4)) == 1) {
                        if (!block.text.contains("Опыт в защите."))
                            block.tapCreature();
                    }
                }
            } else {
                if (Integer.parseInt(parameter.get(3)) == -1) {
                    //Fight with first target
                    Creature block = player.creatures.get(Integer.parseInt(parameter.get(2)));
                    cr.fightCreature(block);
                } else {
                    Creature block = player.creatures.get(Integer.parseInt(parameter.get(3)));
                    //Fight with blocker
                    cr.fightCreature(block);
                    if (Integer.parseInt(parameter.get(4)) == 1) {
                        block.tapCreature();
                    }
                }
            }
            gamer.setPlayerGameStatus(PlayerStatus.EnemyTurn);
            gamer.opponent.setPlayerGameStatus(PlayerStatus.MyTurn);
            dontDoQueue=true;
        }
         else if (fromServer.contains("$PLAYCARD(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            if (!parameter.get(3).equals("-1")) {//if card targets creature
                if ((parameter.get(4).equals(gamer.opponent.player.playerName)))
                    player.playCard(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), gamer.opponent.player.creatures.get(Integer.parseInt(parameter.get(3))), null);
                else //to self creature
                    player.playCard(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), gamer.player.creatures.get(Integer.parseInt(parameter.get(3))), null);
            } else {
                if (parameter.get(4).equals(gamer.opponent.player.playerName))//enemy
                    player.playCard(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), null, gamer.opponent.player);
                else if (parameter.get(4).equals(player.playerName))//target - self player
                    player.playCard(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), null, gamer.player);
                else
                    player.playCard(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), null, null);
            }
        }
        else if (fromServer.contains("$PLAYWITHX(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            int x = Integer.parseInt(parameter.get(5));
            Player apl = gamer.opponent.player;
            if (!parameter.get(3).equals("-1")) {//if card targets creature
                if ((parameter.get(4).equals(apl.playerName)))
                    player.playCardX(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), apl.creatures.get(Integer.parseInt(parameter.get(3))), null, x);
                else //to self creature
                    player.playCardX(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), player.creatures.get(Integer.parseInt(parameter.get(3))), null, x);
            } else {
                if (parameter.get(4).equals(apl.playerName))//enemy
                    player.playCardX(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), null,apl, x);
                else if (parameter.get(5).equals(player.playerName))//target - self player
                    player.playCardX(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), null,player, x);
                else
                    player.playCardX(Integer.parseInt(parameter.get(2)),Card.getCardByName(parameter.get(1)), null, null, x);
            }
        } else if (fromServer.contains("$ATTACKPLAYER(")) {//$ATTACKPLAYER(Player, Creature)
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            gamer.printToView(0, player.creatures.get(Integer.parseInt(parameter.get(1))).name + " атакует " + gamer.opponent.player.name);
            player.creatures.get(Integer.parseInt(parameter.get(1))).attackPlayer(gamer.opponent.player);
        } else if (fromServer.contains("$ATTACKCREATURE(")) {//$ATTACKREATURE(Player, Creature, TargetCreature)
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            gamer.printToView(0, player.creatures.get(Integer.parseInt(parameter.get(1))).name + " атакует " + gamer.opponent.player.creatures.get(Integer.parseInt(parameter.get(2))).name);
            player.creatures.get(Integer.parseInt(parameter.get(1))).attackCreature(gamer.opponent.player.creatures.get(Integer.parseInt(parameter.get(2))));
        } else if (fromServer.contains("$FOUND(")) {//$FOUND(Player, Card)
            gamer.choiceXcolor = 0;
            gamer.choiceXtype = 0;
            gamer.choiceXcost = 0;
            gamer.choiceXcostExactly = 0;
            gamer.choiceXcreatureType = "";
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            if (parameter.get(1).equals("-1")) {
                    gamer.printToView(0, "Вы ищете в колоде, но ничего подходящего не находите.");
                    gamer.opponent.printToView(0, "Противник ищет в колоде, но ничего подходящего не находит.");
            } else {
                    Card card = gamer.player.deck.searchCard(parameter.get(1));
                    //TODO Check exist card and may it be founded. Player may lie.
                    gamer.player.drawSpecialCard(card);
                    gamer.printToView(0, "Вы находите в колоде " + card.name + ".");
                    gamer.opponent.printToView(0, "Противник находит в колоде " + parameter.get(1) + ".");
            }
            dontDoQueue=true;
            freeMonitor=true;
        }  else if (fromServer.contains("$DIGFOUND(")) {
            gamer.choiceXcolor = 0;
            gamer.choiceXtype = 0;
            gamer.choiceXcost = 0;
            gamer.choiceXcostExactly = 0;
            gamer.choiceXcreatureType = "";
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            if (parameter.get(1).equals("-1")) {
                gamer.printToView(0, "Вы ищете на кладбище, но ничего подходящего не находите.");
                gamer.opponent.printToView(0, "Противник ищет на кладбище, но ничего подходящего не находит.");
            } else {
                Card card = player.deck.searchCard(parameter.get(1));
                //TODO Check exist card and may it be founded. Player may lie.
                player.digSpecialCard(card);
                gamer.printToView(0, "Вы берете с кладбища " + card.name + ".");
                gamer.opponent.printToView(0, "Противник берет с кладбища " + parameter.get(1) + ".");
            }
            dontDoQueue=true;
            freeMonitor=true;
        } else if (fromServer.contains("$CHAT(")) {
            ArrayList<String> parameter = MyFunction.getTextBetween(fromServer);
            gamer.opponent.output.println("#Chat("+player.playerName+":"+parameter.get(0)+")");
            dontDoQueue=true;
        }


        if (!dontDoQueue) {
            gamer.opponent.gameQueue.responseAllQueue();
            gamer.gameQueue.responseAllQueue();
        }

        gamer.sendStatus();
        gamer.opponent.sendStatus();

        if (freeMonitor) freeMonitor();

        synchronized (gamer.monitor) {
            gamer.ready = true;
            gamer.monitor.notifyAll();
        }
    }

    private void freeMonitor() {
        synchronized (gamer.cretureDiedMonitor) {
            ActivatedAbility.whatAbility=nothing;
            gamer.cretureDiedMonitor.notify();
        }
    }
}
