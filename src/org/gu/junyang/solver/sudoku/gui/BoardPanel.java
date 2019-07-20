/**
 * Copyright (C) 2011 Junyang Gu <mikejyg@gmail.com>
 * 
 * This file is part of iSudokuSolver.
 *
 * iSudokuSolver is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * iSudokuSolver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iSudokuSolver.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gu.junyang.solver.sudoku.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JLabel;
import org.apache.log4j.Logger;
import org.gu.junyang.solver.sudoku.Board;
import org.gu.junyang.solver.sudoku.Cell;
import org.gu.junyang.solver.sudoku.CellList;
import org.gu.junyang.solver.sudoku.Board.BoardException;
import org.gu.junyang.solver.sudoku.CellPosition;
import org.gu.junyang.utilities.ArrayListUtils;

public class BoardPanel extends javax.swing.JPanel implements Printable {

    Logger logger = Logger.getLogger(this.getClass());

    private Board board;
    private CellList patternCellList;
    
    private boolean showCandidates;
    private boolean editable;
    
    private BlockCellSetPanel[][] blockCellSetPanels;

    /////////////////////////////////////
    // for status label display
    
    // reference to the status label
    private JLabel statusLabel;

    // the contentfor the status label
    private String statusLabelText = "";

	private Color statusLabelColor;

    /**
     * a history to track activity
     */
    private ArrayList<Board> boardHistory;

    private int boardHistoryIdx=-1;			// the curent index

    // for size limit
    static public final int DEFAULT_INPUT_BOARD_HISTORY_SIZE_LIMIT = 1000;		// this is not likely

    // if 0, no limit
    private int boardHistorySizeLimit = DEFAULT_INPUT_BOARD_HISTORY_SIZE_LIMIT;

    private boolean instantSanityCheck;

    // a reference to a display label
    private JLabel boardHistoryIdxLabel;

    private MainWindow parent;

    private boolean showDialogUponSolved = false;

    private boolean highlightChanges = true;

    // patter cell lists associated with the board history
	private ArrayList<CellList> patternCellLists;

    /** Creates new form BoardPanel */
    public BoardPanel() {
        
        initComponents();

        customInitComponents();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFocusable(false);
        setLayout(new java.awt.GridLayout(3, 3));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    private void customInitComponents() {
        blockCellSetPanels = new BlockCellSetPanel[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                blockCellSetPanels[row][col] = new BlockCellSetPanel();
                blockCellSetPanels[row][col].setParent(this);
                add(blockCellSetPanels[row][col]);
            }
        }
//        setPreferredSize(new Dimension(357, 357));
    }
    
    public void highlightChanges(ArrayList<CellPosition> cellPositions) {
        for (CellPosition cellPosition : cellPositions) {
            int row = cellPosition.row;
            int col = cellPosition.col;
            blockCellSetPanels[row/3][col/3].setHighlight(row%3, col%3);
        }
    }

    private void setWarn(int row, int col) {
    	blockCellSetPanels[row/3][col/3].setWarn(row%3, col%3);
	}

    ////////////////////////////////////////////////

    public void setShowCandidates(boolean showCandidates) {
        this.showCandidates = showCandidates;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                blockCellSetPanels[row][col].setShowCandidates(showCandidates);
            }
        }
        updateHighlightChanges();
    }

    public Board getBoard() {
        return board;
    }

    // switch to use the board, and add it to history
    public void setNewBoard(Board board) {
    	setNewBoard(board, null);
    }
    
    // switch to use the board
    private void setBoard(Board board) {
        this.board = board;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                blockCellSetPanels[row][col].setBlockCellList(board.getValidCellList(18 + row * 3 + col));
            }
        }

        updateHighlightChanges();
        
        sanityCheckBoard();
    }
    
    public void setNewBoard(Board board, CellList patternCellList) {
        this.board = board;
        this.patternCellList = patternCellList;
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                blockCellSetPanels[row][col].setBlockCellList(board.getValidCellList(18 + row * 3 + col));
            }
        }

        addCurrentBoardToHistory();

        sanityCheckBoard();
    }

    public void setEditable(boolean editable) {
    	this.editable = editable;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                blockCellSetPanels[row][col].setEditable(editable);
            }
        }
	}

    public void setParent(MainWindow parent) {
        this.parent = parent;
    }

    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
        if (statusLabel!=null) {
            statusLabel.setText(statusLabelText);
            statusLabel.setBackground(statusLabelColor);
        }
    }

    private void setStatusLabelText(String statusLabelText) {
        this.statusLabelText = statusLabelText;
        if (statusLabel!=null)
            statusLabel.setText(statusLabelText);
    }

	private void setStatusLabelColor(Color color) {
		this.statusLabelColor = color;
		if (statusLabel!=null)
			statusLabel.setBackground(statusLabelColor);
	}
	
    public void setEnableBoardHistory(boolean enable) {
        if (enable) {
            if (boardHistory != null)
                return;

            // initialize
            boardHistory = new ArrayList<Board>();
            setBoardHistoryIdx(-1);
        } else {
            boardHistory=null;
        }
    }

    public void setBoardHistorySizeLimit(int boardHistorySizeLimit) {
        if (boardHistorySizeLimit >= 0)
            this.boardHistorySizeLimit = boardHistorySizeLimit;
    }

    boolean stepBack() {
        return setStep(boardHistoryIdx - 1);
    }

    boolean stepForward() {
        return setStep(boardHistoryIdx + 1);
    }

    boolean setStep(int i) {
    	if (i<0 || i>=boardHistory.size())
    		return false;

    	setBoardHistoryIdx(i);

        Board newBoard = boardHistory.get(boardHistoryIdx);

        if (editable) {
            // board is mutable, so make a new copy
            newBoard = new Board(newBoard);
        }

    	setBoard( newBoard );

    	return true;
    }

    enum Status { OK, UNSOLVABLE, SOLVED }; 
    
    boolean sanityCheckBoard() {
    	if (!instantSanityCheck) {
    		setStatus(Status.OK);
    		return true;
    	}
    		
        try {
            // sanity check
        	if (showCandidates)
        		board.sanityCheck();
        	else
        		board.sanityCheckPuzzle();

            // passed sanity check
        	if (board.getUnknowns()==0) {
        		setStatus(Status.SOLVED);
        	} else
        		setStatus(Status.OK);

            return true;

        } catch (BoardException ex) {
//            java.util.logging.Logger.getLogger(CellPanel.class.getName()).log(Level.SEVERE, null, ex);
        	
        	setStatus(Status.UNSOLVABLE);
            
            // highlight conflict
        	if (board.getDuplicateCells().size()==2) {
        		for (int i=0; i<2; i++) {
        			Cell cell = board.getDuplicateCells().get(i);
        			setWarn(cell.getRow(), cell.getCol());
        		}
        	}
            
            return false;
        }
    }

	private void setStatus(Status status) {
		if (status==Status.OK) {
    		setStatusLabelText("");
    		setStatusLabelColor(Color.WHITE);
		} else if (status==Status.UNSOLVABLE) {
            setStatusLabelText("not solvable");
            setStatusLabelColor(Color.YELLOW);
		} else {
    		setStatusLabelText("solved");
    		setStatusLabelColor(Color.GREEN);
		}
	}

	void updateHighlightChanges() {
    	// clear highlight
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                blockCellSetPanels[row][col].resetHighlighting();
            }
        }

		// highlight pattern cells
		if (patternCellLists!=null) {
			if (patternCellLists.get(boardHistoryIdx) != null) {	
				for (Cell cell: patternCellLists.get(boardHistoryIdx)) {
					int row = cell.getRow();
					int col = cell.getCol();
					blockCellSetPanels[row/3][col/3].setPatternHighlight(row%3, col%3);
				}
			}
		}
		
        if (boardHistoryIdx == 0)
            return;

        if (!highlightChanges)
            return;

        ArrayList<CellPosition> diffPositions;
        if (showCandidates) {
            diffPositions = boardHistory.get(boardHistoryIdx - 1).diff(boardHistory.get(boardHistoryIdx), true);
        } else {
            diffPositions = boardHistory.get(boardHistoryIdx - 1).diff(boardHistory.get(boardHistoryIdx), false);
        }

