//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistoryEntry;
import de.dev.eth0.bitcointrader.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

/**
 *
 * @author deveth0
 */
public class ExchangeWalletHistoryEntry {

  public static List<ExchangeWalletHistoryEntry> fromMtGoxWalletHistoryEntries(MtGoxWalletHistoryEntry[] mtGoxWalletHistoryEntries) {
    List<ExchangeWalletHistoryEntry> ret = new ArrayList<ExchangeWalletHistoryEntry>();
    for (MtGoxWalletHistoryEntry entry : mtGoxWalletHistoryEntries) {
      ret.add(ExchangeWalletHistoryEntry.fromMtGoxWalletHistoryEntry(entry));
    }
    return ret;
  }

  public static ExchangeWalletHistoryEntry fromMtGoxWalletHistoryEntry(MtGoxWalletHistoryEntry entry) {
    return new ExchangeWalletHistoryEntry(entry.getDate(),
            entry.getInfo(),
            BigMoney.of(CurrencyUnit.of(entry.getValue().getCurrency()), entry.getValue().getValue()),
            BigMoney.of(CurrencyUnit.of(entry.getBalance().getCurrency()), entry.getBalance().getValue()),
            HISTORY_ENTRY_TYPE.valueOf(entry.getType().toUpperCase(Locale.US)));
  }

  //TODO: can this be changed to Date?
  private final String date;
  private final String infoText;
  private final BigMoney value;
  private final BigMoney balance;
  private final HISTORY_ENTRY_TYPE type;

  private ExchangeWalletHistoryEntry(String date, String infoText, BigMoney value, BigMoney balance, HISTORY_ENTRY_TYPE type) {
    this.date = date;
    this.infoText = infoText;
    this.value = value;
    this.balance = balance;
    this.type = type;
  }

  public HISTORY_ENTRY_TYPE getType() {
    return type;
  }

  public String getDate() {
    return date;
  }

  public BigMoney getValue() {
    return value;
  }

  public BigMoney getBalance() {
    return balance;
  }

  public String getInfoText() {
    return infoText;
  }

  public enum HISTORY_ENTRY_TYPE {

    OUT(R.string.wallet_history_out, R.color.history_out),
    FEE(R.string.wallet_history_fee, R.color.history_fee),
    IN(R.string.wallet_history_in, R.color.history_in),
    SPENT(R.string.wallet_history_spent, R.color.history_spent),
    EARNED(R.string.wallet_history_earned, R.color.history_earned),
    WITHDRAW(R.string.wallet_history_withdraw, R.color.history_withdraw),
    DEPOSIT(R.string.wallet_history_deposit, R.color.history_deposit);
    private final int mText;
    private final int mColor;

    private HISTORY_ENTRY_TYPE(int pText, int pColor) {
      this.mText = pText;
      this.mColor = pColor;
    }

    public int getColor() {
      return mColor;
    }

    public int getText() {
      return mText;
    }
  }
}
