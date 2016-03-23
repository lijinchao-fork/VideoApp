//package com.taro.media.audio;
//
//import android.media.AudioRecord;
//import android.media.MediaCodec;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.util.Log;
//
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.concurrent.CountDownLatch;
//
//
//public class DataEncodeThread extends Thread implements AudioRecord.OnRecordPositionUpdateListener {
//	private final static String TAG = "DataEncodeThread";
//
//	public static final int PROCESS_STOP = 1;
//
//	private StopHandler handler;
//
//	private final byte[] buffer;
//
//	private final byte[] mp3Buffer;
//
//	private final RingBuffer ringBuffer;
//    private MediaCodec mAudioEncoder;
//	private final int bufferSize;
//
//	private final CountDownLatch handlerInitLatch = new CountDownLatch(1);
//
//
//	static class StopHandler extends Handler {
//
//		final WeakReference<DataEncodeThread> encodeThread;
//
//		public StopHandler(DataEncodeThread encodeThread) {
//			this.encodeThread = new WeakReference<>(encodeThread);
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//			if (msg.what == PROCESS_STOP) {
//				DataEncodeThread threadRef = encodeThread.get();
//				// Process all data in ring buffer and flush
//				// left data to file
//				//noinspection StatementWithEmptyBody
//				while (threadRef.processData() > 0);
//				// Cancel any event left in the queue
//				removeCallbacksAndMessages(null);
//				threadRef.flushAndRelease();
//				getLooper().quit();
//			}
//			super.handleMessage(msg);
//		}
//	}
//
//	/**
//	 * Constructor
//	 * @param ringBuffer
//	 * @param os
//	 * @param bufferSize
//	 */
//	public DataEncodeThread(RingBuffer ringBuffer, int bufferSize) {
//		this.ringBuffer = ringBuffer;
//		this.bufferSize = bufferSize;
//		buffer = new byte[bufferSize];
//		mp3Buffer = new byte[(int) (7200 + (buffer.length * 2 * 1.25))];
//	}
//
//	@Override
//	public void run() {
//		Looper.prepare();
//		handler = new StopHandler(this);
//		handlerInitLatch.countDown();
//		Looper.loop();
//	}
//
//	/**
//	 * Return the handler attach to this thread
//	 * @return
//	 */
//	public Handler getHandler() {
//		try {
//			handlerInitLatch.await();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//            Log.e(TAG, "Error when waiting handle to init");
//		}
//		return handler;
//	}
//
//	@Override
//	public void onMarkerReached(AudioRecord recorder) {
//		// Do nothing
//	}
//
//	@Override
//	public void onPeriodicNotification(AudioRecord recorder) {
//		processData();
//	}
//
//	/**
//	 * Get data from ring buffer
//	 * Encode it to mp3 frames using lame encoder
//	 * @return  Number of bytes read from ring buffer
//	 * 			0 in case there is no data left
//	 */
//	private int processData() {
//		int bytes = ringBuffer.read(buffer, bufferSize);
//		if (bytes > 0) {
//            Log.d(TAG, "Read size: " + bytes);
//            ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();
//            int inputBufferIndex = mAudioEncoder.dequeueInputBuffer(10000);
//            if (inputBufferIndex >= 0) {
//                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
//                inputBuffer.clear();
//                inputBuffer.put(buffer);
//                mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, buffer.length, presentationTimeStamp, 0);
//            }
//
//
//			return bytes;
//		}
//		return 0;
//	}
//
//	/**
//	 * Flush all data left in lame buffer to file
//	 */
//	private void flushAndRelease() {
//
//        processData();
//	}
//}
