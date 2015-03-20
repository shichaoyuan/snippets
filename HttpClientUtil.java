
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

/**
 * 
 * @author yuanshichao
 *
 */
public class HttpClientUtil {
	
	private static final Log LOG = LogFactory.getLog(HttpClientUtil.class);

	private static final PoolingHttpClientConnectionManager CONN_MGR;
	private static final CloseableHttpClient HTTP_CLIENT;
	
	private static final SocketConfig DEFAULT_SOCKET_CONFIG;
    private static final ConnectionConfig DEFAULT_CONN_CONFIG;
    private static final RequestConfig DEFAULT_REQ_CONFIG;
    
    private static final int DEFAULT_BUFFER_SIZE = 1024 *1024;
    private static final Charset DEFAULT_CHARSET = Consts.UTF_8;
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 1 * 1000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 1 * 1000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;

    private static final int DEFAULT_MAX_TOTAL = 512;
    private static final int DEFAULT_MAX_PER_ROUTE = 2;

	static {

		DEFAULT_SOCKET_CONFIG = SocketConfig.DEFAULT;

		DEFAULT_CONN_CONFIG = ConnectionConfig
				.custom()
				.setBufferSize(DEFAULT_BUFFER_SIZE)
				.setCharset(DEFAULT_CHARSET)
				.build();

		DEFAULT_REQ_CONFIG = RequestConfig
				.custom()
				.setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT)
				.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
				.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
				.build();

		CONN_MGR = new PoolingHttpClientConnectionManager();
		CONN_MGR.setMaxTotal(DEFAULT_MAX_TOTAL);
		CONN_MGR.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
		CONN_MGR.setDefaultConnectionConfig(DEFAULT_CONN_CONFIG);
		CONN_MGR.setDefaultSocketConfig(DEFAULT_SOCKET_CONFIG);

		HTTP_CLIENT = HttpClients
				.custom()
				.setConnectionManager(CONN_MGR)
				.setDefaultRequestConfig(DEFAULT_REQ_CONFIG)
				.build();
	}
	
	private HttpClientUtil() {}

	/**
	 * 针对不同的host，自定义最大连接数和Socket、Connection配置
	 * 
	 * @param host
	 * @param maxConnection
	 * @param sc
	 * @param cc
	 */
	public static void customize(HttpHost host, int maxConnection,
			SocketConfig sc, ConnectionConfig cc) {
		if (host == null) {
			return;
		}

		if (maxConnection > 0) {
			CONN_MGR.setMaxPerRoute(new HttpRoute(host), maxConnection);
		}

		if (sc != null) {
			CONN_MGR.setSocketConfig(host, sc);
		}

		if (cc != null) {
			CONN_MGR.setConnectionConfig(host, cc);
		}
	}
	
	/**
	 * 针对不同的host，自定义最大连接数
	 * @param host
	 * @param maxConnection
	 */
	public static void customize(HttpHost host, int maxConnection) {
		customize(host, maxConnection, null, null);
	}
	
	/**
	 * 发送Http请求
	 * StatusCode==200时，返回Entity字符串
	 * 否则返回null
	 * 
	 * @param request
	 * @return
	 */
	public static String send(HttpUriRequest request) {
		CloseableHttpResponse response = null;
		try {
			response = HTTP_CLIENT.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return EntityUtils.toString(response.getEntity(), Consts.UTF_8);
			} else {
				LOG.warn("[HttpClient] the Response is [" + response + "]");
			}
		} catch (Exception e) {
			LOG.error("[HttpClient] the Request is [" + request + "]", e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					LOG.error("[HttpCLient] Response close", e);
				}
			}
		}
		return null;
	}

	public static CloseableHttpClient getHttpClient(){
		return HttpClientUtil.HTTP_CLIENT;
	}
}

