package de.dev.eth0.bitcointrader;

public class Constants {

  // General Settings
  public static final char CHAR_HAIR_SPACE = '\u200a';
  public static final char CHAR_THIN_SPACE = '\u2009';
  public static final String CURRENCY_CODE_BITCOIN = "BTC";
  public static final String CURRENCY_CODE_DOLLAR = "USD";
  public static final int PRECISION_BITCOIN = 5;
  public static final int PRECISION_DOLLAR = 5;
  // Prefs
  public static final String DONATION_ADDRESS = "1KjAux47WJUTfwpeTduNkBtbcdKGhN7yVj";
  public static final String REPORT_EMAIL = "bitcointrader@dev-eth0.de";
  public static final String REPORT_SUBJECT_ISSUE = "Reported issue";
  public static final String REPORT_SUBJECT_CRASH = "Crash report";
  public static final String AUTHOR_URL = "http://www.dev-eth0.de";
  public static final String AUTHOR_TWITTER_URL = "https://twitter.com/#!/deveth0";
  public static final String CREDITS_BITCOINWALLET_URL = "http://code.google.com/p/bitcoin-wallet/";
  public static final String CREDITS_XCHANGE_URL = "https://github.com/timmolter/XChange";
  public static final String PREFS_KEY_MTGOX_APIKEY = "mtgox_apikey";
  public static final String PREFS_KEY_MTGOX_SECRETKEY = "mtgox_secretkey";
  public static final String PREFS_KEY_GENERAL_UPDATE = "general_update";
  public static final String MTGOX_SSL_URI = "https://data.mtgox.com";
  public static final String MTGOX_PLAIN_WEBSOCKET_URI = "ws://websocket.mtgox.com";
  public static final String MTGOX_SSL_WEBSOCKET_URI = "ws://websocket.mtgox.com";
  // Broadcast events
  public static final String UPDATE_SUCCEDED = "de.dev.eth0.bitcointrader.UPDATE_SUCCEDED";
  public static final String UPDATE_SERVICE_ACTION = "de.dev.eth0.bitcointrader.UPDATE_SERVICE_ACTION";
  public static final String UPDATE_FAILED = "de.dev.eth0.bitcointrader.UPDATE_FAILED";
  public static final String ORDER_EXECUTED = "de.dev.eth0.bitcointrader.ORDER_EXECUTED";
  // Extras for intents
  public static final String EXTRA_ORDERS = "orders";
}
