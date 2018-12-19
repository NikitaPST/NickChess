package com.nikitapestin.nickchess;

/**
 * Created by UserX on 12/18/2018.
 */

class Move {
    private int from;
    private int to;

    int getFrom() {
        return from;
    }

    int getTo() {
        return to;
    }

    Move(int from, int to) {
        this.from = from;
        this.to = to;
    }
}
