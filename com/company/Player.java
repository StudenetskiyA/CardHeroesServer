package com.company;

import java.awt.*;
import java.util.*;

import static com.company.MyFunction.*;
import static com.company.MyFunction.ActivatedAbility.WhatAbility.*;

/**
 * Created by StudenetskiyA on 30.12.2016.
 */

public class Player extends Card {
    ArrayList<Card> cardInHand = new ArrayList<>();//Don't do cardInHand.add or remove! Use addToGra...
    ArrayList<Card> graveyard = new ArrayList<>();//Same for graveyard
    ArrayList<Creature> creatures = new ArrayList<>();//And same for creatures

    Gamer owner;
    int numberPlayer;
    int damage;
    String playerName;
    int totalCoin;
    int untappedCoin;
    int temporaryCoin = 0;
    boolean isTapped = false;
    public Deck deck;

    Equpiment equpiment[];//0-armor,1-amulet,2-weapon,3-event
    public ArrayList<Creature> crDied;//Temporary list for queue
    public ArrayList<Creature> crCryed;
    public ArrayList<Creature> crUpkeeped;
    //Effects
    Boolean bbshield = false;//Bjornbon shield
    //
    private static int tempX;//For card with X, for correct minus cost

    void addCreatureToList(Creature c){
        creatures.add(c);
        owner.sendBoth("#PutCreatureToBoard("+playerName+","+c.name+")");
        owner.sendStatus();
        owner.opponent.sendStatus();
    }
    void removeCreatureFromList(Creature c){
       owner.sendBoth("#DieCreature("+ playerName+","+getNumberOfCreature(c)+")");
       creatures.remove(c);
    }

    void addCardToHand(Card c){
        cardInHand.add(c);
        owner.output.println("#AddCardToHand("+c.name+")");
    }

    void addCardToGraveyard(Card c){
        graveyard.add(c);
        owner.sendBoth("#AddCardToGraveyard("+playerName+","+c.name+")");
    }

    void removeCardFromGraveyard(Card c) {
        graveyard.remove(c);
        owner.sendBoth("#RemoveCardFromGraveyard("+playerName+","+c.name+")");
    }
    void removeCardFromHand(Card c){
        cardInHand.remove(c);
        owner.sendBoth("#RemoveCardFromHand("+playerName+","+c.name+")");
    }

    void removeCardFromHand(int n){
        cardInHand.remove(n);
    }

    int getNumberOfCreature(Creature _cr){
        return creatures.indexOf(_cr);
    }

    void setNumberPlayer(int _n){
      numberPlayer=_n;
    }

    Player(Gamer _owner, Card _card, Deck _deck, String _playerName) {
        super(0, _card.name, _card.creatureType, 1, 0, _card.targetType, _card.tapTargetType, _card.text, 0, _card.hp);
        owner=_owner;
        deck = _deck;
        isTapped = false;
        playerName = _playerName;
        cardInHand = new ArrayList<>();
        graveyard = new ArrayList<>();
        equpiment = new Equpiment[4];
        equpiment[0] = null;
        equpiment[1] = null;
        equpiment[2] = null;
        equpiment[3] = null;
    }

    Player(Gamer _owner,Deck _deck, String _heroName, String _playerName, int _hp) {
        super(0, _heroName, "", 1, 0, 0, 0, "", 0, _hp);
        owner=_owner;
        deck = _deck;
        isTapped = false;
        playerName = _playerName;
        cardInHand = new ArrayList<>();
        graveyard = new ArrayList<>();
      //  numberPlayer = _n;
        equpiment = new Equpiment[4];
        equpiment[0] = null;
        equpiment[1] = null;
        equpiment[2] = null;
        equpiment[3] = null;
    }

    void endTurn() {
        totalCoin -= temporaryCoin;

        if (owner.opponent.player.bbshield) owner.opponent.player.bbshield = false;

        if (untappedCoin > totalCoin) untappedCoin = totalCoin;
        temporaryCoin = 0;
        //Creature effects until eot
        if (creatures.isEmpty()) {
            for (int i = creatures.size() - 1; i >= 0; i--)
                creatures.get(i).effects.EOT();
        }
        if (owner.opponent.player.creatures.isEmpty()) {
            for (int i = owner.opponent.player.creatures.size() - 1; i >= 0; i--)
                owner.opponent.player.creatures.get(i).effects.EOT();
        }
        owner.opponent.player.newTurn();
    }

