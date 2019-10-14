package main;

import reasons.BashReason;
import reasons.BruteReason;
import reasons.LogicReason;
import reasons.Reason;
import java.util.ArrayList;

public class Cell {
    //Possible additions: a CheckMinBombs() function that further optimizes brute force searching. would need a boolean on cells being brute forced
    // to track if the cell around has been reached by brute force recursion.
    private SolverMain parent; //So the cell can interact with other cells on the board
    private ArrayList<Cell> cellsAround, cellsAroundURUF; //Utility groupings to prevent messy code
    private int numBombs, row, col, bombsLeft, timesGhostBomb, brutePermutations; //a GhostBomb is basically a question mark while its guessing
    private boolean isFlagged, isBomb, isRevealed, doneWith, inBruteGroup, isGhostBomb; //inBruteGroup is vital for correct formation of brute groups, doneWith ensures a step actually changes the board
    private double bruteOdds; //odds that this square is a bomb with the information I have
    private ArrayList<Group> groups; //see groups class
    private BruteGroup bruteGroup; //used to separate cells so I am not brute forcing the entire board

    public Cell(int row, int col, SolverMain parent){ //primary constructor
        this.row = row;
        this.col = col;
        this.parent = parent;
        bruteOdds = -1; //default for a cell I do not need to perform brute force calculations on
        cellsAroundURUF = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public Cell(double bruteOdds){ //used for determining most and least likely to be a bomb when guessing
        this.bruteOdds = bruteOdds;
    }

    public void makeBomb(){
        isBomb = true;
        doneWith = true;
    }

    public void addSurround(){ //loop through cells within 1 col/row and add them to cells around
        cellsAround = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if(!(i == 0 && j == 0)) {
                    try {
                        cellsAround.add(parent.getBoard()[row + i][col + j]);
                    }catch(IndexOutOfBoundsException e){
                        //For edge cases, because this is the cleanest way I could think of. (laziest?)
                    }
                }
            }
        }
    }

    public void calcCellsAroundURUF(){ //calculate the cells around that are neither revealed nor flagged
        if(!doneWith) {
            cellsAroundURUF.clear();
            for (Cell c : cellsAround) {
                if (!c.isFlagged() && !c.isRevealed()) {
                    cellsAroundURUF.add(c);
                }
            }
            if(cellsAroundURUF.size() == 0){
                doneWith = true;
            }
        }
    }

    public void calcBombs(){ //calculate the number of bombs around this cell, if it isn't a bomb
        if(!isBomb) {
            for (Cell c : cellsAround) {
                if (c.isBomb()) {
                    numBombs++;
                }
            }
            if (numBombs == 0) { //for minimizing the work the computer does later
                doneWith = true;
            }
            bombsLeft = numBombs;
        }
    }

    public void calcBombsLeft(){ //calculate the effective number of bombs left, useful in logic
        if(!isBomb) {
            int numFlagged = 0;
            for (Cell c : cellsAround) {
                if (c.isFlagged()) {
                    numFlagged++;
                }
            }
            bombsLeft = numBombs - numFlagged;
        }
    }

    public void reveal() { //reveal the cell i.e. "click"
        if(!isRevealed && !isFlagged) {
            isRevealed = true;
            if (isBomb) {
                parent.endGame();
            } else if (numBombs == 0) { //if its clear, reveal all cells around -> recursively opens everything needed
                for (Cell c : cellsAround) {
                    if (!c.isRevealed()) {
                        c.reveal();
                    }
                }
            }
        }
    }

    public void flag(){ //flip flag status
        if(!isFlagged && !isRevealed) {
            isFlagged = true;
            doneWith = true;
        }else if(isFlagged){
            isFlagged = false;
            doneWith = false;
        }
    }



    public boolean checkExclusives(){ //if unrevealed cells around equals bomb value, flag everything (simple logic)
        calcCellsAroundURUF();
        if(!doneWith && isRevealed){
            calcBombsLeft();
            if(cellsAroundURUF.size() == bombsLeft){
                LogicReason reason = new LogicReason(this);
                for(Cell c: cellsAroundURUF){
                    c.flag();
                    reason.addCell(c);
                }
                parent.setMostRecentReason(reason);
                doneWith = true;
                return true;
            }
        }
        return false;
    }

    public boolean checkFill(boolean isBot){ //if correct number of flags reached, reveal all others. Make reason if algorithm did it.
        calcCellsAroundURUF();
        if(!doneWith && isRevealed) {
            calcBombsLeft();
            if (bombsLeft == 0 && cellsAroundURUF.size() > 0) {
                if(isBot) {
                    LogicReason reason = new LogicReason(this);
                    parent.setMostRecentReason(reason);
                    for (Cell c : cellsAroundURUF) {
                        reason.addCell(c);
                    }
                }
                for (Cell c : cellsAroundURUF) {
                    c.reveal();
                }
                doneWith = true;
                return true;
            }
        }
        return false;
    }

    public void clearGroups(){ //clearing groups for advanced logic
        groups.clear();
    }

    public void calcGroups(){ //add surrounding cells to a group with the bomb value and this as parent cell
        calcBombsLeft();
        if(bombsLeft != 0 && isRevealed) {
            calcCellsAroundURUF();
            Group bashGroup = new Group(bombsLeft, this);
            for (Cell c : cellsAroundURUF) {
                bashGroup.addCell(c);
                c.addGroup(bashGroup);
            }
        }
    }

    public void addGroup(Group newGroup){ //give unrevealed cells what groups they're in
        groups.add(newGroup);
    }

    public boolean bashGroups() { //I highly suggest running the program to see what this logic does first.
        if (bombsLeft != 0 && isRevealed && !doneWith) {
            for (Cell c : cellsAroundURUF) { //unrevealed unflagged cells
                for (Group s : c.getGroups()) { //their groups
                    ArrayList<Cell> currentIntersection = s.getIntersection(cellsAroundURUF); //find the intersection of the group and the cells around current cell
                    if (currentIntersection.size() > 1 && currentIntersection.size() < cellsAroundURUF.size()) { //if there is an intersection
                        int maxBombs;
                        if (s.getBombValue() > currentIntersection.size()) { //max bombs in the intersection
                            maxBombs = currentIntersection.size();
                        } else {
                            maxBombs = s.getBombValue();
                        }
                        int minBombs = s.getBombValue() - (s.getCells().size() - currentIntersection.size()); //min bombs in the intersection
                        if (minBombs < 0) {
                            minBombs = 0;
                        }
                        if (bombsLeft - maxBombs == cellsAroundURUF.size() - currentIntersection.size()) { //even if intersection maxed with bombs, remaining cells still must be bombs
                            BashReason reason = new BashReason(this, s.getParent(), currentIntersection, maxBombs);
                            parent.setMostRecentReason(reason);
                            for (Cell currentCell : cellsAroundURUF) {
                                if (!currentIntersection.contains(currentCell)) {
                                    currentCell.flag();
                                    reason.addCell(currentCell);
                                }
                            }
                            return true;
                        } else if (bombsLeft == minBombs) { //even if intersection has least bombs possible, remaining cells would be open
                            BashReason reason = new BashReason(this, s.getParent(), currentIntersection, minBombs);
                            parent.setMostRecentReason(reason);
                            for (Cell currentCell : cellsAroundURUF) {
                                if (!currentIntersection.contains(currentCell)) {
                                    currentCell.reveal();
                                    reason.addCell(currentCell);
                                }
                            }
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }

    public boolean calcBruteGroup(){ //if unrevealed and unflagged add surrounding URUF cells to a brutegroup
        if(!isRevealed && !inBruteGroup && !isFlagged) {
            bruteGroup = new BruteGroup(parent, false);
            for (Cell c : cellsAround) {
                if (c.isRevealed() && c.getCellsAroundURUF().size() > 1) {
                    bruteGroup.fill(this);
                    break;
                }
            }
            if (bruteGroup.getCells().size() > 1) { //if not a lone cell (bascially only walled off cells)
                force(); //try all permutations
                if(bruteGroup.conclude()){ // if something is always a bomb or always not
                    parent.setMostRecentReason(new BruteReason(bruteGroup));
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }


    public void force(){ //recursively trying to try every permutation
        isGhostBomb = true; //theoretically this cell is now a bomb
        boolean maxRules = bruteGroup.checkMaxRules(); //checks if rules have been broken already so computer doesn't waste time. Saves up to 99% of permutations sometimess.
        if(bruteGroup.getCells().indexOf(this) == bruteGroup.getCells().size() - 1 && maxRules){ //if last cell in brutegroup, check if its a working permutation.
            bruteGroup.checkRules();
        }else if(maxRules){ //otherwise keep going down the recursion with the next cell in the bruteGroup
            bruteGroup.getCells().get(bruteGroup.getCells().indexOf(this) + 1).force();
        }
        isGhostBomb = false; //now trying without this cell being a bomb
        if(bruteGroup.getCells().indexOf(this) == bruteGroup.getCells().size() - 1){
            bruteGroup.checkRules();
        }else{
            bruteGroup.getCells().get(bruteGroup.getCells().indexOf(this) + 1).force();
        }
    }

    public void putInBruteGroup(BruteGroup b){ //so cells do not get put in multiple brute groups
        bruteGroup = b;
        inBruteGroup = true;
    }

    public boolean checkGhost(){ //check rules
        int effectiveBombs = 0;
        for(Cell c: cellsAround){
            if(c.isGhostBomb() || c.isFlagged()){
                effectiveBombs++;
            }
        }
            return effectiveBombs == numBombs;
    }

    public boolean checkGhostMax(){ //check if maximum rules have been broken
        int effectiveBombs = 0;
        for(Cell c: cellsAround){
            if(c.isGhostBomb() || c.isFlagged()){
                effectiveBombs++;
            }
        }
        return effectiveBombs <= numBombs;
    }

    public void resetBrute(){ //get ready for the next brute force session
        timesGhostBomb = 0;
        brutePermutations = 0;
        bruteOdds = -1;
        inBruteGroup = false;
    }

    public void incrementBrute(){ //if a working model is found, increment stats for conclusions and guessing
        if(isGhostBomb){
            timesGhostBomb++;
        }
        brutePermutations++;
    }

    public void calcBruteOdds(){
        bruteOdds = (double)timesGhostBomb/(double)brutePermutations;
    }

    public double getBruteOdds() {
        return bruteOdds;
    }

    public double getBrutePermutations() {
        return brutePermutations;
    }

    public double getTimesGhostBomb() {
        return timesGhostBomb;
    }

    public boolean isGhostBomb() {
        return isGhostBomb;
    }

    public int getBombsLeft() {
        return bombsLeft;
    }

    public ArrayList<Cell> getCellsAroundURUF() {
        return cellsAroundURUF;
    }

    public ArrayList<Cell> getCellsAround() {
        return cellsAround;
    }


    public boolean isBomb() {
        return isBomb;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public int getNumBombs() {
        return numBombs;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public BruteGroup getBruteGroup() {
        return bruteGroup;
    }
}
