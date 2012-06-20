/*-
 * Copyright (c) 2012 Red Hat, Inc.
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
package org.fedoraproject.javadeptools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

class ProgressFileInputStream extends FilterInputStream {
  private final long size;
  private long pos;
  private static final long TIME_GRANULARITY = 100;
  private long lastTime;
  private int lastLength;

  public ProgressFileInputStream(File f) throws IOException {
    super(new FileInputStream(f));
    this.size = f.length();
  }

  public int read() throws IOException {
    int rv = in.read();
    updateProgress(rv);
    return rv;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    int rv = in.read(b, off, len);
    updateProgress(rv);
    return rv;
  }

  public void close() throws IOException {
    in.close();
    appendMessage("done.");
    System.err.println();
  }

  private void updateProgress(int cnt) {
    if (cnt <= 0)
      return;
    pos += cnt;
    long thisTime = System.currentTimeMillis();
    if (thisTime < lastTime + TIME_GRANULARITY)
      return;
    lastTime = thisTime;
    double percentage = Math.min(100. * pos / size, 100);

    appendMessage(String.format("%.2f %%", percentage));
  }

  private void appendMessage(String msg) {
    msg = " " + msg;
    int thisLength = msg.length();
    if (lastLength > thisLength)
      msg += StringUtils.repeat(" ", lastLength - thisLength);
    msg += StringUtils.repeat("\b", Math.max(lastLength, thisLength));
    System.err.print(msg);
    lastLength = thisLength;
  }
}
