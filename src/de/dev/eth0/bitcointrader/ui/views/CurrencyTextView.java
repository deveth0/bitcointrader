package de.dev.eth0.bitcointrader.ui.views;

import java.math.BigInteger;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.Constants;

public class CurrencyTextView extends TextView {

  private String prefix = null;
  private Double amount = null;

  public CurrencyTextView(Context context) {
    super(context);
  }

  public CurrencyTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix + Constants.CHAR_HAIR_SPACE;
    updateView();
  }

  public void setAmount(Double amount) {
    this.amount = amount;
    updateView();
  }

  private void updateView() {
    final Editable text;

    if (amount != null) {
      text = new SpannableStringBuilder(amount.toString());

      if (prefix != null) {
        text.insert(0, prefix);
      }
    } else {
      text = null;
    }
    setText(text);
  }
}
