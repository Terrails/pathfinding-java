package pathfinding;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.function.Supplier;

public class SettingsPanel extends JPanel {

    public int FIELD_SIZE = 4;
    public float NOISE_FREQUENCY = 0.025f;
    public JFormattedTextField NOISE_SEED;
    public JCheckBox SEE_RAW_NOISE;
    public final boolean[] ACCESSIBLE_FIELDS = new boolean[MapField.values().length];

    public boolean USE_DIJKSTRA = true;
    public boolean DIAGONAL_MOVEMENT = false;
    public JCheckBox TIE_BREAK;
    public JFormattedTextField HEURISTICS_WEIGHT;

    public JTextArea LOG;

    public SettingsPanel() {
        super(new GridBagLayout());
        setMinimumSize(new Dimension(300, this.getHeight()));

        GridBagConstraints __gbc = this.defaults();
        __gbc.insets = new Insets(10, 5, 0, 5);

        //
        // Map size slider
        // Changes the size of the map by increasing/decreasing the space that a field takes
        //
        this.addComponent(() -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = this.defaults();

            JTextField text = new JTextField("4");
            text.setEditable(false);
            text.setPreferredSize(new Dimension(19, 20));
            panel.add(text, gbc);

            gbc.gridx++;

            JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 4, 10, 4);
            slider.addChangeListener(e -> {
                JSlider source = (JSlider) e.getSource();
                text.setText(String.valueOf(source.getValue()));
                if (!source.getValueIsAdjusting()) {
                    FIELD_SIZE = source.getValue();
                    Main.MAP_PANEL.cacheNoise();
                }
            });
            panel.add(slider, gbc);

            return panel;
        }, __gbc);

        //
        // Frequency slider
        // Changes the noise frequency
        //
        this.addComponent(() -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = this.defaults();

            JTextField text = new JTextField("0.025");
            text.setEditable(false);
            text.setPreferredSize(new Dimension(43, 20));
            panel.add(text, gbc);

            gbc.gridx++;

            JSlider slider = new JSlider(SwingConstants.HORIZONTAL, 1, 20, 5);
            final DecimalFormat df = new DecimalFormat("0.####");
            slider.addChangeListener(e -> {
                JSlider source = (JSlider) e.getSource();
                float val = source.getValue() * 0.00625f;
                text.setText(df.format(val));
                if (!source.getValueIsAdjusting()) {
                    NOISE_FREQUENCY = val;
                    Main.MAP_PANEL.cacheNoise();
                }
            });
            panel.add(slider, gbc);

            return panel;
        }, __gbc);

        //
        // New World button
        // Generates a world with a new seed, unless there is a fixed seed given
        //
        this.addComponent(() -> {
            JButton button = new JButton("New World");
            button.addActionListener(e -> Main.MAP_PANEL.reset(true));
            return button;
        }, __gbc);

        //
        // Input field for noise value and a toggle to see raw black and white noise
        //
        this.addComponent(() -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = this.defaults();

            NumberFormat format = NumberFormat.getIntegerInstance();
            format.setGroupingUsed(false);
            format.setMaximumIntegerDigits(9);
            NOISE_SEED = new JFormattedTextField(new NumberFormatter(format));
            NOISE_SEED.setToolTipText("Seed");
            panel.add(NOISE_SEED, gbc);

            gbc.gridx++;
            gbc.weightx = 0.1;

            SEE_RAW_NOISE = new JCheckBox("Raw Noise View", false);
            panel.add(SEE_RAW_NOISE, gbc);

            return panel;
        }, __gbc);

        //
        // Pathfinding
        // start and clear button for given algorithms
        //
        this.addComponent(() -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = this.defaults();

            JButton button = new JButton("Find Path");
            button.addActionListener(e -> {
                if (Main.MAP_PANEL.pathfinding != null && !Main.MAP_PANEL.pathfinding.PATH_FIELDS.isEmpty()) {
                    if (Main.MAP_PANEL.animIndex < Main.MAP_PANEL.pathfinding.CHECKED_FIELDS.size()) {
                        Main.MAP_PANEL.animIndex = Main.MAP_PANEL.pathfinding.CHECKED_FIELDS.size();
                    }
                } else Main.MAP_PANEL.pathfinding();
            });
            panel.add(button, gbc);

            gbc.gridx++;

            button = new JButton("Clear");
            button.addActionListener(e -> {
                if (Main.MAP_PANEL.pathfinding != null) {
                    Main.MAP_PANEL.pathfinding.PATH_FIELDS.clear();
                    Main.MAP_PANEL.pathfinding.CHECKED_FIELDS.clear();
                }
                Main.MAP_PANEL.animTimer.stop();
                Main.MAP_PANEL.animIndex = -1;
            });
            panel.add(button, gbc);

            return panel;
        }, __gbc);

        //
        // Pathfinding algorithms
        //
        this.addComponent(() -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = this.defaults();
            ButtonGroup groupRadio = new ButtonGroup();

            JRadioButton radioDijkstra = new JRadioButton("Dijkstra", true);
            radioDijkstra.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    HEURISTICS_WEIGHT.setVisible(false);
                    TIE_BREAK.setVisible(false);
                    USE_DIJKSTRA = true;
                    SettingsPanel.this.updateUI();
                    SettingsPanel.this.validate();
                }
            });
            groupRadio.add(radioDijkstra);
            panel.add(radioDijkstra, gbc);

            gbc.gridx++;

            JRadioButton radioAStar = new JRadioButton("A*", false);
            radioAStar.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    HEURISTICS_WEIGHT.setVisible(true);
                    TIE_BREAK.setVisible(true);
                    USE_DIJKSTRA = false;
                    SettingsPanel.this.updateUI();
                    SettingsPanel.this.validate();
                }
            });
            groupRadio.add(radioAStar);
            panel.add(radioAStar, gbc);

            gbc.gridx++;

            JCheckBox checkDiagonal = new JCheckBox("Diagonal movement");
            checkDiagonal.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    DIAGONAL_MOVEMENT = true;
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    DIAGONAL_MOVEMENT = false;
                }
            });
            panel.add(checkDiagonal, gbc);

            return panel;
        }, __gbc);

        //
        // Blacklist
        //
        this.addComponent(() -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = this.defaults();

            NumberFormat format = NumberFormat.getNumberInstance();
            format.setGroupingUsed(false);
            format.setMaximumIntegerDigits(4);
            HEURISTICS_WEIGHT = new JFormattedTextField(new NumberFormatter(format));
            HEURISTICS_WEIGHT.setToolTipText("Heuristics Weight");
            HEURISTICS_WEIGHT.setText("1");
            HEURISTICS_WEIGHT.setVisible(false);
            panel.add(HEURISTICS_WEIGHT, gbc);

            gbc.gridx++;

            TIE_BREAK = new JCheckBox("Tie-Break");
            TIE_BREAK.setToolTipText("Only prioritize a single path. Works considerably faster, but it can also find a path that is not optimal.");
            TIE_BREAK.setVisible(false);
            panel.add(TIE_BREAK, gbc);

            return panel;
        }, __gbc);

        this.addComponent(() -> {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = defaults();

            gbc.gridwidth = 4;
            JTextArea kartenFeldText = new JTextArea("Accessible Fields:");
            kartenFeldText.setFont(new Font("Arial", Font.BOLD, 16));
            panel.add(kartenFeldText, gbc);
            gbc.gridwidth = 1;

            // ----
            gbc.gridy++;
            gbc.gridx = 0;
            panel.add(fieldTypeCheckbox(MapField.DEEP_WATER, false), gbc);

            gbc.gridx++;
            panel.add(fieldTypeCheckbox(MapField.WATER, false), gbc);

            gbc.gridx++;
            panel.add(fieldTypeCheckbox(MapField.SAND, true), gbc);

            // ----
            gbc.gridy++;
            gbc.gridx = 0;
            panel.add(fieldTypeCheckbox(MapField.FLAT, true), gbc);

            gbc.gridx++;
            panel.add(fieldTypeCheckbox(MapField.FOREST, true), gbc);

            gbc.gridx++;
            panel.add(fieldTypeCheckbox(MapField.MOUNTAIN, true), gbc);

            // ----
            gbc.gridy++;
            gbc.gridx = 0;
            panel.add(fieldTypeCheckbox(MapField.MOUNTAINTOP, false), gbc);

            gbc.gridx++;
            panel.add(new JPanel(), gbc);

            return panel;
        }, __gbc);

        //
        // Log
        //
        this.addComponent(() -> {
            LOG = new JTextArea();
            LOG.setEditable(false);
            return LOG;
        }, __gbc);

        //
        // Empty element that fills up the whole panel to the end
        // Makes it so that the elements above are not centered vertically
        //
        __gbc.weighty = 1;
        this.addComponent(JPanel::new, __gbc);
    }

    private JPanel fieldTypeCheckbox(MapField field, boolean checked) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(field.getIcon());
        JCheckBox checkBox = new JCheckBox("", checked);

        ACCESSIBLE_FIELDS[field.ordinal()] = checked;
        checkBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ACCESSIBLE_FIELDS[field.ordinal()] = true;
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                ACCESSIBLE_FIELDS[field.ordinal()] = false;
            }
        });

        panel.add(label);
        panel.add(checkBox);
        return panel;
    }

    private GridBagConstraints defaults() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    private void addComponent(Component component, GridBagConstraints gbc) {
        gbc.gridy++;
        this.add(component, gbc);
    }

    private void addComponent(Supplier<Component> componentSupplier, GridBagConstraints gbc) {
        addComponent(componentSupplier.get(), gbc);
    }
}
