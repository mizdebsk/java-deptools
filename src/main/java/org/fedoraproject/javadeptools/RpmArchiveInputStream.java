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
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;

/**
 * A class for reading RPM package as an archive.
 *
 * @author Mikolaj Izdebski
 */
class RpmArchiveInputStream extends ArchiveInputStream {
  private final Process child;
  private static final File bitBucket = new File("/dev/null");
  private final ArchiveInputStream in;

  public RpmArchiveInputStream(File rpm) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("rpm2cpio");
    pb.redirectInput(Redirect.from(rpm));
    pb.redirectError(bitBucket);

    child = pb.start();
    InputStream childIn = child.getInputStream();
    in = new CpioArchiveInputStream(childIn);
  }

  public void close() throws IOException {
    in.close();
    // If rpm2cpio hasn't terminated yet it should get SIGPIPE now.
    try {
      int status = child.waitFor();
      if (status != 0)
        throw new IOException("rpm2cpio exited with exit status " + status);
    } catch (InterruptedException e) {
      throw new IOException("Interruped while reaping child process", e);
    }
  }

  public ArchiveEntry getNextEntry() throws IOException {
    return in.getNextEntry();
  }

  public int read() throws IOException {
    return in.read();
  }

  public int read(byte[] buf) throws IOException {
    return in.read(buf);
  }

  public int read(byte[] buf, int off, int len) throws IOException {
    return in.read(buf, off, len);
  }
}
