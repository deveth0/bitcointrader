//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.mtgox.v2.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v2.dto.account.polling.MtGoxAccountInfo;
import de.dev.eth0.bitcointrader.R;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.Constants;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import de.dev.eth0.bitcointrader.ui.PlaceOrderActivity;
import de.dev.eth0.bitcointrader.ui.views.CurrencyAmountView;
import de.dev.eth0.bitcointrader.ui.views.CurrencyTextView;
import java.math.BigDecimal;
import org.joda.money.BigMoney;
import org.joda.money.CurrencyUnit;

public final class PlaceOrderFragment extends AbstractBitcoinTraderFragment {
  private static final String TAG = PlaceOrderFragment.class.getSimpleName();
  private AbstractBitcoinTraderActivity activity;
  private BitcoinTraderApplication application;
  private Spinner orderTypeSpinner;
  private CurrencyAmountView amountView;
  private EditText amountViewText;
  private CheckBox marketOrderCheckbox;
  private CurrencyAmountView priceView;
  private EditText priceViewText;
  private CurrencyTextView totalView;
  private CurrencyTextView estimatedFeeView;
  private Button viewGo;
  private Button viewCancel;

  @Override
  public void onAttach(final Activity activity) {
    super.onAttach(activity);

    this.activity = (AbstractBitcoinTraderActivity) activity;
    application = (BitcoinTraderApplication) activity.getApplication();
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.place_order_fragment, container);
    totalView = (CurrencyTextView) view.findViewById(R.id.place_order_total);
    estimatedFeeView = (CurrencyTextView) view.findViewById(R.id.place_order_estimatedfee);
    amountView = (CurrencyAmountView) view.findViewById(R.id.place_order_amount);
    amountView.setCurrencyCode(Constants.CURRENCY_CODE_BITCOIN);
    amountViewText = (EditText) view.findViewById(R.id.place_order_amount_text);
    amountViewText.addTextChangedListener(valueChangedListener);

    priceView = (CurrencyAmountView) view.findViewById(R.id.place_order_price);
    priceView.setCurrencyCode(getExchangeService().getCurrency());
    enablePriceViewContextButton();

