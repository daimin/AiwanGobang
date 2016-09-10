//五子棋的View

package com.codecos.gobang;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.codecos.gobang.R;


public class GobangNetView extends View {

    protected static final String LOG_TAG = "GobangNetView";


    private final static int GRID_SIZE = 14;

    private final static int CHESS_GRID = GRID_SIZE - 1;

    private final static int CHECK_DIR = 4; // 当前位置的四个方向，横、竖、左斜、右斜

    private int chess_dia = 22; // 棋的直径

    private int grid_width; // 棋盘格的宽度
    private int mStartX;// 棋盘定位的左上角X
    private int mStartY;// 棋盘定位的左上角Y

    private Bitmap[] mChessBW; // 黑棋和白棋

    private int[][] mChessTable = new int[CHESS_GRID][CHESS_GRID]; // 网格

    private int[][][] computerTable = new int[CHESS_GRID][CHESS_GRID][CHECK_DIR]; // 电脑棋形表
    private int[][][] playerTable = new int[CHESS_GRID][CHESS_GRID][CHECK_DIR]; // 电脑棋形表

    public static final int BLACK = 2; // 电脑执黑棋
    public static final int WHITE = 1; // 玩家执白棋

    private int whoTurn = WHITE; // 该谁下了，该下白棋了=2，该下黑棋了=1.
    // 这里先下黑棋（黑棋以后设置为机器自动下的棋子）
    private int mWinFlag = 0;

    private static final int GAMESTATE_PRE = 0;
    private static final int GAMESTATE_RUN = 1;
    private static final int GAMESTATE_PAUSE = 2;
    private static final int GAMESTATE_END = 3;
    private int mGameState = GAMESTATE_RUN; // 游戏阶段：0=尚未游戏，1=正在进行游戏，2=游戏结束

    private TextView mStatusTextView; // 根据游戏状态设置显示的文字

    private CharSequence mText;
    private CharSequence STRING_WIN =   "   真厉害，大侠你赢了！ \n" +
                                        "   点击[确认]开始新游戏。";
    private CharSequence STRING_LOSE =  "   大侠请重头来过！ \n" +
                                        "   点击[确认]开始新游戏。";
    private CharSequence STRING_EQUAL = "   酷！你们是平手！ \n" +
                                        "   点击[确认]开始新游戏。";
    private Paint mPaint;
    private boolean bitmapLoaded = false;

    private ChessFormUtil cfUtil;

    // 保存前5个较好的落子点
    private ChessPoint[] fiveBetterPoints = new ChessPoint[5];

    private Bitmap bgImage;
    private Bitmap vsImage;


    private int  bgHoff;
    private int bgWoff;

    private Context ctx;


    public GobangNetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        cfUtil = new ChessFormUtil();
        this.ctx = context;
    }

    public GobangNetView(Context context, AttributeSet attrs) { // 好像调用的是这个构造函数，为什么不是前面的呢
        super(context, attrs);
        this.ctx = context;
        this.setFocusable(true); // 20090530
        this.setFocusableInTouchMode(true);
        cfUtil = new ChessFormUtil();
    }

    // 这里画棋子后来没有用图片画，而是直接画了圆。因为我做的图片不好看。
    // 初始化黑白棋的Bitmap
    public void init() {
        mGameState = 1; // 设置游戏为开始状态
        whoTurn = WHITE; // 初始为先下黑棋
        mWinFlag = 0; // 清空输赢标志。
        // 清空各种表格
        for (int i = 0; i < CHESS_GRID; i++) {
            for (int j = 0; j < CHESS_GRID; j++) {
                mChessTable[i][j] = 0;
                for (int k = 0; k < CHECK_DIR; k++) {
                    computerTable[i][j][k] = 0;
                    playerTable[i][j][k] = 0;
                }
            }

        }

    }

    public void setTextView(TextView tv) {
        mStatusTextView = tv;
        mStatusTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // 加载背景图片
        bgImage = BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.gobang_bg2);
        bgImage = bgImage.copy(Bitmap.Config.ARGB_8888, true);

        vsImage = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.vs);

        int bgW = bgImage.getWidth();
        int bgH = bgImage.getHeight();
        bgHoff = h - bgH;
        bgWoff = w - bgW;

        bgHoff = bgHoff > 0 ? bgHoff : 0;
        bgWoff = bgWoff > 0 ? bgWoff : 0;

        if (w <= h)
            grid_width = w / GRID_SIZE;
        else
            grid_width = h / GRID_SIZE;

        chess_dia = grid_width - 2;
        mStartX = (w - GRID_SIZE * grid_width) >> 1;
