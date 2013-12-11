//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import de.dev.eth0.bitcointrader.exchanges.extensions.ExtendedMtGoxExchange;

/**
 * A serializable ExchangeConfiguration
 *
 * @author Alexander Muthmann
 */
public class ExchangeConfiguration {

  public enum EXCHANGE_CONNECTION_SETTING {

    MTGOX(ExtendedMtGoxExchange.class.getName()),
    BITSTAMP(BitstampExchange.class.getName()),
    BTCN(ExtendedMtGoxExchange.class.getName());

    private final String exchangeClassName;

    private EXCHANGE_CONNECTION_SETTING(String exchangeClassName) {
      this.exchangeClassName = exchangeClassName;
    }

    public String getExchangeClassName() {
      return exchangeClassName;
    }

  }

  private final String name;
  private final String userName;
  private final String apiKey;
  private final String secretKey;
  private final EXCHANGE_CONNECTION_SETTING connectionSettings;

  public ExchangeConfiguration(@JsonProperty("name") String name, @JsonProperty("userName") String userName, @JsonProperty("apiKey") String apiKey,
          @JsonProperty("secretKey") String secretKey,
          @JsonProperty("connectionSettings") EXCHANGE_CONNECTION_SETTING connectionSettings) {
    this.name = name;
    this.userName = userName;
    this.apiKey = apiKey;
    this.secretKey = secretKey;
    this.connectionSettings = connectionSettings;
  }

  public String getName() {
    return name;
  }

  public String getUserName() {
    return userName;
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

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 43 * hash + (this.apiKey != null ? this.apiKey.hashCode() : 0);
    hash = 43 * hash + (this.secretKey != null ? this.secretKey.hashCode() : 0);
    hash = 43 * hash + (this.connectionSettings != null ? this.connectionSettings.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ExchangeConfiguration other = (ExchangeConfiguration) obj;
    if ((this.apiKey == null) ? (other.apiKey != null) : !this.apiKey.equals(other.apiKey)) {
      return false;
    }
    if ((this.secretKey == null) ? (other.secretKey != null) : !this.secretKey.equals(other.secretKey)) {
      return false;
    }
    if (this.connectionSettings != other.connectionSettings) {
      return false;
    }
    return true;
  }


}