    orderTypeSpinner = (Spinner) view.findViewById(R.id.place_order_type);
    ArrayAdapter<Order.OrderType> adapter = new ArrayAdapter<Order.OrderType>(activity,
            R.layout.spinner_item, Order.OrderType.values());
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    orderTypeSpinner.setAdapter(adapter);
    orderTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // if ask has been selected, we need a context button on amount to select all
        if (parent.getItemAtPosition(position).equals(Order.OrderType.ASK)) {
          enableAmountViewContextButton();
        } // remove context button from amount if buying bitcoins
        else if (parent.getItemAtPosition(position).equals(Order.OrderType.BID)) {
          amountView.removeContextButton();
        }
        updateView();
      }

      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    priceViewText = (EditText) view.findViewById(R.id.place_order_price_text);
    priceViewText.addTextChangedListener(valueChangedListener);

    marketOrderCheckbox = (CheckBox) view.findViewById(R.id.place_order_marketorder);
    marketOrderCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Ticker ticker = getExchangeService().getTicker();
        if (ticker != null) {
          priceView.setEnabled(!isChecked);
          Order.OrderType type = (Order.OrderType) orderTypeSpinner.getSelectedItem();
          priceViewText.setText(type.equals(Order.OrderType.ASK)
                  ? ticker.getAsk().getAmount().toString()
                  : ticker.getBid().getAmount().toString());
          priceViewText.clearFocus();
          updateView();
        }
      }
    });
    viewGo = (Button) view.findViewById(R.id.place_order_perform);
    viewGo.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        if (everythingValid()) {
          handleGo();
        } else {
          Toast.makeText(activity, R.string.place_order_invalid, Toast.LENGTH_LONG).show();
        }
      }
    });

    viewCancel = (Button) view.findViewById(R.id.place_order_cancel);
    viewCancel.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        // only finish activity if the order has been created in a PlaceOrderActivity
        if (activity instanceof PlaceOrderActivity) {
          activity.setResult(Activity.RESULT_CANCELED);
          activity.finish();
        }
        else {
          resetValues();
        }
      }
    });
    return view;
  }

  public void updateView() {
    Log.d(TAG, ".updateView()");
    Editable amount = amountViewText.getEditableText();
    Editable price = priceViewText.getEditableText();
    Order.OrderType type = (Order.OrderType) orderTypeSpinner.getSelectedItem();
    if (!TextUtils.isEmpty(amount) && !TextUtils.isEmpty(price)) {
      BigMoney amountBTC = BigMoney.parse("BTC " + amount.toString());
      BigMoney amountUSD = BigMoney.parse(getExchangeService().getCurrency() + " " + price.toString());
      BigMoney totalSpend = amountUSD.multipliedBy(amountBTC.getAmount());
      totalView.setAmount(totalSpend);
      MtGoxAccountInfo accountInfo = getExchangeService().getAccountInfo();
      if (accountInfo != null) {
        if (type.equals(Order.OrderType.ASK)) {
          estimatedFeeView.setPrecision(Constants.PRECISION_DOLLAR);
          estimatedFeeView.setAmount(totalSpend.multipliedBy(accountInfo.getTradeFee().scaleByPowerOfTen(-2)));
        } else {
          BigMoney fee = amountBTC.multipliedBy(accountInfo.getTradeFee().scaleByPowerOfTen(-2));
          estimatedFeeView.setPrecision(Constants.PRECISION_BITCOIN);
          estimatedFeeView.setAmount(fee);
        }
      }
    }
  }

  private void enableAmountViewContextButton() {
    amountView.setContextButton(R.drawable.ic_input_calculator, new OnClickListener() {
      public void onClick(final View v) {
        MtGoxAccountInfo mtgoxaccountInfo = getExchangeService().getAccountInfo();
        if (mtgoxaccountInfo != null) {
          AccountInfo accountInfo = MtGoxAdapters.adaptAccountInfo(mtgoxaccountInfo);
          if (accountInfo != null) {
            amountView.setAmount(accountInfo.getBalance(CurrencyUnit.of("BTC")).getAmount());
          }
        }
      }
    });
  }

  private void enablePriceViewContextButton() {
    priceView.setContextButton(R.drawable.ic_input_calculator, new OnClickListener() {
      public void onClick(final View v) {
        Ticker ticker = getExchangeService().getTicker();
        if (ticker != null && ticker.getLast().getAmount() != BigDecimal.ZERO) {
          priceView.setAmount(ticker.getLast().getAmount());
        }
      }
    });
  }

  private void handleGo() {
    Order order;
    Order.OrderType type = (Order.OrderType) orderTypeSpinner.getSelectedItem();
    boolean marketOrder = marketOrderCheckbox.isChecked();
    Double amount = Double.parseDouble(amountViewText.getEditableText().toString());
    Double price = Double.parseDouble(priceViewText.getEditableText().toString());

    if (marketOrder) {
      order = new MarketOrder(type, BigDecimal.valueOf(amount), "BTC", getExchangeService().getCurrency());
    } else {
      order = new LimitOrder(type, BigDecimal.valueOf(amount), "BTC", getExchangeService().getCurrency(), BigMoney.of(CurrencyUnit.of(getExchangeService().getCurrency()), price));
    }
    getExchangeService().placeOrder(order, activity);

  }

  public void resetValues() {
    amountViewText.setText(null);
    priceViewText.setText(null);
    marketOrderCheckbox.setChecked(false);

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
        switch (ordertype) {
          case ASK:
            enablePriceViewContextButton();
            return;
          case BID:
            amountView.removeContextButton();
            return;
        }
        return;
      }
    }
  }

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
