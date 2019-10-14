package reasons;

import main.Cell;

import java.awt.*;
import java.util.ArrayList;

public class BashReason extends Reason{

    private Cell effectorCell; //cell in the center
    private Cell intersectionEffector; //cell that gave intersection its value (min, max, etc.) basically it made the group that intersects
    private ArrayList<Cell> intersection; //the common cells shared by effectorCell and intersectionEffector
    private int bombsInIntersection;

    public BashReason(Cell effectorCell, Cell intersectionEffector, ArrayList<Cell> intersection, int bombsInIntersection){
        this.effectorCell = effectorCell;
        this.intersectionEffector = intersectionEffector;
        this.intersection = new ArrayList<>();
        this.intersection.addAll(intersection);
        this.bombsInIntersection = bombsInIntersection;
    }

    @Override
    public void draw(Graphics2D g2, int rows){
        super.draw(g2, rows);
        StringBuilder[] reasonStrings = new StringBuilder[6];
        for (int i = 0; i < reasonStrings.length; i++) {
            reasonStrings[i] = new StringBuilder();
        }
        if(affectedCells.size() > 1){
            reasonStrings[0].append("were ");
        }else{
            reasonStrings[0].append("was ");
        }
        if(affectedCells.get(0).isRevealed()){
            reasonStrings[0].append("revealed because the diamond cells at ");
            for(Cell c: intersection){
                appendCoordinates(reasonStrings[0], c);
                reasonStrings[0].append(", ");
            }
            reasonStrings[1].append("have a minimum of ");
            //3rd line starting
            reasonStrings[2].append("even if all non-diamond cells around the triangle cell were filled, there would still be at least ");
            reasonStrings[2].append(bombsInIntersection);
            if(bombsInIntersection == 1){
                reasonStrings[2].append(" bomb");
            }else{
                reasonStrings[2].append(" bombs");
            }
            reasonStrings[2].append(" in the diamond cells.");
            //4th Line
            reasonStrings[3].append("Even if the diamond cells had the minimum amount of bombs they could possibly have,");
            reasonStrings[4].append("they would still hold all the bombs left to flag around the circled cell at ");
            appendCoordinates(reasonStrings[4], effectorCell);
            reasonStrings[4].append(".");
            //5th
            reasonStrings[5].append("Therefore we can reveal any cell around the circled cell that is not a diamond cell. Any revealed cell was boxed.");
        }else{
            reasonStrings[0].append("flagged because the diamond cells at ");
            for(Cell c: intersection){
                appendCoordinates(reasonStrings[0], c);
                reasonStrings[0].append(", ");
            }
            reasonStrings[1].append("have a maximum of ");
            //3rd line starting
            reasonStrings[2].append("the number of bombs left to flag around the triangle cell (");
            reasonStrings[2].append(intersectionEffector.getBombsLeft());
            reasonStrings[2].append("), is less than or equal to the number of diamond squares (");
            reasonStrings[2].append(intersection.size());
            reasonStrings[2].append(").");
            //4th
            reasonStrings[3].append("Even if the diamond cells had the maximum number of bombs they could possibly contain,");
            reasonStrings[4].append("the non-diamond cells around the circled cell at ");
            appendCoordinates(reasonStrings[4], effectorCell);
            reasonStrings[4].append(" would have to be flagged to flag the correct number of bombs around the circled cell.");
            reasonStrings[5].append("Therefore we can flag any cell around the circled cell that is not a diamond cell.  Any flagged cell was boxed.");


        }
        reasonStrings[1].append(bombsInIntersection);
        if(bombsInIntersection == 1){
            reasonStrings[1].append(" bomb");
        }else{
            reasonStrings[1].append(" bombs");
        }
        reasonStrings[1].append(" due to the triangle cell at ");
        appendCoordinates(reasonStrings[1], intersectionEffector);
        reasonStrings[1].append(". We know this because");

        for (int i = 0; i < reasonStrings.length; i++) {
            g2.drawString(reasonStrings[i].toString(), 25, (rows + 3 + i) * 25);
        }
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(3));
        for(Cell c: intersection){
            int[] diamondXPoints = {c.getCol() * 25 + (25/2), c.getCol() * 25, c.getCol() * 25 + (25/2), c.getCol() * 25 + 25};
            int[] diamondYPoints = {c.getRow() * 25, c.getRow() * 25 + (25/2), c.getRow() * 25 + 25, c.getRow() * 25 + (25/2)};
            g2.drawPolygon(diamondXPoints, diamondYPoints, 4);
        }
        int[] triXPoints = {intersectionEffector.getCol() * 25 + (25/2), intersectionEffector.getCol() * 25, intersectionEffector.getCol() * 25 + 25};
        int[] triYPoints = {intersectionEffector.getRow() * 25, intersectionEffector.getRow() * 25 + 25, intersectionEffector.getRow() * 25 + 25};
        g2.drawPolygon(triXPoints, triYPoints, 3);
        g2.drawOval(effectorCell.getCol()*25, effectorCell.getRow()*25, 25, 25);
        g2.setStroke(oldStroke);
    }
}
