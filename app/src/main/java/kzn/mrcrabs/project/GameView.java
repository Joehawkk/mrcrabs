package kzn.mrcrabs.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {

    Bitmap background, ground, crab;
    Rect rectBackground, rectGround;
    Context context;
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable;
    Paint textPaint = new Paint();
    Paint healthPaint = new Paint();
    float TEXT_SIZE = 120;
    int points = 0;
    int life = 3;
    static int dWidth, dHeight;
    Random random;
    float crabX, crabY;
    float oldX;
    float oldCrabX;
    ArrayList<Coconut> coconuts;
    ArrayList<Explosion> explosions;

    public GameView(Context context) {
        super(context);
        this.context = context;
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        crab = BitmapFactory.decodeResource(getResources(), R.drawable.crab);
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;
        rectBackground = new Rect(0, 0, dWidth, dHeight);
        rectGround = new Rect(0, dHeight - ground.getHeight(), dWidth, dHeight);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
        textPaint.setColor(Color.rgb(137, 255, 0));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));
        healthPaint.setColor(Color.GREEN);
        random = new Random();
        crabX = dWidth / 2 - crab.getWidth() / 2;
        crabY = dHeight - ground.getHeight() - crab.getHeight();
        coconuts = new ArrayList<>();
        explosions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            coconuts.add(new Coconut(context));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground, null, rectGround, null);
        canvas.drawBitmap(crab, crabX, crabY, null);

        for (int i = 0; i < coconuts.size(); i++) {
            Coconut c = coconuts.get(i);
            canvas.drawBitmap(c.getCoconut(c.coconutFrame), c.coconutX, c.coconutY, null);
            c.coconutFrame = (c.coconutFrame + 1) % 3;
            c.coconutY += c.coconutVelocity;

            // Check crab collision before ground — prevents scoring on a hit
            if (c.coconutX + c.getCoconutWidth() >= crabX
                    && c.coconutX <= crabX + crab.getWidth()
                    && c.coconutY + c.getCoconutHeight() >= crabY
                    && c.coconutY <= crabY + crab.getHeight()) {
                life--;
                c.resetPosition();
                if (life == 0) {
                    handler.removeCallbacks(runnable);
                    Intent intent = new Intent(context, GameOver.class);
                    intent.putExtra("points", points);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                    return;
                }
                continue;
            }

            if (c.coconutY + c.getCoconutHeight() >= dHeight - ground.getHeight()) {
                points += 10;
                Explosion explosion = new Explosion(context);
                explosion.explosionX = c.coconutX;
                explosion.explosionY = c.coconutY;
                explosions.add(explosion);
                c.resetPosition();
            }
        }

        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion e = explosions.get(i);
            canvas.drawBitmap(e.getExplosion(e.explosionFrame), e.explosionX, e.explosionY, null);
            e.explosionFrame++;
            if (e.explosionFrame > 3) {
                explosions.remove(i);
            }
        }

        if (life == 2) {
            healthPaint.setColor(Color.YELLOW);
        } else if (life == 1) {
            healthPaint.setColor(Color.RED);
        }
        canvas.drawRect(dWidth - 200, 30, dWidth - 200 + 60 * life, 80, healthPaint);
        canvas.drawText(String.valueOf(points), 20, TEXT_SIZE, textPaint);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(runnable);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        if (touchY >= crabY) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                oldX = event.getX();
                oldCrabX = crabX;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                float shift = oldX - touchX;
                float newCrabX = oldCrabX - shift;
                if (newCrabX <= 0)
                    crabX = 0;
                else if (newCrabX >= dWidth - crab.getWidth())
                    crabX = dWidth - crab.getWidth();
                else
                    crabX = newCrabX;
            }
        }
        return true;
    }
}
