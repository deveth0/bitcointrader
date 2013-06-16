package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.model.Order;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyAmountView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.math.BigInteger;
import java.util.Random;

public final class PlaceOrderFragment extends SherlockFragment {

  private AbstractBitcoinTraderActivity activity;
  private BitcoinTraderApplication application;
  private ContentResolver contentResolver;
  private CurrencyAmountView amountView;
  private EditText amountViewText;
  private CurrencyAmountView priceView;
  private EditText priceViewText;
  private CurrencyTextView totalView;
  private Button viewGo;
  private Button viewCancel;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);

    this.activity = (AbstractBitcoinTraderActivity)activity;
    application = (BitcoinTraderApplication)activity.getApplication();
    contentResolver = activity.getContentResolver();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

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


    amountView = (CurrencyAmountView)view.findViewById(R.id.place_order_amount);
    amountView.setCurrencyCode(Constants.CURRENCY_CODE_BITCOIN);
    amountViewText = (EditText)view.findViewById(R.id.place_order_amount_text);
    amountViewText.addTextChangedListener(valueChangedListener);

    priceView = (CurrencyAmountView)view.findViewById(R.id.place_order_price);
    priceView.setCurrencyCode(Constants.CURRENCY_CODE_DOLLAR);
    priceViewText = (EditText)view.findViewById(R.id.place_order_price_text);
    priceViewText.addTextChangedListener(valueChangedListener);

    totalView = (CurrencyTextView)view.findViewById(R.id.place_order_total);

    viewGo = (Button)view.findViewById(R.id.place_order_perform);
    viewGo.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {

        if (everythingValid()) {
          handleGo();
        }
        else {
          Toast.makeText(activity, "something wrong..", Toast.LENGTH_SHORT).show();
        }
      }
    });

    viewCancel = (Button)view.findViewById(R.id.place_order_cancel);
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
    if (amount != null && price != null) {
      BigInteger amountInt = new BigInteger(amount.toString());
      BigInteger priceInt = new BigInteger(price.toString());
      totalView.setText(amountInt.multiply(priceInt).toString());
    }
  }

  private void handleGo() {
    Toast.makeText(activity, "handleGo", Toast.LENGTH_SHORT).show();
  }

  private boolean everythingValid() {
    return new Random().nextBoolean();
  }

  public void update(Order.OrderType ordertype) {
  }

  private final class ValueChangedListener implements OnFocusChangeListener, TextWatcher {

    public void onFocusChange(final View v, final boolean hasFocus) {
      //if (!hasFocus)
      //validateReceivingAddress(true);
    }

    public void afterTextChanged(final Editable s) {
      updateView();
    }

    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
    }

    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
    }
  }
  private final ValueChangedListener valueChangedListener = new ValueChangedListener();
}
