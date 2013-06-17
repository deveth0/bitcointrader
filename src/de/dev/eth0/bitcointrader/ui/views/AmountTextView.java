package de.dev.eth0.bitcointrader.ui.views;


import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import java.math.BigDecimal;

public class AmountTextView extends TextView {

  private BigDecimal amount = null;


  public AmountTextView(Context context) {
    super(context);
  }

  public AmountTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
    updateView();
  }

  private void updateView() {
    if (amount != null) {
      setText(new SpannableStringBuilder(amount.toString()));
    }
  }
}
