package kzn.mrcrabs.project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Coconut {

    // ── Sprite ───────────────────────────────────────────────────────────────
    Bitmap coconut[] = new Bitmap[3];
    int coconutFrame = 0;
    float coconutX, coconutY, coconutVelocity;
    Random random;

    // ── Rotation ─────────────────────────────────────────────────────────────
    float rotRate;   // degrees per pixel fallen (set in resetPosition, sign = direction)

    // ── Type ─────────────────────────────────────────────────────────────────
    public static final int TYPE_NORMAL = 0; // standard,  +10 pts
    public static final int TYPE_FAST   = 1; // red tint,  +20 pts, 1.6× speed, 0.85× size
    public static final int TYPE_GIANT  = 2; // purple,    +30 pts, 0.7× speed, 1.7× size
    public static final int TYPE_GOLDEN = 3; // gold tint, +50 pts, normal speed

    public int   type  = TYPE_NORMAL;
    public float scale = 1f;   // uniform visual + hitbox scale

    public Coconut(Context context) {
        coconut[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.coconut0);
        coconut[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.coconut1);
        coconut[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.coconut2);
        random = new Random();
        resetPosition();
    }

    public Bitmap getCoconut(int frame) { return coconut[frame]; }

    public int getCoconutWidth()  { return coconut[0].getWidth(); }
    public int getCoconutHeight() { return coconut[0].getHeight(); }

    /** Effective width / height accounting for type scale (collision + placement). */
    public int effectiveWidth()  { return (int)(getCoconutWidth()  * scale); }
    public int effectiveHeight() { return (int)(getCoconutHeight() * scale); }

    public void resetPosition() {
        coconutX        = random.nextInt(Math.max(1, GameView.dWidth - getCoconutWidth()));
        coconutY        = -200 - random.nextInt(600);
        coconutVelocity = 20 + random.nextInt(8);   // ~1.5–2.5 s fall time at 30 ms/frame
        // Random spin: ±2–4 deg/px, direction random
        rotRate         = (random.nextBoolean() ? 1f : -1f) * (2f + random.nextFloat() * 2f);
    }
}
