/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package builder.smartfrog.util;

import com.google.common.base.Joiner;
import com.google.common.io.NullOutputStream;
import hudson.util.IOUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ogondza.
 */
public class LineFilterOutputStreamTest {
    
    private static final class Capturing extends LineFilterOutputStream {
        private List<String> record = new ArrayList<String>();

        public Capturing() {
            super(new NullOutputStream());
        }

        protected void writeLine(String line) {
            record.add(line);
        }
    }

    private static final class Noop extends LineFilterOutputStream {
        private int count = 0;
        public Noop() {
            super(new NullOutputStream());
        }

        protected void writeLine(String line) {
            count += line.length();
        }
    }

    @Test
    public void essentials() throws Exception {
        Capturing capturing;
        capturing = new Capturing();
        capturing.write('0');
        capturing.write('\n');
        capturing.write('1');
        capturing.write('\n');
        capturing.write('2');
        assertThat(capturing.record, Matchers.<String>iterableWithSize(2));
        assertThat(capturing.record, contains("0", "1"));

        capturing = new Capturing();
        capturing.write(new byte[] { '\r', '\n', 'A', '\r', '\n' });
        assertThat(capturing.record, Matchers.<String>iterableWithSize(2));
        assertThat(capturing.record, contains("", "A"));

        capturing = new Capturing();
        capturing.write(new byte[] { 'N', 'Y', '\n', 'N', '\n' }, 1, 2);
        assertThat(capturing.record, Matchers.<String>iterableWithSize(1));
        assertThat(capturing.record, contains("Y"));
    }

    @Test
    public void tooLong() throws Exception {
        Capturing capturing = new Capturing();
        String src = line(10000);
        assertThat(src.getBytes().length, equalTo(10000));
        capturing.write((src + '\n').getBytes());
        assertThat(capturing.record, Matchers.<String>iterableWithSize(2));
        assertThat(capturing.record.get(0), equalTo(line(8192)));
        assertThat(capturing.record.get(1), equalTo(line(1808)));

        assertThat(capturing.record.get(0) + capturing.record.get(1), equalTo(src));

    }

    private String line(int len) {
        return String.format("%" + len + "s", "").replaceAll(" ", "A");
    }

    @Test
    public void endToEnd() throws Exception {
        InputStream source = getClass().getResourceAsStream("fake.log");
        Capturing capturing = new Capturing();
        IOUtils.copy(source, new PrintStream(capturing));

        List<String> actual = capturing.record;
        List<String> expected = IOUtils.readLines(getClass().getResourceAsStream("fake.log"));

        assertThat(actual, contains(expected.toArray(new String[] {})));
        Joiner joiner = Joiner.on("");
        assertThat(joiner.join(actual), equalTo(joiner.join(expected)));
    }

    @Test
    public void perf() throws Exception {
        String source = IOUtils.toString(getClass().getResourceAsStream("fake.log"), "UTF-8");
        Noop noop = new Noop();
        for (int i = 0; i < 1000; i++) {
            IOUtils.copy(new StringInputStream(source), new PrintStream(noop));
        }
        System.out.println(noop.count);
    }
}
