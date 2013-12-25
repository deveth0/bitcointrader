//$URL$
//$Id$
package de.dev.eth0.bitcointrader.exchanges.wrappers;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.exchanges.AbstractExchangeWrapper;
import java.util.ArrayList;

/**
 *
 * @author deveth0
 */
public class BitstampExchangeWrapper extends AbstractExchangeWrapper<BitstampExchange> {

  public BitstampExchangeWrapper(ExchangeConfiguration config, Exchange exchange) {
    super(config, (BitstampExchange)exchange, new ArrayList<ExchangeConfiguration.EXCHANGE_FEATURE>());
  }

}
