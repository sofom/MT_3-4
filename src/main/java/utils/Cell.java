package utils;

import main.java.agents.NavigatorAgent;

public class Cell {
    private int exist;
    private int stench;
    private int breeze;
    private int pit;
    private int wampus;
    private int ok;
    private int gold;
    private int noWay;

    public Cell() {
        this.exist = NavigatorAgent.Cell_STATUS_NO_STATUS;
        this.stench = NavigatorAgent.Cell_STATUS_NO_STATUS;
        this.breeze = NavigatorAgent.Cell_STATUS_NO_STATUS;
        this.pit = NavigatorAgent.Cell_STATUS_NO_STATUS;
        this.wampus = NavigatorAgent.Cell_STATUS_NO_STATUS;
        this.ok = NavigatorAgent.Cell_STATUS_NO_STATUS;
        this.gold = NavigatorAgent.Cell_STATUS_NO_STATUS;
        this.noWay = NavigatorAgent.Cell_STATUS_NO_STATUS;
    }

    public void addEvent(String event_name) {
        switch (event_name) {
            case NavigatorAgent.START:
                break;
            case NavigatorAgent.WAMPUS:
                this.setWampus(NavigatorAgent.Cell_STATUS_TRUE);
                break;
            case NavigatorAgent.PIT:
                this.setPit(NavigatorAgent.Cell_STATUS_TRUE);
                break;
            case NavigatorAgent.BREEZE:
                this.setBreeze(NavigatorAgent.Cell_STATUS_TRUE);
                break;
            case NavigatorAgent.STENCH:
                this.setStench(NavigatorAgent.Cell_STATUS_TRUE);
                break;
            case NavigatorAgent.SCREAM:
                break;
            case NavigatorAgent.GOLD:
                this.setGold(NavigatorAgent.Cell_STATUS_TRUE);
                break;
            case NavigatorAgent.BUMP:
                break;
        }
    }

    public int getExist() {
        return exist;
    }

    public void setExist(int exist) {
        this.exist = exist;
    }

    public int getStench() {
        return stench;
    }

    public void setStench(int stench) {
        this.stench = stench;
    }

    public int getBreeze() {
        return breeze;
    }

    public void setBreeze(int breeze) {
        this.breeze = breeze;
    }

    public int getPit() {
        return pit;
    }

    public void setPit(int pit) {
        this.pit = pit;
    }

    public int getWampus() {
        return wampus;
    }

    public void setWampus(int wampus) {
        this.wampus = wampus;
    }

    public int getOk() {
        return ok;
    }

    public void setOk(int ok) {
        this.ok = ok;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getNoWay() {
        return noWay;
    }

    public void setNoWay(int noWay) {
        this.noWay = noWay;
    }

}
