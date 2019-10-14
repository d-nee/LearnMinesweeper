package reasons;

import main.BruteGroup;
import main.Cell;

import java.awt.*;
import java.util.ArrayList;

public class BruteReason extends Reason{
    private double permutations, timesBomb;

    public BruteReason(BruteGroup bruteGroup){
        affectedCells.addAll(bruteGroup.getCells());
        permutations = affectedCells.get(0).getBrutePermutations();
        timesBomb = 0;
    }
    public BruteReason(BruteGroup bruteGroup, Cell guessed){
        affectedCells.addAll(bruteGroup.getCells());
        permutations = guessed.getBrutePermutations();
        timesBomb = guessed.getTimesGhostBomb();
    }

    @Override
    public void draw(Graphics2D g2, int rows) {
        ArrayList<Cell> changed = new ArrayList<>();
        for(Cell c: affectedCells){
            if(c.isFlagged() || c.isRevealed()){
                changed.add(c);
            }
        }
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(3));
        for(Cell c: changed){
            g2.drawOval(c.getCol()*25, c.getRow()*25, 25, 25);
        }
        for(Cell c: affectedCells){
            g2.drawRect(c.getCol()*25, c.getRow()*25, 25, 25);
        }
        g2.setStroke(oldStroke);
        g2.drawString("Since other logic doesn't work, the boxed cells were placed in a group and every permutation of bombs was tried.", 25, (rows + 2) * 25);
        if(timesBomb == 0 ){
            g2.drawString(permutations + " combinations worked for the boxed cells, and circled cells were either always bombs or always not a bomb in every permutation.", 25, (rows + 3) * 25);
            g2.drawString("These circled cells were revealed or flagged accordingly.", 25, (rows + 4) * 25);
        }else{
            g2.drawString(permutations + " combinations worked for the boxed cells. The circled cell was a bomb in " + timesBomb + " of those cases.", 25, (rows + 3) * 25);
            g2.drawString("This represents the lowest chance of a cell either being a bomb or not, and the cell was flagged or revealed accordingly.", 25, (rows + 4) * 25);
        }
        g2.drawString("The program tries to guess at groups of cells that affect each other, and uses the number of bombs left in its calculations.", 25, (rows + 5) * 25);
        g2.drawString("However it is impractical time wise if a group has a size greater than ~28 (2^28 is a lot of potential combinations)", 25, (rows + 6) * 25);
        g2.drawString("Therefore, the solver is imperfect as it is limited to groups of 28.", 25, (rows + 7) * 25);
        g2.drawString("Lower this value if it runs slow as this was not tested on school computers. It is 'private final int bruteMaxSize' in SolverMain.", 25, (rows + 8) * 25);
        g2.drawString("Low effort explanation because unlike the advanced logic, you usually can't really learn from this and apply it to your gameplay.", 25, (rows + 9) * 25);

    }
}
