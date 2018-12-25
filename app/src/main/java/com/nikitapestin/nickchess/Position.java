package com.nikitapestin.nickchess;

/**
 * Created by UserX on 12/16/2018.
 */

class Position {
    static final int BLANK = 0;
    static final int HUMAN = 1;
    static final int PROGRAM = -1;
    static final int PAWN = 1;
    static final int KNIGHT = 2;
    static final int BISHOP = 3;
    static final int ROOK = 4;
    static final int QUEEN = 5;
    static final int KING = 9;
    static final int OFF_BOARD = 7;
    static final int BOARD_SIZE = 120;
    static final int START_INDEX = 22;
    static final int END_INDEX = 99;

    private static int[] initialBoard =
            {
                    7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                    7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                    4, 2, 3, 5, 9, 3, 2, 4, 7, 7,
                    1, 1, 1, 1, 1, 1, 1, 1, 7, 7,
                    0, 0, 0, 0, 0, 0, 0, 0, 7, 7,
                    0, 0, 0, 0, 0, 0, 0, 0, 7, 7,
                    0, 0, 0, 0, 0, 0, 0, 0, 7, 7,
                    0, 0, 0, 0, 0, 0, 0, 0, 7, 7,
                    -1, -1, -1, -1, -1, -1, -1, -1, 7, 7,
                    -4, -2, -3, -5, -9, -3, -2, -4, 7, 7,
                    7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                    7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
            };

    private int[] board = new int[BOARD_SIZE];
    private String commentary = "";

    void initialize() {
        for (int i=0; i<BOARD_SIZE; i++) {
            board[i] = initialBoard[i];
        }
    }

    int get(int index) {
        return board[index];
    }

    int get(String square) { return board[GameSearch.squareToIndex(square)]; }

    String getCommentary() {
        return commentary;
    }

    void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    boolean isOnSide(int square_index, int side) {
        return (((board[square_index] < 0) && (side < 0)) || ((board[square_index] > 0) && (side > 0)));
    }

    Position copy() {
        Position pos = new Position();
        for (int i=0; i<BOARD_SIZE; i++) {
            pos.board[i] = this.board[i];
        }
        return  pos;
    }

    void set(int index, int value) {
        board[index] = value;
    }
}
