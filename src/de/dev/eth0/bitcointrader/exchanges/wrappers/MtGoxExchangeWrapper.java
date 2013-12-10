//$URL$
//$Id$
package de.dev.eth0.bitcointrader.exchanges.wrappers;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistory;
import de.dev.eth0.bitcointrader.data.ExchangeOrderResult;
import de.dev.eth0.bitcointrader.data.ExchangeWalletHistory;
import de.dev.eth0.bitcointrader.exchanges.AbstractExchangeWrapper;
import de.dev.eth0.bitcointrader.exchanges.extensions.ExtendedMtGoxExchange;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author deveth0
 */
public class MtGoxExchangeWrapper extends AbstractExchangeWrapper<ExtendedMtGoxExchange> {

  public MtGoxExchangeWrapper(Exchange exchange) {
    super((ExtendedMtGoxExchange) exchange);
  }

  @Override
  public ExchangeOrderResult getOrderResult(Order lo) {
    return ExchangeOrderResult.from(exchange.getPollingTradeService().getOrderResult(lo));
  }

  @Override
  public ExchangeWalletHistory getWalletHistory(String currency) {
    MtGoxWalletHistory walletHistory = exchange.getPollingAccountService().getMtGoxWalletHistory(currency, null);
    List<MtGoxWalletHistory> pages = new ArrayList<MtGoxWalletHistory>();
    if (walletHistory != null) {
      pages.add(walletHistory);
      if (walletHistory.getCurrentPage() < walletHistory.getMaxPage()) {
        for (int page = 2; page <= walletHistory.getMaxPage(); page++) {
          walletHistory = exchange.getPollingAccountService().getMtGoxWalletHistory(currency, page);
          if (walletHistory != null) {
            pages.add(walletHistory);
          }
        }
      }
    }
    if (!pages.isEmpty()) {
      return ExchangeWalletHistory.from(pages);
    }
    return null;
  }

}
