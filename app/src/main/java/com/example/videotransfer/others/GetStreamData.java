package com.example.videotransfer.others;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.example.videotransfer.thread.TransferThread;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;

public class GetStreamData implements PreviewCallback {
	
	private String ipaddr;
	private TransferThread transferthread;
	
	public GetStreamData(String ipaddr) {
		super();
		this.ipaddr = ipaddr;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Size size = camera.getParameters().getPreviewSize();
		YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
				size.height, null);
		if (image != null) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			image.compressToJpeg(new Rect(0, 0, size.width, size.height),
					80, outStream);
			try {
				outStream.flush();
				transferthread = new TransferThread(outStream, ipaddr);
				transferthread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
