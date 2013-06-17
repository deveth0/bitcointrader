package de.dev.eth0.bitcointrader.ui.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;
import de.schildbach.wallet.util.GenericUtils;

public final class CurrencyAmountView extends FrameLayout {

  private TextView textView;
  private int precision;

  public CurrencyAmountView(final Context context) {
    super(context);
  }

  public CurrencyAmountView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    final Context context = getContext();

    textView = (TextView)getChildAt(0);
    textView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    textView.setHorizontalFadingEdgeEnabled(true);

    setCurrencyCode(Constants.CURRENCY_CODE_BITCOIN);

    updateAppearance();
  }

  public void setCurrencyCode(final String currencyCode) {
    if (currencyCode != null) {
    }

    updateAppearance();
  }

  public void setPrecision(final int precision) {
    this.precision = precision;
  }

  public Long getAmount() {
    return Long.valueOf(textView.getText().toString());
  }

  public void setAmount(final Long amount) {
    if (amount != null) {
      textView.setText(GenericUtils.formatValue(amount, precision));
    }
    else {
      textView.setText(null);
    }
  }

  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);

    textView.setEnabled(enabled);

    updateAppearance();
  }

  private boolean isValidAmount() {
    return true;
  }

  private void updateAppearance() {
    textView.setTextColor(getResources().getColor(R.color.fg_significant));
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    final Bundle state = new Bundle();
    state.putParcelable("super_state", super.onSaveInstanceState());
    state.putParcelable("child_textview", textView.onSaveInstanceState());
    state.putSerializable("amount", getAmount());
    return state;
  }

  @Override
  protected void onRestoreInstanceState(final Parcelable state) {
    if (state instanceof Bundle) {
      final Bundle bundle = (Bundle)state;
      super.onRestoreInstanceState(bundle.getParcelable("super_state"));
      textView.onRestoreInstanceState(bundle.getParcelable("child_textview"));
      setAmount((Long)bundle.getSerializable("amount"));
    }
    else {
      super.onRestoreInstanceState(state);
    }
  }
}
