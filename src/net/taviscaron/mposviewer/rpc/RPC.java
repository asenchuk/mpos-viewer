package net.taviscaron.mposviewer.rpc;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.taviscaron.mposviewer.model.Worker;
import net.taviscaron.mposviewer.rpc.result.GetPoolStatusResult;
import net.taviscaron.mposviewer.rpc.result.GetPublicResult;
import net.taviscaron.mposviewer.rpc.result.GetUserBalanceResult;
import net.taviscaron.mposviewer.rpc.result.GetUserStatusResult;
import net.taviscaron.mposviewer.util.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * RPC performer
 * @author andrey.senchuk
 */
public class RPC {
    private static final String TAG = "RPC";

    private static final String METHOD_ATTR_NAME = "action";
    private static final String TOKEN_ATTR_NAME = "api_key";

    /** supported RPC methods */
    public enum Method {
        GET_BLOCK_COUNT("getblockcount", false, true, null, null, null),
        GET_BLOCKS_FOUND("getblocksfound", false, true, null, null, null),
        GET_CURRENT_WORKERS("getcurrentworkers", false, true, null, null, null),
        GET_DIFFICULTY("getdifficulty", false, true, null, null, null),
        GET_ESTIMATED_TIME("getestimatedtime", false, true, null, null, null),
        GET_POOL_HASH_RATE("getpoolhashrate", false, true, null, null, null),
        GET_POOL_SHARE_RATE("getpoolsharerate", false, true, null, null, null),
        GET_TIME_SINCE_LAST_BLOCK("gettimesincelastblock", false, true, null, null, null),
        GET_PUBLIC("public", true, false, null, null, GetPublicResult.class),
        GET_USER_WORKERS("getuserworkers", false, true, null, new String[] { "id" }, Worker[].class),
        GET_USER_STATUS("getuserstatus", false, true, null, new String[] { "id" }, GetUserStatusResult.class),
        GET_POOL_STATUS("getpoolstatus", false, true, null, null, GetPoolStatusResult.class),
        GET_USER_BALANCE("getuserbalance", false, true, null, null, GetUserBalanceResult.class),
        GET_DASHBOARD_DATA("getdashboarddata", false, true, null, new String[] { "id" }, null),
        GET_TOP_CONTRIBUTORS("gettopcontributors", false, true, null, null, null);

        public final boolean tokenRequired;
        public final boolean reducedResult;
        public final String[] requiredArgs;
        public final String[] optionalArgs;
        public final String name;
        public final Class<?> responseClass;

        private Method(String name, boolean reducedResult, boolean tokenRequired, String[] requiredArgs, String[] optionalArgs, Class<?> responseClass) {
            this.name = name;
            this.reducedResult = reducedResult;
            this.tokenRequired = tokenRequired;
            this.requiredArgs = requiredArgs;
            this.optionalArgs = optionalArgs;
            this.responseClass = responseClass;
        }
    }

    /** RPC errors */
    public enum Error {
        BAD_RESPONSE,
        BAD_IO,
        UNSUPPORTED_VERSION
    }

    public static class RPCResult {
        public final Object result;
        public final Error error;

        public RPCResult(Object result, Error error) {
            this.result = result;
            this.error = error;
        }
    }

    private String token;
    private String url;
    private final HttpClient httpClient = new DefaultHttpClient();
    private final Gson gson = new GsonBuilder().create();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RPCResult call(Method method) {
        return call(method, null);
    }

    public RPCResult call(Method method, Object[] args) {
        // check base url
        if(TextUtils.isEmpty(url)) {
            throw new IllegalStateException("base url should be set before first RPC call");
        }

        // check token
        if(method.tokenRequired && TextUtils.isEmpty(token)) {
            throw new IllegalArgumentException("method requires token which is not set");
        }

        // check required args
        if(method.requiredArgs != null && (args == null || method.requiredArgs.length < args.length)) {
            throw new IllegalArgumentException("method required " + method.requiredArgs.length + " non-optional args but " + ((args != null) ? args.length : 0) + " args provided");
        }

        // custom attrs list
        Map<String, String> attrs = new HashMap<String, String>() {
            @Override
            public String toString() {
                String[] tokens = new String[size()];
                int i = 0;
                for(String k : keySet()) {
                    try {
                        tokens[i++] = String.format("%s=%s",  URLEncoder.encode(k, "UTF-8"),  URLEncoder.encode(get(k), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("WTF: why UTF-8 encoding is not supported!?");
                    }
                }
                return TextUtils.join("&", tokens);
            }
        };

        // put method name
        attrs.put(METHOD_ATTR_NAME, method.name);

        // put token if it's provided
        if(!TextUtils.isEmpty(token)) {
            attrs.put(TOKEN_ATTR_NAME, token);
        }

        // put requierd args
        int i = 0;
        if(method.requiredArgs != null) {
            for(String a : method.requiredArgs) {
                attrs.put(a, args[i++].toString());
            }
        }

        // put the rest args
        i = 0;
        if(method.optionalArgs != null && args != null) {
            for(String a : method.optionalArgs) {
                if(args.length > i) {
                    if(args[i] != null) {
                        attrs.put(a, args[i].toString());
                    }
                }
                i++;
            }
        }

        // rpc url
        String url = String.format("%s%c%s", this.url, ((this.url.contains("?")) ? '&' : '?'), attrs);

        Log.d(TAG, "Start RPC to url: " + url);

        Error error = null;
        Object result = null;
        BufferedReader reader = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);

            int code = response.getStatusLine().getStatusCode();
            if(code == 200) {
                String encoding = "UTF-8";
                Header encodingHeader = response.getEntity().getContentEncoding();
                if(encodingHeader != null) {
                    encoding = encodingHeader.getValue();
                }

                reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), encoding));
                StringBuilder sb = new StringBuilder();

                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                JSONObject jsonObj = new JSONObject(new String(sb));
                Object jsonDataObject = jsonObj;

                if(!method.reducedResult) {
                    jsonObj = jsonObj.getJSONObject(method.name);

                    if(jsonObj.has("version") && jsonObj.has("runtime")) {
                        Log.d(TAG, "Response version: " + jsonObj.getString("version"));
                        Log.d(TAG, "Response generated in: " + jsonObj.getDouble("runtime"));
                    }

                    if(method.responseClass != null && method.responseClass.isArray()) {
                        jsonDataObject = jsonObj.getJSONArray("data");
                    } else {
                        jsonDataObject = jsonObj.getJSONObject("data");
                    }
                }

                if(method.responseClass != null) {
                    result = gson.fromJson(jsonDataObject.toString(), method.responseClass);
                } else {
                    result = jsonDataObject;
                }
            } else {
                Log.w(TAG, "api returned code " + code);
                error = Error.BAD_RESPONSE;
            }
        } catch (MalformedURLException e) {
            Log.w(TAG, "bad url: " + url, e);
            error = Error.BAD_IO;
        } catch (IOException e) {
            Log.w(TAG, "bad io", e);
            error = Error.BAD_IO;
        } catch (JSONException e) {
            Log.w(TAG, "bad json", e);
            error = Error.BAD_RESPONSE;
        } finally {
            IOUtils.close(reader);
        }

        return new RPCResult(result, error);
    }
}
