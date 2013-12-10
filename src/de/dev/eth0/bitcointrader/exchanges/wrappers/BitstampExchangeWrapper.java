//$URL$
//$Id$
package de.dev.eth0.bitcointrader.exchanges.wrappers;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import de.dev.eth0.bitcointrader.exchanges.AbstractExchangeWrapper;
import java.io.IOException;

/**
 *
 * @author deveth0
 */
public class BitstampExchangeWrapper extends AbstractExchangeWrapper<BitstampExchange> {

  public BitstampExchangeWrapper(Exchange exchange) {
    super((BitstampExchange) exchange);
  }

  @Override
  public OrderBook getPartialOrderBook(String currency) throws IOException {
    return exchange.getPollingMarketDataService().getFullOrderBook(Currencies.BTC, currency);
  }

}
