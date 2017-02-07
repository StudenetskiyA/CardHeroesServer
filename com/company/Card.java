package com.company;

import java.util.ArrayList;
import java.util.ListIterator;

import static com.company.Main.main;

/**
 * Created by StudenetskiyA on 30.12.2016.
 */
class Card {
    //Gamer owner;
    int cost;
    String name;
    String text;
    String image;
    String creatureType;
    int color;//1-swamp,2-field,3-mountain,4-forest,5-dark,6-neutral,7 and more - multicolor
    int type;//1 for spell, 2 for creature, 3 equpiment and event
    int targetType;//Battlecry 1 for creatures, 2 for heroes, 3 for heroes and creatures, 4 for only opponent creature, 9 for my creature or hero, 10 as 9, but not self
    int tapTargetType;//May exist cards with Battlecry and TAP. Today its only one)))
    int power;//only for creature, ignore for other
    int hp;//only for creature and hero, its maximum health, not current
    String hash;//for suffling

    static Card simpleCard = new Card(0, "", "", 0, 0, 0, 0, "", 0, 0);

    public Card(Card _card) {
        //owner=_owner;
        name = _card.name;
        text = _card.text;
        cost = _card.cost;
        image = _card.image;
        color = _card.color;
        type = _card.type;
        power = _card.power;
        hp = _card.hp;
        targetType = _card.targetType;
        tapTargetType = _card.tapTargetType;
        creatureType = _card.creatureType;
    }

    public Card(int _cost, String _name, String _crtype, int _color, int _type, int _targetType, int _tapTargetType, String _text, int _power, int _hp) {
        //   board=_board;
       // owner=_owner;
        name = _name;
        text = _text;
        cost = _cost;
        image = _name + ".jpg";
        color = _color;
        type = _type;
        power = _power;
        hp = _hp;
        targetType = _targetType;
        tapTargetType = _tapTargetType;
        creatureType = _crtype;
    }

    public void playOnCreature(Player _pl, Creature creature) {
        if (creature.text.contains("Если выбрана целью заклинание - погибает.")) {
            creature.die();
        } //delete else
        ability(_pl.owner,this, _pl, creature, null, text);
    }

    public void playOnPlayer(Player _pl, Player _player) {
        ability(_pl.owner,this, _pl, null, _player, text);
    }

    void playNoTarget(Player _pl){
        ability(_pl.owner,this, _pl, null, null, this.text);
    }

