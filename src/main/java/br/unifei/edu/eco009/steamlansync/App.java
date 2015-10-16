package br.edu.unifei.eco009.steamlansync;

import java.net.InetSocketAddress;
import java.util.Hashtable;

import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.littleshoot.proxy.ActivityTracker;
import org.littleshoot.proxy.FlowContext;
import org.littleshoot.proxy.FullFlowContext;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                .withPort(12321)
                .withTransparent(true)
//                .plusActivityTracker(imprimirChunks)
                .withFiltersSource(filterChunks)
                .start();
    }
    
    public static Hashtable<String, FullHttpResponse> chunks = new Hashtable<String, FullHttpResponse>();
    
    private static String getChunkId(HttpRequest req) {
    	String uri = req.getUri();
    	return uri.split("chunk\\/")[1].split("\\?")[0];
    }
    
    private static final ActivityTracker imprimirChunks = new ActivityTracker() {
		
		public void responseSentToClient(FlowContext flowContext, HttpResponse httpResponse) {
			// TODO Auto-generated method stub
			
		}
		
		public void responseReceivedFromServer(FullFlowContext flowContext, HttpResponse httpResponse) {
			if (httpResponse.headers().get("content-type").equals("application/x-steam-chunk"))
				System.out.println(httpResponse);
			
		}
		
		public void requestSentToServer(FullFlowContext flowContext, HttpRequest httpRequest) {
			if (httpRequest.getMethod().equals(HttpMethod.GET)
					&& httpRequest.getUri().contains("steampowered.com/depot/"))
			System.out.println(httpRequest.getUri());
			System.out.println(getChunkId(httpRequest));
			
		}
		
		public void requestReceivedFromClient(FlowContext flowContext, HttpRequest httpRequest) {
			// TODO Auto-generated method stub
			
		}
		
		public void clientSSLHandshakeSucceeded(InetSocketAddress clientAddress, SSLSession sslSession) {
			// TODO Auto-generated method stub
			
		}
		
		public void clientDisconnected(InetSocketAddress clientAddress, SSLSession sslSession) {
			// TODO Auto-generated method stub
			
		}
		
		public void clientConnected(InetSocketAddress clientAddress) {
			// TODO Auto-generated method stub
			
		}
		
		public void bytesSentToServer(FullFlowContext flowContext, int numberOfBytes) {
			// TODO Auto-generated method stub
			
		}
		
		public void bytesSentToClient(FlowContext flowContext, int numberOfBytes) {
			// TODO Auto-generated method stub
			
		}
		
		public void bytesReceivedFromServer(FullFlowContext flowContext, int numberOfBytes) {
			// TODO Auto-generated method stub
			
		}
		
		public void bytesReceivedFromClient(FlowContext flowContext, int numberOfBytes) {
			// TODO Auto-generated method stub
			
		}
	};
	private static final HttpFiltersSource filterChunks = new HttpFiltersSource() {

		
		public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
			if (originalRequest.getMethod().equals(HttpMethod.GET)
					&& originalRequest.getUri().contains("steampowered.com/depot/")
					&& originalRequest.getUri().contains("chunk")){
			String chunkId = getChunkId(originalRequest);
			if (chunks.containsKey(chunkId)) { // se o chunk está em cache,
												// alterar o precedimento da
												// request:
				System.out.println("cache HIT");
				return new HttpFiltersAdapter(originalRequest) {
					@Override
					public HttpResponse proxyToServerRequest(HttpObject httpObject) {
						// retorna a resposta que está em cache
						return chunks.get(getChunkId(originalRequest));
					}
				};
			}

			else {
				System.out.println("cache MISS");
				return new HttpFiltersAdapter(originalRequest) {

					@Override
					public HttpObject proxyToClientResponse(HttpObject httpObject) {
						chunks.put(getChunkId(originalRequest), (FullHttpResponse) httpObject);
						return super.proxyToClientResponse(httpObject);
					} 



				};
			}
		}
			else return null;
		}

		public int getMaximumRequestBufferSizeInBytes() {
			// TODO Auto-generated method stub
			return 10000000;
		}

		public int getMaximumResponseBufferSizeInBytes() {
			// TODO Auto-generated method stub
			return 10000000;
		}
	};
	
}
