package comp4932.gogoproj;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int LOAD_IMG_1 = 1;
    private static final int LOAD_IMG_2 = 2;
    MaskView leftM;
    MaskView rightM;
    ImageView leftImage;
    ImageView rightImage;
    boolean morphed = false;
    SeekBar SBID;
    ArrayList<Bitmap> theFrames = new ArrayList<Bitmap>();
    Handler myHandler;
    static int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        leftImage = (ImageView) findViewById(R.id.left);
        rightImage = (ImageView) findViewById(R.id.right);
        leftM = (MaskView) findViewById(R.id.a);
        rightM = (MaskView) findViewById(R.id.b);
        leftM.setMV(rightM);
        rightM.setMV(leftM);
        SBID = (SeekBar)findViewById(R.id.seekBar);

        myHandler = new Handler() {
            public void handleMessage(Message msg) {
                ImageView view = (ImageView) findViewById(R.id.left);
                Drawable display = new BitmapDrawable(getResources(), theFrames.get(index++));
                view.setBackground(display);
            }
        };

        SBID.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(morphed) {
                    ImageView view = (ImageView) findViewById(R.id.left);
                    Drawable display = new BitmapDrawable(getResources(), theFrames.get(progress));
                    view.setBackground(display);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        leftImage.getLayoutParams().width = leftImage.getLayoutParams().width;
        leftImage.getLayoutParams().height = leftImage.getLayoutParams().height;
        rightImage.getLayoutParams().width = rightImage.getLayoutParams().width;
        rightImage.getLayoutParams().height = rightImage.getLayoutParams().height;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //function to load images in!
    public void LoadImage(View view) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        if (findViewById(R.id.buttonLoadPictureL) == view) {
            startActivityForResult(i, LOAD_IMG_1);
        } else if (findViewById(R.id.buttonLoadPictureR) == view) {
            startActivityForResult(i, LOAD_IMG_2);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == LOAD_IMG_1 || requestCode == LOAD_IMG_2) {
                try {
                    InputStream is = getContentResolver().openInputStream(data.getData());
                    BitmapDrawable bmd = new BitmapDrawable(null, BitmapFactory.decodeStream(is));
                    if (requestCode == LOAD_IMG_1) {
                        ((ImageView) findViewById(R.id.left)).setBackground(bmd);
                    } else if (requestCode == LOAD_IMG_2) {
                        ((ImageView) findViewById(R.id.right)).setBackground(bmd);
                    }
                } catch (FileNotFoundException e) {
                }
            }
        }
    }

    //function to undo while drawing lines
    public void undoLastLine(View view) {
        if (leftM.lines.size() > 0) {
            leftM.lines.remove(leftM.lines.size() - 1);
            rightM.lines.remove(rightM.lines.size() - 1);
            leftM.invalidate();
            rightM.invalidate();
        }
    }

    //function to make the midframe vectors
    public void makingMidVectors(ArrayList<Vector> vector1, ArrayList<Vector> vector2) {
        for (int i = 0; i < leftM.lines.size(); i++) {
            Line dest = rightM.lines.get(i);
            Line src = leftM.lines.get(i);

            EditText et=(EditText)findViewById(R.id.frameNum);
            String txt=et.getText().toString();
            int numberFrames = Integer.parseInt(txt);

            Vector v = new Vector((float) (dest.startPt.x - src.startPt.x) / (numberFrames + 1), (float) (dest.endPt.x - src.endPt.x) / (numberFrames + 1), (float) (dest.startPt.y - src.startPt.y) / (numberFrames + 1), (float) (dest.endPt.y - src.endPt.y) / (numberFrames + 1));
            vector1.add(v);
            v = new Vector(((float) src.startPt.x - dest.startPt.x) / (numberFrames + 1), (float) (src.endPt.x - dest.endPt.x) / (numberFrames + 1), (float) (src.startPt.y - dest.startPt.y) / (numberFrames + 1), (float) (src.endPt.y - dest.endPt.y) / (numberFrames + 1));
            vector2.add(v);
        }
    }

    //the function to draw the bitmap projections based on lines
    public ArrayList<Bitmap> drawBitmap(ImageView src, ImageView dst, ArrayList<Vector> vector) {
        Bitmap newbm;
        Drawable v1Draw = src.getBackground(); //get both backgrounds
        Bitmap v1BG = ((BitmapDrawable) v1Draw).getBitmap();
        ArrayList<Bitmap> framesList = new ArrayList<>();

        v1BG = Bitmap.createScaledBitmap(v1BG, 500, 500, false);
        double newX, newY;
        int width = leftImage.getWidth();
        int height = leftImage.getHeight();

        //read integer, create arraylist of empty bitmaps for all frames
        EditText et=(EditText)findViewById(R.id.frameNum);
        String txt=et.getText().toString();
        int numberFrames = Integer.parseInt(txt);

        for (int i = 1; i <= numberFrames; i++) { //for each frame
            ArrayList<Line> tempLines = new ArrayList();
            newbm = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            for (int q = 0; q < leftM.lines.size(); q++) {
                Line sourceLine = rightM.lines.get(q);
                Vector d = vector.get(q);
                Point pt1 = new Point(sourceLine.startPt.x - Math.round(d.x1 * i), sourceLine.startPt.y - Math.round(d.y1 * i));
                Point pt2 = new Point(sourceLine.endPt.x - Math.round(d.x2 * i), sourceLine.endPt.y - Math.round(d.y2 * i));
                Line l = new Line(pt1, pt2, true);
                tempLines.add(l);
            }
            //each pixel, x and y
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double totalWeight = 0;
                    double xDist = 0;
                    double yDist = 0;
                    //for each line in the mask
                    for (int l = 0; l < leftM.lines.size(); l++) {

                        Line destLine = rightM.lines.get(l);
                        Line srcLine = tempLines.get(l);

                        double ptx = destLine.startPt.x - x;
                        double pty = destLine.startPt.y - y;

                        double normX = (destLine.endPt.y - destLine.startPt.y) * -1;
                        double normY = (destLine.endPt.x - destLine.startPt.x);

                        double d = ((normX * ptx) + (normY * pty)) / ((Math.sqrt(normX * normX + normY * normY))); //clean up all this shit since half of it probably isn't needed
                        double f = (((destLine.endPt.x - destLine.startPt.x) * (ptx * -1)) + ((destLine.endPt.y - destLine.startPt.y) * (pty * -1)));
                        double fPercent = f / Math.pow((Math.sqrt(Math.pow((destLine.endPt.x - destLine.startPt.x),2) + Math.pow((destLine.endPt.y - destLine.startPt.y),2))),2);

                        normX = (srcLine.endPt.y - srcLine.startPt.y) * -1;
                        normY = (srcLine.endPt.x - srcLine.startPt.x);

                        newX = ((srcLine.startPt.x) + (fPercent * (srcLine.endPt.x - srcLine.startPt.x))) - ((d * normX / (Math.sqrt(normX * normX + normY * normY))));
                        newY = ((srcLine.startPt.y) + (fPercent * (srcLine.endPt.y - srcLine.startPt.y))) - ((d * normY / (Math.sqrt(normX * normX + normY * normY))));

                        if(fPercent<0) {
                            d = Math.sqrt(Math.pow(x - destLine.startPt.x, 2) + Math.pow(y - destLine.startPt.y, 2));
                        }
                        if(fPercent>1) {
                            d = Math.sqrt(Math.pow(x - destLine.endPt.x, 2) + Math.pow(y - destLine.endPt.y, 2));
                        }

                        double weight = Math.pow((1 / (0.01 + Math.abs(d))),2);

                        totalWeight += weight;
                        xDist += (newX - x) * weight;
                        yDist += (newY - y) * weight;
                    }
                    newX = x + (xDist / totalWeight);
                    newY = y + (yDist / totalWeight);

                    //check to see if the pixel is out of bounds of the bitmap
                    if (xDist == x - newX)
                        newX = x - xDist;
                    if (yDist == y - newY)
                        newY = y - yDist;
                    if (newX < 0)
                        newX = 0;
                    if (newY < 0)
                        newY = 0;
                    if (newY >= 500)
                        newY = 499;
                    if (newX >= 500)
                        newX = 499;

                    //set this pixel from the averaged projected pixel
                    newbm.setPixel(x, y, v1BG.getPixel((int) Math.abs(newX), (int) Math.abs(newY)));
                }
            }
            //add this bitmap to the global framelist
            framesList.add(newbm);
        }
        return framesList;
    }

    //function to do the cross dissolve!
    public void crossDissolve(ArrayList<Bitmap> frames1, ArrayList<Bitmap> frames2) {
        int width = leftImage.getWidth();
        int height = leftImage.getHeight();
        int weight = 1;

        EditText et=(EditText)findViewById(R.id.frameNum);
        String txt=et.getText().toString();
        int numberFrames = Integer.parseInt(txt);

        //get the opposite weight
        int oppWeight = numberFrames-weight;


        //for each frame
        for(int i=0; i < frames1.size();i++) {
            Bitmap view1Background = frames1.get(i);
            Bitmap view2Background = frames2.get(numberFrames-i - 1);
            Bitmap newbm = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);

            //for each pixel
            for(int x=0;x<width;x++) {
                for(int y=0;y<height;y++) {
                    int pixel1 = view1Background.getPixel(x, y);
                    int pixel2 = view2Background.getPixel(x, y);
                    int red = Color.red(pixel1)*oppWeight/numberFrames+Color.red(pixel2)*weight/numberFrames;
                    int green = Color.green(pixel1)*oppWeight/numberFrames+Color.green(pixel2)*weight/numberFrames;
                    int blue = Color.blue(pixel1)*oppWeight/numberFrames+Color.blue(pixel2)*weight/numberFrames;
                    newbm.setPixel(x, y, Color.rgb(red, green, blue));
                }
            }
            theFrames.add(newbm);
            weight++;
            oppWeight = numberFrames-weight;
        }
    }

    //the main function being called that does the entire morph
    public void theWholeMorph(View view) {
        //get the number of frames
        EditText et=(EditText)findViewById(R.id.frameNum);
        String txt=et.getText().toString();
        int numberFrames = Integer.parseInt(txt);

        //clear the list and make the midframe vectors
        theFrames.clear();
        ArrayList<Vector> vectorDiff = new ArrayList();
        ArrayList<Vector> vectorDiff2 = new ArrayList();
        ArrayList<Bitmap> framesForward, framesBackward;
        theFrames.add(((BitmapDrawable) leftImage.getBackground()).getBitmap());
        makingMidVectors(vectorDiff, vectorDiff2);

        //create the frames
        framesForward = drawBitmap(leftImage, rightImage, vectorDiff);
        framesBackward = drawBitmap(rightImage, leftImage, vectorDiff2);

        //cross dissolve
        crossDissolve(framesForward, framesBackward);
        SBID.setMax(numberFrames);
        morphed = true;
        leftM.lines.clear();
    }

    //this is the function that plays the frames in order
    public void play(View view) {
        if(!morphed) {
            return;
        }

        EditText et=(EditText)findViewById(R.id.frameNum);
        String txt=et.getText().toString();
        final int numberFrames = Integer.parseInt(txt);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if(index == numberFrames+1) {
                    this.cancel();
                    index = 0;
                }
                else
                    myHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, 300);
    }
}