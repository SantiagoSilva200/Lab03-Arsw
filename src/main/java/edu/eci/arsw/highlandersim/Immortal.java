package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;
    private int health;
    private int defaultDamageValue;
    private final List<Immortal> immortalsPopulation;
    private final String name;
    private final Random r = new Random(System.currentTimeMillis());
    private boolean activo = true;
    private static final Object pauseLock = new Object();
    private static boolean paused = false;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
    }

    public void run() {
        while (activo) {
            if (immortalsPopulation.size() > 1) {
                Immortal im;
                int myIndex = immortalsPopulation.indexOf(this);
                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = (nextFighterIndex + 1) % immortalsPopulation.size();
                }

                im = immortalsPopulation.get(nextFighterIndex);
                fight(im);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void fight(Immortal i2) {
        Immortal first = this;
        Immortal second = i2;

        if (System.identityHashCode(this) > System.identityHashCode(i2)) {
            first = i2;
            second = this;
        }

        synchronized (first) {
            synchronized (second) {
                if (i2.estaActivo()) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");

                    if (i2.getHealth() <= 0) {
                        i2.morir();
                        updateCallback.processReport(i2 + " ha muerto y ya no pelea.\n");
                    }
                } else {
                    updateCallback.processReport(this + " intentó pelear con " + i2 + ", pero ya está muerto.\n");
                }
            }
        }
    }


    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    public boolean estaActivo() {
        return activo;
    }

    public void morir() {
        activo = false;
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }

    public static void eliminarMuertos(List<Immortal> immortals) {
        immortals.removeIf(i -> !i.estaActivo());
    }



public static void pauseAll() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public static void resumeAll() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

}

