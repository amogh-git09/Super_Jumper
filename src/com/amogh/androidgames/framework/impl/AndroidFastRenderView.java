package com.amogh.androidgames.framework.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AndroidFastRenderView extends SurfaceView implements Runnable{
	AndroidGame game;
	Bitmap frameBuffer;
	Thread renderThread = null;
	SurfaceHolder holder;
	volatile boolean running = false;
	
	public AndroidFastRenderView(AndroidGame game, Bitmap frameBuffer){
		super(game);
		this.game = game;
		this.frameBuffer = frameBuffer;
		this.holder = getHolder();
	}
	
	public void resume(){
		Log.d("AndroidFastRenderView", "Here in resume()");
		running = true;
		renderThread = new Thread(this);
		renderThread.start();
	}
	
	public void run(){
		Log.d("AndroidFastRenderView", "Here in run()");
		Rect dstRect = new Rect();
		long startTime = System.nanoTime();
		while(running){
			if(!holder.getSurface().isValid())
				continue;
			
			float deltaTime = (System.nanoTime()-startTime)/1000000000.0f;
			startTime = System.nanoTime();
			
			game.getCurrentScreen().update(deltaTime);
			game.getCurrentScreen().present(deltaTime);
			
			Canvas canvas = holder.lockCanvas();
			canvas.getClipBounds(dstRect);
			//Log.d("AndroidFastRenderView", frameBuffer.getHeight() + ", " + frameBuffer.getWidth());
			canvas.drawBitmap(frameBuffer, null, dstRect, null);
			holder.unlockCanvasAndPost(canvas);
		}
	}
	
	public void pause(){
		running = false;
		while(true){
			try{
				renderThread.join();
				return;
			}catch(InterruptedException e){
				// retry
			}
		}
	}
}
