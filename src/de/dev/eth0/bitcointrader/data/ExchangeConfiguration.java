//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.dev.eth0.bitcointrader.Constants;

/**
 * A serializable ExchangeConfiguration
 *
 * @author Alexander Muthmann
 */
public class ExchangeConfiguration {

  public enum EXCHANGE_CONNECTION_SETTING {

    MTGOX(Constants.MTGOX_SSL_URI, Constants.MTGOX_PLAIN_WEBSOCKET_URI, Constants.MTGOX_SSL_WEBSOCKET_URI),
    BTCN(Constants.MTGOX_SSL_URI, Constants.MTGOX_PLAIN_WEBSOCKET_URI, Constants.MTGOX_SSL_WEBSOCKET_URI);

    private final String sslUri;
    private final String plainTextUriStreaming;
    private final String sslUriStreaming;

    private EXCHANGE_CONNECTION_SETTING(String sslUri, String plainTextUriStreaming, String sslUriStreaming) {
      this.sslUri = sslUri;
      this.plainTextUriStreaming = plainTextUriStreaming;
      this.sslUriStreaming = sslUriStreaming;
    }

    public String getSslUri() {
      return sslUri;
    }

    public String getPlainTextUriStreaming() {
      return plainTextUriStreaming;
    }

    public String getSslUriStreaming() {
      return sslUriStreaming;
    }
  }

  private final String name;
  private final String apiKey;
  private final String secretKey;
  private final EXCHANGE_CONNECTION_SETTING connectionSettings;

  public ExchangeConfiguration(@JsonProperty("name") String name, @JsonProperty("apiKey") String apiKey,
          @JsonProperty("secretKey") String secretKey,
          @JsonProperty("connectionSettings") EXCHANGE_CONNECTION_SETTING connectionSettings) {
    this.name = name;
    this.apiKey = apiKey;
    this.secretKey = secretKey;
    this.connectionSettings = connectionSettings;
  }

  public String getName() {
    return name;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public EXCHANGE_CONNECTION_SETTING getConnectionSettings() {
    return connectionSettings;
  }

  @Override
  public String toString() {
    return "ExchangeConfiguration{" + "apiKey=" + apiKey + ", secretKey=" + secretKey + ", connectionSettings=" + connectionSettings + '}';
  }

}
