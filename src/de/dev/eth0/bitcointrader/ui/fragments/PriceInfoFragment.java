package de.dev.eth0.bitcointrader.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.math.BigDecimal;
import java.util.Date;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

public final class PriceInfoFragment extends SherlockFragment {

  private CurrencyTextView viewPriceInfoMin;
  private CurrencyTextView viewPriceInfoCurrent;
  private CurrencyTextView viewPriceInfoMax;
  private CurrencyTextView viewPriceInfoAsk;
  private CurrencyTextView viewPriceInfoBid;
  private AmountTextView viewPriceInfoVolume;
  private TextView viewPriceInfoLastUpdate;

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.price_info_fragment, container, false);

  }

  @Override
  public void onResume() {
    super.onResume();
    updateView();
  }

  @Override
  public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewPriceInfoMin = (CurrencyTextView) view.findViewById(R.id.price_info_min);
    viewPriceInfoCurrent = (CurrencyTextView) view.findViewById(R.id.price_info_current);
    viewPriceInfoMax = (CurrencyTextView) view.findViewById(R.id.price_info_max);
    viewPriceInfoAsk = (CurrencyTextView) view.findViewById(R.id.price_info_ask);
    viewPriceInfoBid = (CurrencyTextView) view.findViewById(R.id.price_info_bid);
    viewPriceInfoVolume = (AmountTextView) view.findViewById(R.id.price_info_volume);
    viewPriceInfoLastUpdate = (TextView) view.findViewById(R.id.price_info_lastupdate);
  }

  private void updateView() {
    viewPriceInfoMin.setAmount(BigMoney.of(CurrencyUnit.USD, 99.9943));
    viewPriceInfoCurrent.setAmount(BigMoney.of(CurrencyUnit.USD, 100.345));
    viewPriceInfoMax.setAmount(BigMoney.of(CurrencyUnit.USD, 104.334));
    viewPriceInfoAsk.setAmount(BigMoney.of(CurrencyUnit.USD, 101));
    viewPriceInfoBid.setAmount(BigMoney.of(CurrencyUnit.USD, 99));
    viewPriceInfoVolume.setAmount(BigDecimal.valueOf(12342.12));
    viewPriceInfoLastUpdate.setText(new Date().toLocaleString());
  }
}
