package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by samsung on 21.01.2017.
 */
public class MyFunction {

    static class ActivatedAbility {

        static Creature creature;
        static boolean creatureTap;
        static WhatAbility whatAbility=WhatAbility.nothing;

        enum WhatAbility {
            heroAbility(1), weaponAbility(2), toHandAbility(3), onUpkeepPlayed(4), onDeathPlayed(5), onOtherDeathPlayed(6), nothing(0);

            private final int value;

            WhatAbility(int value) {
                this.value = value;
            }

            public int getValue() {
                return value;
            }

            public static WhatAbility fromInteger(int x) {
                switch (x) {
                    case 0:
                        return nothing;
                    case 1:
                        return heroAbility;
                    case 2:
                        return weaponAbility;
                    case 3:
                        return toHandAbility;
                    case 4:
                        return onUpkeepPlayed;
                    case 5:
                        return onDeathPlayed;
                    case 6:
                        return onOtherDeathPlayed;
                }
                return null;
            }
        }
    }

    public static ArrayList<String> getTextBetween(String fromText) {
        ArrayList<String> rtrn = new ArrayList<String>();
        String beforeText = "(";
        fromText = fromText.substring(fromText.indexOf(beforeText) + 1, fromText.length() - 1);
        String[] par = fromText.split(",");
        for (int i = 0; i < par.length; i++)
            rtrn.add(par[i]);
        return rtrn;
    }

    enum Effect{
        poison(1), vulnerability(2);

        private final int value;

        Effect(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Effect fromInteger(int x) {
            switch(x) {
                case 1:
                    return poison;
                case 2:
                    return vulnerability;
            }
            return null;
        }
    }

    enum Target {myPlayer,myCreature,enemyPlayer,enemyCreature}

    enum PlayerStatus {
        MyTurn(1), EnemyTurn(2), IChoiceBlocker(3), EnemyChoiceBlocker(4), EnemyChoiceTarget(5), MuliganPhase(6), waitingForConnection(7),
        waitOtherPlayer(8), waitingMulligan(9), choiseX(10), searchX(11), choiceTarget(12), digX(13), endGame(14), prepareForBattle(15),
        unknow(0);

        private final int value;

        PlayerStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static PlayerStatus fromInteger(int x) {
            switch(x) {
                case 0:
                    return unknow;
                case 1:
                    return MyTurn;
                case 2:
                    return EnemyTurn;
                case 3:
                    return IChoiceBlocker;
                case 4:
                    return EnemyChoiceBlocker;
                case 5:
                    return EnemyChoiceTarget;
                case 6:
                    return MuliganPhase;
                case 7:
                    return waitingForConnection;
                case 8:
                    return waitOtherPlayer;
                case 9:
                    return waitingMulligan;
                case 10:
                    return choiseX;
                case 11:
                    return searchX;
                case 12:
                    return choiceTarget;
                case 13:
                    return digX;
                case 14:
                    return endGame;
                case 15:
                    return prepareForBattle;
            }
            return null;
        }
    }


    public static boolean canTargetComplex(Player pl, Creature cr){
        boolean canTarget=false;
        if (pl.getNumberOfAlivedCreatures() > 0 && MyFunction.canTarget(MyFunction.Target.myCreature,cr.targetType)) canTarget=true;
        if (pl.owner.opponent.player.getNumberOfAlivedCreatures() > 0 && MyFunction.canTarget(MyFunction.Target.enemyCreature,cr.targetType)) canTarget=true;
        if (MyFunction.canTarget(MyFunction.Target.enemyPlayer,cr.targetType)) canTarget=true;//Both players always stay on board
        if (MyFunction.canTarget(MyFunction.Target.myPlayer,cr.targetType)) canTarget=true;
        return canTarget;
    }

    public static boolean canTarget(Target target,int targetType){
        //10 my hero or my creature, not self
        //12 my creature, not self
        if (target==Target.myPlayer)
        {
            if (targetType==2 || targetType==3 || targetType==9 || targetType==10 ) return true;
        }
        else if (target==Target.myCreature)
        {
            if (targetType==1 || targetType==3 || targetType==7 || targetType==9 || targetType==10 || targetType==12 ) return true;
        }
        else if (target==Target.enemyPlayer)
        {
            if (targetType==2 || targetType==3 || targetType==5 || targetType==6) return true;
        }
        else if (target==Target.enemyCreature)
        {
            if (targetType==1 || targetType==3 || targetType==4 || targetType==6) return true;
        }
        return false;
    }

