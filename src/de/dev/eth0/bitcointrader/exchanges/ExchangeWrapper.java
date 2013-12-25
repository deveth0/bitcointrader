//$URL$
//$Id$
package de.dev.eth0.bitcointrader.exchanges;

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
 */
public interface ExchangeWrapper {

  /**
   * Returns the configuration used for this exchange
   *
   * @return config
   */
  public ExchangeConfiguration getConfig();

  /**
   * Returns the wallet history for the given currency
   *
   * @param currency
   * @return
   * @throws java.io.IOException
   */
  public ExchangeWalletHistory getWalletHistory(String currency) throws IOException;

  /**
   * returns the account info
   *
   * @return
   * @throws java.io.IOException
   */
  public AccountInfo getAccountInfo() throws IOException;


  /**
   * Returns the ticker for the given currency
   *
   * @param currency
   * @return
   * @throws java.io.IOException
   */
  public Ticker getTicker(String currency) throws IOException;

  /**
   * Returns a list with all open orders
   *
   * @return
   * @throws java.io.IOException
   */
  public List<LimitOrder> getOpenOrders() throws IOException;

  /**
   * Returns the result for the given order
   *
   * @param lo
   * @return
   * @throws java.io.IOException
   */
  public ExchangeOrderResult getOrderResult(Order lo) throws IOException;

  /**
   * Returns all trades for the given currency
   *
   * @param currency
   * @return
   * @throws java.io.IOException
   */
  public Trades getTrades(String currency) throws IOException;


  /**
   * Returns the orderbook for the given currency
   *
   * @param currency
   * @throws IOException
   * @return
   */
  public OrderBook getOrderBook(String currency) throws IOException;

  /**
   * cancels the given order
   *
   * @param id
   * @return
   * @throws java.io.IOException
   */
  public boolean cancelOrder(String id) throws IOException;

  /**
   * Places the market order
   *
   * @param mo
   * @return
   * @throws java.io.IOException
   */
  public String placeMarketOrder(MarketOrder mo) throws IOException;

  /**
   * Places a limit order
   *
   * @param lo
   * @return
   * @throws java.io.IOException
   */
  public String placeLimitOrder(LimitOrder lo) throws IOException;

  /**
   * Checks if the exchange supports this feature
   *
   * @param feature
   * @return
   */
  public boolean supportsFeature(ExchangeConfiguration.EXCHANGE_FEATURE feature);
}
