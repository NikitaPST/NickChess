package com.nikitapestin.nickchess;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by UserX on 12/16/2018.
 */

class GameSearch {
    static final boolean PROGRAM = false;
    static final boolean HUMAN = true;
    private static final int MAX_DEPTH = 4; // Main constant. Controls difficulty vs performance

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
        int pieceIndex = index[pieceType];
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

    void generatePosition() {
        Vector v = alphaBeta(0, currentPosition, PROGRAM);
        currentPosition = (Position)v.get(1);
        activity.writeToLog("Black: " + currentPosition.getCommentary());
        drawPosition();
    }

    private Vector alphaBeta(int depth, Position p, boolean player) {
        try {
            return alphaBetaHelper(depth, p, player, 1000000.0f, -1000000.0f);
        }
        catch (Exception ex){
            activity.finishAndRemoveTask();
            return null;
        }
    }

    private Vector alphaBetaHelper(int depth, Position p, boolean player, float alpha, float beta)
        throws Exception {
        if (reachedMaxDepth(p, depth)) {
            Vector v = new Vector(2);
            float value = positionEvaluation(p, player);
            v.add(value);
            v.add(null);
            return v;
        }

        Vector best = new Vector();
        Position[] moves = possibleMoves(p, player);
        for (int i=0; i<moves.length; i++) {
            Vector v2 = alphaBetaHelper(depth + 1, moves[i], !player, -beta, -alpha);
            if (v2 == null)
                continue;
            float value = -(float)v2.get(0);
            if (value > beta) {
                beta = value;
                best = new Vector();
                best.add(moves[i]);
                for (int j=1; j<v2.size(); j++) {
                    if (v2.get(j)!= null) {
                        best.add(v2.get(j));
                    }
                }
            }

            if (beta >= alpha)
                break;
        }

        Vector v3 = new Vector();
        v3.add(beta);
        for (int i=0; i<best.size(); i++) {
            v3.add(best.get(i));
        }

        return v3;
    }

    private boolean reachedMaxDepth(Position p, int depth) {
        return (depth >= MAX_DEPTH);
    }

    private float positionEvaluation(Position p, boolean player) {
        float result = 0.0f;
        for (int i=22; i<100; i++) {
            if ((p.get(i)!= Position.BLANK) && (p.get(i)!= Position.OFF_BOARD))
                result+= 1.75f * p.get(i);
        }

        float control = 0.0f;
        ControlData controlData = calcControlData(p);
        for (int i=22; i<100; i++) {
            control+= controlData.humanControl[i];
            control-= controlData.computerControl[i];
        }

        control+= controlData.humanControl[55] - controlData.computerControl[55];
        control+= controlData.humanControl[56] - controlData.computerControl[56];
        control+= controlData.humanControl[65] - controlData.computerControl[65];
        control+= controlData.humanControl[66] - controlData.computerControl[66];
        control/= 10.0f;
        result+= control;

        for (int i=22; i<100; i++) {
            if ((p.get(i) == Position.BLANK) || (p.get(i) == Position.OFF_BOARD))
                continue;

            if (p.get(i) < 0) {
                if (controlData.humanControl[i] > controlData.computerControl[i]) {
                    result+= 0.9f * value[-p.get(i)];
                }
                if ((p.get(i) == -Position.QUEEN) && (controlData.humanControl[i] > 0)) {
                    result+= 2.0f;
                }
                if ((p.get(i) == -Position.KING) && (controlData.humanControl[i] > 0)) {
                    result+= 4.0f;
                }
            } else {
                if (controlData.humanControl[i] < controlData.computerControl[i]) {
                    result-= 0.9f * value[p.get(i)];
                }
                if ((p.get(i) == Position.QUEEN) && (controlData.computerControl[i] > 0)) {
                    result-= 2.0f;
                }
                if ((p.get(i) == Position.KING) && (controlData.computerControl[i] > 0)) {
                    result-= 4.0f;
                }
            }
        }

        if (wonPosition(player, p))
            return (result + 100.0f);
        if (wonPosition(!player, p))
            return -(result + 100.0f);

        return result;
    }

