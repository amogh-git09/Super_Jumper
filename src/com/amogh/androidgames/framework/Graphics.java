package com.amogh.androidgames.framework;

import android.graphics.Paint;
import android.graphics.Typeface;

public interface Graphics {

	public static enum PixmapFormat{
		ARGB8888, ARGB4444, RGB565
	}
	
	public Pixmap newPixmap(String fileName, PixmapFormat format);
	
	public void clearScreen(int color);
	
	public void drawLine(int x, int y, int x2, int y2, int color);
	
	public void drawRect(int x, int y, int width, int height, int color);
	
	public void drawPixmap(Pixmap pixmap, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight);
	
	public void drawPixmap(Pixmap pixmap, int x, int y);
	
	public Typeface newTypeface(String fileName);
	
	void drawString(String text, int x, int y, Paint paint);
	
	public int getWidth();
	
	public int getHeight();
	
	public void drawARGB(int i, int j, int k, int l);
	
}
