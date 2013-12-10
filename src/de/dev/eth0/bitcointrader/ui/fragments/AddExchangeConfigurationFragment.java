//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import static android.app.Activity.RESULT_OK;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.data.AddExchangeConfigurationEntry;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.fragments.listAdapter.AddExchangeConfigurationListAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Muthmann
 */
public class AddExchangeConfigurationFragment extends SherlockListFragment {

  private static final String TAG = AddExchangeConfigurationFragment.class.getSimpleName();
  private static final int REQUESTCODE = 1337;
  private BitcoinTraderApplication application;
  private AbstractBitcoinTraderActivity activity;
  private AddExchangeConfigurationListAdapter adapter;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
    View layout = super.onCreateView(inflater, container, savedInstanceState);
    ListView lv = (ListView) layout.findViewById(android.R.id.list);
    ViewGroup parent = (ViewGroup) lv.getParent();

    // Remove ListView and add CustomView  in its place
    int lvIndex = parent.indexOfChild(lv);
    parent.removeViewAt(lvIndex);
    View view = inflater.inflate(R.layout.add_exchange_configuration_fragment, container, false);
    parent.addView(view, lvIndex, lv.getLayoutParams());
    return layout;
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);
    this.activity = (AbstractBitcoinTraderActivity) activity;
    this.application = (BitcoinTraderApplication) activity.getApplication();
  }

  @Override
  public void onResume() {
    super.onResume();
    updateView();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    adapter = new AddExchangeConfigurationListAdapter(activity);
    setListAdapter(adapter);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    registerForContextMenu(getListView());
    int text = R.string.exchange_configuration_empty_text;
    SpannableStringBuilder emptyText = new SpannableStringBuilder(getString(text));
    emptyText.setSpan(new StyleSpan(Typeface.BOLD), 0, emptyText.length(), SpannableStringBuilder.SPAN_POINT_MARK);
    setEmptyText(emptyText);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    AddExchangeConfigurationEntry entry = adapter.getItem(position);
    if (entry != null) {
      startActivityForResult(new Intent(activity, entry.getSetupActivity()), REQUESTCODE);
    }
    super.onListItemClick(l, v, position, id);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUESTCODE:
        if (resultCode == RESULT_OK) {
          activity.finish();
          return;
        }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  protected void updateView() {
    Log.d(TAG, ".updateView");
    List<AddExchangeConfigurationEntry> entries = new ArrayList<AddExchangeConfigurationEntry>();
    entries.add(AddExchangeConfigurationEntry.MTGOX);
    entries.add(AddExchangeConfigurationEntry.BITSTAMP);
    entries.add(AddExchangeConfigurationEntry.BTCN);
    adapter.replace(entries);
  }
}
