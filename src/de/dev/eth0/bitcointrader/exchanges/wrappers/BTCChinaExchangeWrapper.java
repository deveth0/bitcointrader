//$URL$
//$Id$
package de.dev.eth0.bitcointrader.exchanges.wrappers;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.btcchina.BTCChinaExchange;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.exchanges.AbstractExchangeWrapper;
import java.util.ArrayList;

/**
 *
 * @author deveth0
 */
public class BTCChinaExchangeWrapper extends AbstractExchangeWrapper<BTCChinaExchange> {

  public BTCChinaExchangeWrapper(ExchangeConfiguration config, Exchange exchange) {
    super(config, (BTCChinaExchange)exchange, new ArrayList<ExchangeConfiguration.EXCHANGE_FEATURE>());
  }

}