//            System.err.println("differences: " + diffPositions.size());
//            inputPanel.update();
        highlightChanges(diffPositions);
    }

    public int getBoardHistoryIdx() {
        if (boardHistory==null)
            throw new Error("attempt to get boardHistoryIdx when it is not enabled.");
        return boardHistoryIdx;
    }

    public int getBoardHistorySize() {
        return boardHistory.size();
    }

    void read(Scanner scanner) {
        board.read(scanner);
        setNewBoard(board);
    }

    public void addCurrentBoardToHistory() {
        if (boardHistory==null)
            return;
        
        logger.debug("addCurrentBoardToHistory()");

        // check whether there is any difference, there might be some spurious events causing this
//        if (boardHistory.size() != 0) {
//            if (boardHistory.get(boardHistoryIdx).diff(board, true).size() == 0) {
//                logger.info("addCurrentBoardToHistory(): no difference detected, ignored.");
//                return;
//            }
//        }

        // remove all the boards after the current insert position
        ArrayListUtils.removeTailStartingAt(boardHistory, boardHistoryIdx + 1);
        if (patternCellLists != null)
        	ArrayListUtils.removeTailStartingAt(patternCellLists, boardHistoryIdx + 1);
        
        boardHistory.add( new Board(board) );
        if (patternCellLists!=null)
        	patternCellLists.add(patternCellList);
        
        setBoardHistoryIdx(boardHistoryIdx+1);
        logger.debug("current board index: " + boardHistoryIdx);

        // check size limit
        if (boardHistorySizeLimit != 0) {
            while (boardHistory.size() > boardHistorySizeLimit) {
                logger.info("input board history exceeds limit, oldest boards removed.");
                for (int i = 0; i < 10; i++) {
                    boardHistory.remove(0);
                    if (patternCellLists!=null)
                    	patternCellLists.remove(0);
                    setBoardHistoryIdx(boardHistoryIdx-1);
                }
            }
        }
        
        // highlight changes depends on history, so do it here
        updateHighlightChanges();

    }

    public void newBoard() {
        setNewBoard(new Board());
    }

    public void setInstantSanityCheck(boolean instantSanityCheck) {
        this.instantSanityCheck = instantSanityCheck;
    }

	public void setBoardHistory(ArrayList<Board> boardHistory,
			ArrayList<CellList> patternCellLists) {
        this.boardHistory = boardHistory;
        this.patternCellLists = patternCellLists;
	}

