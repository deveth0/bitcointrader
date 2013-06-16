//$URL$
//$Id$
package de.dev.eth0.bitcointrader.model;

import java.math.BigInteger;

public class Order {

  public enum OrderType {

    BID, ASK
  }
  private BigInteger price;
  private long time;
  private BigInteger amount;

  public Order(BigInteger price, BigInteger amount, long time) {
    this.price = price;
    this.time = time;
    this.amount = amount;
  }

  public BigInteger getPrice() {
    return price;
  }

  public long getTime() {
    return time;
  }

  public BigInteger getAmount() {
    return amount;
  }

  /**
   * Returns the actual value of this order (price * amount)
   *
   * @return
   */
  public BigInteger getValue() {
    return amount.multiply(price);
  }

  @Override
  public String toString() {
    return "Order{" + "price=" + price + ", time=" + time + ", amount=" + amount + ", value=" + getValue() + "}";
  }
}
