package main;

import reasons.BruteReason;
import reasons.Reason;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SolverMain extends JPanel{
    //Things that could be done: minimum checking when brute force to further optimize how the program is brute searching
    //Challenge for anyone working with this: use it to smash the leaderboards on for minesweeperonline
    private Cell[][] board;
    private final int totalBombs = 99; //Pretty self explanatory. Basically set up the game from here.
    private final int rows = 16;
    private final int cols = 30;
    private final int bruteMaxSize = 12;
    private final int timerDelay = 1; //How fast the auto solve runs
    private boolean hasChanged, gameOver, gameStarted;
    private Reason mostRecentReason; //why computer did last step (if computer made a step)
    private HashMap<String, BufferedImage> images; //load images once to save time
    private Timer timer;

    public SolverMain(){
        images = new HashMap<>(); //load images once to save time
        try {
            images.put("0", ImageIO.read(new File("res/0.jpg")));
            images.put("1", ImageIO.read(new File("res/1.jpg")));
            images.put("2", ImageIO.read(new File("res/2.jpg")));
            images.put("3", ImageIO.read(new File("res/3.jpg")));
            images.put("4", ImageIO.read(new File("res/4.jpg")));
            images.put("5", ImageIO.read(new File("res/5.jpg")));
            images.put("6", ImageIO.read(new File("res/6.jpg")));
            images.put("7", ImageIO.read(new File("res/7.jpg")));
            images.put("8", ImageIO.read(new File("res/8.jpg")));
            images.put("bomb", ImageIO.read(new File("res/bomb.jpg")));
            images.put("falseBomb", ImageIO.read(new File("res/falseBomb.jpg")));
            images.put("flagged", ImageIO.read(new File("res/flagged.jpg")));
            images.put("unfoundBomb", ImageIO.read(new File("res/unfoundBomb.jpg")));
            images.put("unknown", ImageIO.read(new File("res/unknown.jpg")));
        }catch(IOException e){
            //IMAGE ERROR
        }
        restart();
        addKeyListener(new KeyListener() { // key controls
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(gameStarted) {
                    if (e.getKeyCode() == KeyEvent.VK_I && !gameOver) {
                        step();
                    }
                    if (e.getKeyCode() == KeyEvent.VK_S && !gameOver) {
                        if (timer.isRunning()) {
                            timer.stop();
                        } else {
                            timer.start();
                        }

                    }
                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        timer.stop();
                        restart();
                    }
                }else{
                    if (e.getKeyCode() == KeyEvent.VK_I || e.getKeyCode() == KeyEvent.VK_S) {
                        generate((int) (Math.random() * rows), (int) (Math.random() * cols));
                    }
                    if(e.getKeyCode() == KeyEvent.VK_S){
                        timer.start();
                    }
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getY() < rows*25 && e.getX() < cols*25 && e.getY() > 0 && e.getX() > 0) {
                    if (!gameStarted && e.getButton() == 1) {
                        generate(e.getY() / 25, e.getX() / 25);
                    } else if (!gameOver) {
                        if (e.getButton() == 1) {
                            if (!board[e.getY() / 25][e.getX() / 25].isRevealed()) {
                                board[e.getY() / 25][e.getX() / 25].reveal();
                                mostRecentReason = new Reason();
                            } else {
                                board[e.getY() / 25][e.getX() / 25].checkFill(false);
                                mostRecentReason = new Reason();
                            }
                        }
                        if (e.getButton() == 3) {
                            board[e.getY() / 25][e.getX() / 25].flag();
                            mostRecentReason = new Reason();
                        }
                    }
                    if (!gameOver) {
                        repaint();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public void generate(int initialR, int initialC){ //open the board from a given square, leaving 1 row/col buffer around (starting area = 3x3 as long as not on edge)
        int bombsToPlace = totalBombs;
        while(bombsToPlace > 0){
            int r = (int)(Math.random()*rows);
            int c = (int)(Math.random()*cols);
            if((Math.abs(r - initialR) > 1 || Math.abs(c - initialC) > 1) && !board[r][c].isBomb()){
                board[r][c].makeBomb();
                bombsToPlace--;
            }
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j].calcBombs();
            }
        }
        board[initialR][initialC].reveal();
        gameStarted = true;
        repaint();
    }
    
    public int getNumFlagged(){
        int flagCount = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if(board[i][j].isFlagged()){
                    flagCount++;
                }
            }
        }
        return flagCount;
    }

    public int getBombsLeft(){
        return totalBombs - getNumFlagged();
    }

    public void restart(){ //set everything to defaults for another run
        board = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = new Cell(i, j, this);
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c].addSurround();
            }
        }
        gameStarted = false;
        mostRecentReason = new Reason();
        hasChanged = true;
        gameOver = false;
        repaint();
        timer = new Timer(timerDelay, e -> step());
    }


    public void step(){ //computer logic
        if(hasChanged) { //basic logic. check to see if cells are filled with right number of flags around or have empty cells equal to number of flags left
            hasChanged = false;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    Cell currentCell = board[i][j];
                    if(currentCell.checkExclusives() || currentCell.checkFill(true)){
                        hasChanged = true; //only change one cell
                        i = rows;
                        break;
                    }
                }
            }
        }
        if(!hasChanged) { //advanced logic. run program until you see triangles to see how it works
            calcGroups();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (board[i][j].bashGroups()) {
                        hasChanged = true; //only change one cell
                        i = rows;
                        break;
                    }
                }
            }
        }
        if(!hasChanged) { //brute force logic.
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    board[i][j].calcCellsAroundURUF();
                }
            }
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    hasChanged = board[i][j].calcBruteGroup();
                    if (hasChanged) {
                        i = rows; //brute is hard on computer so only change one cell
                        break;
                    }
                }
            }
        }

        if(!hasChanged) { //guessing if brute force is not conclusive
            Cell mostLikely = new Cell(-1);
            Cell leastLikely = new Cell(2);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if(board[i][j].getBruteOdds() != -1){
                        if(board[i][j].getBruteOdds() > mostLikely.getBruteOdds()){
                            mostLikely = board[i][j];
                        }
                        if(board[i][j].getBruteOdds() < leastLikely.getBruteOdds()){
                            leastLikely = board[i][j];
                        }
                    }
                }
            }
            hasChanged = compareChances(mostLikely, leastLikely);
        }

        if(!hasChanged){ //Checking for "Walled Off"  (imagine a square surrounded by bombs, usually on edge)
            BruteGroup wallOff = new BruteGroup(this, true);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if(!board[i][j].isRevealed() && !board[i][j].isFlagged()){
                        wallOff.addCell(board[i][j]);
                    }
                }
            }
            if(wallOff.getCells().size() > 0) {
                wallOff.getCells().get(0).force();
                hasChanged = wallOff.conclude();
                if (!hasChanged) {
                    Cell mostLikely = new Cell(-1);
                    Cell leastLikely = new Cell(2);
                    for (Cell c : wallOff.getCells()) {
                        if (c.getBruteOdds() != -1) {
                            if (c.getBruteOdds() > mostLikely.getBruteOdds()) {
                                mostLikely = c;
                            }
                            if (c.getBruteOdds() < leastLikely.getBruteOdds()) {
                                leastLikely = c;
                            }
                        }
                    }
                    hasChanged = compareChances(mostLikely, leastLikely);
                }
            }
        }
        if(!hasChanged){
            endGame();
        }
        repaint();
        resetBruteGroups();
    }

    public void endGame(){
        timer.stop();
        gameOver = true;
        repaint();
    }

    public boolean compareChances(Cell mostLikely, Cell leastLikely){ //guessing logic to find which square to reveal/flag
        if(mostLikely.getBruteOdds() > -1 || leastLikely.getBruteOdds() < 2) {
            if (1 - mostLikely.getBruteOdds() >= leastLikely.getBruteOdds()) {
                leastLikely.reveal();
                setMostRecentReason(new BruteReason(leastLikely.getBruteGroup(), leastLikely));
            } else {
                mostLikely.flag();
                setMostRecentReason(new BruteReason(mostLikely.getBruteGroup(), mostLikely));
            }
            return true;
        }
        return false;
    }

    public void calcGroups(){ //used for advanced logic
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j].clearGroups();
            }
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j].calcGroups();
            }
        }
    }

    public void resetBruteGroups(){ //clear brute force for next run
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j].resetBrute();
            }
        }

    }

    public Cell[][] getBoard() {
        return board;
    }

    public int getBruteMaxSize() {
        return bruteMaxSize;
    }

    public void setMostRecentReason(Reason mostRecentReason) { //set the explanation
        this.mostRecentReason = mostRecentReason;
    }

    public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;
        for (int i = 0; i < rows; i++) { //game drawing
            for (int j = 0; j < cols; j++) {
                Cell currentDraw  = board[i][j];
                if(currentDraw.isRevealed()) {
                    g2.drawImage(images.get(Integer.toString(currentDraw.getNumBombs())), j*25, i*25, 25, 25, null);
                    if(currentDraw.isBomb()){
                        g2.drawImage(images.get("bomb"), j*25, i*25, 25 ,25 , null);
                    }
                }else{
                    g2.drawImage(images.get("unknown"), j * 25, i * 25, 25, 25, null);
                    if(currentDraw.isBomb() && gameOver) {
                        g2.drawImage(images.get("unfoundBomb"), j * 25, i * 25, 25, 25, null);
                    }
                }
                if(currentDraw.isFlagged()){
                    g2.drawImage(images.get("flagged"), j*25, i*25, 25, 25 ,null);
                    if(!currentDraw.isBomb() && gameOver){
                        g2.drawImage(images.get("falseBomb"), j*25, i*25, 25, 25, null);
                    }
                }
            }
        }
        g2.drawString("Bombs Left: " + getBombsLeft(), 25, (rows + 1) * 25); // bombs left
        if(!gameStarted){ //initial instructions drawing
            g2.drawString("Instructions: Play as you wish, if you get stuck press space to see what my program would've done next, with an explanation.", 25, (rows + 2) * 25);
            g2.drawString("Standard mouse controls: Lclick to reveal, Rclick to flag, Lclick on a number to open cells around provided correct number of flags.", 25, (rows + 3)  * 25);
            g2.drawString("It is not a perfect program and will fail if you have incorrectly flagged a bomb, though it shouldn't freeze in most situations.", 25, (rows + 4) * 25);
            g2.drawString("If it does freeze for more than a minute force stop/quit the program and consider lowering the bruteMaxSize parameter.", 25, (rows + 5) * 25);
            g2.drawString("Pressing 's' will auto-run the solver until victory or failure. It will win 35-45% of the time, as most games require some guessing.", 25, (rows + 6) * 25);
            g2.drawString("Press 'r' to restart whenever. Solver is imperfect (still very good) because of time limitations with brute force (explained more later).", 25, (rows + 7) * 25);
            g2.drawString("Made by Daniel Nee '19 in HACS", 25, (rows + 8) * 25);
        }
        mostRecentReason.draw(g2, rows); //explanation, if needed
        if(gameOver){//end game drawing
            g2.drawString("Game Over. If lost because of incorrect flagging either you flagged wrong or the computer made a guess. Congrats if completed.", 25, (rows + 11) * 25);
            g2.drawString("Press 'r' to restart'", 25, (rows + 12) * 25);
        }

    }

    public static void main(String[] args) { //Standard graphics stuff I kinda almost get.
        JFrame window = new JFrame("Minesweeper");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setBounds(500, 0, 768, 768); //(x, y, w, h)
        SolverMain panel = new SolverMain();
        panel.setFocusable(true);
        panel.grabFocus();
        window.add(panel);
        window.setVisible(true); //calls paint.
    }

}
