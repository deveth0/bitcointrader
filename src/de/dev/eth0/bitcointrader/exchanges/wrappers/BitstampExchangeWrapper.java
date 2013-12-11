//$URL$
//$Id$
package de.dev.eth0.bitcointrader.exchanges.wrappers;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import de.dev.eth0.bitcointrader.exchanges.AbstractExchangeWrapper;

/**
 *
 * @author deveth0
 */
public class BitstampExchangeWrapper extends AbstractExchangeWrapper<BitstampExchange> {

  public BitstampExchangeWrapper(String name, Exchange exchange) {
    super(name, (BitstampExchange)exchange);
  }

}
