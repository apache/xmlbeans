package org.apache.xmlbeans.test.jam.cases.ejb;

public class TraderEJB implements IEnv {
  /**
   * @ejbgen:remote-method 
   *   isolation-level = Serializable
   *
   */
  public TradeResult buy(String customerName, String stockSymbol, int shares)
  {
    return null;
  }
}
