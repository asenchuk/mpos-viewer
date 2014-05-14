package net.taviscaron.mposviewer.rpc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import net.taviscaron.mposviewer.core.Constants;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Creates RPC instances
 * @author Andrei Senchuk
 */
public class RPCFactory {
    private static final String TAG = "RPCFactory";
    private static final int DEFAULT_TIMEOUT = 30 * 1000;

    public static RPC createRPC(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean trustAllSSLCerts = sp.getBoolean(Constants.TRUST_ALL_SSL_CERTS_PREF_KEY, false);
        HttpClient client = (trustAllSSLCerts) ? createTrustAllSSLCertsHttpClient() : createDefaultHttpClient();
        return new RPC(client);
    }

    private static HttpParams createHttpParams() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, DEFAULT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, DEFAULT_TIMEOUT);
        return params;
    }

    private static HttpClient createDefaultHttpClient() {
        return new DefaultHttpClient(createHttpParams());
    }

    private static HttpClient createTrustAllSSLCertsHttpClient() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // noop
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // noop
                }
            }}, null);

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new SSLSocketFactory(trustStore) {
                @Override
                public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
                    return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
                }

                @Override
                public Socket createSocket() throws IOException {
                    return sslContext.getSocketFactory().createSocket();
                }
            };

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            HttpParams params = createHttpParams();

            return new DefaultHttpClient(new ThreadSafeClientConnManager(params, registry), params);
        } catch (Exception e) {
            Log.w(TAG, "Can't create trust all ssl certs http client. Create default instead.", e);
            return createDefaultHttpClient();
        }
    }
}
