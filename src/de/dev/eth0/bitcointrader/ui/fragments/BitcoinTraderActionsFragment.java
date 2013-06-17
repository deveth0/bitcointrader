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
import com.xeiam.xchange.dto.Order;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.ui.PlaceOrderActivity;

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

    final Button buyButton = (Button)view.findViewById(R.id.bitcointrader_actions_buy);
    buyButton.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        Intent i = new Intent(activity, PlaceOrderActivity.class);
        i.putExtra(PlaceOrderActivity.INTENT_EXTRA_TYPE, Order.OrderType.BID.name());
        startActivity(i);
      }
    });

    final Button sellButton = (Button)view.findViewById(R.id.bitcointrader_actions_sell);
    sellButton.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        Intent i = new Intent(activity, PlaceOrderActivity.class);
        i.putExtra(PlaceOrderActivity.INTENT_EXTRA_TYPE, Order.OrderType.ASK.name());
        startActivity(i);
      }
    });

    return view;
  }

}
