//$URL$
//$Id$
package de.dev.eth0.bitcointrader.exchanges;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.data.ExchangeOrderResult;
import de.dev.eth0.bitcointrader.data.ExchangeWalletHistory;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author deveth0
 * @param <T>
 */
public abstract class AbstractExchangeWrapper<T extends Exchange> implements ExchangeWrapper {

  private final ExchangeConfiguration config;
  protected final T exchange;
  private final List<ExchangeConfiguration.EXCHANGE_FEATURE> supportedFeatures;

  public AbstractExchangeWrapper(ExchangeConfiguration config, T exchange, List<ExchangeConfiguration.EXCHANGE_FEATURE> supportedFeatures) {
    this.config = config;
    this.exchange = exchange;
    this.supportedFeatures = supportedFeatures;
  }

  @Override
  public ExchangeConfiguration getConfig() {
    return config;
  }

  @Override
  public OrderBook getOrderBook(String currency) throws IOException {
    return exchange.getPollingMarketDataService().getOrderBook(Currencies.BTC, currency);
  }

  public String placeLimitOrder(LimitOrder lo) throws IOException {
    return exchange.getPollingTradeService().placeLimitOrder(lo);
  }

  public String placeMarketOrder(MarketOrder mo) throws IOException {
    return exchange.getPollingTradeService().placeMarketOrder(mo);
  }

  public boolean cancelOrder(String id) throws IOException {
    return exchange.getPollingTradeService().cancelOrder(id);
  }

  public Trades getTrades(String currency) throws IOException {
    return exchange.getPollingMarketDataService().getTrades(Currencies.BTC, currency);
  }

  public Ticker getTicker(String currency) throws IOException {
    return exchange.getPollingMarketDataService().getTicker(Currencies.BTC, currency);
  }

  public AccountInfo getAccountInfo() throws IOException {
    return exchange.getPollingAccountService().getAccountInfo();
  }


  public List<LimitOrder> getOpenOrders() throws IOException {
    return exchange.getPollingTradeService().getOpenOrders().getOpenOrders();
  }

  // Those are optional methods not supported by many exchanges
  @Override
  public ExchangeOrderResult getOrderResult(Order lo) {
    // Not supported
    return null;
  }

  @Override
  public ExchangeWalletHistory getWalletHistory(String currency) {
    // Not supported
    return null;
  }

  @Override
  public boolean supportsFeature(ExchangeConfiguration.EXCHANGE_FEATURE feature) {
    return supportedFeatures.contains(feature);
  }

}
