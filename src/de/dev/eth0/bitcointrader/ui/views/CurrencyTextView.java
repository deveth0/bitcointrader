package de.dev.eth0.bitcointrader.ui.views;


import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.Constants;
import de.schildbach.wallet.util.GenericUtils;
import java.math.BigInteger;

public class CurrencyTextView extends TextView {

  private String prefix = null;
  private BigInteger amount = null;
  private int precision = Constants.PRECISION_BITCOIN;


  public CurrencyTextView(Context context) {
    super(context);
  }

  public CurrencyTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix + Constants.CHAR_HAIR_SPACE;
    updateView();
  }

  public void setAmount(BigInteger amount) {
    this.amount = amount;
    updateView();
  }

  private void updateView() {
    Editable text = null;

    if (amount != null) {
      text = new SpannableStringBuilder(GenericUtils.formatValue(amount, precision));
      if (prefix != null) {
        text.insert(0, prefix);
      }
    }
    setText(text);
  }
}
