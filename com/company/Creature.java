package com.company;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by StudenetskiyA on 30.12.2016.
 */

public class Creature extends Card {
    boolean isTapped;
    boolean isSummonedJust;
    boolean activatedAbilityPlayed = false;
    boolean takedDamageThisTurn = false;
    boolean attackThisTurn = false;
    boolean blockThisTurn = false;

    Player owner;
    int currentArmor = 0;
    int maxArmor = 0;
    int damage;//taked damage

    enum DamageSource {fight, spell, poison, ability, scoot}

    Effects effects = new Effects(this);

    public class Effects {
        Creature whis;
        String additionalText = "";
        public int poison = 0;
        public int bonusPower = 0;
        public int bonusPowerUEOT = 0;
        public int bonusTougness = 0;
        public int bonusArmor = 0;
        public int cantAttackOrBlock = 0;
        public int turnToDie = 999;
        boolean vulnerability = false;
        public boolean upkeepPlayed = false;
        public boolean battlecryPlayed = false;
        public boolean deathPlayed = false;

        Effects(Creature _cr) {
            whis = _cr;
        }

        public void EOT() {
            cantAttackOrBlock--;
            upkeepPlayed = false;
            turnToDie--;
            bonusPowerUEOT = 0;
            if (cantAttackOrBlock < 0) cantAttackOrBlock = 0;
            if (turnToDie == 0) {
                owner.owner.gameQueue.push(new GameQueue.QueueEvent("Die", whis, 0));
            }
            activatedAbilityPlayed = false;
        }

        boolean getVulnerability() {
            if (owner.owner.opponent.player.equpiment[3] != null && owner.owner.opponent.player.equpiment[3].name.equals("Аккения")) {
                System.out.println("Аккения детектед");
                return true;
            }
            return vulnerability;
        }

        int getBonusPower(){
            int staticBonus = 0;
            //TanGnome take + for power
            if ((creatureType.equals("Гном")) && (!name.equals("Тан гномов"))) {
                int tanFounded = 0;
                for (int i = 0; i < owner.creatures.size(); i++) {
                    if (owner.creatures.get(i).name.equals("Тан гномов")) tanFounded++;
                }
                staticBonus += tanFounded;
            }
            //Chain dog take and get + power
            if ((name.equals("Цепной пес"))) {
                int houndFounded = 0;
                for (int i = 0; i < owner.creatures.size(); i++) {
                    if (owner.creatures.get(i).name.equals("Цепной пес")) houndFounded++;
                }
                //and for opponent
                for (int i = 0; i < owner.owner.opponent.player.creatures.size(); i++) {
                    if (owner.owner.opponent.player.creatures.get(i).name.equals("Цепной пес")) houndFounded++;
                }
                staticBonus += houndFounded - 1;
            }
            return staticBonus+bonusPower+bonusPowerUEOT;
        }

        int getBonusTougness(){
            return bonusTougness;
        }

        void takePoison(int p){
            if (poison<=p)
            poison=p;
            //#TakeCreatureEffect(Player, CreatureNumOnBoard,Effect,EffectCount)
            owner.owner.sendBoth("#TakeCreatureEffect("+owner.playerName+","+owner.getNumberOfCreature(this.whis)+","+ MyFunction.Effect.poison.getValue()+","+p+")");
        }
    }

    int getBonusOrMinusTougness(){
        if (damage>0) return -1;
        else if (damage==0 && effects.getBonusTougness()>0) return 1;
        else return 0;
    }

    int getBonusOrMinusPower(){
        if (getPower()>power) return 1;
        if (getPower()==power) return 0;
        else return -1;
    }

    boolean getIsSummonedJust() {
        if (text.contains("Рывок")) return false;
        if (effects.additionalText.contains("Рывок")) return false;
        //Chain dog take charge
        if ((name.equals("Цепной пес"))) {
            int houndFounded = 0;
            for (int i = 0; i < owner.creatures.size(); i++) {
                if (owner.creatures.get(i).name.equals("Цепной пес")) houndFounded++;
            }
            //and for opponent
            for (int i = 0; i < owner.owner.opponent.player.creatures.size(); i++) {
                if (owner.owner.opponent.player.creatures.get(i).name.equals("Цепной пес")) houndFounded++;
            }
            if (houndFounded > 1) return false;
        }
        return isSummonedJust;
    }

    int getMaxArmor() {
        return maxArmor + effects.bonusArmor;
    }

    public int getCurrentArmor() {
        return currentArmor;
    }

    int getPower() {
        return power + effects.getBonusPower();
    }

    int getTougness() {
        return hp + effects.getBonusTougness();
    }

