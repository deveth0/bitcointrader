package de.dev.eth0.bitcointrader.ui.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.math.BigInteger;
import java.util.Date;

public final class PriceInfoFragment extends Fragment {

  private CurrencyTextView viewPriceInfoMin;
  private CurrencyTextView viewPriceInfoCurrent;
  private CurrencyTextView viewPriceInfoMax;
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
    Resources resources = getResources();
    int textColor = resources.getColor(R.color.fg_significant);
    viewPriceInfoMin = (CurrencyTextView) view.findViewById(R.id.price_info_min);
    viewPriceInfoMin.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoMin.setPrecision(2);
    viewPriceInfoMin.setTextColor(textColor);
    viewPriceInfoCurrent = (CurrencyTextView)view.findViewById(R.id.price_info_current);
    viewPriceInfoCurrent.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoCurrent.setPrecision(2);
    viewPriceInfoCurrent.setTextColor(textColor);
    viewPriceInfoMax = (CurrencyTextView)view.findViewById(R.id.price_info_max);
    viewPriceInfoMax.setPrefix(Constants.CURRENCY_CODE_DOLLAR);
    viewPriceInfoMax.setPrecision(2);
    viewPriceInfoMax.setTextColor(textColor);
    viewPriceInfoVolume = (TextView)view.findViewById(R.id.price_info_volume);
    viewPriceInfoVolume.setTextColor(textColor);
    viewPriceInfoLastUpdate = (TextView)view.findViewById(R.id.price_info_lastupdate);
    viewPriceInfoLastUpdate.setTextColor(textColor);
  }

  private void updateView() {
    viewPriceInfoMin.setAmount(BigInteger.valueOf(12300000000L));
    viewPriceInfoCurrent.setAmount(BigInteger.valueOf(13500000000L));
    viewPriceInfoMax.setAmount(BigInteger.valueOf(14000000000L));
    viewPriceInfoVolume.setText("12345");
    viewPriceInfoLastUpdate.setText(new Date().toLocaleString());
  }
}
