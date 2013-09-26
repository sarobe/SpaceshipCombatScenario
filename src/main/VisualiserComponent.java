package main;

import problem.SpaceshipCombatProblem;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Samuel Roberts, 2012
 */
public class VisualiserComponent extends JComponent {

    SpaceshipCombatProblem problem;
    double[] solution;

    public VisualiserComponent(SpaceshipCombatProblem problem, double[] solution) {
        this.problem = problem;
        this.solution = solution;
    }

    public void paintComponent(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, 200, 200);

//        problem.visualiseSolution(g, solution);
    }

    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }
}
