package de.dev.eth0.bitcointrader.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.math.BigDecimal;
import java.util.Date;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

public final class PriceInfoFragment extends Fragment {

  private CurrencyTextView viewPriceInfoMin;
  private CurrencyTextView viewPriceInfoCurrent;
  private CurrencyTextView viewPriceInfoMax;
  private CurrencyTextView viewPriceInfoAsk;
  private CurrencyTextView viewPriceInfoBid;
  private TextView viewPriceInfoVolume;
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
    viewPriceInfoMin = (CurrencyTextView)view.findViewById(R.id.price_info_min);
    viewPriceInfoMin.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoCurrent = (CurrencyTextView)view.findViewById(R.id.price_info_current);
    viewPriceInfoCurrent.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoMax = (CurrencyTextView)view.findViewById(R.id.price_info_max);
    viewPriceInfoMax.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoAsk = (CurrencyTextView)view.findViewById(R.id.price_info_ask);
    viewPriceInfoAsk.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoBid = (CurrencyTextView)view.findViewById(R.id.price_info_bid);
    viewPriceInfoBid.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoVolume = (TextView)view.findViewById(R.id.price_info_volume);
    viewPriceInfoLastUpdate = (TextView)view.findViewById(R.id.price_info_lastupdate);
  }

  private void updateView() {
    viewPriceInfoMin.setAmount(BigMoney.of(CurrencyUnit.USD, 99.9943));
    viewPriceInfoCurrent.setAmount(BigMoney.of(CurrencyUnit.USD, 100.345));
    viewPriceInfoMax.setAmount(BigMoney.of(CurrencyUnit.USD, 104.334));
    viewPriceInfoAsk.setAmount(BigMoney.of(CurrencyUnit.USD, 101));
    viewPriceInfoBid.setAmount(BigMoney.of(CurrencyUnit.USD, 99));
    viewPriceInfoVolume.setText("12345");
    viewPriceInfoLastUpdate.setText(new Date().toLocaleString());
  }
}
