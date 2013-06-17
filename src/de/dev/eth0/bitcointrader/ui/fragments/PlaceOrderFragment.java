package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.service.ExchangeService;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyAmountView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.math.BigDecimal;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

public final class PlaceOrderFragment extends SherlockFragment {

  private AbstractBitcoinTraderActivity activity;
  private BitcoinTraderApplication application;
  private Spinner orderTypeSpinner;
  private CurrencyAmountView amountView;
  private EditText amountViewText;
  private CheckBox marketOrderCheckbox;
  private CurrencyAmountView priceView;
  private EditText priceViewText;
  private CurrencyTextView totalView;
  private Button viewGo;
  private Button viewCancel;
  private ExchangeService exchangeService;
  private final ServiceConnection serviceConnection = new ServiceConnection() {
    public void onServiceConnected(final ComponentName name, final IBinder binder) {
      exchangeService = ((ExchangeService.LocalBinder) binder).getService();
    }

    public void onServiceDisconnected(final ComponentName name) {
      exchangeService = null;
    }
  };

  @Override
  public void onDestroy() {
    activity.bindService(new Intent(activity, ExchangeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    super.onDestroy();
  }

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);

    this.activity = (AbstractBitcoinTraderActivity) activity;
    application = (BitcoinTraderApplication) activity.getApplication();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activity.bindService(new Intent(activity, ExchangeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    setHasOptionsMenu(true);

//    if (savedInstanceState != null) {
//      state = (State)savedInstanceState.getSerializable("state");
//
//      if (savedInstanceState.containsKey("validated_address_bytes")) {
//        validatedAddress = new Address((NetworkParameters)savedInstanceState.getSerializable("validated_address_params"),
//                savedInstanceState.getByteArray("validated_address_bytes"));
//      }
//      else {
//        validatedAddress = null;
//      }
//
//      receivingLabel = savedInstanceState.getString("receiving_label");
//
//      isValidAmounts = savedInstanceState.getBoolean("is_valid_amounts");
//
//      if (savedInstanceState.containsKey("sent_transaction_hash")) {
//        sentTransaction = wallet.getTransaction((Sha256Hash)savedInstanceState.getSerializable("sent_transaction_hash"));
//        sentTransaction.getConfidence().addEventListener(sentTransactionConfidenceListener);
//      }
//    }
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.place_order_fragment, container);
    orderTypeSpinner = (Spinner) view.findViewById(R.id.place_order_type);
    ArrayAdapter<Order.OrderType> adapter = new ArrayAdapter<Order.OrderType>(activity,
            android.R.layout.simple_spinner_item, Order.OrderType.values());
    orderTypeSpinner.setAdapter(adapter);
    amountView = (CurrencyAmountView) view.findViewById(R.id.place_order_amount);
    amountView.setCurrencyCode(Constants.CURRENCY_CODE_BITCOIN);
    amountViewText = (EditText) view.findViewById(R.id.place_order_amount_text);
    amountViewText.addTextChangedListener(valueChangedListener);


    priceView = (CurrencyAmountView) view.findViewById(R.id.place_order_price);
    priceView.setCurrencyCode(Constants.CURRENCY_CODE_DOLLAR);
    priceViewText = (EditText) view.findViewById(R.id.place_order_price_text);
    priceViewText.addTextChangedListener(valueChangedListener);

    marketOrderCheckbox = (CheckBox) view.findViewById(R.id.place_order_marketorder);
    marketOrderCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        priceView.setEnabled(!isChecked);
        //@TODO: set value
        priceViewText.setText("current value");
        updateView();
      }
    });

    totalView = (CurrencyTextView) view.findViewById(R.id.place_order_total);

    viewGo = (Button) view.findViewById(R.id.place_order_perform);
    viewGo.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {

        if (everythingValid()) {
          handleGo();
        } else {
          Toast.makeText(activity, "something wrong..", Toast.LENGTH_SHORT).show();
        }
      }
    });

    viewCancel = (Button) view.findViewById(R.id.place_order_cancel);
    viewCancel.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        activity.setResult(Activity.RESULT_CANCELED);
        activity.finish();
      }
    });
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  protected void updateView() {
    Editable amount = amountViewText.getEditableText();
    Editable price = priceViewText.getEditableText();
    if (!TextUtils.isEmpty(amount) && !TextUtils.isEmpty(price)) {

      Double amountInt = new Double(amount.toString());
      Double priceInt = new Double(price.toString());
      //@TODO: fix multiply
      totalView.setText(((Double) (amountInt * priceInt)).toString());
    }
  }

  private void handleGo() {
    PlaceOrderTask task = new PlaceOrderTask();
    task.execute(activity);
    //@TODO: Give feedback..
  }

  private boolean everythingValid() {
    Editable amount = amountViewText.getEditableText();
    Editable price = priceViewText.getEditableText();
    boolean marketOrder = marketOrderCheckbox.isChecked();
    if (!TextUtils.isEmpty(amount) && (!TextUtils.isEmpty(price) || marketOrder)) {
      return true;
    }
    return false;
  }

  public void update(Order.OrderType ordertype) {
    for (int i = 0; i < orderTypeSpinner.getCount(); i++) {
      if (orderTypeSpinner.getItemAtPosition(i).equals(ordertype)) {
        orderTypeSpinner.setSelection(i);
        return;
      }
    }
  }

  private class PlaceOrderTask extends AsyncTask<Activity, Void, Void> {

    private ProgressDialog mDialog;

    @Override
    protected void onPreExecute() {
      mDialog = new ProgressDialog(activity);
      mDialog.setMessage(getString(R.string.place_order_submitting));
      mDialog.show();
    }

    @Override
    protected void onPostExecute(Void result) {
      mDialog.dismiss();
    }

    @Override
    protected Void doInBackground(Activity... params) {
      boolean marketOrder = marketOrderCheckbox.isChecked();
      Double amount = Double.parseDouble(amountViewText.getEditableText().toString());
      Double price = Double.parseDouble(priceViewText.getEditableText().toString());
      if (marketOrder) {
        MarketOrder order = new MarketOrder(Order.OrderType.BID, BigDecimal.valueOf(amount), "BTC", "USD");
        exchangeService.placeMarketOrder(order);
      } else {
        LimitOrder order = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(amount), "BTC", "USD", BigMoney.of(CurrencyUnit.USD, price));
        exchangeService.placeLimitOrder(order);
      }
      activity.setResult(Activity.RESULT_OK);
      activity.finish();
      return null;
    }
  };

  private final class ValueChangedListener implements TextWatcher {

    @Override
    public void afterTextChanged(final Editable s) {
      updateView();
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
    }
  }
  private final ValueChangedListener valueChangedListener = new ValueChangedListener();
}
