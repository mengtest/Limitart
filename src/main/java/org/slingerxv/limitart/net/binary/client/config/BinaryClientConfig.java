package org.slingerxv.limitart.net.binary.client.config;

import org.slingerxv.limitart.funcs.Proc1;
import org.slingerxv.limitart.funcs.Proc2;
import org.slingerxv.limitart.net.binary.client.BinaryClient;
import org.slingerxv.limitart.net.binary.codec.AbstractBinaryDecoder;
import org.slingerxv.limitart.net.binary.codec.AbstractBinaryEncoder;
import org.slingerxv.limitart.net.binary.message.Message;
import org.slingerxv.limitart.net.binary.message.MessageFactory;
import org.slingerxv.limitart.net.struct.AddressPair;

/**
 * 二进制通信客户端配置
 * 
 * @author hank
 *
 */
public final class BinaryClientConfig {
	private String clientName;
	private AddressPair remoteAddress;
	private int autoReconnect;
	private AbstractBinaryDecoder decoder;
	private AbstractBinaryEncoder encoder;
	private MessageFactory factory;
	// ----listener
	private Proc1<BinaryClient> onChannelActive;
	private Proc1<BinaryClient> onChannelInactive;
	private Proc2<BinaryClient, Throwable> onExceptionCaught;
	private Proc1<BinaryClient> onConnectionEffective;
	private Proc1<Message> dispatchMessage;

	private BinaryClientConfig(BinaryClientConfigBuilder builder) {
		this.clientName = builder.clientName;
		this.remoteAddress = builder.remoteAddress;
		this.autoReconnect = builder.autoReconnect;
		this.decoder = builder.decoder;
		this.encoder = builder.encoder;
		if (builder.factory == null) {
			throw new NullPointerException("factory");
		}
		this.factory = builder.factory;
	}

	public String getClientName() {
		return this.clientName;
	}

	public AddressPair getRemoteAddress() {
		return remoteAddress;
	}

	public int getAutoReconnect() {
		return autoReconnect;
	}

	public AbstractBinaryDecoder getDecoder() {
		return decoder;
	}

	public AbstractBinaryEncoder getEncoder() {
		return encoder;
	}

	public MessageFactory getFactory() {
		return factory;
	}

	public static class BinaryClientConfigBuilder {
		private String clientName;
		private AddressPair remoteAddress;
		private int autoReconnect;
		private AbstractBinaryDecoder decoder;
		private AbstractBinaryEncoder encoder;
		private MessageFactory factory;

		public BinaryClientConfigBuilder() {
			this.clientName = "Binary-Client";
			this.remoteAddress = new AddressPair("127.0.0.1", 8888);
			this.autoReconnect = 0;
			this.decoder = AbstractBinaryDecoder.DEFAULT_DECODER;
			this.encoder = AbstractBinaryEncoder.DEFAULT_ENCODER;
		}

		/**
		 * 构建配置
		 * 
		 * @return
		 */
		public BinaryClientConfig build() {
			return new BinaryClientConfig(this);
		}

		public BinaryClientConfigBuilder decoder(AbstractBinaryDecoder decoder) {
			this.decoder = decoder;
			return this;
		}

		public BinaryClientConfigBuilder encoder(AbstractBinaryEncoder encoder) {
			this.encoder = encoder;
			return this;
		}

		public BinaryClientConfigBuilder clientName(String clientName) {
			this.clientName = clientName;
			return this;
		}

		/**
		 * 服务器IP
		 * 
		 * @param remoteIp
		 * @return
		 */
		public BinaryClientConfigBuilder remoteAddress(AddressPair remoteAddress) {
			this.remoteAddress = remoteAddress;
			return this;
		}

		/**
		 * 自动重连尝试间隔(秒)
		 * 
		 * @param autoReconnect
		 * @return
		 */
		public BinaryClientConfigBuilder autoReconnect(int autoReconnect) {
			this.autoReconnect = autoReconnect;
			return this;
		}

		public BinaryClientConfigBuilder factory(MessageFactory factory) {
			this.factory = factory;
			return this;
		}
	}
}
