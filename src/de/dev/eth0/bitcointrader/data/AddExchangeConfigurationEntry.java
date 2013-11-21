//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import de.dev.eth0.bitcointrader.ui.InitialSetupActivity;
import de.dev.eth0.bitcointrader.ui.exchanges.MtGoxSetupActivity;

/**
 *
 * @author deveth0
 */
public class AddExchangeConfigurationEntry {
  public final static AddExchangeConfigurationEntry MTGOX = new AddExchangeConfigurationEntry("mtGox", MtGoxSetupActivity.class);
  public final static AddExchangeConfigurationEntry BTCN = new AddExchangeConfigurationEntry("btcn", InitialSetupActivity.class);

  private final String name;
  private final Class setupActivity;

  private AddExchangeConfigurationEntry(String name, Class setupActivity) {
    this.name = name;
    this.setupActivity = setupActivity;
  }

  public String getName() {
    return name;
  }

  public Class getSetupActivity() {
    return setupActivity;
  }

}
