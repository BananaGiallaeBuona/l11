package it.unibo.oop.reactivegui03;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();
    private final JButton up;
    private final JButton down;
    private final JButton stop;

    AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        display.setText("0");
        panel.add(display);
        this.up = new JButton("up");
        panel.add(up);
        this.down = new JButton("down");
        panel.add(down);
        this.stop = new JButton("stop");
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        final Agent agent = new Agent();
        new Thread(agent).start();
        /*
         * Register a listener that stops it
         */
        up.addActionListener(e -> agent.directionUp = true);
        down.addActionListener(e -> agent.directionUp = false);
        stop.addActionListener(e -> agent.stopCounting());
    }

    /**
     * disable all buttons, for the current implementation isn't very useful becaus if exit the agent we 
     * automatically disable the funcionalitis of the buttons.
     */
    public void setEnabled() {
        up.setEnabled(false);
        down.setEnabled(false);
        stop.setEnabled(false);
    }

    private final class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         *
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         *
         * For more details on how to use volatile:
         *
         * http://archive.is/4lsKW
         *
         */
        private volatile boolean stop;
        private int counter;
        private volatile boolean directionUp = true; 

        @Override
        public void run() {
            while (!this.stop) {
                final int limit = 10_000;
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (directionUp) {
                        if (this.counter < limit) {
                            this.counter++;
                        } else {
                            this.stopCounting();
                        }
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(1);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            /*
            *NO MORE USED an agent that works to stop the program after 10 sec
            final Stopper stopper = new Stopper();
            stopper.setAgent(this);
            new Thread(stopper).start();
            */
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
            setEnabled();
        }
    }

    /*
    *i removed the other agent because i reached the goal whith the if
    */
}
