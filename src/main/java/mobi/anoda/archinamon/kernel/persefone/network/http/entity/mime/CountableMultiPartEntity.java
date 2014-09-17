package mobi.anoda.archinamon.kernel.persefone.network.http.entity.mime;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import android.support.annotation.NonNull;

@SuppressWarnings("deprecation")
public class CountableMultiPartEntity extends MultipartEntity {

    public static interface ProgressListener {

        void transferred(long num, long total);
    }

    public static class CountingOutputStream extends FilterOutputStream {

        private final ProgressListener mProgressListener;
        private final long             mTotalLength;
        private       long             mTransferred;

        public CountingOutputStream(final OutputStream out, final long length, final ProgressListener listener) {
            super(out);
            this.mProgressListener = listener;
            this.mTotalLength = length;
            this.mTransferred = 0;
        }

        @Override
        public void write(@NonNull byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.mTransferred += len;
            this.mProgressListener.transferred(this.mTransferred, this.mTotalLength);
        }

        @Override
        public void write(int oneByte) throws IOException {
            out.write(oneByte);
            this.mTransferred++;
            this.mProgressListener.transferred(this.mTransferred, this.mTotalLength);
        }
    }

    private final ProgressListener mProgressListener;

    public CountableMultiPartEntity(final ProgressListener listener) {
        super();
        this.mProgressListener = listener;
    }

    public CountableMultiPartEntity(final HttpMultipartMode mode, final ProgressListener listener) {
        super(mode);
        this.mProgressListener = listener;
    }

    public CountableMultiPartEntity(HttpMultipartMode mode, final String boundary, final Charset charset, final ProgressListener listener) {
        super(mode, boundary, charset);
        this.mProgressListener = listener;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream, getContentLength(), this.mProgressListener));
    }
}