//    public void clearHistory() {
//        if (boardHistory!=null) {
//            boardHistory.clear();
//            boardHistoryIdx=-1;
//        }
//    }

    int getUnknowns() {
        return board.getUnknowns();
    }

    void reset(int row, int col) {
        board.reset(row, col);
    }

    void setValue(int row, int col, int val) {
        board.setValue(row, col, val);
    }

    public void setBoardHistoryIdxLabel(JLabel boardHistoryIdxLabel) {
        this.boardHistoryIdxLabel = boardHistoryIdxLabel;
        updateBoardHistoryIdxLabel();
    }

    private void updateBoardHistoryIdxLabel() {
        if (boardHistoryIdxLabel!=null) {
            if (boardHistoryIdx==-1)
                boardHistoryIdxLabel.setText("");
            else
                boardHistoryIdxLabel.setText(Integer.toString(boardHistoryIdx) + " / " +
                        (boardHistory.size()-1) );
        }
    }

    private void setBoardHistoryIdx(int boardHistoryIdx) {
        this.boardHistoryIdx = boardHistoryIdx;
        updateBoardHistoryIdxLabel();
    }

    void takeFocus() {
        parent.requestFocusInWindow();
    }

    // return warn, if sanity check failed
    void boardChanged() {
    	// reset highlighting
    	for (int i=0; i<3; i++)
    		for (int j=0; j<3; j++)
    			blockCellSetPanels[i][j].resetHighlighting();
    	
       	addCurrentBoardToHistory();

        if (sanityCheckBoard()) {

        	if (getUnknowns()==0 && instantSanityCheck) {
        		if (showDialogUponSolved)
        			parent.showMessageDialog("Success!");
        	}
        }
    }

    public void setShowDialogUponSolved(boolean showDialogUponSolved) {
        this.showDialogUponSolved = showDialogUponSolved;
    }

    void logMessage(String msg) {
    	parent.logMessage(msg);
    }

    //////////////////////////////////////
    // for concurrent full solvability test
    
    // full solvability test result
    boolean currentBoardSolvable;
    
    public void setCurrentBoardSolvable(boolean currentBoardSolvable) {
        this.currentBoardSolvable = currentBoardSolvable;
    }

    /////////////////////////////
    // print the board window

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) { /* We have only one page, and 'page' is zero-based */
            return NO_SUCH_PAGE;
        }

        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         */
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        /* Now print the window and its visible contents */
        this.printAll(graphics);

        /* tell the caller that this page is part of the printed document */
        return PAGE_EXISTS;
    }

    public void setHighlightChanges(boolean highlightIncrementalChanges) {
        this.highlightChanges = highlightIncrementalChanges;
        updateHighlightChanges();
    }

    public void setEnablePatternCellLists() {
    	patternCellLists = new ArrayList<CellList>();
    }
}