//$URL$
//$Id$
package de.dev.eth0.bitcointrader.data;

import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxWalletHistory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author deveth0
 */
public class ExchangeWalletHistory {

  public final List<ExchangeWalletHistoryEntry> entries;

  public static ExchangeWalletHistory fromMtGoxWalletHistory(List<MtGoxWalletHistory> pages) {
    List<ExchangeWalletHistoryEntry> entries = new ArrayList<ExchangeWalletHistoryEntry>();
    for (MtGoxWalletHistory page : pages) {
      entries.addAll(ExchangeWalletHistoryEntry.fromMtGoxWalletHistoryEntries(page.getMtGoxWalletHistoryEntries()));
    }
    return new ExchangeWalletHistory(entries);
  }

  private ExchangeWalletHistory(List<ExchangeWalletHistoryEntry> entries) {
    this.entries = entries;
  }

  public Collection<ExchangeWalletHistoryEntry> getWalletHistoryEntries() {
    return entries;
  }

}