    private ControlData calcControlData(Position p) {
        ControlData controlData = new ControlData();
        float[] control = null;

        for (int squareIndex = 22; squareIndex<100; squareIndex++) {
            int piece = p.get(squareIndex);
            if ((piece == Position.OFF_BOARD) || (piece == Position.BLANK))
                continue;
            int pieceType = piece;
            if (piece < 0) {
                pieceType = -pieceType;
                control = controlData.computerControl;
            } else {
                control = controlData.humanControl;
            }

            int sideIndex = 0;
            int moveOffset = 0;
            int nextSquare = 0;
            int pieceIndex = index[pieceType];
            int moveIndex = pieceMovementTable[pieceIndex];
            if (piece < 0)
                sideIndex = -1;
            else
                sideIndex = 1;

            switch (pieceType) {
                case Position.PAWN:
                    for (int delta=-1; delta<=1; delta+=2) {
                        moveOffset = squareIndex + sideIndex * 10 + delta;
                        control[moveOffset]+= 1.1f;
                        int target = p.get(moveOffset);
                        if (((target <= -1) && (target!= Position.OFF_BOARD) && (piece>0)) ||
                                ((target >= 1) && (target!= Position.OFF_BOARD) && (piece<0))) {
                            control[squareIndex+sideIndex*delta]+= 1.25f;
                        }
                    }
                    //break;
                case Position.KNIGHT:
                case Position.ROOK:
                case Position.BISHOP:
                case Position.QUEEN:
                case Position.KING:
                    moveIndex = Math.abs(piece);
                    moveIndex = index[moveIndex];
                    nextSquare = squareIndex + pieceMovementTable[moveIndex];
                    while (true) {
                        while (true) {
                            if ((nextSquare > 99) || (nextSquare < 22) || (p.get(nextSquare) == Position.OFF_BOARD))
                                break;

                            control[nextSquare]+= 1.0f;

                            if (((sideIndex < 0) && (p.get(nextSquare) < 0)) ||
                                    ((sideIndex > 0) && (p.get(nextSquare) > 0) && (p.get(nextSquare)!= Position.OFF_BOARD)))
                                break;

                            if ((pieceType == Position.PAWN) && (squareIndex / 10 == 3))
                                break;
                            if ((pieceType == Position.KNIGHT) || (pieceType == Position.KING))
                                break;

                            nextSquare+= pieceMovementTable[moveIndex];
                        }

                        moveIndex++;
                        if (pieceMovementTable[moveIndex] == 0)
                            break;

                        nextSquare = squareIndex + pieceMovementTable[moveIndex];
                    }
                    break;
            }
        }

        return controlData;
    }

    private Position[] possibleMoves(Position p, boolean player) throws Exception {
        List<Move> possibleMoveList = calcPossibleMoves(p, player);
        if (possibleMoveList.size() == 0) {
            throw new Exception("No possible moves!");
        }

        Position[] chessPos = new Position[possibleMoveList.size()];
        for (int i=0; i<possibleMoveList.size(); i++) {
            chessPos[i] = new Position();
            for (int j=22; j<100; j++) {
                chessPos[i].set(j, p.get(j));
            }

            chessPos[i].set(possibleMoveList.get(i).getTo(), chessPos[i].get(possibleMoveList.get(i).getFrom()));
            chessPos[i].set(possibleMoveList.get(i).getFrom(), Position.BLANK);
            chessPos[i].setCommentary(indexToSquare(possibleMoveList.get(i).getFrom()) + indexToSquare(possibleMoveList.get(i).getTo()));
        }

        return chessPos;
    }

    private List<Move> calcPossibleMoves(Position p, boolean player) {
        List<Integer> pieceMoves = null;
        List<Move> possibleMoveList = new ArrayList<Move>();
        for (int i=22; i<100; i++) {
            int boardVal = p.get(i);
            if (boardVal == Position.OFF_BOARD)
                continue;

            if (((boardVal < 0) && !player) || (boardVal > 0) && player) {
                pieceMoves = pieceMoves(p, i);
                for (int j=0; j<pieceMoves.size(); j++) {
                    if (p.get(pieceMoves.get(j))!= Position.OFF_BOARD) {
                        Move m = new Move(i, pieceMoves.get(j));
                        possibleMoveList.add(m);
                    }
                }
            }
        }

        return possibleMoveList;
    }

    private class ControlData {
        float[] humanControl;
        float[] computerControl;

        ControlData() {
            computerControl = new float[Position.BOARD_SIZE];
            humanControl = new float[Position.BOARD_SIZE];
        }
    }
}
