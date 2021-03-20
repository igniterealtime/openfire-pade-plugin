package nl.martijndwars.webpush;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

/**
 * Java 7's try-with-resource closes the client before the future is completed.
 * This callback captures the client and closes it once the request is
 * completed.
 *
 * See also http://stackoverflow.com/a/35962718/368220.
 */
public class ClosableCallback implements FutureCallback<HttpResponse> {
    private CloseableHttpAsyncClient closeableHttpAsyncClient;

    public ClosableCallback(CloseableHttpAsyncClient closeableHttpAsyncClient) {
        this.closeableHttpAsyncClient = closeableHttpAsyncClient;
    }

    @Override
    public void completed(HttpResponse httpResponse) {
        close();
    }

    @Override
    public void failed(Exception e) {
        close();
    }

    @Override
    public void cancelled() {
        close();
    }

    private void close() {
        try {
            closeableHttpAsyncClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
