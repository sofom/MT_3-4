package utils;

import main.java.agents.NavigatorAgent;

import java.util.Hashtable;
import java.util.Set;

public class WumpusWorld {

    private Hashtable<Position, Cell> worldGrid;
    private boolean isWampusAlive;
    private int wampusCellCount;
    private Position wampusCoords;

    public WumpusWorld() {
        worldGrid = new Hashtable<>();
        isWampusAlive = true;
        wampusCellCount = 0;
    }

    public Position getWampusCoords() {
        int xWampusCoord = 0;
        int yWampusCoord = 0;

        Set<Position> keys = worldGrid.keySet();
        for (Position CellPosition : keys) {
            Cell Cell = worldGrid.get(CellPosition);
            if (Cell.getWampus() == NavigatorAgent.Cell_STATUS_POSSIBLE) {
                xWampusCoord += CellPosition.getX();
                yWampusCoord += CellPosition.getY();
            }
        }
        xWampusCoord /= wampusCellCount;
        yWampusCoord /= wampusCellCount;
        this.wampusCoords = new Position(xWampusCoord, yWampusCoord);
        return this.wampusCoords;
    }

    public Hashtable<Position, Cell> getWorldGrid() {
        return worldGrid;
    }


    public boolean isWampusAlive() {
        return isWampusAlive;
    }

    public void setWampusAlive(boolean wampusAlive) {
        isWampusAlive = wampusAlive;
    }

    public int getWampusCellCount() {
        return wampusCellCount;
    }

    public void setWampusCellCount(int wampusCellCount) {
        this.wampusCellCount = wampusCellCount;
    }
}
