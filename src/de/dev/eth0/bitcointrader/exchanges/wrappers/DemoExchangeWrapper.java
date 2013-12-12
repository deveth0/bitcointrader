//$URL$
//$Id$
package de.dev.eth0.bitcointrader.exchanges.wrappers;

import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.dto.trade.Wallet;
import de.dev.eth0.bitcointrader.data.ExchangeOrderResult;
import de.dev.eth0.bitcointrader.data.ExchangeWalletHistory;
import de.dev.eth0.bitcointrader.exchanges.ExchangeWrapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

public class DemoExchangeWrapper implements ExchangeWrapper {

  private final static Map<String, LimitOrder> orders = new HashMap<String, LimitOrder>();

  private final static Wallet BTC = new Wallet("BTC", BigMoney.parse("BTC 1.4923"));
  private final static Wallet USD = new Wallet("USD", BigMoney.parse("USD 842.123"));
  private final static List<Wallet> wallets = Arrays.asList(BTC, USD);

  public AccountInfo getAccountInfo() throws IOException {

    return new AccountInfo("bitcoinTraderDemo", BigDecimal.valueOf(0.35), wallets);
  }

  public Ticker getTicker(String currency) throws IOException {
    Ticker.TickerBuilder builder = Ticker.TickerBuilder.newInstance();
    builder.withAsk(BigMoney.of(CurrencyUnit.USD, 862.28));
    builder.withBid(BigMoney.of(CurrencyUnit.USD, 860.99));
    builder.withLast(BigMoney.of(CurrencyUnit.USD, 861.11));
    builder.withHigh(BigMoney.of(CurrencyUnit.USD, 991.48));
    builder.withLow(BigMoney.of(CurrencyUnit.USD, 810.53));
    builder.withVolume(BigDecimal.valueOf(12850.31));
    builder.withTimestamp(new Date());
    return builder.build();
  }

  @Override
  public List<LimitOrder> getOpenOrders() throws IOException {
    return new ArrayList<LimitOrder>(orders.values());
  }

  @Override
  public Trades getTrades(String currency) throws IOException {
    // TODO: implement
    return null;
  }

  @Override
  public OrderBook getOrderBook(String currency) throws IOException {
    // TODO: implement
    return null;
  }

  @Override
  public boolean cancelOrder(String id) throws IOException {
    return orders.remove(id) != null;
  }

  @Override
  public String placeMarketOrder(MarketOrder mo) throws IOException {
    return "abcdefghijklmnopqrstuvwxyz1234567890";
  }

  @Override
  public String placeLimitOrder(LimitOrder lo) throws IOException {
    String id = String.valueOf(new Random().nextInt());
    orders.put(id, new LimitOrder(lo.getType(), lo.getTradableAmount(), lo.getTradableIdentifier(),
            lo.getTransactionCurrency(),
            id, new Date(), lo.getLimitPrice()));
    return id;
  }

  @Override
  public ExchangeOrderResult getOrderResult(Order lo) {
    // Not supported
    return null;
  }

  @Override
  public ExchangeWalletHistory getWalletHistory(String currency) {
    //TODO: implement
    return null;
  }

  public String getName() {
    return "Demo Exchange";
  }

}
