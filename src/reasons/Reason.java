package reasons;

import main.Cell;

import java.awt.*;
import java.util.ArrayList;

public class Reason {
    protected ArrayList<Cell> affectedCells;

    public Reason(){
        affectedCells = new ArrayList<>();
    }

    public void addCell(Cell c){
        affectedCells.add(c);
    }

    protected void appendCoordinates(StringBuilder text, Cell toAppend){
        text.append("(");
        text.append(toAppend.getRow());
        text.append(", ");
        text.append(toAppend.getCol());
        text.append(")");
    }

    public void draw(Graphics2D g2, int rows){
        if(affectedCells.size() > 0) {
            StringBuilder affectedString = new StringBuilder();
            if(affectedCells.size() > 1){
                affectedString.append("The cells boxed at: ");
            }else{
                affectedString.append("The cell boxed at: ");
            }
            for (Cell c : affectedCells) {
                appendCoordinates(affectedString, c);
                affectedString.append(", ");
            }
            g2.drawString(affectedString.toString(), 25, (rows + 2) * 25);

        }
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(3));
        for(Cell c: affectedCells){
            g2.drawRect(c.getCol()*25, c.getRow()*25, 25, 25);
        }
        g2.setStroke(oldStroke);

    }




}
