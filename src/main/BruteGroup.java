package main;

import java.util.ArrayList;

public class BruteGroup{
    //"bug" where for a walloff if incorrect flags it just reveals all the cells which is fine by me
    private ArrayList<Cell> cells, checkCells, checkMaxCells;
    private boolean wallOff;
    private SolverMain parent;

    public BruteGroup(SolverMain parent, boolean wallOff){
        cells = new ArrayList<>();
        checkCells = new ArrayList<>();
        checkMaxCells = new ArrayList<>();
        this.parent = parent;
        this.wallOff = wallOff;
    }



    public void fill(Cell c){//fill the brute group
        cells.add(c);
        c.putInBruteGroup(this);
        ArrayList<Cell> newCheck = new ArrayList<>();
        ArrayList<Cell> newCells = new ArrayList<>();
        while(cells.size() < parent.getBruteMaxSize()){
            newCells.clear();
            newCheck.clear();
            for(Cell cell: cells){ //add cells around current cells that are revealed to a tentative list of cells needed for rules to be checked
                for(Cell cellAroundC: cell.getCellsAround()){
                    if(cellAroundC.isRevealed() && !checkCells.contains(cellAroundC) && !newCheck.contains(cellAroundC)){
                        newCheck.add(cellAroundC);
                    }
                }
            }
            if(newCheck.size() == 0){ //if no new cells
                break;
            }
            for(Cell cell: newCheck){ //add all URUF cells around new check cells to the cells about to be forced
                for(Cell cellAroundC: cell.getCellsAroundURUF()){
                    if(!cells.contains(cellAroundC) && !newCells.contains(cellAroundC)){
                        newCells.add(cellAroundC);
                    }
                }
            }
            if(cells.size() + newCells.size() <= parent.getBruteMaxSize()){ //dont add new cells if exceeds the max size of brute group
                cells.addAll(newCells);
                checkCells.addAll(newCheck);
                for(Cell cell: newCells){
                    cell.putInBruteGroup(this);
                }
            }else{
                checkMaxCells.addAll(newCheck); //but still add the newcheck to cells that need to be checked if brute force violates their bomb value
                break;
            }
        }
    }

    public void addCell(Cell c){ //used for walloffs (cells surrounded by bombs)
        if(cells.size() < 13) { //solely to prevent freezing/abusing the program by creating fake walls
            cells.add(c);
            c.putInBruteGroup(this);
        }
    }
    public ArrayList<Cell> getCells() {
        return cells;
    }

    public void checkRules(){ //see if we should increment permutations and such
        boolean workingModel = true;
        if((!wallOff && numGhost() > parent.getBombsLeft()) || (wallOff && numGhost() != parent.getBombsLeft())){ //checking if bombsleft is violated
            workingModel = false;
        }
        if(workingModel) { //checking rules, max rules already checked to get here in the Cell class
            for (Cell c : checkCells) {
                if (!c.checkGhost()) {
                    workingModel = false;
                    break;
                }
            }
        }
        if(workingModel){ //we have a working model!
            for(Cell c: cells){
                c.incrementBrute();
            }
        }
    }

    private int numGhost(){//used for checking if bombs left on the board is violated
        int numGhost = 0;
        for(Cell c: cells){
            if(c.isGhostBomb()){
                numGhost++;
            }
        }
        return numGhost;
    }

    public boolean checkMaxRules(){ //see if bombs/ghost bombs around a cell exceeds the cells value
        for(Cell c: checkCells){
            if(!c.checkGhostMax()){
                return false;
            }
        }
        for(Cell c: checkMaxCells){
            if(!c.checkGhostMax()){
                return false;
            }
        }
        return true;
    }



    public boolean conclude(){ //see if definitive action can be taken
        boolean hasChanged = false;
        for(Cell c: cells){
            c.calcBruteOdds();
            if(c.getTimesGhostBomb() == 0){
                c.reveal();
                hasChanged = true;
            }else if(c.getBrutePermutations() == c.getTimesGhostBomb()){
                c.flag();
                hasChanged = true;
            }
        }
        return hasChanged;
    }


}
