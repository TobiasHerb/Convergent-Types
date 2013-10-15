package de.tuberlin.rcd.protocol.types.tests;

public class StateMachineTest {

    public interface StateAction {

        public abstract StateAction action( Object data );
    }

    public enum State implements StateAction {

        STATE_UNINITIALIZED {
            @Override
            public StateAction action( Object data ) {
                return STATE_OPERATIVE;
            }
        },

        STATE_OPERATIVE {
            @Override
            public StateAction action( Object data ) {
                return STATE_LOCKED;
            }
        },

        STATE_LOCKED {
            @Override
            public StateAction action( Object data ) {
                return STATE_EXCLUSIVE_WRITE;
            }
        },

        STATE_EXCLUSIVE_WRITE {
            @Override
            public StateAction action( Object data ) {
                return STATE_UNINITIALIZED;
            }
        }

    }




    public static void main( String[] args ) {
        StateAction s = State.STATE_UNINITIALIZED;
        for( int i = 0; i < 10; ++i )
            s = s.action( null );
    }
}
