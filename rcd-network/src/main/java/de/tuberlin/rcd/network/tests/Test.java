/**
 * 
 */
package de.tuberlin.rcd.network.tests;

import de.tuberlin.rcd.network.Message;
import de.tuberlin.rcd.network.MessageBuilder;

import java.io.Serializable;
import java.util.UUID;

/**
 * Do a bit testing. Unit tests will come :)
 * @author Tobias Herb
 *
 */
public final class Test {

	public static class TestObject implements Serializable {
		private static final long serialVersionUID = 1L;
		public TestObject( int a, int b ) {
			this.a = a;
			this.b = b;
		}
		private Integer a;
		private Integer b;
		public String toString() {
			return "( a = " + this.a + ", b = " + this.b + " )";
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final UUID sideUID = UUID.randomUUID();
		
		final MessageBuilder msgBuilder = new MessageBuilder( false );
		
		final Message msg1 = msgBuilder.begin(UUID.randomUUID())
									   .setTimeStamp( 12 ) 
									   .addParameter( "int", new Integer( 100 ) )
									   .addParameter( "float", new Float( 33.33f ) )
									   .addParameter( "string", "tests" )
									   .build( sideUID );
		
		System.out.println( msg1 + "\n" );
		
		final Message msg2 = msgBuilder.addParameter( "object", new TestObject( 10, 5 ) )
									   .addParameter( "int", new Integer( 100 ) )
									   .build( sideUID );
		
		System.out.println( msg2 );
	}
}
