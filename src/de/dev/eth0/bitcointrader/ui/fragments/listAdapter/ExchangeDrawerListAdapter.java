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
public class ExchangeDrawerListAdapter extends AbstractListAdapter<ExchangeConfiguration> {

  private final AbstractBitcoinTraderActivity bitcoinTraderActivity;

  public ExchangeDrawerListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
    this.bitcoinTraderActivity = activity;
  }

  @Override
  public int getRowLayout() {
    return R.layout.exchange_configuration_row;
  }

  public void bindView(View row, ExchangeConfiguration entry) {
    TextView rowType = (TextView)row.findViewById(R.id.exchange_configuration_row_type);
    TextView rowName = (TextView)row.findViewById(R.id.exchange_configuration_row_name);
    rowType.setText(entry.getConnectionSettings().getDisplayName());
    rowName.setText(entry.getName());
    if (bitcoinTraderActivity.getBitcoinTraderApplication().getExchangeService() != null) {
      if (entry.equals(bitcoinTraderActivity.getBitcoinTraderApplication().getExchangeService().getExchangeConfig())) {
        rowName.append(" " + bitcoinTraderActivity.getString(R.string.exchange_configuration_active));
      }
    }
  }

}
