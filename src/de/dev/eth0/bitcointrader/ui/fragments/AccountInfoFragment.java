package de.dev.eth0.bitcointrader.ui.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.xeiam.xchange.currency.MoneyUtils;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.math.BigDecimal;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

public final class AccountInfoFragment extends Fragment {

  private CurrencyTextView viewDollar;
  private CurrencyTextView viewBtc;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.account_info_fragment, container, false);
    
  }

  @Override
  public void onResume() {
    super.onResume();
    updateView();
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Resources resources = getResources();
    int textColor = resources.getColor(R.color.fg_significant);
    viewDollar = (CurrencyTextView)view.findViewById(R.id.your_wallet_dollar);
    viewDollar.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewDollar.setTextColor(textColor);
    viewBtc = (CurrencyTextView)view.findViewById(R.id.your_wallet_btc);
    viewBtc.setPrefix(Constants.CURRENCY_CODE_BITCOIN);
    viewBtc.setTextColor(textColor);
  }

  private void updateView() {
    viewDollar.setAmount(MoneyUtils.parseBitcoin("BTC 123.232"));
    viewBtc.setAmount(BigMoney.of(CurrencyUnit.USD, 44.21));
  }
}
