//Autores: Guillermo Tanamachi A01631327 & Hugo Valdez A01631301
//Fecha: 25/11/2019

import java.awt.*;

import javax.swing.ImageIcon;

public class Collector implements Runnable {

	private final int DELAY = 50;
	private int x,
				y;

	private int[] pos = new int[2];

	private Thread hilo;

	private Game game;
	
	private Image[] sprites = {new ImageIcon("collector2.png").getImage(),
							   new ImageIcon("collector3.png").getImage(),
							   new ImageIcon("collector.png").getImage(),
							   new ImageIcon("collector4.png").getImage(),
							   new ImageIcon("collector5.png").getImage(),
							   new ImageIcon("collector6.png").getImage(),
							   new ImageIcon("collector7.png").getImage(),
							   new ImageIcon("collector8.png").getImage(),};
	
	private Image sprite;
	
	public Collector(int x, int y, Game game) {
		this.x = x;
		this.y = y;

		this.pos[0] = x;
		this.pos[1] = y; 

		this.game = game;
		this.sprite = this.sprites[2];
		
		game.setGrid(this.pos, -3);

		this.hilo = new Thread(this);
		this.hilo.start();
	}

	public void paintCollector(Graphics g) {
		this.paintCollectorArea(g);
		g.drawImage(this.sprite, Game.CELL_SIZE*this.x+1, Game.CELL_SIZE*this.y+1, this.game);
	}
	
	public void paintCollectorArea(Graphics g) {
		g.setColor(new Color(255, 0, 255));
		int x_ = this.x*Game.CELL_SIZE - Game.CELL_SIZE*2;
		int y_ = this.y*Game.CELL_SIZE - Game.CELL_SIZE*2;
		g.drawRect(x_+1, y_+1, Game.CELL_SIZE*5, Game.CELL_SIZE*5);
		g.setColor(new Color(255, 0, 255,50));
		g.fillRect(x_+1, y_+2, Game.CELL_SIZE*5, Game.CELL_SIZE*5);
	}

	public int collect(Game game) {
		int sum = 0;
		int[] cell = new int[2];
		for(int i = x-2; i < x+3; i++) {
			for (int j = y-2; j < y+3; j++) {
				cell[0] = i;
				cell[1] = j;
				if(game.getGrid(cell) == -2) {
					sum++;
				}
			}
		}
		return sum;
	}

	public int[] getPos(){
		return this.pos;
	}

	@Override
	public void run() {

		long beforeTime, timeDiff, sleep;
        beforeTime = System.currentTimeMillis();
        int cont = 0;
        int index = 2;

		while(true) {
			timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = this.DELAY - timeDiff;
            if (sleep < 0) {
                sleep = 2;
            }

            try {
				Thread.sleep(sleep);
				cont++;
				if(cont%8 == 0) {
					this.sprite = this.sprites[index++];
				}
				if(cont%48 == 0) {
					index = 2;
				}
				if(cont%50 == 0) {
					cont = 0;
					game.accumMoney(collect(game));
				}
			} catch (InterruptedException e) {

			}
            beforeTime = System.currentTimeMillis();
		}
	}

}
