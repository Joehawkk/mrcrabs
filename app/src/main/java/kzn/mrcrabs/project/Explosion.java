package kzn.mrcrabs.project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Explosion {

    private static Bitmap[] frames;
    int explosionFrame = 0;
    int explosionX, explosionY;

    public Explosion(Context context) {
        if (frames == null) {
            frames = new Bitmap[4];
            frames[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode0);
            frames[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode1);
            frames[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode2);
            frames[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode3);
        }
    }

    public Bitmap getExplosion(int frame) {
        return frames[frame];
    }
}
