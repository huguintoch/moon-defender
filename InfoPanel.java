import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

public class InfoPanel extends JPanel {

	public static final int WIDTH = 200,
							HEIGHT = 600;
	
	private JLabel money,
			       level;
	
	private JButton btnCollector;
	private JButton btnTurrets;
	private JButton btnConstructors;
	
	private List<Collector> listCollectors;
	private List<Turret> listTurrets;
	
	private Cola queueConstructors;
	
	public InfoPanel() {
		super();
		this.setPreferredSize(new Dimension(InfoPanel.WIDTH, InfoPanel.HEIGHT));
		this.setLayout(new GridLayout(1, 2));
		
		this.money = new JLabel("0");
		this.level = new JLabel("1");
		
		this.money.setHorizontalAlignment(JLabel.CENTER);
		this.level.setHorizontalAlignment(JLabel.CENTER);
		
		this.add(this.money);
		this.add(this.level);
		
		this.btnCollector = new JButton("C");
		this.btnCollector.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listCollectors.setVisible(true);
			}
			
		} );
		this.add(this.btnCollector);
		
		this.btnTurrets = new JButton("T");
		this.btnTurrets.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				listTurrets.setVisible(true);
			}
			
		} );
		this.add(this.btnTurrets);
		
		this.btnConstructors = new JButton("C");
		this.btnConstructors.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				queueConstructors.setVisible(true);
			}
			
		} );
		this.add(this.btnConstructors);
		
		
	}
	
	public void updateMoney(int money) {
		this.money.setText(money+"");
	}
	
	public void updateLevel(int level) {
		this.level.setText(level+"");
	}
	
	public void sendCollectors(LinkedList<Collector> collectors) {
		this.listCollectors = new List<>(collectors, "Collector");
	}
	
	public void sendDeadCollector(Collector e) {
		this.listCollectors.sendDead(e);
	}
	
	public void sendTurrets(LinkedList<Turret> turrets) {
		this.listTurrets = new List<>(turrets, "Turret");
	}
	
	public void sendDeadTurret(Turret e) {
		this.listTurrets.sendDead(e);
	}
	
	public void sendConstructors(Queue<Constructor> constructors) {
		this.queueConstructors = new Cola(constructors);
	}
	
	public void sendType(int type) {
		this.queueConstructors.sendType(type);
	}

}
