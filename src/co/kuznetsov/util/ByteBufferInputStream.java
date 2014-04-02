package co.kuznetsov.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
    private ByteBuffer byteBuffer;

    public ByteBufferInputStream () {
    }

    /** Creates an uninitialized stream that cannot be used until {@link #setByteBuffer(java.nio.ByteBuffer)} is called. */
    public ByteBufferInputStream (ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBuffer getByteBuffer () {
        return byteBuffer;
    }

    public void setByteBuffer (ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public int read () throws IOException {
        if (!byteBuffer.hasRemaining()) return -1;
        return byteBuffer.get();
    }

    public int read (byte[] bytes, int offset, int length) throws IOException {
        int count = Math.min(byteBuffer.remaining(), length);
        if (count == 0) return -1;
        byteBuffer.get(bytes, offset, count);
        return count;
    }

    public int available () throws IOException {
        return byteBuffer.remaining();
    }
}