//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments.listAdapter;

import android.view.View;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.AddExchangeConfigurationEntry;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;

/**
 * @author Alexander Muthmann
 */
public class AddExchangeConfigurationListAdapter extends AbstractListAdapter<AddExchangeConfigurationEntry> {


  public AddExchangeConfigurationListAdapter(AbstractBitcoinTraderActivity activity) {
    super(activity);
  }

  @Override
  public int getRowLayout() {
    return R.layout.add_exchange_configuration_row;
  }

  public void bindView(View row, AddExchangeConfigurationEntry entry) {
    TextView rowType = (TextView) row.findViewById(R.id.add_exchange_configuration_row_name);
    rowType.setText(entry.getName());
  }

}
