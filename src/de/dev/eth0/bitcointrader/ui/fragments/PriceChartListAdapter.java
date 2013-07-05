//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.xeiam.xchange.bitcoincharts.dto.marketdata.BitcoinChartsTicker;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.AmountTextView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import org.joda.money.BigMoney;

/**
 *
 * @author deveth0
 */
public class PriceChartListAdapter extends AbstractListAdapter<BitcoinChartsTicker> {
  
  private final static String TAG = PriceChartListAdapter.class.getSimpleName();

  public PriceChartListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
  }

  @Override
  public int getRowLayout() {
    return R.layout.price_chart_row_extended;
  }

  @Override
  public void bindView(View row, BitcoinChartsTicker entry) {
    TextView symbolView = (TextView) row.findViewById(R.id.chart_row_symbol);
    CurrencyTextView lastView = (CurrencyTextView) row.findViewById(R.id.chart_row_last);
    AmountTextView volView = (AmountTextView) row.findViewById(R.id.chart_row_vol);
    CurrencyTextView lowView = (CurrencyTextView) row.findViewById(R.id.chart_row_low);
    CurrencyTextView highView = (CurrencyTextView) row.findViewById(R.id.chart_row_high);

    symbolView.setText(entry.getSymbol());
    
    volView.setAmount(entry.getVolume());
    volView.setPrecision(2);
    
    lastView.setAmount(BigMoney.parse("BTC " + entry.getClose()));
    lastView.setDisplayMode(CurrencyTextView.DISPLAY_MODE.NO_CURRENCY_CODE);
    lastView.setPrecision(2);
   
    lowView.setAmount(BigMoney.parse("BTC " + entry.getLow()));
    lowView.setDisplayMode(CurrencyTextView.DISPLAY_MODE.NO_CURRENCY_CODE);
    lowView.setPrecision(2);
    highView.setAmount(BigMoney.parse("BTC " + entry.getHigh()));
    highView.setDisplayMode(CurrencyTextView.DISPLAY_MODE.NO_CURRENCY_CODE);
    highView.setPrecision(2);
  }
}
