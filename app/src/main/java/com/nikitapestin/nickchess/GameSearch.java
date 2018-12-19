package com.nikitapestin.nickchess;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by UserX on 12/16/2018.
 */

class GameSearch {
    public static final boolean PROGRAM = false;
    public static final boolean HUMAN = true;

    private static int[] index = { 0, 12, 15, 10, 1, 6, 0, 0, 0, 6 };
    private static int[] pieceMovementTable =
            {
                    0, -1, 1, 10, -10, 0, -1, 1, 10, -10, -9, -11, 9,
                    11, 0, 8, -8, 12, -12, 19, -19, 21, -21, 0, 10, 20,
                    0, 0, 0, 0, 0, 0, 0, 0
            };
    private static int[] value = { 0, 1, 3, 3, 5, 9, 0, 0, 0, 20 };

    private Position currentPosition;
    private ChessActivity activity;

    GameSearch(ChessActivity activity) {
        this.currentPosition = new Position();
        this.activity = activity;

        currentPosition.initialize();
    }

    void drawPosition() {
        activity.drawPosition(currentPosition);
    }

    Position getCurrentPosition() {
        return currentPosition;
    }

    static int squareToIndex(String square) {
        int c = ((int)square.charAt(0) - (int)'A' + 2);
        int r = ((int)square.charAt(1) - (int)'1' + 2);
        return (r*10 + c);
    }

    static String indexToSquare(int square_index) {
        char c = (char)((square_index / 10) - 2 + (int)'1');
        char r = (char)((square_index % 10) - 2 + (int)'A');
        return (Character.toString(r) + Character.toString(c));
    }

    List<Integer> pieceMoves(Position p, String square) {
        return pieceMoves(p, squareToIndex(square));
    }

    List<Integer> pieceMoves(Position p, int squareIndex) {
        int moveOffset = 0;
        int temp = 0;
        int nextSquare = 0;
        int piece = p.get(squareIndex);
        int pieceType = Math.abs(piece);
        int pieceIndex = index[piece];
        int sideIndex = piece / Math.abs(piece);
        List<Integer> pieceMoves = new ArrayList<Integer>();

        switch (pieceType) {
            case Position.PAWN:
                for (int delta=-1; delta<=1;delta+=2) {
                    moveOffset= squareIndex + sideIndex*10+delta;
                    int target = p.get(moveOffset);
                    if (((target<=Position.PROGRAM) && (target!= Position.OFF_BOARD) && (piece > 0)) ||
                            ((target>=Position.HUMAN) && (target!= Position.OFF_BOARD) && (piece < 0))) {
                        pieceMoves.add(squareIndex + sideIndex*10+delta);
                    }
                }

                moveOffset = squareIndex + sideIndex*20;
                if (piece > 0) {
                    temp = 3;
                } else {
                    temp = 8;
                }
                if ((p.get(moveOffset) == Position.BLANK) && ((squareIndex/10) == temp) &&
                        (((piece<0) && (p.get(squareIndex-10) == Position.BLANK)) ||
                                ((piece>0) && (p.get(squareIndex+10)==Position.BLANK)))) {
                    pieceMoves.add(squareIndex+sideIndex*20);
                }

                moveOffset = squareIndex + sideIndex*10;
                if (p.get(moveOffset) == Position.BLANK) {
                    pieceMoves.add(moveOffset);
                }
                break;

            case Position.KNIGHT:
            case Position.BISHOP:
            case Position.ROOK:
            case Position.QUEEN:
            case Position.KING:
                nextSquare = squareIndex + pieceMovementTable[pieceIndex];

                while (true) {
                    while (true) {
                        if ((nextSquare > 99) || (nextSquare < 22) || (p.get(nextSquare) == Position.OFF_BOARD))
                            break;

                        if (((sideIndex < 0) && (p.get(nextSquare) < 0)) || ((sideIndex > 0) && (p.get(nextSquare) > 0)))
                            break;

                        pieceMoves.add(nextSquare);

                        if ((p.get(nextSquare)!= Position.BLANK) || (pieceType == Position.KNIGHT) ||
                                (pieceType == Position.KING))
                            break;

                        nextSquare+= pieceMovementTable[pieceIndex];
                    }
                    pieceIndex++;
                    if (pieceMovementTable[pieceIndex] == 0)
                        break;

                    nextSquare = squareIndex + pieceMovementTable[pieceIndex];
                }
                break;
        }

        return pieceMoves;
    }

    boolean isMoveValid(String figure, String destination) {
        int figure_index = squareToIndex(figure);
        int dest_index = squareToIndex(destination);
        List<Integer> moves = pieceMoves(currentPosition, figure_index);
        return moves.contains(dest_index);
    }

    void makeMove(Move move) {
        Position pos = currentPosition.copy();
        pos.set(move.getTo(), pos.get(move.getFrom()));
        pos.set(move.getFrom(), Position.BLANK);
        currentPosition = pos;

        drawPosition();
    }

    boolean wonPosition(boolean player) {
        return wonPosition(player, currentPosition);
    }

    boolean wonPosition(boolean player, Position p) {
        int piece = Position.KING;
        if (player == HUMAN) {
            piece*= Position.PROGRAM;
        }

        boolean result = true;
        for (int i=0; i<Position.BOARD_SIZE; i++) {
            if (p.get(i) == piece) {
                result = false;
                break;
            }
        }

        return result;
    }

    boolean drawnPosition() {
        return drawnPosition(currentPosition);
    }

    boolean drawnPosition(Position p) {
        return false;
    }
}