    ArrayList<Creature> diedCreatureOnBoard() {
        ArrayList<Creature> r = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.isDie()) {
                r.add(c);
            }
        }
        return r;
    }

    Creature searchWhenOtherDieAbility(Creature cr) {
        for (Creature p : creatures) {
            if (p.text.contains("При гибели другого вашего существа:") && p != cr && p.getTougness() > p.damage)
                return p;
            if (p.text.contains("При гибели в ваш ход другого вашего существа:") && p.owner.playerName.equals(owner.board.whichTurn) && p != cr && p.getTougness() > p.damage)
                return p;
        }
        return null;
    }

    void massDieCheckNeededTarget() {//if someone wants to choice target at death(self or other) - pause game
        crDied = new ArrayList<>(diedCreatureOnBoard());//died creature
        ListIterator<Creature> temp = crDied.listIterator();
        System.out.println("massDie, pl=" + playerName + ", found died " + crDied.size());

        while (temp.hasNext()) {
            Creature tmp = temp.next();
            //Creature ability at death
            Creature cr = searchWhenOtherDieAbility(tmp);//creature, who wants to other die(ex. Падальщик Пустоши)
            if (cr != null && crDied.size() > 0 && !cr.activatedAbilityPlayed) {
                System.out.println("Падальщик " + playerName);
                //CHECK EXIST TARGET
                if (MyFunction.canTargetComplex(this,cr)) {

                    if (numberPlayer == 0) {
                        owner.setPlayerGameStatus(MyFunction.PlayerStatus.choiceTarget);
                    } else {
                        owner.setPlayerGameStatus(MyFunction.PlayerStatus.EnemyChoiceTarget);
                    }

                    ActivatedAbility.creature = cr;
                    ActivatedAbility.whatAbility = onOtherDeathPlayed;
                    //pause until player choice target.
                    owner.sendChoiceTarget(cr.name + " просит выбрать цель.");
                    System.out.println("pause");
                    synchronized (owner.cretureDiedMonitor) {
                        try {
                            owner.cretureDiedMonitor.wait();
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                    System.out.println("resume");
                    ActivatedAbility.creature.activatedAbilityPlayed = true;//if you remove it, may play any times at turn.
                } else {
                    owner.printToView(0, "Целей для " + cr.name + " нет.");
                    cr.activatedAbilityPlayed = true;//If you can't target, after you can't play this ability
                }
            }
            if (tmp.text.contains("Гибель:")) {
                tmp.deathratleNoTarget(tmp, tmp.owner);
                tmp.effects.deathPlayed = true;
            }
            if (tmp.text.contains("Гибельт:") && !tmp.effects.deathPlayed) {
                //CHECK EXIST TARGET
                if (MyFunction.canTargetComplex(this,tmp)) {
                    if (numberPlayer == 0) {
                        owner.setPlayerGameStatus(MyFunction.PlayerStatus.choiceTarget);
                    } else {
                        owner.setPlayerGameStatus(MyFunction.PlayerStatus.EnemyChoiceTarget);
                    }

                    ActivatedAbility.creature = new Creature(tmp);
                    ActivatedAbility.whatAbility = onDeathPlayed;
                    //pause until player choice target.
                    owner.sendChoiceTarget(tmp.name + " просит выбрать цель.");
                    System.out.println("pause");
                    synchronized (owner.cretureDiedMonitor) {
                        try {
                            owner.cretureDiedMonitor.wait();
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                    System.out.println("resume");
                    tmp.effects.deathPlayed = true;
                } else if (tmp.targetType == 99) {
                    //Check n card
                    int n = cardInHand.size();
                    if (n > 1) {
                        if (numberPlayer == 0) {
                           owner.setPlayerGameStatus(MyFunction.PlayerStatus.choiceTarget);
                        } else {
                            owner.setPlayerGameStatus(MyFunction.PlayerStatus.EnemyChoiceTarget);
                        }

                        ActivatedAbility.creature = new Creature(tmp);
                        ActivatedAbility.whatAbility = ActivatedAbility.WhatAbility.toHandAbility;
                        //pause until player choice target.
                        owner.sendChoiceTarget(tmp.name + " просит cбросить карту.");
                        System.out.println("pause");
                        synchronized (owner.cretureDiedMonitor) {
                            try {
                                owner.cretureDiedMonitor.wait();
                            } catch (InterruptedException e2) {
                                e2.printStackTrace();
                            }
                        }
                        System.out.println("resume");
                    }
                    if (n == 1) {
                        owner.printToView(0, tmp.name + " заставляет сбросить " + cardInHand.get(0));
                        Board.putCardToGraveyard(cardInHand.get(0), this);
                        removeCardFromHand(cardInHand.get(0));
                    }
                    if (n == 0) {
                        owner.printToView(0, tmp.name + " заставляет сбросить карту, но ее нет.");
                    }
                } else {
                    owner.printToView(0, "Целей для " + tmp.name + " нет.");
                    tmp.effects.deathPlayed = true;//If you can't target, after you can't play this ability
                }
            }
        }
    }

    void massUpkeepCheckNeededTarget() {//if someone wants to choice target at death(self or other) - pause game
        crUpkeeped = new ArrayList<>(creatures);//died creature
        ListIterator<Creature> temp = crUpkeeped.listIterator();
        while (temp.hasNext()) {
            Creature tmp = temp.next();
            //Creature ability at upkeep
            if (tmp.text.contains("В начале хода") || tmp.text.contains("В начале вашего хода") && tmp.getTougness() > tmp.damage)
                if (creatures.size() > 1 && !tmp.effects.upkeepPlayed) {
                    System.out.println("Амбрадоринг " + playerName);
                    //CHECK EXIST TARGET
                    if (numberPlayer == 0) {
                        owner.setPlayerGameStatus(MyFunction.PlayerStatus.choiceTarget);
                    } else {
                        owner.setPlayerGameStatus(MyFunction.PlayerStatus.EnemyChoiceTarget);
                    }

                    ActivatedAbility.creature = tmp;
                    ActivatedAbility.whatAbility = onUpkeepPlayed;
                    //pause until player choice target.
                    owner.sendChoiceTarget(tmp.name + " просит выбрать цель.");
                    System.out.println("pause");
                    synchronized (owner.cretureDiedMonitor) {
                        try {
                            owner.cretureDiedMonitor.wait();
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                    System.out.println("resume");
                    tmp.effects.upkeepPlayed = true;
                    break;//Upkeep played creature by creature.
                }
        }
    }

    void massSummonCheckNeededTarget() {//if someone wants to choice target at death(self or other) - pause game
        crCryed = new ArrayList<>(creatures);//died creature
        ListIterator<Creature> temp = crCryed.listIterator();
        while (temp.hasNext()) {
            Creature tmp = temp.next();
            //Creature ability at enter to board
            if (tmp.text.contains("Найм:") && !tmp.effects.battlecryPlayed && tmp.getTougness() > tmp.damage) {
                tmp.battlecryNoTarget();
                tmp.effects.battlecryPlayed = true;
            }
            if (tmp.text.contains("Наймт:") && !tmp.effects.battlecryPlayed && tmp.getTougness() > tmp.damage)
                //CHECK EXIST TARGET
                if (MyFunction.canTargetComplex(this,tmp)) {
                        owner.setPlayerGameStatus(MyFunction.PlayerStatus.choiceTarget);
                    ActivatedAbility.creature = tmp;
                    ActivatedAbility.whatAbility = nothing;
                    //pause until player choice target.
                    owner.sendChoiceTarget(tmp.name + " просит выбрать цель.");
                    System.out.println("pause");
                    synchronized (owner.cretureDiedMonitor) {
                        try {
                            owner.cretureDiedMonitor.wait();
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                    System.out.println("resume");
                    tmp.effects.battlecryPlayed = true;
                    break;//Cry played creature by creature.
                } else {
                    owner.printToView(0, "Целей для " + tmp.name + " нет.");
                    tmp.effects.battlecryPlayed = true;//If you can't target, after you can't play this ability
                }
        }
    }

    void newTurn() {
        owner.board.whichTurn=playerName;
        owner.board.turnCount++;
        owner.printToView(0, "Ход номер " + owner.board.turnCount + ", игрок " + playerName);
        if (numberPlayer == 0)
            owner.printToView(1, Color.GREEN, "Ваш ход");
        else owner.printToView(1, Color.RED, "Ход противника");

        //Tull-Bagar
        //TODO FIX null
        if (this.equpiment[3] != null && this.equpiment[3].name.equals("Пустошь Тул-Багара")) {
            owner.printToView(0, "Пустошь Тул-Багара ранит всех героев.");
            this.takeDamage(1);
            owner.opponent.player.takeDamage(1);
        }
        if (owner.opponent.player.equpiment[3] != null && owner.opponent.player.equpiment[3].name.equals("Пустошь Тул-Багара")) {
            owner.printToView(0, "Пустошь Тул-Багара ранит всех героев.");
            this.takeDamage(1);
            owner.opponent.player.takeDamage(1);
        }

        //Search for Ambrador
        if (creatures.size() > 1) {
            for (Creature p : creatures) {
                if (p.text.contains("В начале хода") || p.text.contains("В начале вашего хода") && p.getTougness() > p.damage)
                    owner.gameQueue.push(new GameQueue.QueueEvent("Upkeep", p, 0));
            }
        }
        //Bogart and other
        //owner.gameQueue.responseAllQueue();

        untap();
        //Get coin
        if (totalCoin < 10) totalCoin++;
        //Untap
        untappedCoin = totalCoin;

        //TODO tap() and untap()
        if (equpiment[0] != null) equpiment[0].isTapped = false;
        if (equpiment[1] != null) equpiment[1].isTapped = false;
        if (equpiment[2] != null) equpiment[2].isTapped = false;

        for (int i = creatures.size() - 1; i >= 0; i--) {
            //untap
            creatures.get(i).isSummonedJust = false;
            creatures.get(i).untapCreature();
            //armor
            creatures.get(i).currentArmor = creatures.get(i).maxArmor;
            //for rage
            creatures.get(i).takedDamageThisTurn = false;
            creatures.get(i).attackThisTurn = false;
            creatures.get(i).blockThisTurn = false;
            //poison, here creature may die, check it for after.
            if ((creatures.get(i).effects.poison != 0) && (!creatures.get(i).text.contains("Защита от отравления.")))
                creatures.get(i).takeDamage(creatures.get(i).effects.poison, Creature.DamageSource.poison);
          //  owner.gameQueue.responseAllQueue();//poison queue
        }

        //Draw
        if (owner.board.turnCount != 1) drawCard();//First player not draw card in first turn. It's rule.
        //Send status
        owner.sendStatus();
        //owner.opponent.sendStatus();
    }

    void playCardX(int num, Card _card, Creature _targetCreature, Player _targetPlayer, int x) {
        owner.printToView(0, "X = " + x + ".");
        _card.text = _card.text.replace("ХХХ", String.valueOf(x));
        System.out.println("text after replace:" + _card.text);
        tempX = x;
        playCard(num,_card, _targetCreature, _targetPlayer);
        tempX = 0;
    }

    void playCard(int num, Card _card, Creature _targetCreature, Player _targetPlayer) {
        int effectiveCost = _card.getCost(this);
        if (tempX != 0) effectiveCost += tempX;

        if (untappedCoin >= effectiveCost) {
            untappedCoin -= effectiveCost;
            owner.printToView(0, "Розыгрышь карты " + _card.name + ".");
            //remove from hand
            removeCardFromHand(num);
            //put on table or cast spell
            if (_card.type == 1) {
                //release text on spell
                //#PlaySpell(Player, SpellName, TargetHalfBoard[0-self,1-enemy], TargetCreatureNum[-1 means targets player])

                //check target
                if (_targetPlayer != null) {
                    _card.playOnPlayer(this, _targetPlayer);
                    if (_targetPlayer==this)
                    owner.sendBoth("#PlaySpell("+playerName+","+_card.name+",0,-1)");
                    else owner.sendBoth("#PlaySpell("+playerName+","+_card.name+",1,-1)");
                }
                if (_targetCreature != null) {
                    _card.playOnCreature(this, _targetCreature);
                    if (_targetCreature.owner==this)
                        owner.sendBoth("#PlaySpell("+playerName+","+_card.name+",0,"+getNumberOfCreature(_targetCreature)+")");
                    else owner.sendBoth("#PlaySpell("+playerName+","+_card.name+",1,"+owner.opponent.player.getNumberOfCreature(_targetCreature)+")");
                }
                //No target
                if ((_targetCreature == null) && (_targetPlayer == null)) {
                    _card.playNoTarget(this);
                    owner.sendBoth("#PlaySpell("+playerName+","+_card.name+",-1,-1)");
                }
                //and after play
                Board.putCardToGraveyard(_card, this);
            } else if (_card.type == 2) {
                //creature
                owner.board.addCreatureToBoard(_card, this);
            } else if (_card.type == 3) {
                //TODO Equpiment command server
                owner.printToView(0, name + " экипировал " + _card.name + ".");
                if (_card.creatureType.equals("Броня")) {
                    if (this.equpiment[0] != null) Board.putCardToGraveyard(this.equpiment[0], this);
                    this.equpiment[0] = new Equpiment(_card, this);
                } else if (_card.creatureType.equals("Амулет")) {
                    if (this.equpiment[1] != null) Board.putCardToGraveyard(this.equpiment[1], this);
                    this.equpiment[1] = new Equpiment(_card, this);
                } else if (_card.creatureType.equals("Оружие")) {
                    if (this.equpiment[2] != null) Board.putCardToGraveyard(this.equpiment[2], this);
                    this.equpiment[2] = new Equpiment(_card, this);
                }
            } else if (_card.type == 4) {
                owner.printToView(0, name + " экипировал " + _card.name + ".");
                if (this.equpiment[3] != null) Board.putCardToGraveyard(this.equpiment[3], this);
                this.equpiment[3] = new Equpiment(_card, this);
            }

        } else {
            owner.printToView(0, "Не хватает монет.");
        }

      //  owner.gameQueue.responseAllQueue();
    }

    void drawCard() {
        if (deck.haveTopDeck())
            addCardToHand(deck.drawTopDeck());
        else {
            owner.printToView(0, "Deck of " + playerName + " is empty.");
        }
    }

    void drawSpecialCard(Card c) {
        addCardToHand(c);
        deck.cards.remove(c);
        deck.suffleDeck(owner.sufflingConst);
    }

    void digSpecialCard(Card c) {
        addCardToHand(c);
        removeCardFromGraveyard(c);
    }

    void takeDamage(int dmg) {
        //equpiment[1]
        if (equpiment[1] != null) {
            if (equpiment[1].name.equals("Браслет подчинения")) {
                //Плащ исхара
                if (dmg != 1)
                    owner.printToView(0, "Браслет подчинения свел атаку к 1.");
                dmg = 1;

            }
        }
        //equpiment[0]
        if (equpiment[0] != null) {
            if (equpiment[0].name.equals("Плащ Исхара")) {
                //Плащ исхара
                int tmp = dmg;
                dmg -= equpiment[0].hp;
                equpiment[0].hp -= tmp;
                if (dmg < 0) dmg = 0;
                owner.printToView(0, "Плащ Исхара предотвратил " + (tmp - dmg) + " урона.");
                if (equpiment[0].hp <= 0) {
                    Board.putCardToGraveyard(equpiment[0], this);
                    equpiment[0] = null;
                }
            }
        }
        if (hp > damage + dmg) {
            damage += dmg;
            if (dmg != 0){
                owner.sendBoth("#TakeHeroDamage("+playerName+","+dmg+")");
                owner.printToView(0, this.name + " получет " + dmg + " урона.");
            }
        } else {//Not here, because it may be in queue and at end of queue restore status
            if (this.numberPlayer==0){
                System.out.println("You lose game.");
                owner.sendBoth("#LoseGame("+playerName+")");
                owner.printToView(2, Color.RED, "Вы проиграли игру.");
                owner.setPlayerGameStatus(MyFunction.PlayerStatus.endGame);
            } else {
                System.out.println("You win game.");
                owner.sendBoth("#WinGame("+playerName+")");
                owner.printToView(2, Color.GREEN, "Вы выиграли игру.");
                owner.setPlayerGameStatus(MyFunction.PlayerStatus.endGame);
            }
        }
    }

    void heal(int dmg) {
        if (equpiment[1].name.equals("Браслет подчинения")) {
            owner.printToView(0, name + " не может быть излечен.");
        } else {
            damage -= dmg;
            if (damage < 0) damage = 0;
            owner.sendBoth("#TakeHeroDamage("+playerName+","+dmg+",0)");
        }
    }

    void abilityNoTarget() {
        String txt = this.text.substring(this.text.indexOf("ТАП:") + "ТАП:0".length() + 1, this.text.indexOf(".", this.text.indexOf("ТАП:")) + 1);
        System.out.println("ТАП HERO: " + txt);
        tap();
        Card.ability(owner,this, this, null, null, txt);
    }

    void ability(Creature _cr, Player _pl) {
        String txt = this.text.substring(this.text.indexOf("ТАПТ: ") + "ТАПТ: ".length() + 1, this.text.indexOf(".", this.text.indexOf("ТАПТ: ") + 1));
        System.out.println("ТАПТ HERO: " + txt);
        tap();
        Card.ability(owner,this, this, _cr, _pl, txt);
    }

    public Card searchInGraveyard(String name) {
        for (int i = 0; i <= graveyard.size(); i++) {
            if (graveyard.get(i).name.equals(name)) return graveyard.get(i);
        }
        return null;
    }

    public void tap(){
        //TODO #Tap
        owner.sendBoth("#TapPlayer("+playerName+",1)");
        isTapped=true;
    }
    public void untap(){
        owner.sendBoth("#TapPlayer("+playerName+",0)");
        isTapped=false;
    }
}
