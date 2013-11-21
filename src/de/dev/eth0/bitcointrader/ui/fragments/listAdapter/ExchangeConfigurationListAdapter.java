//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments.listAdapter;

import android.view.View;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.ExchangeConfiguration;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;

/**
 * @author Alexander Muthmann
 */
public class ExchangeConfigurationListAdapter extends AbstractListAdapter<ExchangeConfiguration> {


  public ExchangeConfigurationListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
  }

  @Override
  public int getRowLayout() {
    return R.layout.exchange_configuration_row;
  }

  public void bindView(View row, ExchangeConfiguration entry) {
    // type (out, fee, earned)
    TextView rowType = (TextView) row.findViewById(R.id.exchange_configuration_row_type);
    rowType.setText(entry.getConnectionSettings().toString());
  }

}
