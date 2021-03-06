/*
 * Copyright 2012 LevelUp Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webwalker.framework.system.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;

public abstract class LogCollectorFile implements LogUtils.LogHandler {
	public LogCollectorFile(Context context) {
		mContext = context;
		mLogDirectory = new File(Environment.getExternalStorageDirectory(),
				"Android/data/" + mContext.getPackageName() + "/logs");
		mLogBuilder = new File(mLogDirectory, "log_"
				+ System.currentTimeMillis() + ".txt");
	}

	private static final byte[] CRLN = new byte[] { '\r', '\n' };

	protected final Context mContext;
	private File mLogBuilder;
	protected OutputStream mOutLogger;
	private final File mLogDirectory;
	protected boolean mFileIsCreated;

	public File getLogDirectory() {
		return mLogDirectory;
	}

	public File getLogFile() {
		return mLogBuilder;
	}

	@Override
	public void addNewLogLine(String line) {
		if (!mFileIsCreated) {
			if (Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				if (!mLogDirectory.exists() || !mLogDirectory.isDirectory()) {
					boolean mDirAsserted = mLogDirectory.mkdirs();
					// mLogger.w("cache dir=" +
					// dir.getAbsolutePath()+" asserted:"+DirAsserted);
					if (mDirAsserted) {
						File noMedia = new File(mLogDirectory, ".nomedia");
						try {
							noMedia.createNewFile();
						} catch (IOException e) {
						}
					}
				}

				mOutLogger = null;
				try {
					mLogBuilder.createNewFile();
					mFileIsCreated = true;
					mOutLogger = new BufferedOutputStream(new FileOutputStream(
							mLogBuilder));

					String manufacturer = "";
					try {
						manufacturer = android.os.Build.MANUFACTURER + " ";
					} catch (NoSuchMethodError e) {
					} catch (VerifyError e) {
					}

					final String deviceName = manufacturer
							+ android.os.Build.MODEL + " ("
							+ android.os.Build.DISPLAY + ")";
					mOutLogger.write(deviceName.getBytes());
					mOutLogger.write(CRLN);

					final String ApiVersion = "API v"
							+ android.os.Build.VERSION.SDK_INT + " ("
							+ android.os.Build.VERSION.RELEASE + ")";
					mOutLogger.write(ApiVersion.getBytes());
					mOutLogger.write(CRLN);
					mOutLogger.write(CRLN);
				} catch (IOException e) {
					e.printStackTrace();
					try {
						if (mOutLogger != null)
							mOutLogger.close();
					} catch (IOException ee) {
					} finally {
						mOutLogger = null;
						mFileIsCreated = false;
						mLogBuilder.delete();
					}
				}
			}
		}

		try {
			if (mOutLogger != null) {
				mOutLogger.write(line.getBytes());
				mOutLogger.write(CRLN);
			}
		} catch (IOException e) {
		}
	}

	@Override
	public void finishedReadingLogs() {
		try {
			if (mOutLogger != null)
				mOutLogger.close();
		} catch (IOException e) {
			mOutLogger = null;
		}
	}
}
