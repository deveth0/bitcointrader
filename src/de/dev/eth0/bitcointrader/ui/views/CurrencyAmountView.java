package de.dev.eth0.bitcointrader.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import de.dev.eth0.R;
import de.dev.eth0.bitcointrader.Constants;
import java.math.BigDecimal;

public final class CurrencyAmountView extends FrameLayout {

  private TextView textView;
  private View contextButton;
  private int significantColor, lessSignificantColor, errorColor;
  private Drawable deleteButtonDrawable, contextButtonDrawable;
  private CurrencyCodeDrawable currencyCodeDrawable;
  private OnClickListener contextButtonClickListener;

  public CurrencyAmountView(final Context context) {
    super(context);
    init(context);
  }

  public CurrencyAmountView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(final Context context) {
    final Resources resources = context.getResources();
    significantColor = resources.getColor(R.color.fg_significant);
    lessSignificantColor = resources.getColor(R.color.fg_less_significant);
    errorColor = resources.getColor(R.color.fg_error);
    deleteButtonDrawable = resources.getDrawable(R.drawable.ic_input_delete);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    final Context context = getContext();
    textView = (TextView) getChildAt(0);
    textView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    textView.setHorizontalFadingEdgeEnabled(true);
    textView.addTextChangedListener(new TextWatcher() {
      public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        updateAppearance();
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void afterTextChanged(Editable s) {
      }
    });
    contextButton = new View(context) {
      @Override
      protected void onMeasure(final int wMeasureSpec, final int hMeasureSpec) {
        setMeasuredDimension(textView.getCompoundPaddingRight(), textView.getMeasuredHeight());
      }
    };
    final LayoutParams chooseViewParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    chooseViewParams.gravity = Gravity.RIGHT;
    contextButton.setLayoutParams(chooseViewParams);
    this.addView(contextButton);
    setCurrencyCode(Constants.CURRENCY_CODE_BITCOIN);

    updateAppearance();
  }

  public void setCurrencyCode(final String currencyCode) {
    if (currencyCode != null) {
      final float textSize = textView.getTextSize();
      currencyCodeDrawable = new CurrencyCodeDrawable(currencyCode, textSize, lessSignificantColor, textSize * 0.37f);
    } else {
      currencyCodeDrawable = null;
    }
    updateAppearance();
  }

  public BigDecimal getAmount() {
    return new BigDecimal(textView.getText().toString());
  }

  public void setAmount(BigDecimal amount) {
    if (amount != null) {
      textView.setText(amount.toString());
    } else {
      textView.setText(null);
    }
  }

  @Override
  public void setEnabled(final boolean enabled) {
    super.setEnabled(enabled);
    textView.setEnabled(enabled);
    updateAppearance();
  }

  public void setContextButton(final int contextButtonResId, final OnClickListener contextButtonClickListener) {
    this.contextButtonDrawable = getContext().getResources().getDrawable(contextButtonResId);
    this.contextButtonClickListener = contextButtonClickListener;

    updateAppearance();
  }

  private boolean isValidAmount() {
    String amount = textView.getText().toString().trim();
    try {
      if (!amount.isEmpty()) {
        new BigDecimal(amount);
        return true;
      }
    } catch (NumberFormatException x) {
    }
    return false;
  }
  private final OnClickListener deleteClickListener = new OnClickListener() {
    public void onClick(final View v) {
      setAmount(null);
      textView.requestFocus();
    }
  };

  private void updateAppearance() {
    final boolean enabled = textView.isEnabled();

    contextButton.setEnabled(enabled);

    final String amount = textView.getText().toString().trim();

    if (enabled && !amount.isEmpty()) {
      textView.setCompoundDrawablesWithIntrinsicBounds(currencyCodeDrawable, null, deleteButtonDrawable, null);
      contextButton.setOnClickListener(deleteClickListener);
    } else if (enabled && contextButtonDrawable != null) {
      textView.setCompoundDrawablesWithIntrinsicBounds(currencyCodeDrawable, null, contextButtonDrawable, null);
      contextButton.setOnClickListener(contextButtonClickListener);
    } else {
      textView.setCompoundDrawablesWithIntrinsicBounds(currencyCodeDrawable, null, null, null);
      contextButton.setOnClickListener(null);
    }
    contextButton.requestLayout();
    textView.setTextColor(isValidAmount() ? significantColor : errorColor);
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
      final Bundle bundle = (Bundle) state;
      super.onRestoreInstanceState(bundle.getParcelable("super_state"));
      textView.onRestoreInstanceState(bundle.getParcelable("child_textview"));
      setAmount((BigDecimal) bundle.getSerializable("amount"));
    } else {
      super.onRestoreInstanceState(state);
    }
  }
}
