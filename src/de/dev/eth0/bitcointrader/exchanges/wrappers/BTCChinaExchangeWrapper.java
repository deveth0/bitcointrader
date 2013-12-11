//$URL$
//$Id$
package de.dev.eth0.bitcointrader.exchanges.wrappers;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.btcchina.BTCChinaExchange;
import de.dev.eth0.bitcointrader.exchanges.AbstractExchangeWrapper;

/**
 *
 * @author deveth0
 */
public class BTCChinaExchangeWrapper extends AbstractExchangeWrapper<BTCChinaExchange> {

  public BTCChinaExchangeWrapper(String name, Exchange exchange) {
    super(name, (BTCChinaExchange)exchange);
  }

}