    public static Card getCardByName(String name) {
        //Here is all cards!
        switch (name) {
            case "Тарна":
                return new Card(0, "Тарна", "", 1, 0, 0, 0, "ТАП:4 Взять карт 1.", 0, 28);
            case "Рэйвенкар":
                return new Card(0, name, "", 5, 0, 0, 0, "ТАП:4 Ранить героя противника на 2. Излечить вашего героя на 2.", 0, 24);
            case "Бьорнбон":
                return new Card(0, name, "", 3, 0, 0, 0, "ТАП:0 Получить щит ББ.", 0, 30);
            case "Тиша":
                return new Card(0, "Тиша", "", 1, 0, 0, 1, "ТАПТ:2 Отравить+ выбранное существо на 1.", 0, 26);
            case "Свирепый резак":
                return new Card(0, name, "", 2, 0, 0, 1, "ТАПТ:2 Выбранное существо получает 'Опыт в атаке, Рывок'.", 0, 28);
            case "Эндор Флем":
                return new Card(0, name, "", 2, 0, 0, 7, "ТАПТ:3 Ранить выбранное существо на 1, Взять карт 1.", 0, 26);
            case "Руах":
                return new Card(0, name, "", 2, 0, 0, 1, "ТАПТ:1 Ранить на половину жизней выбранное существо.", 0, 25);
            case "Раскат грома":
                return new Card(1, "Раскат грома", "", 3, 1, 1, 0, "Ранить выбранное существо на 3.", 0, 0);
            case "Выброс силы":
                return new Card(2, name, "", 3, 1, 1, 0, "Ранить выбранное существо на 5.", 0, 0);
            case "Неудача":
                return new Card(2, name, "", 5, 1, 1, 0, "Ранить на остаток выбранное существо и своего героя на столько же.", 0, 0);
            case "Возрождение":
                return new Card(1, name, "", 5, 1, 0, 0, "Раскопать (2,0, ,0,0).", 0, 0);
            case "Гьерхор":
                return new Card(1, "Гьерхор", "Йордлинг", 3, 2, 0, 0, "", 2, 2);
            case "Алчущие крови":
                return new Card(2, name, "Слуа", 5, 2, 10, 0, "Направленный удар. Наймт: Жажда 1.", 3, 3);
            case "Змееуст":
                return new Card(1, name, "Слуа", 5, 2, 10, 0, "Защита от заклинаний. Наймт: Жажда 2.", 3, 2);
            case "Лики судьбы":
                return new Card(3, name, "Пустой", 6, 2, 0, 0, "Найм: Лики-абилка.", 2, 3);
            case "Найтин":
                return new Card(2, "Найтин", "", 6, 2, 0, 0, "Направленный удар. Рывок.", 2, 2);
            case "Кригторн":
                return new Card(2, "Кригторн", "", 3, 2, 0, 0, "Первый удар. Рывок.", 2, 1);
            case "Гном":
                return new Card(2, name, "Гном", 3, 2, 0, 0, "", 3, 3);
            case "Секретное существо":
                return new Card(2, name, "Гном", 3, 2, 0, 0, "Рывок", 3, 3);
            case "Гном-легионер":
                return new Card(4, name, "Гном", 3, 2, 0, 0, "Направленный удар. Рывок.", 3, 5);
            case "Гном-смертник":
                return new Card(3, name, "Гном", 3, 2, 0, 0, "Защита от заклинаний. Рывок.", 3, 4);
            case "Цепной пес":
                return new Card(1, name, "Зверь", 2, 2, 0, 0, "Орда. Статичный эффект. Получает за каждого другого Цепного пса рывок и +1 к удару.", 1, 2);
            case "Амбрадор":
                return new Card(1, name, "Зверь", 6, 2, 12, 0, "В начале вашего хода: Верните выбранное существо в руку его владельца.", 4, 3);
            case "Трюкач":
                return new Card(1, name, "", 6, 2, 0, 0, "", 4, 5);
            case "Поглощение души":
                return new Card(3, "Поглощение душ", "", 5, 1, 2, 0, "Ранить выбранного героя на 3. Излечить вашего героя на 3.", 0, 0);
            case "Эльф-дозорный":
                return new Card(4, "Эльф-дозорный", "", 4, 2, 0, 0, "Найм: Возьмите карт 1.", 2, 5);
            case "Послушник":
                return new Card(5, "Послушник", "Лингунг", 3, 2, 1, 0, "Наймт: Выстрел по существу на 4.", 2, 3);
            case "Гном-лучник":
                return new Card(3, "Гном-лучник", "Гном", 3, 2, 3, 0, "Защита от выстрелов. Наймт: Выстрел на 2.", 2, 3);
            case "Лучник Захры":
                return new Card(4, "Лучник Захры", "Орк", 2, 2, 3, 0, "Защита от заклинаний. Наймт: Выстрел на 2.", 4, 2);
            case "Жрец клана":
                return new Card(2, name, "Орк", 2, 2, 0, 0, "Рывок.", 3, 2);
            case "Молодой орк":
                return new Card(1, name, "Орк", 2, 2, 0, 0, "", 3, 1);
            case "Орк-провокатор":
                return new Card(1, name, "Орк", 2, 2, 0, 0, "Рывок.", 2, 1);
            case "Цверг-заклинатель":
                return new Card(3, name, "Гном", 3, 2, 0, 0, "Защита от заклинаний. Защита от выстрелов. Защита от отравления.", 3, 3);
            case "Верцверг":
                return new Card(4, name, "Гном", 3, 2, 1, 0, "Направленный удар. Наймт: Получает к атаке + 3.", 2, 4);
            case "Цепная молния":
                return new Card(6, "Цепная молния", "", 3, 1, 0, 0, "Ранить каждое существо противника на 3.", 0, 0);
            case "Волна огня"://Fix it
                return new Card(3, "Волна огня", "", 2, 1, 0, 0, "Ранить каждое существо на 5.", 0, 0);
            case "Чешуя дракона":
                return new Card(2, "Чешуя дракона", "", 4, 1, 0, 0, "Получите * 1.", 0, 0);
            case "Выслеживание":
                return new Card(0, "Выслеживание", "", 4, 1, 0, 0, "Получите до конца хода * 2.", 0, 0);
            case "Фиал порчи":
                return new Card(2, "Фиал порчи", "", 1, 1, 1, 0, "Отравить выбранное существо на 2.", 0, 0);
            case "Глашатай пустоты":
                return new Card(1, "Глашатай пустоты", "Пустой", 6, 2, 0, 0, "Уникальность. Не получает ран.", 0, 1);
            case "Мастер теней":
                return new Card(1, name, "Наемник", 6, 2, 0, 0, "Найм: Посмотрите топдек противника, можете положить его на кладбище.", 2, 1);
            case "Велит":
                return new Card(2, "Велит", "", 2, 2, 0, 3, "ТАПТ: Выстрел на 1.", 1, 3);
            case "Пуф":
                return new Card(2, name, "Гном", 3, 2, 0, 3, "ТАПТ: Выстрел на 1.", 2, 3);
            case "Кьелэрн":
                return new Card(1, "Кьелэрн", "", 6, 2, 0, 0, "Уникальность. Рывок. ТАП: Получите до конца хода * 1.", 0, 1);
            case "Агент Разана":
                return new Card(2, "Агент Разана", "", 1, 2, 4, 0, "Наймт: Отравить выбранное существо на 1.", 1, 2);
            case "Скованный еретик":
                return new Card(1, "Скованный еретик", "", 5, 2, 0, 0, "Найм: Закрыться.", 3, 2);
            case "Вэлла":
                return new Card(3, "Вэлла", "", 4, 2, 3, 0, "Наймт: Излечить выбранное существо или героя на 2.", 3, 4);
            case "Рыцарь Туллена":
                return new Card(6, "Рыцарь Туллена", "", 2, 2, 0, 0, "Броня 3.", 6, 3);
            case "Орк-лучник":
                return new Card(1, name, "", 2, 2, 3, 0, "Гнев. Наймт: Выстрел на 1.", 1, 1);
            case "Безумие":
                return new Card(3, name, "", 1, 1, 1, 0, "Нанести урон выбранному существу, равный его удару.", 0, 0);
            case "Зельеварение":
                return new Card(1, name, "", 1, 1, 1, 0, "Верните выбранное существо в руку его владельца.", 0, 0);
            case "Дахут":
                return new Card(3, name, "", 1, 2, 1, 0, "Наймт: Верните выбранное существо в руку его владельца.", 2, 3);
            case "Забира":
                return new Card(2, "Забира", "", 1, 2, 0, 0, "Если выбрана целью заклинание - погибает.", 3, 4);
            case "Волнорез":
                return new Card(3, name, "", 1, 2, 0, 0, "Если выбрана целью заклинание - погибает.", 4, 5);
            case "Десница Архааля":
                return new Card(4, name, "", 1, 2, 1, 0, "Опыт в защите. Наймт: Уничтожьте отравленное существо.", 1, 4);
            case "Нойта":
                return new Card(1, name, "Йордлинг", 1, 2, 1, 0, "Наймт: Ранить существо без ран на 3.", 1, 1);
            case "Орк-мародер":
                return new Card(5, name, "", 2, 2, 0, 0, "Опыт в атаке. Первый удар. Рывок.", 5, 2);
            case "Менгир Каррефура":
                return new Card(3, name, "", 1, 2, 0, 1, "ТАПТ: Отравить+ выбранное существо на 1.", 0, 10);
            case "Рыцарь реки":
                return new Card(5, name, "", 1, 2, 1, 0, "Наймт: Выбранное существо не может атаковать и выступать защитником до конца следующего хода.", 4, 6);
            case "Поиск кладов":
                return new Card(6, name, "", 1, 1, 0, 0, "Взять карт 4.", 0, 0);
            case "Прозрение":
                return new Card(2, name, "", 1, 1, 0, 0, "Взять карт 1. Если у соперника больше существ, чем у вас, взять еще карт 1.", 0, 0);
            case "Плащ Исхара":
                return new Card(1, name, "Броня", 1, 3, 0, 0, "", 0, 6);
            case "Богарт":
                return new Card(4, name, "", 6, 2, 0, 0, "Уникальность. Найм: Каждое другое существо погибает в конце хода противника.", 2, 7);
            case "Полевица":
                return new Card(4, name, "", 1, 2, 0, 0, "Гибель: Взять карт 2.", 2, 3);
            case "Смайта":
                return new Card(4, name, "", 6, 2, 3, 0, "Гибельт: Ранить выбранное существо или героя на 2.", 4, 3);
            case "Вестник смерти":
                return new Card(1, name, "Слуа", 5, 2, 99, 0, "Гибельт: Сбросьте карту.", 3, 3);
            case "Падальщик пустоши"://fix
                return new Card(1, name, "Зверь", 6, 2, 4, 0, "При гибели в ваш ход другого вашего существа: Ранить выбранное существо на 2.", 1, 2);
            case "Ядовитое пламя":
                return new Card(0, name, "", 1, 1, 1, 0, "Доплатите Х *. Ранить выбранное существо на ХХХ.", 0, 0);
            case "Вольный воитель":
                return new Card(0, name, "", 6, 2, 0, 0, "Доплатите Х *. Найм: Получает к характеристикам + ХХХ.", 0, 0);
            case "Шар тины":
                return new Card(2, name, "", 1, 1, 0, 0, "Поиск (0,1, ,0,0).", 0, 0);
            case "Карта сокровищ":
                return new Card(2, name, "", 6, 1, 0, 0, "Поиск (0,6, ,0,0).", 0, 0);
            case "Шар молний":
                return new Card(2, name, "", 3, 1, 0, 0, "Поиск (0,3, ,0,0).", 0, 0);
            case "Гном-кузнец":
                return new Card(3, name, "Гном", 1, 2, 0, 0, "Найм: Поиск (3,0, ,0,0).", 1, 4);
            case "Гном-кладоискатель":
                return new Card(5, name, "Гном", 3, 2, 0, 0, "Броня 1. Найм: Поиск (2,0,Гном,2,0).", 5, 4);
            case "Шаман племени ворона":
                return new Card(1, name, "Наемник", 6, 2, 0, 0, "Найм: Поиск (2,0, ,0,2).", 1, 1);
            case "Дух Эллиона":
                return new Card(1, name, "Дух", 6, 2, 0, 0, "Найм: Потеряйте * 1.", 3, 4);
            case "Рунопевец":
                return new Card(3, name, "Гном", 3, 2, 0, 0, "Статичный эффект.", 3, 3);
            case "Гном-каратель":
                return new Card(4, name, "Гном", 3, 2, 0, 3, "Броня 2. ТАПТ: Ранить выбранное существо или героя на 2.", 1, 3);
            case "Тан гномов":
                return new Card(6, name, "Гном", 3, 2, 0, 0, "Броня 2. Статичный эффект.", 5, 4);
            case "Безумный охотник":
                return new Card(5, name, "", 6, 2, 0, 0, "Найм: Получает +Х к удару и Броню Х, где Х - число других ваших существ.", 4, 4);
            case "Браслет подчинения":
                return new Card(3, name, "Амулет", 1, 3, 0, 0, "", 0, 0);
            case "Молот прародителя":
                return new Card(2, name, "Оружие", 3, 3, 0, 1, "ТАПТ: Выбранное существо до конца хода получает к атаке + 2.", 0, 0);
            case "Аккения":
                return new Card(4, name, "Событие", 2, 4, 0, 0, "Статичный эффект.", 0, 0);
            case "Пустошь Тул-Багара":
                return new Card(1, name, "Событие", 5, 4, 0, 0, "Статичный эффект.", 0, 0);
            default:
                System.out.println("Ошибка - Неопознанная карта:" + name);
                return null;
        }
    }

