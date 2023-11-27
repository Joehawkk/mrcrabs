package kzn.mrcrabs.project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Coconut {
    Bitmap coconut[] = new Bitmap[3];
    int coconutFrame = 0;
    int coconutX, coconutY, coconutVelocity;
    Random random;

    public Coconut(Context context){
        coconut[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.coconut0);
        coconut[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.coconut1);
        coconut[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.coconut2);
        random = new Random();
        resetPosition();
    }

    public Bitmap getCoconut(int coconutFrame){
        return coconut[coconutFrame];
    }

    public int getCoconutWidth(){
        return coconut[0].getWidth();
    }

    public int getCoconutHeight(){
        return coconut[0].getHeight();
    }

    public void resetPosition() {
        coconutX = random.nextInt(GameView.dWidth - getCoconutWidth());
        coconutY = -200 + random.nextInt(600) * -1;
        coconutVelocity = 35 + random.nextInt(16);
    }
}
