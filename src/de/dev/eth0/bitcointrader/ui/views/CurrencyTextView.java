//$URL: $
//$Id: $
package de.dev.eth0.bitcointrader.ui.views;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import de.dev.eth0.bitcointrader.Constants;
import java.math.RoundingMode;
import org.joda.money.BigMoney;
import org.joda.money.format.MoneyAmountStyle;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;

public class CurrencyTextView extends TextView {

  public enum DISPLAY_MODE {

    NO_CURRENCY_CODE(new MoneyFormatterBuilder().
    appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter()),
    CURRENCY_CODE(new MoneyFormatterBuilder().appendCurrencyCode().appendLiteral(" ")
    .appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter()),
    CURRENCY_SYMBOL(new MoneyFormatterBuilder().appendCurrencySymbolLocalized().appendLiteral(" ")
    .appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_NO_GROUPING).toFormatter());
    private MoneyFormatter formater;

    private DISPLAY_MODE(MoneyFormatter formater) {
      this.formater = formater;
    }

    public MoneyFormatter getFormater() {
      return formater;
    }
  }
  private BigMoney amount = null;
  private String prefix = null;
  private Integer precision = null;
  private DISPLAY_MODE displayMode = DISPLAY_MODE.CURRENCY_CODE;

  public CurrencyTextView(Context context) {
    super(context);
  }

  public CurrencyTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setDisplayMode(DISPLAY_MODE displayMode) {
    this.displayMode = displayMode;
    updateView();
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix + Constants.CHAR_HAIR_SPACE;
    updateView();
  }

  public void setPrecision(int precision) {
    this.precision = precision;
    updateView();
  }

  public void setAmount(BigMoney amount) {
    this.amount = amount;
    updateView();
  }

  private void updateView() {
    if (amount != null) {
      Editable text;
      if (precision != null) {
        text = new SpannableStringBuilder(
                displayMode.getFormater().print(amount.withScale(precision, RoundingMode.HALF_EVEN)));
      } else {
        text = new SpannableStringBuilder(
                displayMode.getFormater().print(amount.withCurrencyScale(RoundingMode.HALF_EVEN)));
      }
      if (prefix != null) {
        text.insert(0, prefix);
      }
      setText(text);
    }
  }
}
