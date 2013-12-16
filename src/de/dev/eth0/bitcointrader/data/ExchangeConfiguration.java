//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import android.app.Activity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import de.dev.eth0.bitcointrader.exchanges.extensions.ExtendedMtGoxExchange;
import de.dev.eth0.bitcointrader.ui.exchanges.BitstampSetupActivity;
import de.dev.eth0.bitcointrader.ui.exchanges.MtGoxSetupActivity;
import java.util.UUID;

/**
 * A serializable ExchangeConfiguration
 *
 * @author Alexander Muthmann
 */
public class ExchangeConfiguration {

  public enum EXCHANGE_CONNECTION_SETTING {
    DEMO("Demo", "Demo Exchange", null),
    MTGOX("MtGox", ExtendedMtGoxExchange.class.getName(), MtGoxSetupActivity.class),
    BITSTAMP("Bitstamp", BitstampExchange.class.getName(), BitstampSetupActivity.class),
    BTCN("BTCN", ExtendedMtGoxExchange.class.getName(), BitstampSetupActivity.class);

    private final String displayName;
    private final String exchangeClassName;
    private final Class<? extends Activity> setupActivity;

    private EXCHANGE_CONNECTION_SETTING(String displayName, String exchangeClassName, Class<? extends Activity> setupActivity) {
      this.displayName = displayName;
      this.exchangeClassName = exchangeClassName;
      this.setupActivity = setupActivity;
    }

    public String getExchangeClassName() {
      return exchangeClassName;
    }

    public String getDisplayName() {
      return displayName;
    }

    public Class<? extends Activity> getSetupActivity() {
      return setupActivity;
    }
  }
  private final String id;
  private final String name;
  private final String userName;
  private final String apiKey;
  private final String secretKey;
  private boolean primary;
  private boolean enabled;
  private final EXCHANGE_CONNECTION_SETTING connectionSettings;

  public ExchangeConfiguration(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("userName") String userName, @JsonProperty("apiKey") String apiKey,
          @JsonProperty("secretKey") String secretKey, @JsonProperty("primary") Boolean primary, @JsonProperty("enabled") Boolean enabled,
          @JsonProperty("connectionSettings") EXCHANGE_CONNECTION_SETTING connectionSettings) {
    this.id = id == null ? UUID.randomUUID().toString() : id;
    this.primary = primary == null ? false : primary;
    this.enabled = enabled == null ? true : enabled;
    this.name = name;
    this.userName = userName;
    this.apiKey = apiKey;
    this.secretKey = secretKey;
    this.connectionSettings = connectionSettings;
  }

  public String getId() {
    return id;
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

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return "ExchangeConfiguration{" + "apiKey=" + apiKey + ", secretKey=" + secretKey + ", connectionSettings=" + connectionSettings + '}';
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + (this.id != null ? this.id.hashCode() : 0);
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
    final ExchangeConfiguration other = (ExchangeConfiguration)obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    return true;
  }
}