    static void ability(Gamer owner, Card _who, Player _whis, Creature _cr, Player _pl, String txt) {
        //Super function! Do all what do cards text!
        //Which Card player(_who), who player(_whis), on what creature(_cr, may null), on what player(_pl, may null), text to play(txt)
        if (txt.contains("Закрыться.")) {//Only here - _cr=_who to get access to creature
            _cr.tapCreature();
            owner.printToView(0, _cr.name + " закрывается.");
        }
        if (txt.contains("Посмотрите топдек противника, можете положить его на кладбище")) {
            //Card c = new Card(_whis.owner.opponent.player.deck.topDeck());

            _whis.owner.opponent.player.graveyard.add(_whis.owner.opponent.player.deck.topDeck());
            _whis.owner.opponent.player.deck.removeTopDeck();
            owner.printToView(0, _cr.name + " сбрасывает верхнюю карту с колоды "+ _whis.owner.opponent.player.playerName);
        }
        if (txt.contains("Получить щит ББ.")) {//Only here - _cr=_who to get access to creature
            _whis.bbshield = true;
            owner.printToView(0, "Бьорнбон активирует свой щит.");
        }
        if (txt.contains("Лики-абилка.")) {//Only for player, who called it.
            if (_whis.playerName.equals(owner.name)) {
                ArrayList<Card> a = new ArrayList<>();
                if (owner.player.deck.topDeck()!=null ) a.add(owner.player.deck.topDeck());
                if (owner.player.deck.topDeck(2)!=null ) a.add(owner.player.deck.topDeck(2));
                if (owner.player.deck.topDeck(3)!=null ) a.add(owner.player.deck.topDeck(3));


                for (Card c : a) {
                    owner.printToView(0,"Лики показывают "+c.name);

                    if (c.cost <= 1 && c.type==2) {
                        Creature cr = new Creature(c, owner.player);
                        owner.board.addCreatureToBoard(cr,owner.player);
                        owner.gameQueue.push(new GameQueue.QueueEvent("Summon",cr,0));
                        owner.player.deck.cards.remove(c);
                    }
                }
                owner.player.deck.suffleDeck(owner.sufflingConst);
               // main.repaint();
            } else {
                ArrayList<Card> a = new ArrayList<>();
                if (owner.player.deck.topDeck()!=null ) a.add(owner.player.deck.topDeck());
                if (owner.player.deck.topDeck(2)!=null ) a.add(owner.player.deck.topDeck(2));
                if (owner.player.deck.topDeck(3)!=null ) a.add(owner.player.deck.topDeck(3));
                for (Card c : a) {
                    if (c.cost <= 1 && c.type==2) {
                        Creature cr = new Creature(c, owner.player);
                        owner.board.addCreatureToBoard(cr, owner.player);
                        owner.gameQueue.push(new GameQueue.QueueEvent("Summon",cr,0));
                        owner.player.deck.cards.remove(c);
                        owner.printToView(0,"Лики вызывают "+c.name);
                    }
                }
                owner.player.deck.suffleDeck(owner.sufflingConst);
            }
        }
        if (txt.contains("Поиск (")) {//Поиск (type,color,creatureType,cost,costEx)
            if (_whis.playerName.equals(owner.name)) {
                ArrayList<String> parameter = MyFunction.getTextBetween(txt);
                owner.choiceXtype = Integer.parseInt(parameter.get(0));
                owner.choiceXcolor = Integer.parseInt(parameter.get(1));
                owner.choiceXcreatureType = parameter.get(2);
                if (owner.choiceXcreatureType.equals(" ")) owner.choiceXcreatureType="";
                owner.choiceXcost =  Integer.parseInt(parameter.get(3));
                owner.choiceXcostExactly = Integer.parseInt(parameter.get(4));
                owner.setPlayerGameStatus(MyFunction.PlayerStatus.digX);
                owner.sendChoiceSearch(false,_whis.name+" ищет в колоде.");
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
        }
        else if (txt.contains("Раскопать (")) {//Only for player, who called it.
            if (_whis.playerName.equals(owner.player.playerName)) {
                ArrayList<String> parameter = MyFunction.getTextBetween(txt);
                owner.choiceXtype = Integer.parseInt(parameter.get(0));
                owner.choiceXcolor = Integer.parseInt(parameter.get(1));
                owner.choiceXcreatureType = parameter.get(2);
                if (owner.choiceXcreatureType.equals(" ")) owner.choiceXcreatureType="";
                owner.choiceXcost =  Integer.parseInt(parameter.get(3));
                owner.choiceXcostExactly = Integer.parseInt(parameter.get(4));
                owner.setPlayerGameStatus(MyFunction.PlayerStatus.digX);
                owner.sendChoiceSearch(true,_whis.name+" ищет в колоде.");
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
        }
        if (txt.contains("Получает к характеристикам + ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Получает к характеристикам + ");
            _cr.effects.bonusTougness += dmg;
            _cr.effects.bonusPower += dmg;
            owner.printToView(0, _cr.name + " получает + " + dmg + " к характеристикам.");
        }
        if (txt.contains("Выбранное существо до конца хода получает к атаке + ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Выбранное существо до конца хода получает к атаке + ");
            _cr.effects.takeBonusPowerUEOT(dmg);
        }
        if (txt.contains("Получает к броне + ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Получает к броне + ");
            _cr.effects.bonusArmor += dmg;
        }
        if (txt.contains("Получает +Х к удару и Броню Х, где Х - число других ваших существ.")) {
            int dmg = _cr.owner.creatures.size() - 1;
            if (dmg > 0) {
                _cr.effects.bonusPower += dmg;
                _cr.effects.bonusArmor += dmg;
                _cr.currentArmor += dmg;
                owner.printToView(0, _cr.name + " получает +" + dmg + " к удару и броне.");
            }
        }
        if (txt.contains("Получает к атаке + ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Получает к атаке + ");
            _cr.effects.bonusPower += dmg;
            owner.printToView(0, _cr.name + " получает +" + dmg + " к атаке.");
        }
        if (txt.contains("Излечить выбранное существо или героя на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Излечить выбранное существо или героя на ");
            if (_cr != null) {
                owner.printToView(0, _who.name + " излечивает " + _cr.name + " на " + dmg + ".");
                _cr.heal(dmg);
            } else {
                owner.printToView(0, _who.name + " излечивает " + _pl.name + " на " + dmg + ".");
                _pl.heal(dmg);
            }
        }
        if (txt.contains("Ранить на половину жизней выбранное существо")) {
            if (_cr != null) {
                int dmg=(_cr.getTougness()-_cr.damage)/2;
                owner.printToView(0, _who.name + " ранит " + _cr.name + " на " + dmg + ".");
                _cr.takeDamage(dmg, Creature.DamageSource.ability);
            }
        }
        if (txt.contains("Ранить выбранное существо или героя на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Ранить выбранное существо или героя на ");
            if (_cr != null) {
                owner.printToView(0, _who.name + " ранит " + _cr.name + " на " + dmg + ".");
                _cr.takeDamage(dmg, Creature.DamageSource.ability);
            } else {
                owner.printToView(0, _who.name + " ранит " + _pl.name + " на " + dmg + ".");
                _pl.takeDamage(dmg);
            }
        }
        if (txt.contains("Жажда ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Жажда ");
            if (_cr != null) {
                owner.printToView(0, _who.name + " жаждит " + _cr.name + " на " + dmg + ".");
                _cr.takeDamage(dmg, Creature.DamageSource.ability);
            } else {
                owner.printToView(0, _who.name + " жаждит " + _pl.name + " на " + dmg + ".");
                _pl.takeDamage(dmg);
            }
        }
        if (txt.contains("Ранить выбранного героя на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Ранить выбранного героя на ");
            owner.printToView(0, _pl.playerName + " получил " + dmg + " урона.");
            _pl.takeDamage(dmg);

        }
        if (txt.contains("Ранить героя противника на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Ранить выбранного героя на ");
            owner.printToView(0, _pl.playerName + " получил " + dmg + " урона.");
            _pl.takeDamage(dmg);

        }
        if (txt.contains("Уничтожьте отравленное существо.")) {
            if (_cr.effects.poison > 0) {
                owner.printToView(0, _who.name + " уничтожает " + _cr.name + ".");
                _cr.die();
            }
        }
        if (txt.contains("Ранить существо без ран на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Ранить существо без ран на ");
            if (_cr != null && _cr.damage == 0) {
                owner.printToView(0, _cr.name + " получил " + dmg + " урона.");
                _cr.takeDamage(dmg, Creature.DamageSource.ability);
            }
        }
        if (txt.contains("Ранить выбранное существо на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Ранить выбранное существо на ");
            owner.printToView(0, _cr.name + " получил " + dmg + " урона.");
            _cr.takeDamage(dmg, Creature.DamageSource.ability);
        }
        if (txt.contains("Ранить на остаток выбранное существо и своего героя на столько же")) {
            int dmg = _cr.getTougness()-_cr.damage;
            owner.printToView(0, _cr.name + " получил " + dmg + " урона.");
            owner.printToView(0, _whis.name + " получил " + dmg + " урона.");
            _cr.takeDamage(dmg, Creature.DamageSource.ability);
            _whis.takeDamage(dmg);
        }
        if (txt.contains("Выбранное существо не может атаковать и выступать защитником до конца следующего хода.")) {
            _cr.effects.cantAttackOrBlock = 2;
            owner.printToView(0, _cr.name + " не может атаковать и выступать защитником до конца следующего хода.");
        }
        if (txt.contains("Нанести урон выбранному существу, равный его удару.")) {
            int dmg = _cr.getPower();
            owner.printToView(0, _cr.name + " получил " + dmg + " урона.");
            _cr.takeDamage(dmg, Creature.DamageSource.spell);
        }
        if (txt.contains("Выбранное существо получает 'Опыт в атаке, Рывок'")) {
            owner.printToView(0, _whis.name+" дает " + _cr.name + " опыт в атаке и рывок.");
            _cr.effects.additionalText+="Опыт в атаке. Рывок.";
        }
        if (txt.contains("Верните выбранное существо в руку его владельца.")) {
            _cr.returnToHand();
            owner.printToView(0, _cr.name + " возвращается в руку владельца.");
        }
        if (txt.contains("Отравить+ выбранное существо на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Отравить+ выбранное существо на ");
            if (_cr.effects.poison != 0) {
                _cr.effects.takePoison(_cr.effects.poison+dmg);
            } else {
                if (_cr.effects.poison <= dmg)
                    _cr.effects.takePoison(dmg);
            }
        }
        if (txt.contains("Отравить выбранное существо на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Отравить выбранное существо на ");
            _cr.effects.takePoison(dmg);
        }
        if (txt.contains(("Излечить вашего героя на "))) {
            int dmg = MyFunction.getNumericAfterText(txt, "Излечить вашего героя на ");
            _whis.heal(dmg);
            owner.printToView(0, _whis.playerName + " излечил " + dmg + " урона.");
        }
        if (txt.contains(("Получите * "))) {
            int dmg = MyFunction.getNumericAfterText(txt, "Получите * ");
            _whis.untappedCoin += dmg;
            _whis.totalCoin += dmg;
            owner.printToView(0, _whis.playerName + " получил " + dmg + " монет.");
        }
        if (txt.contains(("Потеряйте * "))) {
            int dmg = MyFunction.getNumericAfterText(txt, "Потеряйте * ");
            int tmp = dmg;
            dmg -= _whis.temporaryCoin;
            _whis.temporaryCoin -= tmp;
            if (dmg < 0) dmg = 0;
            if (_whis.temporaryCoin< 0) _whis.temporaryCoin = 0;
            _whis.totalCoin -= dmg;
            if (_whis.untappedCoin > _whis.totalCoin) _whis.untappedCoin = _whis.totalCoin;
            owner.printToView(0, _whis.playerName + " потерял " + dmg + " монет.");
        }
        if (txt.contains(("Получите до конца хода * "))) {
            int dmg = MyFunction.getNumericAfterText(txt, "Получите до конца хода * ");
            _whis.untappedCoin += dmg;
            _whis.totalCoin += dmg;
            _whis.temporaryCoin += dmg;
            owner.printToView(0, _whis.playerName + " получил " + dmg + " монет до конца хода.");
        }
        if (txt.contains(("Ранить каждое существо противника на "))) {
            int dmg = MyFunction.getNumericAfterText(txt, "Ранить каждое существо противника на ");

            ListIterator<Creature> temp = _whis.owner.opponent.player.creatures.listIterator();
            while (temp.hasNext()) {
                Creature tmp = temp.next();
                tmp.takeDamage(dmg, Creature.DamageSource.ability);
            }
            owner.printToView(0, _who.name + " ранит всех существ противника на " + dmg + ".");
        }
        if (txt.contains(("Ранить каждое существо на "))) {
            int dmg = MyFunction.getNumericAfterText(txt, "Ранить каждое существо на ");

            ListIterator<Creature> temp = _whis.owner.opponent.player.creatures.listIterator();
            while (temp.hasNext()) {
                Creature tmp = temp.next();
                tmp.takeDamage(dmg, Creature.DamageSource.ability);
            }
            ListIterator<Creature> temp2 = _whis.creatures.listIterator();
            while (temp2.hasNext()) {
                Creature tmp = temp2.next();
                tmp.takeDamage(dmg, Creature.DamageSource.ability);
            }
            owner.printToView(0, _who.name + " ранит всех существ на " + dmg + ".");
        }
        if (txt.contains(("Каждое другое существо погибает в конце хода противника."))) {//TODO Fix it with deathrattle
            for (int i = _whis.owner.opponent.player.creatures.size() - 1; i >= 0; i--) {
                _whis.owner.opponent.player.creatures.get(i).effects.takeTurnToDie(2);
            }
            for (int i = _whis.creatures.size() - 1; i >= 0; i--) {
                if (!_whis.creatures.get(i).name.equals("Богарт"))
                    _whis.creatures.get(i).effects.takeTurnToDie(2);
            }
            owner.printToView(0, _who.name + " чумит весь стол!");
        }
        if (txt.contains("Взять карт ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Взять карт ");
            owner.printToView(0, _who.name + " берет " + dmg + " карт.");
            for (int i = 0; i < dmg; i++)
                _whis.drawCard();
        }
        if (txt.contains("Если у соперника больше существ, чем у вас, взять еще карт ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Если у соперника больше существ, чем у вас, взять еще карт ");
            int n1 = _whis.creatures.size();
            int n2 = _whis.owner.opponent.player.creatures.size();
            if (n1 < n2) {
                owner.printToView(0, _who.name + " берет " + dmg + " карт.");
                for (int i = 0; i < dmg; i++)
                    _whis.drawCard();
            }
        }
        //target
        if (txt.contains("Выстрел по существу на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Выстрел по существу на ");
            owner.printToView(0, _who.name + " стреляет на " + dmg + " по " + _cr.name);
            if (!_cr.text.contains("Защита от выстрелов."))
                _cr.takeDamage(dmg, Creature.DamageSource.scoot, _who.haveRage());
            else {
                owner.printToView(0, "У " + _cr.name + " защита от выстрелов.");
            }
        }
        if (txt.contains("Выстрел на ")) {
            int dmg = MyFunction.getNumericAfterText(txt, "Выстрел на ");
            if (_cr != null) {
                owner.printToView(0, _who.name + " стреляет на " + dmg + " по " + _cr.name);
                if (!_cr.text.contains("Защита от выстрелов."))
                    _cr.takeDamage(dmg, Creature.DamageSource.scoot, _who.haveRage());
                else {
                    owner.printToView(0, "У " + _cr.name + " защита от выстрелов.");
                }
            } else {
                owner.printToView(0, _who.name + " стреляет на " + dmg + " по " + _pl.name);
                _pl.takeDamage(dmg);
            }
        }

        // Main.gameQueue.responseAllQueue();
    }

    boolean haveRage() {
        return (text.contains("Гнев."));
    }

    int getCost(Player pl){
        int effectiveCost = cost;
        //Gnome cost less
        if (creatureType.equals("Гном")) {
            int runopevecFounded = 0;
            for (int i = 0; i < pl.creatures.size(); i++) {
                if (pl.creatures.get(i).name.equals("Рунопевец")) runopevecFounded++;
            }
            effectiveCost -= runopevecFounded;
        }

        if (name.equals("Трюкач")){
            effectiveCost+=pl.cardInHand.size()-1;
        }

        return effectiveCost;
    }
}
