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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {

    // ── Sprites ──────────────────────────────────────────────────────────────
    Bitmap background, ground, crab;
    Rect rectBackground, rectGround;
    Context context;

    // ── Game loop ────────────────────────────────────────────────────────────
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable;

    // ── Paints ───────────────────────────────────────────────────────────────
    Paint textPaint     = new Paint();
    Paint healthPaint   = new Paint();
    Paint shadowPaint   = new Paint();
    Paint streakPaint   = new Paint();
    Paint particlePaint = new Paint();
    Paint overlayPaint  = new Paint();
    Paint pauseBarPaint = new Paint();
    Paint popupPaint    = new Paint();
    Paint btnBgPaint    = new Paint();
    Paint btnTextPaint  = new Paint();
    Rect  pauseResumeRect = new Rect();
    Rect  pauseMenuRect   = new Rect();
    // Coconut type tints
    Paint fastPaint     = new Paint();
    Paint giantPaint    = new Paint();
    Paint goldenPaint   = new Paint();
    float TEXT_SIZE = 120;

    // ── State ────────────────────────────────────────────────────────────────
    int     points = 0;
    int     life   = 3;
    int     streak = 0;
    boolean isPaused = false;
    static int dWidth, dHeight;
    Random random;
    float crabX, crabY;
    float oldX, oldCrabX;

    // ── Countdown (3 → 2 → 1 → 0="GO!" → -1=running) ────────────────────────
    int      countdown = 3;
    Runnable countdownRunnable;

    // ── Game objects ─────────────────────────────────────────────────────────
    ArrayList<Coconut>    coconuts;
    ArrayList<Explosion>  explosions;
    ArrayList<Particle>   particles;
    ArrayList<ScorePopup> scorePopups;

    // ── Vibration ────────────────────────────────────────────────────────────
    Vibrator vibrator;

    // ── Time Trial ───────────────────────────────────────────────────────────
    final boolean isTimeMode;
    long gameStartMs;
    static final int TIME_LIMIT_MS = 60_000;

    // ── Particle palette ─────────────────────────────────────────────────────
    private static final int[] PARTICLE_COLORS_NORMAL =
        { 0xFF5C3317, 0xFF8B5E3C, 0xFFD2A679, 0xFFF5DEB3, 0xFFFFFFFF,
          0xFF3B1F0E, 0xFFA0522D, 0xFFE8C99A };
    private static final int[] PARTICLE_COLORS_FAST   =
        { 0xFFFF2200, 0xFFFF6600, 0xFFFF9900, 0xFF5C1100, 0xFFFFCC00 };
    private static final int[] PARTICLE_COLORS_GIANT  =
        { 0xFF9900FF, 0xFFCC66FF, 0xFF6600AA, 0xFF5C3317, 0xFFFFFFFF };
    private static final int[] PARTICLE_COLORS_GOLDEN =
        { 0xFFFFD700, 0xFFFFEC4C, 0xFFFFA500, 0xFFFFFFE0, 0xFFFFFFFF };

    private static class Particle {
        float x, y, vx, vy, alpha = 255f, size;
        int color;
    }

    // ── Score popups ─────────────────────────────────────────────────────────
    private static class ScorePopup {
        float x, y, alpha = 255f;
        String text;
        int color;
    }

    // ─────────────────────────────────────────────────────────────────────────

    public GameView(Context context, boolean isTimeMode) {
        super(context);
        this.context    = context;
        this.isTimeMode = isTimeMode;

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ground     = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
        crab       = BitmapFactory.decodeResource(getResources(), R.drawable.crab);

        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth  = size.x;
        dHeight = size.y;

        rectBackground = new Rect(0, 0, dWidth, dHeight);
        rectGround     = new Rect(0, dHeight - ground.getHeight(), dWidth, dHeight);

        handler  = new Handler(Looper.getMainLooper());
        runnable = this::invalidate;

        // ── Paint setup ──────────────────────────────────────────────────────
        textPaint.setColor(Color.rgb(137, 255, 0));
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));

        healthPaint.setColor(Color.GREEN);

        shadowPaint.setColor(Color.argb(70, 0, 0, 0));
        shadowPaint.setAntiAlias(true);

        streakPaint.setColor(Color.rgb(255, 210, 0));
        streakPaint.setTextSize(TEXT_SIZE * 0.65f);
        streakPaint.setTextAlign(Paint.Align.LEFT);
        streakPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));

        overlayPaint.setColor(Color.argb(170, 0, 0, 0));

        pauseBarPaint.setColor(Color.argb(200, 255, 255, 255));
        pauseBarPaint.setAntiAlias(true);

        btnBgPaint.setColor(Color.argb(230, 255, 255, 255));
        btnBgPaint.setAntiAlias(true);

        btnTextPaint.setTextSize(TEXT_SIZE * 0.55f);
        btnTextPaint.setTextAlign(Paint.Align.CENTER);
        btnTextPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));
        btnTextPaint.setAntiAlias(true);

        popupPaint.setTextSize(TEXT_SIZE * 0.65f);
        popupPaint.setTextAlign(Paint.Align.CENTER);
        popupPaint.setTypeface(ResourcesCompat.getFont(context, R.font.kenney_blocks));
        popupPaint.setAntiAlias(true);

        // Coconut type colour-filter tints
        fastPaint.setColorFilter(
            new PorterDuffColorFilter(0xAAFF2200, PorterDuff.Mode.SRC_ATOP));
        giantPaint.setColorFilter(
            new PorterDuffColorFilter(0x99AA00FF, PorterDuff.Mode.SRC_ATOP));
        goldenPaint.setColorFilter(
            new PorterDuffColorFilter(0xCCFFD700, PorterDuff.Mode.SRC_ATOP));

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        random = new Random();
        crabX = dWidth  / 2f - crab.getWidth()  / 2f;
        crabY = dHeight - ground.getHeight() - crab.getHeight();

        coconuts    = new ArrayList<>();
        explosions  = new ArrayList<>();
        particles   = new ArrayList<>();
        scorePopups = new ArrayList<>();

        for (int i = 0; i < 3; i++) coconuts.add(new Coconut(context));

        // ── Countdown: show "3" on first draw, tick every second ─────────────
        countdownRunnable = () -> {
            countdown--;
            invalidate();
            if (countdown > 0) {
                handler.postDelayed(countdownRunnable, 1000);
            } else if (countdown == 0) {
                // "GO!" — display for 650 ms then start
                handler.postDelayed(countdownRunnable, 650);
            } else {
                // Game starts — begin timer for time mode
                if (isTimeMode) gameStartMs = System.currentTimeMillis();
                handler.post(runnable);
            }
        };
        handler.postDelayed(countdownRunnable, 1000); // first tick in 1 s
    }

    // ── onDraw ───────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(background, null, rectBackground, null);
        canvas.drawBitmap(ground,     null, rectGround,     null);

        // ── Crab shadow + sprite ─────────────────────────────────────────────
        float shadowCx    = crabX + crab.getWidth() / 2f;
        float shadowTop   = crabY + crab.getHeight() - 10;
        float shadowHalfW = crab.getWidth() * 0.38f;
        canvas.drawOval(shadowCx - shadowHalfW, shadowTop,
                        shadowCx + shadowHalfW, shadowTop + 20, shadowPaint);
        canvas.drawBitmap(crab, crabX, crabY, null);

        // ── Countdown overlay ────────────────────────────────────────────────
        if (countdown >= 0) {
            canvas.drawRect(0, 0, dWidth, dHeight, overlayPaint);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(TEXT_SIZE * 2.5f);
            if (countdown == 0) {
                textPaint.setColor(Color.rgb(137, 255, 0));
                canvas.drawText("GO!", dWidth / 2f, dHeight / 2f, textPaint);
            } else {
                textPaint.setColor(Color.WHITE);
                canvas.drawText(String.valueOf(countdown), dWidth / 2f, dHeight / 2f, textPaint);
            }
            // Restore paint state
            textPaint.setTextSize(TEXT_SIZE);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(Color.rgb(137, 255, 0));
            return; // game logic frozen during countdown
        }

        // ── PAUSED overlay ───────────────────────────────────────────────────
        if (isPaused) {
            canvas.drawRect(0, 0, dWidth, dHeight, overlayPaint);

            int midX = dWidth  / 2;
            int midY = dHeight / 2;
            int btnW = dWidth * 3 / 5;
            int btnH = 110;
            int btnL = midX - btnW / 2;
            int btnR = midX + btnW / 2;

            // Title
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("PAUSED", midX, midY - 60, textPaint);
            textPaint.setTextAlign(Paint.Align.LEFT);

            // RESUME button
            pauseResumeRect.set(btnL, midY + 30, btnR, midY + 30 + btnH);
            canvas.drawRect(pauseResumeRect, btnBgPaint);
            btnTextPaint.setColor(Color.rgb(20, 110, 10));
            canvas.drawText("RESUME", midX, midY + 30 + (int)(btnH * 0.7f), btnTextPaint);

            // MAIN MENU button
            pauseMenuRect.set(btnL, midY + 175, btnR, midY + 175 + btnH);
            canvas.drawRect(pauseMenuRect, btnBgPaint);
            btnTextPaint.setColor(Color.rgb(140, 60, 10));
            canvas.drawText("MAIN MENU", midX, midY + 175 + (int)(btnH * 0.7f), btnTextPaint);

            return;
        }

        // ── Pause icon (two bars, top-centre) ────────────────────────────────
        float px = dWidth / 2f;
        canvas.drawRect(px - 24, 20, px - 8,  68, pauseBarPaint);
        canvas.drawRect(px +  8, 20, px + 24, 68, pauseBarPaint);

        // ── Timer check (time trial) ─────────────────────────────────────────
        if (isTimeMode) {
            if (System.currentTimeMillis() - gameStartMs >= TIME_LIMIT_MS) {
                endGame(); return;
            }
        }

        // ── Difficulty progression ───────────────────────────────────────────
        int targetCoconuts = 3;
        if      (points >= 1200) targetCoconuts = 5;
        else if (points >= 600)  targetCoconuts = 4;

        while (coconuts.size() < targetCoconuts) {
            Coconut extra = new Coconut(context);
            coconuts.add(extra);
            placeInZone(extra);
        }

        // ── Coconuts ─────────────────────────────────────────────────────────
        for (int i = 0; i < coconuts.size(); i++) {
            Coconut c   = coconuts.get(i);
            float   cocW = c.effectiveWidth();
            float   cocH = c.effectiveHeight();

            // Tint paint by type (null = no tint for normal)
            Paint cp = null;
            if      (c.type == Coconut.TYPE_FAST)   cp = fastPaint;
            else if (c.type == Coconut.TYPE_GIANT)  cp = giantPaint;
            else if (c.type == Coconut.TYPE_GOLDEN) cp = goldenPaint;

            // Smooth rotation based on how far the coconut has fallen
            float cx     = c.coconutX + cocW / 2f;
            float cy     = c.coconutY + cocH / 2f;
            float angle  = c.coconutY * c.rotRate;
            Rect dest    = new Rect((int) c.coconutX, (int) c.coconutY,
                                    (int)(c.coconutX + cocW), (int)(c.coconutY + cocH));
            canvas.save();
            canvas.rotate(angle, cx, cy);
            canvas.drawBitmap(c.getCoconut(0), null, dest, cp);
            canvas.restore();

            c.coconutY += c.coconutVelocity;

            // ── Collision ────────────────────────────────────────────────────
            if (c.coconutX + cocW >= crabX
                    && c.coconutX <= crabX + crab.getWidth()
                    && c.coconutY + cocH >= crabY
                    && c.coconutY        <= crabY + crab.getHeight()) {

                streak = 0;
                vibrate();
                placeInZone(c);

                if (!isTimeMode) {
                    life--;
                    if (life == 0) { endGame(); return; }
                }
                continue;
            }

            // ── Reached ground ───────────────────────────────────────────────
            if (c.coconutY + cocH >= dHeight - ground.getHeight()) {
                int base;
                switch (c.type) {
                    case Coconut.TYPE_FAST:   base = 20; break;
                    case Coconut.TYPE_GIANT:  base = 30; break;
                    case Coconut.TYPE_GOLDEN: base = 50; break;
                    default:                  base = 10; break;
                }
                // Time mode gets streak bonus; classic always flat
                int earned = base + (isTimeMode ? Math.min(streak * 5, 50) : 0);
                points += earned;
                streak++;

                // Colour for this type's popup
                int popColor;
                switch (c.type) {
                    case Coconut.TYPE_FAST:   popColor = 0xFFFF4400; break;
                    case Coconut.TYPE_GIANT:  popColor = 0xFFCC66FF; break;
                    case Coconut.TYPE_GOLDEN: popColor = 0xFFFFD700; break;
                    default:                  popColor = Color.rgb(137, 255, 0); break;
                }
                ScorePopup sp = new ScorePopup();
                sp.x     = c.coconutX + cocW / 2f;
                sp.y     = dHeight - ground.getHeight() - 20;
                sp.text  = "+" + earned;
                sp.color = popColor;
                scorePopups.add(sp);

                Explosion exp = new Explosion(context);
                exp.explosionX = c.coconutX;
                exp.explosionY = dHeight - ground.getHeight() - cocH;
                explosions.add(exp);

                spawnParticles(c.coconutX + cocW / 2f,
                               dHeight - ground.getHeight(), c.type);
                placeInZone(c);
            }
        }

        // ── Explosions ───────────────────────────────────────────────────────
        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion e = explosions.get(i);
            canvas.drawBitmap(e.getExplosion(e.explosionFrame),
                              e.explosionX, e.explosionY, null);
            if (++e.explosionFrame > 3) explosions.remove(i);
        }

        // ── Particles (pixel shards) ─────────────────────────────────────────
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.x  += p.vx;  p.y  += p.vy;
            p.vy += 0.6f;  p.alpha -= 12f;
            if (p.alpha <= 0f) { particles.remove(i); continue; }
            particlePaint.setColor(p.color);
            particlePaint.setAlpha((int) p.alpha);
            canvas.drawRect(p.x - p.size, p.y - p.size,
                            p.x + p.size, p.y + p.size, particlePaint);
        }

        // ── Score popups (float upward, fade out) ────────────────────────────
        for (int i = scorePopups.size() - 1; i >= 0; i--) {
            ScorePopup sp = scorePopups.get(i);
            sp.y    -= 4f;
            sp.alpha -= 7f;
            if (sp.alpha <= 0f) { scorePopups.remove(i); continue; }
            popupPaint.setColor(sp.color);
            popupPaint.setAlpha((int) sp.alpha);
            canvas.drawText(sp.text, sp.x, sp.y, popupPaint);
        }

        // ── HUD ──────────────────────────────────────────────────────────────
        canvas.drawText(String.valueOf(points), 20, TEXT_SIZE, textPaint);

        if (isTimeMode) {
            long remaining = TIME_LIMIT_MS - (System.currentTimeMillis() - gameStartMs);
            int  secLeft   = (int)(remaining / 1000) + 1;
            textPaint.setTextAlign(Paint.Align.RIGHT);
            textPaint.setColor(secLeft <= 10 ? Color.RED : Color.rgb(137, 255, 0));
            canvas.drawText(secLeft + "s", dWidth - 20, TEXT_SIZE, textPaint);
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setColor(Color.rgb(137, 255, 0));
        } else {
            if      (life == 2) healthPaint.setColor(Color.YELLOW);
            else if (life == 1) healthPaint.setColor(Color.RED);
            canvas.drawRect(dWidth - 200, 30, dWidth - 200 + 60 * life, 80, healthPaint);
        }

        if (streak >= 2) canvas.drawText("x" + streak, 20, TEXT_SIZE + 80, streakPaint);

        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    // ── Spawn helpers ─────────────────────────────────────────────────────────

    /**
     * N+1 zone placement — one zone always empty (guaranteed dodge lane).
     * Also assigns coconut type and applies its speed/scale modifiers.
     */
    private void placeInZone(Coconut target) {
        target.resetPosition();

        // ── Assign type (probability ramps with score) ────────────────────────
        int r = random.nextInt(100);
        if (points >= 300 && r < 8) {
            target.type  = Coconut.TYPE_GOLDEN;
            target.scale = 1f;
        } else if (points >= 150 && r < 22) {
            target.type  = Coconut.TYPE_GIANT;
            target.scale = 1.7f;
        } else if (points >= 80 && r < 40) {
            target.type  = Coconut.TYPE_FAST;
            target.scale = 0.85f;
        } else {
            target.type  = Coconut.TYPE_NORMAL;
            target.scale = 1f;
        }

        // ── Type speed modifiers ──────────────────────────────────────────────
        if (target.type == Coconut.TYPE_FAST)  target.coconutVelocity *= 1.4f;
        if (target.type == Coconut.TYPE_GIANT) target.coconutVelocity *= 0.7f;

        // ── N+1 zone placement (one zone always stays free) ───────────────────
        int n          = coconuts.size();
        int totalZones = n + 1;
        int zoneW      = dWidth / totalZones;
        int cocW       = target.effectiveWidth();

        boolean[] occupied = new boolean[totalZones];
        for (int i = 0; i < coconuts.size(); i++) {
            Coconut c = coconuts.get(i);
            if (c == target) continue;
            int z = (int)(c.coconutX / zoneW);
            if (z < 0) z = 0;
            if (z >= totalZones) z = totalZones - 1;
            occupied[z] = true;
        }

        int freeCount = 0;
        for (boolean b : occupied) if (!b) freeCount++;
        int[] freeZones = new int[freeCount];
        int fi = 0;
        for (int i = 0; i < totalZones; i++) if (!occupied[i]) freeZones[fi++] = i;

        int zIdx = (freeCount > 0)
            ? freeZones[random.nextInt(freeCount)]
            : random.nextInt(totalZones);

        int pad  = Math.max(4, (zoneW - cocW) / 5);
        int minX = zIdx * zoneW + pad;
        int maxX = (zIdx + 1) * zoneW - cocW - pad;

        target.coconutX = (maxX > minX)
            ? minX + random.nextInt(maxX - minX)
            : Math.max(0, zIdx * zoneW + (zoneW - cocW) / 2);

        // ── Difficulty speed bonus: +1 px/frame per 200 pts, cap +6 ─────────
        target.coconutVelocity += Math.min(points / 200f, 6f);
    }

    private void spawnParticles(float cx, float groundY, int coconutType) {
        int count;
        int[] palette;
        switch (coconutType) {
            case Coconut.TYPE_FAST:
                count = 18; palette = PARTICLE_COLORS_FAST;   break;
            case Coconut.TYPE_GIANT:
                count = 30; palette = PARTICLE_COLORS_GIANT;  break;
            case Coconut.TYPE_GOLDEN:
                count = 28; palette = PARTICLE_COLORS_GOLDEN; break;
            default:
                count = 18; palette = PARTICLE_COLORS_NORMAL; break;
        }
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x    = cx + (random.nextFloat() - 0.5f) * 20f;
            p.y    = groundY;
            double angle = random.nextDouble() * Math.PI * 2;
            float  speed = 3f + random.nextFloat() * 9f;
            p.vx   = (float)(Math.cos(angle) * speed);
            p.vy   = (float)(Math.sin(angle) * speed) - 5f;
            p.size = random.nextInt(4) + 3;
            p.color = palette[random.nextInt(palette.length)];
            particles.add(p);
        }
    }

    // ── End / vibrate ─────────────────────────────────────────────────────────

    private void goToMenu() {
        handler.removeCallbacks(runnable);
        handler.removeCallbacks(countdownRunnable);
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    private void endGame() {
        handler.removeCallbacks(runnable);
        handler.removeCallbacks(countdownRunnable);
        Intent intent = new Intent(context, GameOver.class);
        intent.putExtra("points",   points);
        intent.putExtra("timeMode", isTimeMode);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    @SuppressWarnings("deprecation")
    private void vibrate() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
        else
            vibrator.vibrate(80);
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    public void pause() {
        if (!isPaused) handler.removeCallbacks(runnable);
    }

    public void resume() {
        // Don't restart game loop if countdown is still running
        if (!isPaused && countdown < 0) {
            handler.removeCallbacks(runnable);
            handler.post(runnable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(runnable);
        handler.removeCallbacks(countdownRunnable);
    }

    // ── Touch ────────────────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (countdown >= 0) return true;   // block all input during countdown

        // ── Pause menu buttons ───────────────────────────────────────────────
        if (isPaused) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int tx = (int) event.getX(), ty = (int) event.getY();
                if (pauseResumeRect.contains(tx, ty)) {
                    isPaused = false;
                    handler.post(runnable);
                } else if (pauseMenuRect.contains(tx, ty)) {
                    goToMenu();
                } else if (ty < 90) {
                    isPaused = false;
                    handler.post(runnable);
                }
            }
            return true;
        }

        // ── Open pause via top strip ─────────────────────────────────────────
        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getY() < 90) {
            isPaused = true;
            handler.removeCallbacks(runnable);
            invalidate();
            return true;
        }

        float touchX = event.getX();
        float touchY = event.getY();
        if (touchY >= crabY) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                oldX     = touchX;
                oldCrabX = crabX;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                float newCrabX = oldCrabX - (oldX - touchX);
                if      (newCrabX <= 0)                        crabX = 0;
                else if (newCrabX >= dWidth - crab.getWidth()) crabX = dWidth - crab.getWidth();
                else                                           crabX = newCrabX;
            }
        }
        return true;
    }
}
