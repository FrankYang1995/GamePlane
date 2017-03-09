/*
 * This is a simple Android game - CombatAircraft
 * This file is the main file of the game.
 * It initialize almost all items that needed to display on the screen
 * 
 * @Package	com.ispring.gameplane.game
 * @author	iSpring
 * @author	ReactNativeX
 * @link	https://github.com/iSpring/GamePlane
 */
package com.ispring.gameplane.game;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ispring.gameplane.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GameView extends View {

    private Paint paint;
    private Paint textPaint;
    private CombatAircraft combatAircraft = null;
    private List<Sprite> sprites = new ArrayList<Sprite>();
    private List<Sprite> spritesNeedAdded = new ArrayList<Sprite>();
    /*
     * Arraylist: List
     * The arraylist stores all the *.png file that needed to use in the game
     * There are 11 numbers, each represent one file.
     * 0:combatAircraft
     * 1:explosion
     * 2:yellowBullet
     * 3:blueBullet
     * 4:smallEnemyPlane
     * 5:middleEnemyPlane
     * 6:bigEnemyPlane
     * 7:bombAward
     * 8:bulletAward
     * 9:pause1
     * 10:pause2
     * 11:bomb
     */
    private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
    /*
     * Float variable: density
     * Variable to represent density of screen
     */
    private float density = getResources().getDisplayMetrics().density;
    /*
     * Int varaible: STATUS_GAME_STARTED
     * 				 STATUS_GAME_PAUSED
     * 				 STATUS_GAME_OVER
     * 				 STATUS_GAME_DESTROYED
     * 4 integers variable to represent 4 status of the game.
     * 1 represents game is started
     * 2 represents game is paused
     * 3 represents game is over
     * 4 represents game if not started yet
     */
    public static final int STATUS_GAME_STARTED = 1;
    public static final int STATUS_GAME_PAUSED = 2;
    public static final int STATUS_GAME_OVER = 3;
    public static final int STATUS_GAME_DESTROYED = 4;
    private int status = STATUS_GAME_DESTROYED;
    /*
     * Long variable:	frame
     * 					score
     * Frame is the total frame that needed to draw
     * Score is the total score
     */
    private long frame = 0;
    private long score = 0;
    private float fontSize = 12;//Default font size
    /*
     * Float variable:	fontSize2
     * This size is used for the dialog displayed at the end of the game
     */
    private float fontSize2 = 20;
    private float borderSize = 2;//Default font size for dialog
    /*
     * Rect variable:	continueRect
     * The button rectangle for "Restart" and "Continue"
     */
    private Rect continueRect = new Rect();

    /*
     * Variables of Touching events
     * 
     * TOUCH_MOVE:			Movements
     * TOUCH_SINGLE_CLICK:  Single Click on screen
     * TOUCH_DOUBLE_CLICK:  Double Click on screen
     */
    private static final int TOUCH_MOVE = 1;
    private static final int TOUCH_SINGLE_CLICK = 2;
    private static final int TOUCH_DOUBLE_CLICK = 3;
    /*
     * Variables of differentiate single click and double cilck
     * 
     * See Also:
     * 			Function:	resolveTouchType
     */
    private static final int singleClickDurationTime = 200;
    private static final int doubleClickDurationTime = 300;
    private long lastSingleClickTime = -1;//The time of last single click
    private long touchDownTime = -1;//the time of touching down 
    private long touchUpTime = -1;//the time of touching up
    private float touchX = -1;//X coordinate of touching position
    private float touchY = -1;//Y coordiante of touching position
    
    /*
     * Constructor: GameView
     * Initializes the game
     * @overload
     */
    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /*
     * Function:	init
     * Initilzes paint tool and font size
     * 
     * @param ArrributeSet	Android variable defined in XML
     * @param int			Android variable defined in XML
     * @return				None
     */
    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GameView, defStyle, 0);
        a.recycle();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        textPaint.setColor(0xff000000);
        fontSize = textPaint.getTextSize();
        fontSize *= density;
        fontSize2 *= density;
        textPaint.setTextSize(fontSize);
        borderSize *= density;
    }
    
    /*
     * Function: start
     * Starts the game
     * 
     * @param int[]	The ID of picture that will be used in this game.
     * @return		None
     */
    public void start(int[] bitmapIds){
        destroy();
        for(int bitmapId : bitmapIds){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bitmapId);
            bitmaps.add(bitmap);
        }
        startWhenBitmapsReady();
    }
    
    /*
     * Function: startWhenBitmapsReady
     * Set the status of the game as started
     * 
     * @param	None
     * @Return	None
     */
    private void startWhenBitmapsReady(){
        combatAircraft = new CombatAircraft(bitmaps.get(0));
        status = STATUS_GAME_STARTED;
        postInvalidate();//Redraw function built in Android
    }
    
    /*
     * Function: restart
     * Destroy previous data and set the status of the game as started
     * 
     * @param	None
     * @Return	None
     */
    private void restart(){
        destroyNotRecyleBitmaps();
        startWhenBitmapsReady();
    }

    /*
     * Function: pause
     * Set the status of the game as paused
     * 
     * @param	None
     * @Return	None
     */
    public void pause(){
        status = STATUS_GAME_PAUSED;
    }

    /*
     * Function: resume
     * Set the status of the game as started
     * 
     * @param	None
     * @Return	None
     */
    private void resume(){
        status = STATUS_GAME_STARTED;
        postInvalidate();
    }

    /*
     * Funtion: getScore
     * Return the score that player earned 
     * 
     * @param		None
     * @return long Long variable of score
     */
    private long getScore(){
        return score;
    }

    /*-------------------------------draw-------------------------------------*/

    /*
     * Function: onDraw
     * Draw the status of the game
     * 
     * @param Canvas	Paint tool built in Android
     * @return			None
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if(isSingleClick()){
            onSingleClick(touchX, touchY);
        }

        super.onDraw(canvas);

        if(status == STATUS_GAME_STARTED){
            drawGameStarted(canvas);
        }else if(status == STATUS_GAME_PAUSED){
            drawGamePaused(canvas);
        }else if(status == STATUS_GAME_OVER){
            drawGameOver(canvas);
        }
    }

    /*
     * Function: drawGameStarted
     * Draw all the items as the game is started
     * 
     * @param Canvas	Paint tool built in Android
     * @return			None
     */
    private void drawGameStarted(Canvas canvas){

        drawScoreAndBombs(canvas);

        /*
         * At the begining of the game, put the aircraft to the bottome and mif of the screen
         */
        if(frame == 0){
            float centerX = canvas.getWidth() / 2;
            float centerY = canvas.getHeight() - combatAircraft.getHeight() / 2;
            combatAircraft.centerTo(centerX, centerY);
        }

        //Create enemy aircraft
        if(spritesNeedAdded.size() > 0){
            sprites.addAll(spritesNeedAdded);
            spritesNeedAdded.clear();
        }

        //A fuction about bullets. See detail in the defination of this function.
        destroyBulletsFrontOfCombatAircraft();

        //A function about enemy aircraft. See detail in the defination of this function.
        removeDestroyedSprites();

        //Create enemy aircraft every 30 seconds
        if(frame % 30 == 0){
            createRandomSprites(canvas.getWidth());
        }
        frame++;

        //Check status of Sprites. Sprites include enemy aircraft, bullet and bomb
        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()){
            Sprite s = iterator.next();
            if(!s.isDestroyed()){
                s.draw(canvas, paint, this);
            }
            //Check whether enemy is destroyed
            if(s.isDestroyed()){
                iterator.remove();
            }
        }
        //Draw the palyer aircraft
        if(combatAircraft != null){
            combatAircraft.draw(canvas, paint, this);
            if(combatAircraft.isDestroyed()){
                //If player is attacked, game is over
                status = STATUS_GAME_OVER;
            }
            postInvalidate();
        }
    }

    /*
     * Function: drawGamePaused
     * Draw all the items that will dipaly if the game is paused
     * 
     * @param	Paint tool built in Android
     * @return	None
     */
    private void drawGamePaused(Canvas canvas){
        drawScoreAndBombs(canvas);
        for(Sprite s : sprites){
            s.onDraw(canvas, paint, this);
        }
        if(combatAircraft != null){
            combatAircraft.onDraw(canvas, paint, this);
        }

        //Draw dialog of scores
        drawScoreDialog(canvas, "缁х画");

        if(lastSingleClickTime > 0){
            postInvalidate();
        }
    }

    /*
     * Function: drawGameOver
     * Draw all the items that will dispaly if game is over
     * 
     * @param Canvas 	Paint tool built in Android
     * @return			None
     */
    private void drawGameOver(Canvas canvas){
        drawScoreDialog(canvas, "閲嶆柊寮�濮�");

        if(lastSingleClickTime > 0){
            postInvalidate();
        }
    }

    /*
     * Function: drawScoreDialog
     * Draw the dialog to show score
     * 
     * @param Canvas 	Paint tool built in Android
     * @param string	
     * @return			None
     */
    private void drawScoreDialog(Canvas canvas, String operation){
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        float originalFontSize = textPaint.getTextSize();
        Paint.Align originalFontAlign = textPaint.getTextAlign();
        int originalColor = paint.getColor();
        Paint.Style originalStyle = paint.getStyle();

        int w1 = (int)(20.0 / 360.0 * canvasWidth);
        int w2 = canvasWidth - 2 * w1;
        int buttonWidth = (int)(140.0 / 360.0 * canvasWidth);

        int h1 = (int)(150.0 / 558.0 * canvasHeight);
        int h2 = (int)(60.0 / 558.0 * canvasHeight);
        int h3 = (int)(124.0 / 558.0 * canvasHeight);
        int h4 = (int)(76.0 / 558.0 * canvasHeight);
        int buttonHeight = (int)(42.0 / 558.0 * canvasHeight);

        canvas.translate(w1, h1);
        //background color is white
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFD7DDDE);
        Rect rect1 = new Rect(0, 0, w2, canvasHeight - 2 * h1);
        canvas.drawRect(rect1, paint);
        //draw dialog
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF515151);
        paint.setStrokeWidth(borderSize);
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawRect(rect1, paint);
        textPaint.setTextSize(fontSize2);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("椋炴満澶ф垬鍒嗘暟", w2 / 2, (h2 - fontSize2) / 2 + fontSize2, textPaint);
        //draw score
        canvas.translate(0, h2);
        canvas.drawLine(0, 0, w2, 0, paint);
        String allScore = String.valueOf(getScore());
        canvas.drawText(allScore, w2 / 2, (h3 - fontSize2) / 2 + fontSize2, textPaint);
        canvas.translate(0, h3);
        canvas.drawLine(0, 0, w2, 0, paint);
        //draw dialog rectangle
        Rect rect2 = new Rect();
        rect2.left = (w2 - buttonWidth) / 2;
        rect2.right = w2 - rect2.left;
        rect2.top = (h4 - buttonHeight) / 2;
        rect2.bottom = h4 - rect2.top;
        canvas.drawRect(rect2, paint);
        canvas.translate(0, rect2.top);
        canvas.drawText(operation, w2 / 2, (buttonHeight - fontSize2) / 2 + fontSize2, textPaint);
        continueRect = new Rect(rect2);
        continueRect.left = w1 + rect2.left;
        continueRect.right = continueRect.left + buttonWidth;
        continueRect.top = h1 + h2 + h3 + rect2.top;
        continueRect.bottom = continueRect.top + buttonHeight;

        //reset
        textPaint.setTextSize(originalFontSize);
        textPaint.setTextAlign(originalFontAlign);
        paint.setColor(originalColor);
        paint.setStyle(originalStyle);
    }

    /*
     * Function: drawScoreAndBombs
     * Draw score on top left corner. Draw the number of bomb on bottom left corner
     * 
     * @param Canvas 	Paint tool built in Android
     * @return			None
     */
    private void drawScoreAndBombs(Canvas canvas){
        //Draw the pause button with picture 9
        Bitmap pauseBitmap = status == STATUS_GAME_STARTED ? bitmaps.get(9) : bitmaps.get(10);
        RectF pauseBitmapDstRecF = getPauseBitmapDstRecF();
        float pauseLeft = pauseBitmapDstRecF.left;
        float pauseTop = pauseBitmapDstRecF.top;
        canvas.drawBitmap(pauseBitmap, pauseLeft, pauseTop, paint);
        //draw score dialog
        float scoreLeft = pauseLeft + pauseBitmap.getWidth() + 20 * density;
        float scoreTop = fontSize + pauseTop + pauseBitmap.getHeight() / 2 - fontSize / 2;
        canvas.drawText(score + "", scoreLeft, scoreTop, textPaint);

        //draw bomb
        if(combatAircraft != null && !combatAircraft.isDestroyed()){
            int bombCount = combatAircraft.getBombCount();
            if(bombCount > 0){
                //bomb icon
                Bitmap bombBitmap = bitmaps.get(11);
                float bombTop = canvas.getHeight() - bombBitmap.getHeight();
                canvas.drawBitmap(bombBitmap, 0, bombTop, paint);
                //the number of bomb
                float bombCountLeft = bombBitmap.getWidth() + 10 * density;
                float bombCountTop = fontSize + bombTop + bombBitmap.getHeight() / 2 - fontSize / 2;
                canvas.drawText("X " + bombCount, bombCountLeft, bombCountTop, textPaint);
            }
        }
    }

    /*
     * Function destroyBulletsFrontOfCombatAircraft
     * Check the position of bullet by coordinates.
     * If bullet is on the front of aircraft, delete those bullets.
     * If player move the aircraft too fast, there will be much bullets are on the front of aircraft which is never happen in reality
     * 
     * @param	None
     * @return	None
     */
    private void destroyBulletsFrontOfCombatAircraft(){
        if(combatAircraft != null){
            float aircraftY = combatAircraft.getY();
            List<Bullet> aliveBullets = getAliveBullets();
            for(Bullet bullet : aliveBullets){
                if(aircraftY <= bullet.getY()){
                    bullet.destroy();
                }
            }
        }
    }

    /*
     * Function: removeDestroyedSprites
     * Remove destroyed Enemy aircraft, bullets and bombs
     * 
     * @param	None
     * @return	None
     */
    private void removeDestroyedSprites(){
        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()){
            Sprite s = iterator.next();
            if(s.isDestroyed()){
                iterator.remove();
            }
        }
    }

    /*
     * Function: createRandomSprites
     * Create random Enemy aircraft
     * Create random bomb
     * Create double bullets
     * 
     * @param	None
     * @return	None
     */
    private void createRandomSprites(int canvasWidth){
        Sprite sprite = null;
        int speed = 2;
        int callTime = Math.round(frame / 30);
        if((callTime + 1) % 25 == 0){
            //create bomb
            if((callTime + 1) % 50 == 0){
                sprite = new BombAward(bitmaps.get(7));
            }
            else{
                //create double bullet
                sprite = new BulletAward(bitmaps.get(8));
            }
        }
        else{
            /*
             * Create enemy aircraft
             * In this game, there are 3 types of enemy
             * Enemy is defined in class "Sprite"
             */
            int[] nums = {0,0,0,0,0,1,0,0,1,0,0,0,0,1,1,1,1,1,1,2};
            int index = (int)Math.floor(nums.length*Math.random());
            int type = nums[index];
            if(type == 0){
                //samll enemy
                sprite = new SmallEnemyPlane(bitmaps.get(4));
            }
            else if(type == 1){
                //medium enemy
                sprite = new MiddleEnemyPlane(bitmaps.get(5));
            }
            else if(type == 2){
                //big enemy
                sprite = new BigEnemyPlane(bitmaps.get(6));
            }
            if(type != 2){
                if(Math.random() < 0.33){
                    speed = 4;
                }
            }
        }

        if(sprite != null){
            float spriteWidth = sprite.getWidth();
            float spriteHeight = sprite.getHeight();
            float x = (float)((canvasWidth - spriteWidth)*Math.random());
            float y = -spriteHeight;
            sprite.setX(x);
            sprite.setY(y);
            if(sprite instanceof AutoSprite){
                AutoSprite autoSprite = (AutoSprite)sprite;
                autoSprite.setSpeed(speed);
            }
            addSprite(sprite);
        }
    }

    /*-------------------------------touch------------------------------------*/

    /*
     * Function: onTouchEvent
     * Define what event will happen as player click screen
     * Inlcudes both single click and double click
     * 
     * @param MotionEvent	Event variable
     * @return bool			true if palyer never touch the screen
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int touchType = resolveTouchType(event);
        if(status == STATUS_GAME_STARTED){
            if(touchType == TOUCH_MOVE){
                if(combatAircraft != null){
                    combatAircraft.centerTo(touchX, touchY);
                }
            }else if(touchType == TOUCH_DOUBLE_CLICK){
                if(status == STATUS_GAME_STARTED){
                    if(combatAircraft != null){
                        //Double click to use bomb
                        combatAircraft.bomb(this);
                    }
                }
            }
        }else if(status == STATUS_GAME_PAUSED){
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }else if(status == STATUS_GAME_OVER){
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }
        return true;
    }

    /*
     * Function: resolveTouchType
     * Define single click and double click
     * 
     * @param MotionEvent	Event variable
     * @return int			return touchtype
     */
    private int resolveTouchType(MotionEvent event){
        int touchType = -1;
        int action = event.getAction();
        touchX = event.getX();
        touchY = event.getY();
        if(action == MotionEvent.ACTION_MOVE){
            long deltaTime = System.currentTimeMillis() - touchDownTime;
            if(deltaTime > singleClickDurationTime){
                touchType = TOUCH_MOVE;
            }
        }else if(action == MotionEvent.ACTION_DOWN){
            //record the time that player touching the screen
            touchDownTime = System.currentTimeMillis();
        }else if(action == MotionEvent.ACTION_UP){
            //record the time player's finger leave scrren
            touchUpTime = System.currentTimeMillis();
            //count the delta time of touching down and up
            long downUpDurationTime = touchUpTime - touchDownTime;
            /*
             * if the delta time is less than 200ms, we define it as single click
             * if the delta time of two single click is less than 300ms, we define it as double click
             */
            if(downUpDurationTime <= singleClickDurationTime){
                //record the delta time of two single click
                long twoClickDurationTime = touchUpTime - lastSingleClickTime;

                if(twoClickDurationTime <=  doubleClickDurationTime){
                    touchType = TOUCH_DOUBLE_CLICK;
                    //reset data for next clicking
                    lastSingleClickTime = -1;
                    touchDownTime = -1;
                    touchUpTime = -1;
                }else{
                    lastSingleClickTime = touchUpTime;
                }
            }
        }
        return touchType;
    }

    /*
     * Function: isSingleClick
     * Check the clicking is single click or not
     * 
     * @param		None
     * @return bool	true if the click is single click
     */
    private boolean isSingleClick(){
        boolean singleClick = false;
        if(lastSingleClickTime > 0){
            //count the delta time from last single click to now
            long deltaTime = System.currentTimeMillis() - lastSingleClickTime;

            if(deltaTime >= doubleClickDurationTime){
            	/*
            	 * if the delta time greater than the time needed for double click
            	 * we define it as a single click
            	 */
                singleClick = true;
                //reset data for next clicking event
                lastSingleClickTime = -1;
                touchDownTime = -1;
                touchUpTime = -1;
            }
        }
        return singleClick;
    }

    /*
     * Function: onSingleClick
     * Define special single click
     * if the coordiante of single is in the pause/continue/restart button, 
     * pause/continue.restart the game
     * 
     * @param float		X coordiante of this single click
     * @param float		Y coordiante of this single click
     * @return 			None
     */
    private void onSingleClick(float x, float y){
        if(status == STATUS_GAME_STARTED){
            if(isClickPause(x, y)){
                //pause button is clicked
                pause();
            }
        }else if(status == STATUS_GAME_PAUSED){
            if(isClickContinueButton(x, y)){
                //continue button is clicked
                resume();
            }
        }else if(status == STATUS_GAME_OVER){
            if(isClickRestartButton(x, y)){
                //restart button is clicked
                restart();
            }
        }
    }

    /*
     * Function: isClickPause
     * Check whether pause button is clicked
     * 
     * @param float		X coordiante of this single click
     * @param float		Y coordiante of this single click
     * @return bool		true if coordination of single slick is in the range of pause button
     */
    private boolean isClickPause(float x, float y){
        RectF pauseRecF = getPauseBitmapDstRecF();
        return pauseRecF.contains(x, y);
    }

    /*
     * Function: isClickContinueButton
     * Check whether pause button is clicked
     * 
     * @param float		X coordiante of this single click
     * @param float		Y coordiante of this single click
     * @return bool		true if coordination of single slick is in the range of continue button
     */
    private boolean isClickContinueButton(float x, float y){
        return continueRect.contains((int)x, (int)y);
    }

    /*
     * Function: isClickRestartButton
     * Check whether pause button is clicked
     * 
     * @param float		X coordiante of this single click
     * @param float		Y coordiante of this single click
     * @return bool		true if coordination of single slick is in the range of restart button
     */
    private boolean isClickRestartButton(float x, float y){
        return continueRect.contains((int)x, (int)y);
    }

    /*
     * Function: getPauseBitmapDstRecF
     * Set the range of pause buuton
     * 
     * @param	None
     * @return	None
     */
    private RectF getPauseBitmapDstRecF(){
        Bitmap pauseBitmap = status == STATUS_GAME_STARTED ? bitmaps.get(9) : bitmaps.get(10);
        RectF recF = new RectF();
        recF.left = 15 * density;
        recF.top = 15 * density;
        recF.right = recF.left + pauseBitmap.getWidth();
        recF.bottom = recF.top + pauseBitmap.getHeight();
        return recF;
    }

    /*-------------------------------destroy------------------------------------*/
    /*
     * Function: destroyNotRecyleBitmaps
     * clean all data and pictures if game is over or restart
     */
    private void destroyNotRecyleBitmaps(){
        status = STATUS_GAME_DESTROYED;
        frame = 0;
        score = 0;
        //destroy aircraft
        if(combatAircraft != null){
            combatAircraft.destroy();
        }
        combatAircraft = null;

        //destroy enemy,bullet,bomb
        for(Sprite s : sprites){
            s.destroy();
        }
        sprites.clear();
    }

    /*
     * Function: destroy
     * Release resources
     */
    public void destroy(){
        destroyNotRecyleBitmaps();

        for(Bitmap bitmap : bitmaps){
            bitmap.recycle();
        }
        bitmaps.clear();
    }

    /*-------------------------------public methods-----------------------------------*/

    /*
     * Function: addSprite
     * Add Sprite to Sprite class
     * 
     * @param	None
     * @return	None
     */
    public void addSprite(Sprite sprite){
        spritesNeedAdded.add(sprite);
    }

    /*
     * Function: addScore
     * Add score that will display on the screen
     * 
     * @param int 	the number of score palyer earned
     * @return		None
     */
    public void addScore(int value){
        score += value;
    }

    public int getStatus(){
        return status;
    }

    public float getDensity(){
        return density;
    }

    public Bitmap getYellowBulletBitmap(){
        return bitmaps.get(2);
    }

    public Bitmap getBlueBulletBitmap(){
        return bitmaps.get(3);
    }

    public Bitmap getExplosionBitmap(){
        return bitmaps.get(1);
    }

    /*
     * Function: getAliveEnemyPlanes
     * get the data of alive enemy plane
     * 
     * @param 		None
     * @return List	return enemy plane list
     */
    public List<EnemyPlane> getAliveEnemyPlanes(){
        List<EnemyPlane> enemyPlanes = new ArrayList<EnemyPlane>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof EnemyPlane){
                EnemyPlane sprite = (EnemyPlane)s;
                enemyPlanes.add(sprite);
            }
        }
        return enemyPlanes;
    }

    /*
     * Function: getAliveBombAwards
     * get the data of alive bomb
     * 
     * @param 		None
     * @return List	return bomb list
     */
    public List<BombAward> getAliveBombAwards(){
        List<BombAward> bombAwards = new ArrayList<BombAward>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof BombAward){
                BombAward bombAward = (BombAward)s;
                bombAwards.add(bombAward);
            }
        }
        return bombAwards;
    }

    /*
     * Function: getAliveBulletAwards
     * get the data of alive bullet award
     * There will be random bullet award in this gmae
     * if player touch it, bullet will upgrade to double bullet
     * 
     * @param 		None
     * @return List	return bullet award list
     */
    public List<BulletAward> getAliveBulletAwards(){
        List<BulletAward> bulletAwards = new ArrayList<BulletAward>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof BulletAward){
                BulletAward bulletAward = (BulletAward)s;
                bulletAwards.add(bulletAward);
            }
        }
        return bulletAwards;
    }

    /*
     * Function: getAliveBulletAwards
     * get the data of alive bullet
     * 
     * @param 		None
     * @return List	return bullet list
     */
    public List<Bullet> getAliveBullets(){
        List<Bullet> bullets = new ArrayList<Bullet>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof Bullet){
                Bullet bullet = (Bullet)s;
                bullets.add(bullet);
            }
        }
        return bullets;
    }
}