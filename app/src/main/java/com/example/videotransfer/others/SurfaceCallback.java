package com.example.videotransfer.others;

import java.io.IOException;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public class SurfaceCallback implements Callback {
	
	private Camera camera;
	private SurfaceHolder surfaceHolder;
	private String ipaddr;
	
	public SurfaceCallback(SurfaceHolder surfaceHolder, String ipaddr) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.ipaddr = ipaddr;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = Camera.open();
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(640, 480);
			parameters.setPreviewFrameRate(5);
			parameters.setPictureFormat(PixelFormat.JPEG);
			parameters.setPictureSize(640, 480);
			parameters.setJpegQuality(90);
			camera.setDisplayOrientation(90);
			camera.setParameters(parameters);
			camera.setPreviewDisplay(surfaceHolder);
//			camera.setPreviewCallback(new getStreamData(ipaddr));
			camera.setPreviewCallback(new GetStreamData(ipaddr));
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.startPreview();
		camera.autoFocus(null);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

}
