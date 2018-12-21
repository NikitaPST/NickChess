package com.nikitapestin.nickchess;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ChessActivity extends AppCompatActivity {

    private GameSearch game;
    private boolean inputEnabled = true;
    private String selectedFigure = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initialize();

        game = new GameSearch(ChessActivity.this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hide();
        }

        game.drawPosition();
    }

    private void hide() {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void initialize() {
        final TableLayout chessBoard = (TableLayout)findViewById(R.id.chessBoard);
        for (int i=0; i<64; i++) {
            ImageView img = (ImageView)chessBoard.findViewWithTag(String.format("picture%d", i));
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!inputEnabled) {
                        return;
                    }

                    String square = ((ViewGroup)v.getParent()).getTag().toString();
                    if (selectedFigure.isEmpty()) {
                        if (game.getCurrentPosition().get(square) == Position.BLANK) {
                            return;
                        }
                        if (game.getCurrentPosition().isOnSide(GameSearch.squareToIndex(square), Position.PROGRAM)) {
                            return;
                        }

                        selectedFigure = square;
                        v.setBackgroundColor(0xFFF426FF);

                        List<Integer> possibleMoves = game.pieceMoves(game.getCurrentPosition(), selectedFigure);
                        for (Integer move : possibleMoves) {
                            ViewGroup panel = (ViewGroup)chessBoard.findViewWithTag(GameSearch.indexToSquare(move));
                            ImageView img = (ImageView)panel.getChildAt(0);
                            img.setBackgroundColor(0XFF30FFE3);
                        }
                    } else if (selectedFigure.equals(square)) {
                        selectedFigure = "";
                        clearHighlights();
                    } else {
                        if (!game.isMoveValid(selectedFigure, square)) {
                            Context context = getApplicationContext();
                            String msg = "Move is invalid!";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, msg, duration);
                            toast.show();
                            return;
                        }

                        humanTurn(selectedFigure, square);

                        selectedFigure = "";
                        clearHighlights();
                    }
                }
            });
        }
    }

    public void drawPosition(Position pos) {
        int square_index = 0;
        for (int col=92; col>=22; col-= 10) {
            for (int ii=0; ii<8; ii++) {
                int i = ii + col;
                if (pos.get(i)!= Position.OFF_BOARD) {
                    renderSquare(pos.get(i), square_index);
                    square_index++;
                }
            }
        }
    }

    private void renderSquare(int piece, int square_index) {
        TableLayout chessBoard = (TableLayout)findViewById(R.id.chessBoard);
        ImageView img = (ImageView)chessBoard.findViewWithTag(String.format("picture%d", square_index));
        if (piece == Position.BLANK) {
            img.setImageResource(android.R.color.transparent);
            return;
        }

        String pieceName = "";
        String color = "white";
        if (piece < 0) {
            color = "black";
            piece*= -1;
        }
        switch (piece) {
            case Position.PAWN:
                pieceName = "pawn";
                break;
            case Position.BISHOP:
                pieceName = "bishop";
                break;
            case Position.KNIGHT:
                pieceName = "knight";
                break;
            case Position.ROOK:
                pieceName = "rook";
                break;
            case Position.QUEEN:
                pieceName = "queen";
                break;
            case Position.KING:
                pieceName = "king";
                break;
        }
        Context context = getApplicationContext();
        int resId = context.getResources().getIdentifier("drawable/" + pieceName + "_" + color,
                null, context.getPackageName());
        Drawable bitmap = context.getResources().getDrawable(resId);
        bitmap = ImageResizer.resize(bitmap, img.getWidth(), img.getHeight(), context);
        img.setImageDrawable(bitmap);
    }

    private void clearHighlights() {
        TableLayout chessBoard = (TableLayout)findViewById(R.id.chessBoard);
        for (int i=0; i<64; i++) {
            ImageView img = (ImageView) chessBoard.findViewWithTag(String.format("picture%d", i));
            img.setBackgroundColor(0x00000000);
        }
    }

    private void humanTurn(String figure, String destination) {
        inputEnabled = false;

        int figure_index = GameSearch.squareToIndex(figure);
        int dest_index = GameSearch.squareToIndex(destination);
        Move newMove = new Move(figure_index, dest_index);
        game.makeMove(newMove);

        if (checkConditions())
            return;

        machineTurn();

        inputEnabled = true;
    }

    private boolean checkConditions() {
        if (game.wonPosition(GameSearch.PROGRAM)) {
            messageBox("Program won!");
            return true;
        } else if (game.wonPosition(GameSearch.HUMAN)) {
            messageBox("Player won!");
            return true;
        } else if (game.drawnPosition()) {
            messageBox("Stalemate!");
            return true;
        }
        return false;
    }

    private void messageBox(CharSequence msg) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(msg);
        dlgAlert.setTitle("Chess");
        dlgAlert.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAndRemoveTask();
                    }
                });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }

    private void machineTurn() {
        game.generatePosition();

        checkConditions();
    }
}
