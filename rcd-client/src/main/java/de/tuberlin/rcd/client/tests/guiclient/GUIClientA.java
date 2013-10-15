package de.tuberlin.rcd.client.tests.guiclient;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import de.tuberlin.rcd.client.runtime.ClientFactory;
import de.tuberlin.rcd.client.types.string.ClientReplicatedString;
import de.tuberlin.rcd.network.common.IEventListener;
import de.tuberlin.rcd.network.common.Event;
import de.tuberlin.rcd.network.common.Pair;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationDefinition;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.OTOperationHistory;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearCursor;
import de.tuberlin.rcd.protocol.concurrencycontrol.otsystem.datamodels.linearmodel.OTLinearOperations;
import de.tuberlin.rcd.protocol.runtimedefinition.IDataManager;
import de.tuberlin.rcd.protocol.types.IReplicatedType;

/**
 * A Simple GUI for OT testing.
 */
public final class GUIClientA extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7955290839816351515L;

	/**
     * Log4J.
     */
    private static final Logger LOGGER = Logger.getRootLogger();

    private static final List<Pair<Color,Boolean>> colors = new ArrayList<Pair<Color,Boolean>>();

    private final Map<UUID,Color> authorColor = new HashMap<UUID, Color>();

    private final static ReentrantLock lock = new ReentrantLock();

    static {
        colors.add( new Pair<Color, Boolean>( Color.BLUE, false ) );
        colors.add( new Pair<Color, Boolean>( Color.DARK_GRAY, false ) );
        colors.add( new Pair<Color, Boolean>( Color.GRAY, false ) );
        colors.add( new Pair<Color, Boolean>( Color.GREEN, false ) );
        colors.add( new Pair<Color, Boolean>( Color.ORANGE, false ) );
        colors.add( new Pair<Color, Boolean>( Color.RED, false ) );
        colors.add( new Pair<Color, Boolean>( Color.PINK, false ) );
    }

    /**
     * Listener for text actions.
     */
    public class TextDocumentListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            switch( e.getKeyChar() ) {
                case '\u0008':
                    //caretCursor.deleteElement();
                    sharedText.deleteChar( textArea.getCaretPosition() );
                    //caretCursor.moveBackward();
                break;
                case '\u007F':
                    //caretCursor.deleteElement();
                    sharedText.deleteChar( textArea.getCaretPosition() );
                    //caretCursor.moveBackward();
                break;
                default:
                    if( e.getKeyChar() == '\n' ) {
                        //caretCursor.insertElement( e.getKeyChar() );
                        sharedText.insertChar( textArea.getCaretPosition() - 1, e.getKeyChar() );
                        //caretCursor.moveForward();
                    } else {
                        //caretCursor.insertElement( e.getKeyChar() );
                        sharedText.insertChar( textArea.getCaretPosition(), e.getKeyChar() );
                        //caretCursor.moveForward();
                    }
                break;
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    /**
     * Constructor.
     */
    public GUIClientA() {
        setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        setTitle( "OT GUI Client (A)" );
        addComponentsToPane( this.getContentPane() );
        pack();
    }

    private final JTextPane textArea = new JTextPane();

    private final JScrollPane scrollPane = new JScrollPane( textArea );

    private final JButton replayButton = new JButton( "Replay" );

    private final TextDocumentListener documentListener = new TextDocumentListener();

    private ClientReplicatedString sharedText;

    private OTLinearCursor<Character> caretCursor;

    /**
     * Add components and associated handler function to the window.
     * @param pane The content pane of the displayed window.
     */
    private void addComponentsToPane( Container pane ) {
        textArea.addKeyListener( documentListener );

        //textArea.addCaretListener( new CaretListener() {
        //    @Override
        //    public void caretUpdate( CaretEvent e ) {
        //        System.out.println( e.getDot() );
        //        caretCursor.reset( e.getDot() );
        //    }
        //});

        replayButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                textArea.setEditable( false );
                textArea.setText( "" );

                final OTOperationHistory<Character> history = sharedText.getHistory();
                final SimpleAttributeSet attributes = new SimpleAttributeSet();
                final SwingWorker worker = new SwingWorker<Void, OTOperationDefinition.OTOperation<Character>>() {

                    @Override
                    protected Void doInBackground(){
                        for ( Iterator<OTOperationDefinition.OTOperation<Character>> iterator =
                                      history.getHistoryIterator(); iterator.hasNext(); ) {
                            publish( iterator.next() );
                            try {
                                Thread.sleep( 10 );
                            } catch( InterruptedException e1 ) {
                                throw new IllegalStateException( e1 );
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void process( List<OTOperationDefinition.OTOperation<Character>> chunks ) {
                        for( OTOperationDefinition.OTOperation<Character> op : chunks ) {

                            if( !authorColor.containsKey( op.getMetaData().creator ) ) {
                                final Random rand = new Random();
                                boolean gotColor = false;
                                while( !gotColor ) {
                                    int index = rand.nextInt( colors.size() );
                                    final Pair<Color,Boolean> c = colors.get( index );
                                    if( !c.getSecond() ) {
                                        authorColor.put( op.getMetaData().creator, c.getFirst() );
                                        colors.set( index, new Pair<Color,Boolean>( c.getFirst(), true ) );
                                        gotColor = true;
                                    }
                                }
                            }

                            StyleConstants.setForeground( attributes, authorColor.get( op.getMetaData().creator ) );
                            if( op instanceof OTLinearOperations.InsertSEOperation ) {
                                final OTLinearOperations.InsertSEOperation<Character> insertOp =
                                        (OTLinearOperations.InsertSEOperation<Character>)op;
                                try {
                                    textArea.getDocument().insertString( insertOp.position, insertOp.insertedElement.toString(), attributes );
                                } catch (BadLocationException e1) {
                                    throw new IllegalStateException( e1 );
                                }
                            } else if( op instanceof OTLinearOperations.DeleteSEOperation ) {
                                final OTLinearOperations.DeleteSEOperation<Character> deleteOp =
                                        (OTLinearOperations.DeleteSEOperation<Character>)op;
                                try {
                                    textArea.getDocument().remove( deleteOp.position, 1 );
                                } catch (BadLocationException e1) {
                                    throw new IllegalStateException( e1 );
                                }
                            }
                        }
                    }

                    @Override
                    protected void done() {
                        textArea.setEditable( true );
                        StyleConstants.setForeground( attributes, Color.BLACK );
                    }
                };
                worker.execute();
            }
        });

        final JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
        scrollPane.setBorder( BorderFactory.createMatteBorder( 10, 10, 10, 10, pane.getBackground() ) );
        panel.add( scrollPane );
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.X_AXIS ) );
        buttonPanel.add( replayButton );
        panel.add( buttonPanel );
        pane.add( panel );
    }

    /**
     * Application entry point.
     * @param args No used.
     */
    @SuppressWarnings("unchecked")
    public static void main( String[] args ) {

        //-----------------------------------
        // initialize log4j.
        //-----------------------------------
        final SimpleLayout layout = new SimpleLayout();
        final ConsoleAppender consoleAppender = new ConsoleAppender( layout );
        LOGGER.addAppender( consoleAppender );
        LOGGER.setLevel( Level.INFO );

        try {
            final GUIClientA app = new GUIClientA();

            //-----------------------------------
            // initialize tests components.
            //-----------------------------------
            final Socket socket = new Socket( "localhost", 2832 );
            final ClientFactory factory = new ClientFactory();
            final ClientFactory.ClientContext context = factory.create( socket );
            final IDataManager dataManager = context.dataManager;

            //-----------------------------------
            // initialize types.
            //-----------------------------------
            context.nameRegistry.insertMapping( "ReplicatedString", ClientReplicatedString.class.getName() );
            app.sharedText = (ClientReplicatedString) dataManager.createReplicatedType( "sharedText", null, ClientReplicatedString.class );

            app.sharedText.addEventListener( IReplicatedType.ReplicatedTypeEvent.RS_FILL,
                    new IEventListener() {
                        @Override
                        public void handleEvent( Event event ) {
                            app.textArea.setText( event.data.toString() );
                        }
                    });

            app.caretCursor = (OTLinearCursor<Character>)app.sharedText.createCursor( 0 );

            app.sharedText.addEventListener( IReplicatedType.ReplicatedTypeEvent.RS_REMOTE_UPDATE,
                    new IEventListener() {
                        @Override
                        public void handleEvent( Event event ) {
                            lock.lock();
                            if( event.data instanceof OTLinearOperations.InsertSEOperation ) {
                                final OTLinearOperations.InsertSEOperation<Character> insertOp =
                                        (OTLinearOperations.InsertSEOperation<Character>)event.data;
                                try {
                                    app.textArea.getDocument().insertString( insertOp.position,
                                            insertOp.insertedElement.toString(), null );
                                } catch( BadLocationException e ) {
                                    throw new IllegalStateException( e );
                                }
                                if( app.textArea.getCaretPosition() > insertOp.position ) {
                                    app.textArea.setCaretPosition( app.textArea.getCaretPosition() + 1 );
                                }
                            } else if( event.data instanceof OTLinearOperations.DeleteSEOperation ) {
                                final OTLinearOperations.DeleteSEOperation<Character> deleteOp =
                                        (OTLinearOperations.DeleteSEOperation<Character>)event.data;
                                try {
                                    app.textArea.getDocument().remove( deleteOp.position, 1 );
                                } catch( BadLocationException e ) {
                                    throw new IllegalStateException( e );
                                }
                                if( app.textArea.getCaretPosition() > deleteOp.position ) {
                                    app.textArea.setCaretPosition( app.textArea.getCaretPosition() - 1 );
                                }
                            }
                            //app.textArea.setCaretPosition( app.caretCursor.getPosition() );
                            lock.unlock();
                        }
                    } );

            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    UIManager.put( "swing.boldMetal", Boolean.FALSE );
                    app.setSize( 650, 450 );
                    app.setVisible( true );
                }
            } );

        } catch( Exception e ) {
            throw new IllegalStateException( e );
        }
    }
}