    Creature(Creature _card) {
        super(_card.cost, _card.name, _card.creatureType, _card.color, _card.type, _card.targetType, _card.tapTargetType, _card.text, _card.power, _card.hp);
        image = _card.image;
        cost = _card.cost;
        isTapped = false;
        isSummonedJust = true;
        name = _card.name;
        owner = _card.owner;

        if (text.contains("Броня ")) {
            maxArmor = MyFunction.getNumericAfterText(text, "Броня ");
            currentArmor = getMaxArmor();
        }
    }

    Creature(Card _card, Player _owner) {
        super(_card.cost, _card.name, _card.creatureType, _card.color, _card.type, _card.targetType, _card.tapTargetType, _card.text, _card.power, _card.hp);
        image = _card.image;
        cost = _card.cost;
        isTapped = false;
        isSummonedJust = true;
        name = _card.name;
        owner = _owner;
        if (text.contains("Броня ")) {
            maxArmor = MyFunction.getNumericAfterText(text, "Броня ");
            currentArmor = getMaxArmor();
        }
    }

    void tapCreature() {
        isTapped = true;
        owner.owner.sendBoth("#TapCreature("+owner.playerName+","+owner.getNumberOfCreature(this)+",1)");
    }

    void untapCreature() {
        isTapped = false;
        owner.owner.sendBoth("#TapCreature("+owner.playerName+","+owner.getNumberOfCreature(this)+",0)");
    }
    private ArrayList<Creature> canAnyoneBlock(Creature target) {
        //Return list of creature, who may be block this

        //get list of opponent creature
        ArrayList<Creature> crt = new ArrayList<>(owner.owner.opponent.player.creatures);
        ArrayList<Creature> crtCopy = new ArrayList<>(crt);
        ListIterator<Creature> temp = crtCopy.listIterator();

        while (temp.hasNext()) {
            Creature tmp = temp.next();
            if (tmp.blockThisTurn || tmp.isTapped || tmp.effects.cantAttackOrBlock > 0 || tmp==target)
                crt.remove(tmp); //it make error
        }
        return crt;
    }

    void fightCreature(Creature second) {
        if (!second.isTapped) {//First is passive
            owner.owner.printToView(0, this.name + " сражается с " + second.name + ".");
            if ((second.text.contains("Первый удар.")) && (!this.text.contains("Первый удар."))) {
                if (this.damage < this.hp) second.takeDamage(this.getPower(), DamageSource.fight, second.haveRage());
                this.takeDamage(second.getPower(), DamageSource.fight, second.haveRage());
            } else if ((this.text.contains("Первый удар.")) && (!second.text.contains("Первый удар."))) {
                second.takeDamage(this.getPower(), DamageSource.fight, second.haveRage());
                if (second.damage < second.hp) this.takeDamage(second.getPower(), DamageSource.fight, second.haveRage());
            } else {
                second.takeDamage(this.getPower(), DamageSource.fight, second.haveRage());
                this.takeDamage(second.getPower(), DamageSource.fight, second.haveRage());
            }
        } else {
            owner.owner.printToView(0, this.name + " ударяет " + second.name + ".");
            second.takeDamage(this.getPower(), DamageSource.fight, second.haveRage());
        }

        //Response queue
//        owner.owner.gameQueue.responseAllQueue();
    }

    void heal(int dmg) {
        damage -= dmg;
        if (damage < 0) damage = 0;
        owner.owner.sendBoth("#TakeCreatureDamage("+owner.playerName+","+owner.getNumberOfCreature(this)+","+(-dmg)+")");
    }

    void fightPlayer(Player second) {
        second.takeDamage(this.getPower());
    }

    void attackCreature(Creature target) {
        if (!text.contains("Опыт в атаке") || !effects.additionalText.contains("Опыт в атаке"))
            tapCreature();
        attackThisTurn = true;

        if (this.text.contains("Направленный удар")) {
            fightCreature(target);
        } else {
            ArrayList<Creature> blocker;
            blocker = canAnyoneBlock(target);
            if (blocker.size() != 0) {
                int nc = owner.creatures.indexOf(this);
                int nt = owner.owner.opponent.player.creatures.indexOf(target);
                owner.owner.opponent.setPlayerGameStatus(MyFunction.PlayerStatus.IChoiceBlocker);
                owner.owner.opponent.creatureWhoAttack = nc;
                owner.owner.opponent.creatureWhoAttackTarget = nt;
                owner.owner.opponent.output.println("#ChoiceBlocker("+owner.owner.opponent.name+","+nc+","+nt+")");
                owner.owner.setPlayerGameStatus(MyFunction.PlayerStatus.EnemyChoiceBlocker);
            } else {
                fightCreature(target);
            }
        }
    }

