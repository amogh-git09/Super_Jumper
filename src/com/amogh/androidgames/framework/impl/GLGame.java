package com.amogh.androidgames.framework.impl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.amogh.androidgames.framework.Audio;
import com.amogh.androidgames.framework.FileIO;
import com.amogh.androidgames.framework.Game;
import com.amogh.androidgames.framework.Graphics;
import com.amogh.androidgames.framework.Input;
import com.amogh.androidgames.framework.Screen;

public class GLGame extends Activity implements Game, Renderer{
	enum GLGameState {
		Initialized,
		Running,
		Paused,
		Finished,
		Idle
	}
	
	GLSurfaceView glView;
	GLGraphics glGraphics;
	Audio audio;
	Input input;
	FileIO fileIO;
	Screen screen;
	GLGameState state = GLGameState.Initialized;
	Object stateChanged = new Object();
	long startTime = System.nanoTime();
	WakeLock wakeLock;
	
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		glView = new GLSurfaceView(this);
		glView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION 
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				);

		glView.setRenderer(this);
		setContentView(glView);
		
		glGraphics = new GLGraphics(glView);
		fileIO = new AndroidFileIO(this);
		audio = new AndroidAudio(this);
		input = new AndroidInput(this, glView, 1, 1);
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GLGame");
	}
	
	@Override
	public void onResume(){
		super.onResume();
		glView.onResume();
		wakeLock.acquire();
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		glGraphics.setGL(gl);
		
		synchronized(stateChanged){
			if(state == GLGameState.Initialized)
				screen = getStartScreen();
			state = GLGameState.Running;
			screen.resume();
			startTime = System.nanoTime();
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLGameState state = null;
		
		synchronized(stateChanged){
			state = this.state;
		}
		
		if(state == GLGameState.Running){
			float deltaTime = (System.nanoTime() - startTime) / 1000000000.0f;
			startTime = System.nanoTime();
			
			screen.update(deltaTime);
			screen.present(deltaTime);
		}
		
		if(state == GLGameState.Paused){
			screen.pause();
			synchronized(stateChanged){
				this.state = GLGameState.Idle;
				stateChanged.notifyAll();
			}
		}
		
		if(state == GLGameState.Finished){
			screen.pause();
			screen.dispose();
			synchronized(stateChanged){
				this.state = GLGameState.Idle;
				stateChanged.notifyAll();
			}
		}
	}
	
	public void onPause(){
		synchronized(stateChanged){
			if(isFinishing())
				state = GLGameState.Finished;
			else
				state = GLGameState.Paused;
			while(true){
				try{
					stateChanged.wait();
					break;
				}catch(InterruptedException e){		
				
				}
			}
		}
		wakeLock.release();
		glView.onPause();
		super.onPause();
	}
	
	public GLGraphics getGLGraphics(){
		return glGraphics;
	}
	
	public Input getInput(){
		return input;
	}
	
	public FileIO getFileIO(){
		return fileIO;
	}
	
	public Graphics getGraphics(){
		throw new IllegalStateException("We are using OpenGL!");
	}
	
	@Override
	public Audio getAudio() {
		return audio;
	}

	@Override
	public void setScreen(Screen newScreen) {
		if(newScreen == null)
			throw new IllegalArgumentException("Screen must not be null");
		
		this.screen.pause();
		this.screen.dispose();
		newScreen.resume();
		newScreen.update(0);
		this.screen = newScreen;
	}

	@Override
	public Screen getCurrentScreen() {
		return screen;
	}

	@Override
	public Screen getStartScreen() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