    public static int searchCardInHandByName(ArrayList<Card> _array,String _name){
        for (int i=0;i<_array.size();i++) {
            if (_array.get(i).name.equals(_name)) return i;
        }
        return -1;
    }

    public static int getNumericAfterText(String fromText, String afterText) {
        int begin = fromText.indexOf(afterText);
        int end1 = fromText.indexOf(" ", begin + afterText.length() + 1);
        if (end1 == -1) end1 = 1000;
        int end2 = fromText.indexOf(".", begin + afterText.length() + 1);
        if (end2 == -1) end2 = 1000;
        int end3 = fromText.indexOf(",", begin + afterText.length() + 1);
        if (end3 == -1) end3 = 1000;
        int end = Math.min(end1, end2);
        end = Math.min(end, end3);
        if (end == 1000) end = fromText.length();
        String dmg = fromText.substring(begin + afterText.length(), end);
        int numdmg = 0;
        try {
            numdmg = Integer.parseInt(dmg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numdmg;
    }
//
    public static int getNumDiedButNotRemovedYet(ArrayList<Creature> list){
        int n=0;
        for (Creature cr:list){
            if (cr.getTougness()<=cr.damage) n++;
        }
        return n;
    }

//    public static boolean canTargetComplex(Creature cr){
//        boolean canTarget=false;
//        if (Board.creature.get(0).size() > 0 && MyFunction.canTarget(MyFunction.Target.myCreature,cr.targetType)) canTarget=true;
//        if (Board.creature.get(1).size() > 0 && MyFunction.canTarget(MyFunction.Target.enemyCreature,cr.targetType)) canTarget=true;
//        if (MyFunction.canTarget(MyFunction.Target.enemyPlayer,cr.targetType)) canTarget=true;//Both players always stay on board
//        if (MyFunction.canTarget(MyFunction.Target.myPlayer,cr.targetType)) canTarget=true;
//        return canTarget;
//    }

    public static BufferedImage tapImageOnAngle(BufferedImage src,int angle) {
        double rotationRequired = angle;
        AffineTransform tx = new AffineTransform();
        tx.translate(0.5 * src.getHeight(), 0.5 * src.getWidth());
        tx.rotate(rotationRequired);
        tx.translate(-0.5 * src.getWidth(), -0.5 * src.getHeight());
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(src, null);
    }

    public static BufferedImage tapImage(BufferedImage src) {
        double rotationRequired = Math.toRadians(90);
        AffineTransform tx = new AffineTransform();
        tx.translate(0.5 * src.getHeight(), 0.5 * src.getWidth());
        tx.rotate(rotationRequired);
        tx.translate(-0.5 * src.getWidth(), -0.5 * src.getHeight());
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(src, null);
    }

    public static class ClickImage extends JLabel{
        public BufferedImage image;

        ClickImage(){
            super();
        }

        void drawImage(Graphics g){
            if (isVisible())
                g.drawImage(image,getX(),getY(),getWidth(),getHeight(),null);
        }

        void LSD(Graphics g, int x,int y, int w, int h){//Location, Size, Draw! )))
            setLocation(x,y);
            setSize(w,h);
            drawImage(g);
        }

        void LSDtap(Graphics g, int x,int y, int w, int h){//Location, Size, Draw! )))
            setLocation(x,y);
            setSize(w,h);
            drawTapped(g);
        }

        void LSDiftap(Graphics g,boolean t, int x,int y, int w, int h){
            if (t) LSDtap(g,x,y,w,h);
            else LSD(g,x,y,w,h);
        }
        void drawTapped(Graphics g){
            if (isVisible())
                g.drawImage(tapImage(image),getX(),getY()+getHeight()/2-getWidth()/2, getHeight(),getWidth(),null);
        }
    }
}
