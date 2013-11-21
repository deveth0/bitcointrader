//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.trade.Wallet;
import com.xeiam.xchange.mtgox.v2.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import java.math.BigDecimal;
import java.util.List;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

/**
 * Generic AccountInfo Class
 *
 * @author deveth0
 */
public class ExchangeAccountInfo {

  private final String username;
  private final List<Wallet> wallets;
  private final BigDecimal tradeFee;

  public static ExchangeAccountInfo fromAccountInfo(AccountInfo pAccountInfo) {
    return new ExchangeAccountInfo(pAccountInfo.getUsername(), pAccountInfo.getWallets());
  }

  public static ExchangeAccountInfo fromAccountInfo(MtGoxAccountInfo pAccountInfo) {
    return new ExchangeAccountInfo(pAccountInfo.getLogin(), MtGoxAdapters.adaptWallets(pAccountInfo.getWallets()), pAccountInfo.getTradeFee());
  }

  private ExchangeAccountInfo(String username, List<Wallet> wallets) {
    this.username = username;
    this.wallets = wallets;
    this.tradeFee = null;
  }

  private ExchangeAccountInfo(String username, List<Wallet> wallets, BigDecimal tradeFee) {
    this.username = username;
    this.wallets = wallets;
    this.tradeFee = tradeFee;
  }

  public String getUsername() {
    return username;
  }

  public List<Wallet> getWallets() {
    return wallets;
  }

  public BigDecimal getTradeFee() {
    return tradeFee;
  }

  /**
   * Utility method to locate an exchange balance in the given currency
   *
   * @param currencyUnit A valid currency unit (e.g. CurrencyUnit.USD or CurrencyUnit.of("BTC"))
   * @return The balance, or zero if not found
   */
  public BigMoney getBalance(CurrencyUnit currencyUnit) {

    for (Wallet wallet : wallets) {
      if (wallet.getBalance().getCurrencyUnit().equals(currencyUnit)) {
        return wallet.getBalance();
      }
    }

    // Not found so treat as zero
    return BigMoney.zero(currencyUnit);
  }

}
