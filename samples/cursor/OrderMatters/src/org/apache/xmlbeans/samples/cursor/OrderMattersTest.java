package org.apache.xmlbeans.samples.cursor;

import statement.StatementDocument;
import statement.StatementDocument.Statement;
import statement.Transaction;
import java.io.File;

import org.apache.xmlbeans.XmlCursor;

import javax.xml.namespace.QName;

public class OrderMattersTest
{
    private static QName deposit = new QName( "http://statement", "deposit" );

    public static void main ( String[] args ) throws Exception
    {
        StatementDocument stmtDoc = StatementDocument.Factory.parse( new File( args[ 0 ] ) );

        if (!stmtDoc.validate())
            throw new RuntimeException("expected valid instance: " + args[0]);

        float balance = OrderMatters.balanceOutOfOrder(stmtDoc);
        if (1010F != balance)
            throw new RuntimeException("expected out of order to return $1010.0: " + balance);

        balance = OrderMatters.balanceInOrder(stmtDoc);
        if (960F != balance)
            throw new RuntimeException("expected in order to return $960.0: " + balance);
    }

}
