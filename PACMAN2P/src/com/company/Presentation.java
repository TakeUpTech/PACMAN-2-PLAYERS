package com.company;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import java.util.HashMap;

public class Presentation implements EventHandler<KeyEvent> {

    private Game game;
    private Vue vue;
    private Timeline timeline;
    private HashMap<KeyCode, KeyFrame> keyEvents;
    private HashMap<KeyCode, Integer> keyRef;
    private double pacmanSpeed;
    private HashMap<Integer, KeyFrame> keyInProcess;
    private AnimationTimer animationTimer;
    private int timePerSec;

    public Presentation(Game game) {
        this.game = game;
        timePerSec = 0;
        pacmanSpeed = 200;

        keyRef = new HashMap<KeyCode, Integer>(); //touche de clavier - numéro du joueur
        keyEvents = new HashMap<KeyCode, KeyFrame>(); //lien touche de clavier - animation
        keyInProcess = new HashMap<Integer, KeyFrame>(); //animation/joueur en cours d'exécution

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void initializeKey() {
        if (game.getNumberJ()==2){
            keyEvents.put(KeyCode.W, new KeyFrame(Duration.millis(pacmanSpeed), e -> typeDeplacement(0, -1, 1)));
            keyEvents.put(KeyCode.S, new KeyFrame(Duration.millis(pacmanSpeed), e -> typeDeplacement(0, 1, 1)));
            keyEvents.put(KeyCode.A, new KeyFrame(Duration.millis(pacmanSpeed), e -> typeDeplacement(-1, 0, 1)));
            keyEvents.put(KeyCode.D, new KeyFrame(Duration.millis(pacmanSpeed), e -> typeDeplacement(1, 0, 1)));

            keyRef.put(KeyCode.W, 1);
            keyRef.put(KeyCode.S, 1);
            keyRef.put(KeyCode.A, 1);
            keyRef.put(KeyCode.D, 1);
        }
        keyEvents.put(KeyCode.UP, new KeyFrame(Duration.millis(pacmanSpeed), e -> typeDeplacement(0, -1,0)));
        keyEvents.put(KeyCode.DOWN, new KeyFrame(Duration.millis(pacmanSpeed), e -> typeDeplacement(0, 1, 0)));
        keyEvents.put(KeyCode.LEFT, new KeyFrame(Duration.millis(pacmanSpeed), e -> typeDeplacement(-1, 0, 0)));
        keyEvents.put(KeyCode.RIGHT, new KeyFrame(Duration.millis(pacmanSpeed), e -> typeDeplacement(1, 0, 0)));

        keyRef.put(KeyCode.UP, 0);
        keyRef.put(KeyCode.DOWN, 0);
        keyRef.put(KeyCode.LEFT, 0);
        keyRef.put(KeyCode.RIGHT, 0);
    }

    public void handle(KeyEvent event) {
        if (!game.winCondition() && keyEvents.containsKey(event.getCode())){
            timeLineProcess(event.getCode());
        }

        if (event.getCode() == KeyCode.F1){
            vue.fenetreSize();
        }

        if (event.getCode() == KeyCode.ESCAPE){
            System.exit(1);
        }

        if (event.getCode() == KeyCode.P){
            timeline.stop();
            animationTimer.stop();
        }
    }

    // Events and time gestion

    private void timeLineProcess(KeyCode keyCode){
        timeline.stop();    //On stoppe toutes les animations
        timeline.getKeyFrames().remove(keyInProcess.get(keyRef.get(keyCode))); //On retire l'animation associée au joueur qui clique
        keyInProcess.put(keyRef.get(keyCode), keyEvents.get(keyCode)); //On met à jour l'animation en cours
        timeline.getKeyFrames().add(keyInProcess.get(keyRef.get(keyCode))); //On ajoute dans la timeline l'animation mise à jour
        timeline.play(); //Et c'est reparti !
    }

    public void typeDeplacement(int x, int y, int nJ){
        switch(game.walkable(x,y,nJ)){
            case 1:
                vue.updatePlayerVue(nJ, game.movePlayer(x,y,nJ), new int[]{x,y});
                break;
            case 2:
                vue.updatePlayerVue(nJ, game.tpPlayer(nJ, game.tp(getCplayer(nJ), getLplayer(nJ))), new int[]{x,y});
                break;
        }
    }

    public void actualizeGameElements(){
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                timePerSec++;
                if(timePerSec % 20 == 1) {
                    for (int nE = 0; nE < getNumberE(); nE++) {
                        int[] couple = game.findEnnemyDirection(nE);
                        vue.updateEnnemyVue(nE, game.moveEnnemy(couple[0], couple[1], nE));
                    }
                }
                checkoutRoutine();
                if(timePerSec % 60 == 1) {
                    for (int nJ = 0; nJ < getNumberJ(); nJ++) {
                        game.incrementBersekerTime(nJ);
                        game.incrementInvincibleTime(nJ);
                        vue.bersekerAmbiance(nJ, game.getPlayerBerseker(nJ), game.getNumberBerseker());
                    }
                }
            }
        };
        animationTimer.start();
    }

    public void checkoutRoutine(){
        game.collisionPlayer();
        game.collisionEnnemy();
        vue.updatePlayerStat();
        if (game.winCondition()){
            timeline.stop();
            animationTimer.stop();
            vue.showWinner(game.nWinner());
        }
    }

    //Autres méthodes

    public void associerVue(Vue vue){ this.vue = vue; }

    // Send Data to "Game"

    public void setNumberJ(int nJ) {
        game.setNumberJ(nJ);
        initializeKey();
        actualizeGameElements();
    }

    public void setNumberE(int nE) { game.setNumberE(nE); }

    //Get and Update Data through "Game" from "Player" and "Ennemy"

    public int getScore(int nJ){ return game.getScore(nJ); }

    public int getLife(int nJ){ return game.getLife(nJ); }

    public int getCplayer(int nJ){ return game.getCplayer(nJ); }

    public int getLplayer(int nJ){ return game.getLplayer(nJ); }

    public int getLennemy(int id){ return game.getLennemy(id); }

    public int getCennemy(int id){ return game.getCennemy(id); }

    //Get Data from "Game"

    public int getColonnes() { return game.getColonnes(); }

    public int getLignes() { return game.getLignes(); }

    public int statZone(int L, int C){ return game.statZone(L,C); }

    public int setTaillePixel(int hauteur) { return game.setTaillePixel(hauteur); }

    public int getNumberJ() { return game.getNumberJ(); }

    public int getNumberE() { return game.getNumberE(); }
}