    void attackPlayer(Player target) {
        if (target.bbshield) target.bbshield = false;
        if (!text.contains("Опыт в атаке."))
            tapCreature();
        attackThisTurn = true;

        if (this.text.contains("Направленный удар.")) {
            fightPlayer(target);
        } else {
            ArrayList<Creature> blocker = canAnyoneBlock(null);
            if (blocker.size() != 0) {
                int nc = owner.creatures.indexOf(this);
                int nt = -1;
                owner.owner.opponent.setPlayerGameStatus(MyFunction.PlayerStatus.IChoiceBlocker);
                owner.owner.opponent.creatureWhoAttack = nc;
                owner.owner.opponent.creatureWhoAttackTarget = nt;
                owner.owner.opponent.output.println("#ChoiceBlocker("+owner.owner.opponent.name+","+nc+","+nt+")");
                owner.owner.setPlayerGameStatus(MyFunction.PlayerStatus.EnemyChoiceBlocker);
            } else {
                fightPlayer(target);
            }
        }
    }

    void takeDamage(int dmg, DamageSource dmgsrc, Boolean... rage) {
        if (!this.text.contains("Не получает ран.")) {
            if ((dmgsrc == DamageSource.scoot) || (dmgsrc == DamageSource.fight)) {
                if ((takedDamageThisTurn) && (rage[0])) {
                    dmg++;
                    System.out.println("RAGE!");
                }
                int tmp = dmg;
                dmg -= currentArmor;
                currentArmor -= tmp;
                if (dmg < 0) dmg = 0;
                if (currentArmor < 0) currentArmor = 0;
            }
            if ((effects.getVulnerability())) dmg++;

            damage += dmg;
            owner.owner.sendBoth("#TakeCreatureDamage("+owner.playerName+","+owner.getNumberOfCreature(this)+","+dmg+")");

            takedDamageThisTurn = true;

            if (getTougness() <= damage) {
                die();
            }
        }
    }

    void tapNoTargetAbility() {
        String txt = this.text.substring(this.text.indexOf("ТАП:") + "ТАП:".length() + 1, this.text.indexOf(".", this.text.indexOf("ТАП:")) + 1);
        System.out.println("ТАПТ: " + txt);
        tapCreature();
        Card.ability(owner.owner,this, owner, null, null, txt);
    }

    void tapTargetAbility(Creature _cr, Player _pl) {
        String txt = this.text.substring(this.text.indexOf("ТАПТ:") + "ТАПТ:".length() + 1, this.text.indexOf(".", this.text.indexOf("ТАПТ:")) + 1);
        System.out.println("ТАПТ: " + txt);
        tapCreature();
        Card.ability(owner.owner,this, owner, _cr, _pl, txt);
    }

    void deathratle(Creature _cr, Player _pl) {
        String txt = this.text.substring(this.text.indexOf("Гибельт:") + "Гибельт:".length() + 1, this.text.indexOf(".", this.text.indexOf("Гибельт:")) + 1);
        System.out.println("Гибельт: " + txt);
        Card.ability(owner.owner,this, owner, _cr, _pl, txt);
    }

    void battlecryNoTarget() {
        String txt = this.text.substring(this.text.indexOf("Найм:") + "Найм:".length() + 1, this.text.indexOf(".", this.text.indexOf("Найм:")) + 1);
        Card.ability(owner.owner,this, this.owner, this, null, txt);//Only here 3th parametr=1th
    }

    void battlecryTarget(Creature _cr, Player _pl) {
        String txt = this.text.substring(this.text.indexOf("Наймт:") + "Наймт:".length() + 1, this.text.indexOf(".", this.text.indexOf("Наймт:")) + 1);
        System.out.println("Наймт: " + txt);
        Card.ability(owner.owner,this, owner, _cr, _pl, txt);
    }

    static void deathratleNoTarget(Creature _card, Player _owner) {
        String txt = _card.text.substring(_card.text.indexOf("Гибель:") + "Гибель:".length() + 1, _card.text.indexOf(".", _card.text.indexOf("Гибель:")) + 1);
        Card.ability(_owner.owner,_card, _owner, _card, null, txt);//Only here 3th parametr=1th
    }

    void die() {
        System.out.println("Die!");
        Board.putCardToGraveyard(this, this.owner);
        owner.owner.sendBoth("#AddCardToGraveyard("+owner.playerName+","+this.name+")");

        owner.owner.gameQueue.push(new GameQueue.QueueEvent("Die", this, 0));
    }

    boolean isDie() {
        return (getTougness() <= damage);//And other method to die!
    }

    void returnToHand() {
        owner.owner.sendBoth("#ReturnToHand("+owner.playerName+","+owner.getNumberOfCreature(this)+")");
        owner.owner.output.println("#AddCardToHand("+this.name+")");
        removeCreatureFromPlayerBoard();
        owner.cardInHand.add(0,this);
    }

    void removeCreatureFromPlayerBoard() {
        owner.creatures.remove(this);
    }
}
