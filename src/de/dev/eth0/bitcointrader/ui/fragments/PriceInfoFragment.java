package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;

public final class PriceInfoFragment extends Fragment {

  private CurrencyTextView viewPriceInfoMin;
  private CurrencyTextView viewPriceInfoCurrent;
  private CurrencyTextView viewPriceInfoMax;

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
    viewPriceInfoMin.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoCurrent = (CurrencyTextView) view.findViewById(R.id.price_info_current);
    viewPriceInfoCurrent.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoMax = (CurrencyTextView) view.findViewById(R.id.price_info_max);
    viewPriceInfoMax.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
  }

  private void updateView() {
    viewPriceInfoMin.setAmount(123.45);
    viewPriceInfoCurrent.setAmount(129.45);
    viewPriceInfoMax.setAmount(135.45);
  }
}
