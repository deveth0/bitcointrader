package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.ui.AboutActivity;
import de.dev.eth0.bitcointrader.ui.PreferencesActivity;

public final class BitcoinTraderActionsFragment extends Fragment {

  private Activity activity;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);

    this.activity = activity;
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.bitcointrader_actions_fragment, container);

    final Button requestButton = (Button)view.findViewById(R.id.bitcointrader_actions_request);
    requestButton.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        startActivity(new Intent(activity, AboutActivity.class));
      }
    });

    final Button sendButton = (Button)view.findViewById(R.id.bitcointrader_actions_send);
    sendButton.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        startActivity(new Intent(activity, PreferencesActivity.class));
      }
    });

    return view;
  }

}
