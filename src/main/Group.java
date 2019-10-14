package main;

import java.util.ArrayList;

public class Group {
    private Cell parent;
    private ArrayList<Cell> cells;
    private int bombValue;

    public Group(int bombValue, Cell parent){//used for advanced logic
        this.bombValue = bombValue;
        this.parent  = parent;
        cells = new ArrayList<>();
    }

    public void addCell(Cell c){
        cells.add(c);
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public ArrayList<Cell> getIntersection(ArrayList<Cell> cellsAround){ //only important function here, gives intersection of two array lists
        ArrayList<Cell> intersection = new ArrayList<>();
        for(Cell aroundCell: cellsAround){
            if(cells.contains(aroundCell)){
                intersection.add(aroundCell);
            }
        }
        return intersection;
    }

    public Cell getParent() {
        return parent;
    }

    public int getBombValue() {
        return bombValue;
    }




}
