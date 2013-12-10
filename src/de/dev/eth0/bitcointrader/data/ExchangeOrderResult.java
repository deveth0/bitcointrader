//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import com.xeiam.xchange.mtgox.v2.dto.trade.polling.MtGoxOrderResult;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

/**
 *
 * @author deveth0
 */
public class ExchangeOrderResult {

  public static ExchangeOrderResult from(MtGoxOrderResult orderResult) {
    return new ExchangeOrderResult(
            BigMoney.of(CurrencyUnit.of(orderResult.getTotalAmount().getCurrency()), orderResult.getTotalAmount().getValue()),
            BigMoney.of(CurrencyUnit.of(orderResult.getAvgCost().getCurrency()), orderResult.getAvgCost().getValue()),
            BigMoney.of(CurrencyUnit.of(orderResult.getTotalSpent().getCurrency()), orderResult.getTotalSpent().getValue()));
  }

  private final BigMoney totalAmount;
  private final BigMoney avgCost;
  private final BigMoney totalSpent;

  private ExchangeOrderResult(BigMoney totalAmount, BigMoney avgCost, BigMoney totalSpent) {
    this.totalAmount = totalAmount;
    this.avgCost = avgCost;
    this.totalSpent = totalSpent;
  }

  public BigMoney getTotalAmount() {
    return totalAmount;
  }

  public BigMoney getAvgCost() {
    return avgCost;
  }

  public BigMoney getTotalSpent() {
    return totalSpent;
  }
}
