package reasons;

import main.Cell;

import java.awt.*;

public class LogicReason extends Reason{
    private Cell effectorCell; //cell that was the basis for the action

    public LogicReason(Cell effectorCell){
        super();
        this.effectorCell = effectorCell;
    }

    @Override
    public void draw(Graphics2D g2, int rows){
        super.draw(g2, rows);
        StringBuilder reasonString = new StringBuilder();
        if (affectedCells.size() > 1) {
            reasonString.append("were ");
        } else {
            reasonString.append("was ");
        }
        if (affectedCells.get(0).isRevealed()) {
            reasonString.append("revealed because the circled cell at ");
            appendCoordinates(reasonString, effectorCell);
            reasonString.append(" has a bomb value of ");
            reasonString.append(effectorCell.getNumBombs());
            reasonString.append(" and has that many flags around it already.");
        } else {
            reasonString.append("flagged because the circled cell at ");
            appendCoordinates(reasonString, effectorCell);
            reasonString.append(" has a bomb value of ");
            reasonString.append(effectorCell.getNumBombs());
            reasonString.append(" and has that many unopened squares around it.");
        }
        g2.drawString(reasonString.toString(), 25, (rows + 3) * 25);
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(effectorCell.getCol() * 25, effectorCell.getRow() * 25, 25, 25);
        g2.setStroke(oldStroke);
    }








}
