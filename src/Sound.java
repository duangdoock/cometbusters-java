/*
 * Copyright 2013 Mark Injerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public enum Sound {
	THRUST("snd/thrust.wav"),
	SHOOT("snd/shoot.wav"),
	SHIP_EXPLODE("snd/ship_explode.wav"),
	ROCK_EXPLODE("snd/rock_explode.wav");

	static void init() {
		values();
	}

	private String filename;
	private SoundPlayer player;

	private Sound(String filename) {
		this.filename = filename;
	}

	void play() {
		new SoundPlayer(filename, false).start();
	}

	void loop() {
		player = new SoundPlayer(filename, true);
		player.start();
	}

	void stop() {
		player.stop = true;
		player = null;
	}

	public static class SoundPlayer extends Thread {
		private String filename;
		private boolean doLoop, stop;

		public SoundPlayer(String file, boolean loop) {
			filename = file;
			doLoop = loop;
		}

		public void run() {
			AudioInputStream stream;
			try {
				stream = AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource(filename));
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			AudioFormat format = stream.getFormat();
			SourceDataLine line;
			try {
				line = (SourceDataLine)AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
				line.open(format);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				return;
			}
			line.start();
			int nBytesRead = 0;
			byte[] buffer = new byte[256];
			stream.mark(32000);
			try {
				while (nBytesRead != -1 && !stop) {
					nBytesRead = stream.read(buffer, 0, buffer.length);
					if (nBytesRead >= 0 && !stop) {
						line.write(buffer, 0, nBytesRead);
					} else if (doLoop && !stop) {
						stream.reset();
						nBytesRead = 0;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} finally {
				if (doLoop) {
					line.flush();
				} else {
					line.drain();
				}
				line.close();
			}
		}
	}
}