//        mStartY = (h - GRID_SIZE * grid_width) >> 1;
        mStartY = grid_width * 2;

        if (!bitmapLoaded) {
            // 两张背景，防止闪屏, 这个是棋子的杯具
            mChessBW = new Bitmap[2];
            // 绘制背景
            Bitmap bitmap = Bitmap.createBitmap(chess_dia, chess_dia,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Resources r = this.getContext().getResources();
            Drawable tile = r.getDrawable(R.drawable.chess1);
            tile.setBounds(0, 0, chess_dia, chess_dia);
            tile.draw(canvas);
            mChessBW[0] = bitmap;

            // bitmap.recycle(); //
            // 清除该bitmap的内存，保证该Bitmap被回收，但是一般不需要手动调用，正常的GC会清除该Bitmap

            bitmap = Bitmap.createBitmap(chess_dia, chess_dia,
                    Bitmap.Config.ARGB_8888);

            canvas = new Canvas(bitmap); // Bitmap + Canvas + Drawable ==
            // 标准的Bitmap绘制。
            tile = r.getDrawable(R.drawable.chess2);
            tile.setBounds(0, 0, chess_dia, chess_dia);
            tile.draw(canvas);

            mChessBW[1] = bitmap;
            bitmapLoaded = true;

            init();
        }

    }


    private void checkGameStatus(MotionEvent event){
            switch (mGameState) {
                case GAMESTATE_PRE:
                    break;
                case GAMESTATE_RUN: {
                    int x;
                    int y;
                    float x0 = grid_width - (event.getX() - mStartX) % grid_width;
                    float y0 = grid_width - (event.getY() - mStartY) % grid_width;
                    if (x0 < (grid_width >> 1)) {
                        x = (int) ((event.getX() - mStartX) / grid_width);
                    } else {
                        x = (int) ((event.getX() - mStartX) / grid_width) - 1;
                    }
                    if (y0 < (grid_width >> 1)) {
                        y = (int) ((event.getY() - mStartY) / grid_width);
                    } else {
                        y = (int) ((event.getY() - mStartY) / grid_width) - 1;
                    }
                    Log.v("x,y", "" + y + "," + x);
                    if ((x >= 0 && x < CHESS_GRID) && (y >= 0 && y < CHESS_GRID)) {
                        if (mChessTable[x][y] == 0) {
                            if (whoTurn == WHITE) {
                                putChess(x, y, WHITE);
                                if (checkWin(WHITE)) {
                                    mText = STRING_WIN;
                                    mGameState = GAMESTATE_END;
                                    showEndDialog(mText);
                                    //showTextView(mText);
                                } else if (checkFull()) {// 如果棋盘满了
                                    mText = STRING_EQUAL;
                                    mGameState = GAMESTATE_END;
                                    showEndDialog(mText);
                                    //showTextView(mText);
                                }
                                whoTurn = BLACK;

                                // 黑棋下了之后开始分析黑棋此时是否胜利。
                                if (checkWin(BLACK)) { // 如果是黑棋赢了
                                    mText = STRING_LOSE;
                                    mGameState = GAMESTATE_END;
                                    showEndDialog(mText);
                                    //showTextView(mText);
                                } else if (checkFull()) {// 如果棋盘满了
                                    mText = STRING_EQUAL;
                                    mGameState = GAMESTATE_END;
                                    showEndDialog(mText);
                                    //showTextView(mText);
                                }

                                whoTurn = WHITE;
                            }
                        }
                    }
                }

                break;
                case GAMESTATE_PAUSE:
                    break;
                case GAMESTATE_END:
                    break;
            }

            this.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            this.checkGameStatus(event);
            return true;
        }
        return super.onTouchEvent(event);
    }


   void gameRestart(){
        mGameState = GAMESTATE_RUN;
        this.setVisibility(View.VISIBLE);
        this.mStatusTextView.setVisibility(View.INVISIBLE);
        this.init();
        this.invalidate();
   }

   private void showEndDialog(CharSequence mText) {
        new AlertDialog.Builder(this.ctx).setTitle("提示")
                        .setMessage(mText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gameRestart();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                .show();
            // 提示框方法

   }

    private void clearChessArray() {
        int i, j;
        for (i = 0; i < CHESS_GRID; i++) {
            for (j = 0; j < CHESS_GRID; j++) {
                computerTable[i][j][0] = 0;
                computerTable[i][j][1] = 0;
                computerTable[i][j][2] = 0;
                computerTable[i][j][3] = 0;

                playerTable[i][j][0] = 0;
                playerTable[i][j][1] = 0;
                playerTable[i][j][2] = 0;
                playerTable[i][j][3] = 0;

            }
        }

        for (i = 0; i < 5; i++) {
            fiveBetterPoints[i] = null;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        return super.onKeyDown(keyCode, msg);
    }

    @Override
    public void onDraw(Canvas canvas) {

//        canvas.drawColor(Color.LTGRAY);

        // 画棋盘
        {
            Paint paintRect = new Paint();
            int bgw = bgImage.getWidth();
            int bgh = bgImage.getHeight();
            canvas.drawBitmap(bgImage, 0, 0, paintRect);


            int bgrptime = (int)(Math.ceil(bgWoff / (bgw * 1.0)));
            if(bgrptime < (int)(Math.ceil(bgHoff / (bgh * 1.0)))) {
                bgrptime = (int)(Math.ceil(bgHoff / (bgh * 1.0)));
            }

            Log.i(LOG_TAG, "bgrptime = " + bgrptime);
            for(int i = 0; i < bgrptime; i++) {
                canvas.drawBitmap(bgImage, 0, (i + 1) * bgh, paintRect);
            }

            paintRect.setColor(getResources().getColor(R.color.dgridmo));
            paintRect.setAntiAlias(true);
            paintRect.setStrokeWidth(1);
            paintRect.setStyle(Style.STROKE);

            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    int mLeft = i * grid_width + mStartX;
                    int mTop = j * grid_width + mStartY;
                    int mRright = mLeft + grid_width;
                    int mBottom = mTop + grid_width;
                    canvas.drawRect(mLeft, mTop, mRright, mBottom, paintRect);
                }
            }

            // 画棋盘的外边框
            paintRect.setStrokeWidth(2);
            canvas.drawRect(mStartX, mStartY, mStartX + grid_width * GRID_SIZE,
                    mStartY + grid_width * GRID_SIZE, paintRect);
        }

        this.drawFightBoard(canvas);

        mPaint = new Paint();
        // 画棋子
        if (bitmapLoaded) {
            for (int i = 0; i < CHESS_GRID; i++) {
                for (int j = 0; j < CHESS_GRID; j++) {
                    if (mChessTable[i][j] == BLACK) {
                        // 通过图片来画
                        canvas.drawBitmap(mChessBW[0], mStartX + (i + 1)
                                        * grid_width - (chess_dia >> 1), mStartY
                                        + (j + 1) * grid_width - (chess_dia >> 1),
                                mPaint);

                    } else if (mChessTable[i][j] == WHITE) {
                        // 通过图片来画
                        canvas.drawBitmap(mChessBW[1], mStartX + (i + 1)
                                        * grid_width - (chess_dia >> 1), mStartY
                                        + (j + 1) * grid_width - (chess_dia >> 1),
                                mPaint);

                    }
                }
            }
        }
    }

    /**
     * 绘制对战面板
     */
    private void drawFightBoard(Canvas canvas){
        Paint boardPaint = new Paint();

        boardPaint.setColor(getResources().getColor(R.color.dgridmo));
        boardPaint.setAntiAlias(true);
        boardPaint.setStyle(Style.STROKE);

        // 画棋盘的外边框
        boardPaint.setStrokeWidth(8);

        int boardWidth = grid_width * (GRID_SIZE - 4) / 2;
        int boardHeight = grid_width * 5;

        int startY = GRID_SIZE * grid_width + 3 * grid_width;
        int startX = grid_width / 2;

        Player p1 = new Player("无名", "98胜14负", 20000);
        Player p2 = new Player("东方", "68胜20负", 50000);
        boardPaint.setColor(Color.argb(255, p1.rgbColor[0], p1.rgbColor[1], p1.rgbColor[2]));
        canvas.drawRect(startX, startY, startX + boardWidth,
                startY + boardHeight, boardPaint);



        this.drawPlayerInfo(canvas, startX * 2, startY + grid_width, p1);

        this.drawVsString(canvas, startX + boardWidth + (int)(grid_width * 0.6), (int)(startY + grid_width * 1.5));

        int start2X = startX + boardWidth + 3 * grid_width;

        boardPaint.setColor(Color.argb(255, p2.rgbColor[0], p2.rgbColor[1], p2.rgbColor[2]));
        canvas.drawRect(start2X, startY, start2X + boardWidth,
                startY + boardHeight, boardPaint);

        this.drawPlayerInfo(canvas, start2X + grid_width/2, startY + grid_width, p2);
    }

    private void drawVsString(Canvas canvas, int x, int y) {
        Paint textpaint = new Paint();
        canvas.drawBitmap(vsImage, x, y, textpaint);

    }

    private void drawPlayerInfo(Canvas canvas, int strStartX, int strStartY, Player player){
        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTypeface(Typeface.MONOSPACE);
        textpaint.setAntiAlias(true);//去除锯齿
        textpaint.setFilterBitmap(true);//对位图进行滤波处理
        int fontsize = (int)(grid_width * 0.5);
        textpaint.setTextSize(fontsize);

        canvas.drawText("姓名: " + player.name, strStartX, strStartY, textpaint);
        canvas.drawText("战绩: " + player.dp, strStartX, strStartY + grid_width , textpaint);
        canvas.drawText("称号: ", strStartX, strStartY + 2 * grid_width, textpaint);

        textpaint.setColor(Color.argb(255, player.rgbColor[0]/3, player.rgbColor[1]/3, player.rgbColor[2]/3));
        textpaint.setStyle(Style.FILL);
        int titleTextSize = (int)(fontsize * 1.2);
        RectF titleBkRect = new RectF(
                strStartX + fontsize * 3,
                strStartY + grid_width + titleTextSize/2,
                strStartX + fontsize * 3 + (int)(titleTextSize * 2.1),
                strStartY + 2 * grid_width + titleTextSize/2
        );
        canvas.drawRoundRect(
                titleBkRect,
                12,12,
                textpaint);

        textpaint.setColor(Color.argb(255, player.rgbColor[0], player.rgbColor[1], player.rgbColor[2]));
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        textpaint.setTypeface(font);
        textpaint.setTextSize(titleTextSize);

        canvas.drawText(player.title, strStartX + fontsize * 3, strStartY + 2 * grid_width, textpaint);
        textpaint.setColor(Color.BLACK);
        textpaint.setTypeface(Typeface.MONOSPACE);
        textpaint.setTextSize(fontsize);
        canvas.drawText("积分: " + player.score, strStartX, strStartY + 3 * grid_width, textpaint);
    }

    private void putChess(int x, int y, int blackwhite) {
        mChessTable[x][y] = blackwhite;
    }

    private boolean checkWin(int wbflag) {
        for (int i = 0; i < GRID_SIZE - 1; i++)
            for (int j = 0; j < GRID_SIZE - 1; j++) {
                // 就是检查四条线，参见英国国旗
                // 检测横轴五个相连
                if (((i + 4) < (GRID_SIZE - 1))
                        && (mChessTable[i][j] == wbflag)
                        && (mChessTable[i + 1][j] == wbflag)
                        && (mChessTable[i + 2][j] == wbflag)
                        && (mChessTable[i + 3][j] == wbflag)
                        && (mChessTable[i + 4][j] == wbflag)) {
                    // Log.e("check win or loss:", wbflag + "win");

                    mWinFlag = wbflag;
                }

                // 纵轴5个相连
                if (((j + 4) < (GRID_SIZE - 1))
                        && (mChessTable[i][j] == wbflag)
                        && (mChessTable[i][j + 1] == wbflag)
                        && (mChessTable[i][j + 2] == wbflag)
                        && (mChessTable[i][j + 3] == wbflag)
                        && (mChessTable[i][j + 4] == wbflag)) {
                    // Log.e("check win or loss:", wbflag + "win");

                    mWinFlag = wbflag;
                }

                // 左上到右下5个相连
                if (((j + 4) < (GRID_SIZE - 1)) && ((i + 4) < (GRID_SIZE - 1))
                        && (mChessTable[i][j] == wbflag)
                        && (mChessTable[i + 1][j + 1] == wbflag)
                        && (mChessTable[i + 2][j + 2] == wbflag)
                        && (mChessTable[i + 3][j + 3] == wbflag)
                        && (mChessTable[i + 4][j + 4] == wbflag)) {
                    // Log.e("check win or loss:", wbflag + "win");

                    mWinFlag = wbflag;
                }

                // 右上到左下5个相连
                if (((i - 4) >= 0) && ((j + 4) < (GRID_SIZE - 1))
                        && (mChessTable[i][j] == wbflag)
                        && (mChessTable[i - 1][j + 1] == wbflag)
                        && (mChessTable[i - 2][j + 2] == wbflag)
                        && (mChessTable[i - 3][j + 3] == wbflag)
                        && (mChessTable[i - 4][j + 4] == wbflag)) {
                    // Log.e("check win or loss:", wbflag + "win");

                    mWinFlag = wbflag;
                }
            }

        if (mWinFlag == wbflag) {
            return true;
        } else
            return false;

    }

    private boolean checkFull() {
        int mNotEmpty = 0;
        for (int i = 0; i < GRID_SIZE - 1; i++)
            for (int j = 0; j < GRID_SIZE - 1; j++) {
                if (mChessTable[i][j] != 0)
                    mNotEmpty += 1;
            }

        if(mNotEmpty == (GRID_SIZE - 1) * (GRID_SIZE - 1))
            return true;
        else
            return false;
    }

    private void showTextView(CharSequence mT) {
        this.mStatusTextView.setText(mT);
        mStatusTextView.setVisibility(View.VISIBLE);

    }

}