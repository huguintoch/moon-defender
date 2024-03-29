//Autores: Guillermo Tanamachi A01631327 & Hugo Valdez A01631301
//Fecha: 25/11/2019

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

public class Game extends JPanel implements Runnable, MouseListener, MouseMotionListener {

	public static final int WIDTH = 800,
							HEIGHT = 680,
							CELL_SIZE = 20, //10
							COLS = Game.WIDTH/Game.CELL_SIZE, //80
							ROWS = Game.HEIGHT/Game.CELL_SIZE;

	public final int WALL_PRICE = 10,
					 COLLECTOR_PRICE = 50,
					 CURE_PRICE = 2000,
					 TURRET_PRICE = 100;

	private int maxConstructors,
			 	maxTurrets,
			 	maxCollectors,
			 	baseUpdateCost;

	private final int DELAY;

	private int state,
	 			money,
	 			wallCounter;

	private int[] mousePos;
	private int[][] grid;

	private Base base;
	private VirusSpawner virusSpawner;

	private Resource[] resources;
	private Terrain[] terrain;

	private Hashtable<Integer, Wall> walls;

	private LinkedList<Collector> collectors;
	private LinkedList<Turret> turrets;

	private Queue<Constructor> constructors;

	private Thread animator;

	private InfoPanel info;

	private SpriteManager spriteManager; 
	
	private boolean debugPaint,
					gameOver = true;
	
	private Window window;
	
	private Sound audio;
	
	public Game(InfoPanel info, Window window) {
		super();
		this.setPreferredSize(new Dimension(Game.WIDTH, Game.HEIGHT));
		this.DELAY = 50;
		
		this.audio = new Sound("GameSound.wav");

		this.maxConstructors = 2;
		this.maxTurrets = 2;
		this.maxCollectors = 2;
		this.baseUpdateCost = 500;

		this.state = 2;
		this.money = 0;
		this.wallCounter = 1;

		this.mousePos = new int[2];
		this.grid = new int[COLS][ROWS];

		this.base = new Base(this);
		this.virusSpawner = new VirusSpawner(this);

		this.generateResources(30);
		this.generateTerrain(25);
		
		this.info = info;
		this.info.setGame(this);

		this.walls = new Hashtable<>();

	    this.collectors = new LinkedList<>();
	    this.info.sendCollectors(this.collectors);
	    this.turrets = new LinkedList<>();
	    this.info.sendTurrets(this.turrets);
	    this.constructors = new LinkedList<>();
	    this.info.sendConstructors(this.constructors);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);;
		
		//Key Input
		this.addKeyBinding(this, KeyEvent.VK_1, "WALL", (e) -> {
			state = 1;
		});
		this.addKeyBinding(this, KeyEvent.VK_2, "COLLECTOR", (e) -> {
			state = 2;
		});
		this.addKeyBinding(this, KeyEvent.VK_3, "TURRET", (e) -> {
			state = 3;
		});
		this.addKeyBinding(this, KeyEvent.VK_4, "BASE", (e) -> {
			int tmp = this.base.getLevel();
			if(tmp+1<4 && this.money >= this.baseUpdateCost) {
				this.money -= this.baseUpdateCost;
				this.base.setLevel(tmp+1);
				this.info.updateLevel(this.base.getLevel());
			}
		});
		this.addKeyBinding(this, KeyEvent.VK_5, "WIN", (e) -> {
			if(this.money >= 2000) {
				this.virusSpawner.setLife(-100);
			}
		});
		
		this.window = window;

		this.animator = new Thread(this);
	    this.animator.start();
	    
	    this.spriteManager = new SpriteManager();
	    this.debugPaint = false;
	}
	
	private void addKeyBinding(JComponent comp, int keyCode, String id, ActionListener act) {
		InputMap im = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap ap = comp.getActionMap();
		
		im.put(KeyStroke.getKeyStroke(keyCode, 0, false), id);
		ap.put(id, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				act.actionPerformed(e);
			}
		});
	}

	public void generateResources(int size) {
		this.resources = new Resource[size];
	    Random rand = new Random();
	    int x, y;
	    for (int i = 0; i < this.resources.length; i++) {
			x = rand.nextInt(COLS);
			y = rand.nextInt(ROWS);
			this.resources[i] = new Resource(this);
			this.resources[i].generateResource(x, y, 0);
		}
	}

	public void generateTerrain(int size) {
		this.terrain = new Terrain[size];
	    Random rand = new Random();
	    int x, y;
	    for (int i = 0; i < this.terrain.length; i++) {
			x = rand.nextInt(COLS);
			y = rand.nextInt(ROWS);
			this.terrain[i] = new Terrain(this);
			this.terrain[i].generateTerrain(this, x, y, 0);
		}
	}

	public void paint(Graphics g) {
		super.paintComponent(g);
		if(this.debugPaint) {
			this.setBackground(Color.WHITE);
			this.paintGridDebug(g);
			this.paintBaseDebug(g);
			this.paintResourcesDebug(g);
			this.paintTerrainDebug(g);
			this.paintWallsDebug(g);
			this.paintTurretsDebug(g);
			this.paintCollectorsDebug(g);
			this.paintConstructorsDebug(g);
			this.paintVirusDebug(g);
			this.paintVirusSpawnerDebug(g);
			this.paintContourDebug(g);
			this.paintValues(g);
		}else {
			this.setBackground(new Color(163,163,163));
			this.paintGrid(g);
			this.paintBase(g);
			this.paintResources(g);
			this.paintTerrain(g);
			this.paintWalls(g);
			this.paintTurrets(g);
			this.paintCollectors(g);
			this.paintConstructors(g);
			this.paintVirus(g);
			this.paintVirusSpawner(g);
			this.paintContour(g);
		}
		
	}
	
	//Debug Paint Methods

	private void paintConstructorsDebug(Graphics g) {
		for(Constructor c : this.constructors) {
			g.setColor(Color.CYAN);
			g.fillRect(c.getX()*Game.CELL_SIZE+1, c.getY()*Game.CELL_SIZE, Game.CELL_SIZE, Game.CELL_SIZE);
		}
	}

	private void paintValues(Graphics g) {
		for (int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				g.setColor(Color.BLACK);
				g.drawString(Integer.toString(grid[i][j]), i*CELL_SIZE+6, j*CELL_SIZE+15);
			}
		}
	}

	private void paintGridDebug(Graphics g) {
		g.setColor(new Color(230, 230, 230));
		for (int i = 0; i <= COLS; i++) {
			g.drawLine(i*CELL_SIZE, 0, i*CELL_SIZE+1, Game.HEIGHT+1);
		}
		for (int i = 0; i <= ROWS; i++) {
			g.drawLine(1, i*CELL_SIZE, Game.WIDTH, i*CELL_SIZE+1);
		}
	}

	private void paintContourDebug(Graphics g) {
		if(this.state == 1) {
			if(grid[this.mousePos[0]/20][this.mousePos[1]/20] == 0 && this.money >= WALL_PRICE) {
				g.setColor(Color.BLUE);
				g.drawRect(this.mousePos[0], this.mousePos[1], CELL_SIZE, CELL_SIZE);
			} else {
				g.setColor(new Color(255, 0, 0, 150));
				g.fillRect(this.mousePos[0], this.mousePos[1], CELL_SIZE, CELL_SIZE);
			}
		} else if(this.state == 2) {
			if(grid[this.mousePos[0]/20][this.mousePos[1]/20] == 0 && (this.money >= COLLECTOR_PRICE || this.collectors.size() == 0 && this.constructors.size() == 0) && (this.collectors.size()+this.constructors.size() < this.maxCollectors)) {
				g.setColor(Color.PINK);
				g.drawRect(this.mousePos[0]-2*CELL_SIZE, this.mousePos[1]-2*CELL_SIZE, CELL_SIZE*5, CELL_SIZE*5);
				g.drawRect(this.mousePos[0], this.mousePos[1], CELL_SIZE, CELL_SIZE);
			} else {
				g.setColor(new Color(255, 0, 0, 150));
				g.fillRect(this.mousePos[0]-2*CELL_SIZE, this.mousePos[1]-2*CELL_SIZE, CELL_SIZE*5, CELL_SIZE*5);
				g.fillRect(this.mousePos[0], this.mousePos[1], CELL_SIZE, CELL_SIZE);
			}
		} else if(this.state == 3) {
			if(grid[this.mousePos[0]/20][this.mousePos[1]/20] == 0 && this.money >= TURRET_PRICE && (this.turrets.size()+this.constructors.size() < this.maxTurrets)) {
				g.setColor(Color.ORANGE);
				g.drawRect(this.mousePos[0]-3*CELL_SIZE, this.mousePos[1]-3*CELL_SIZE, CELL_SIZE*7, CELL_SIZE*7);
				g.drawRect(this.mousePos[0], this.mousePos[1], CELL_SIZE, CELL_SIZE);
			} else {
				g.setColor(new Color(255, 0, 0, 150));
				g.fillRect(this.mousePos[0]-3*CELL_SIZE, this.mousePos[1]-3*CELL_SIZE, CELL_SIZE*7, CELL_SIZE*7);
				g.fillRect(this.mousePos[0], this.mousePos[1], CELL_SIZE, CELL_SIZE);
			}
		}
	}

	private void paintBaseDebug(Graphics g) {
		g.setColor(Color.GREEN);
		for(int[] cell : this.base.getLocation()) {
			g.fillRect(cell[0]*CELL_SIZE+1, cell[1]*CELL_SIZE+1, CELL_SIZE, CELL_SIZE);
		}
	}

	private void paintVirusSpawnerDebug(Graphics g) {
		g.setColor(new Color(255,0,0,90));
		for(int[] cell : this.virusSpawner.getLocation()) {
			g.fillRect(cell[0]*CELL_SIZE+1, cell[1]*CELL_SIZE+1, CELL_SIZE, CELL_SIZE);
		}
	}

	private void paintVirusDebug(Graphics g) {
		int x, y;
		g.setColor(new Color(0, 0, 0, 100));
		for (int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if(grid[i][j] == -1) {
					x = Game.CELL_SIZE*i+1;
					y = Game.CELL_SIZE*j+1;
					g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
				}
			}
		}
	}

	private void paintWallsDebug(Graphics g) {
		g.setColor(new Color(0, 0, 255, 150));
		int x, y;
		int[] cell;
		Set<Integer> keys = walls.keySet();
		for(Integer key : keys) {
			cell = walls.get(key).getGridCell();
			x = CELL_SIZE*cell[0]+1;
			y = CELL_SIZE*cell[1]+1;
			g.fillRect(x, y, CELL_SIZE, CELL_SIZE);

		}
	}

	private void paintResourcesDebug(Graphics g) {
		g.setColor(new Color(255, 255, 0, 170));
		int cell[][];
		int x, y;
		for (int i = 0; i < this.resources.length; i++) {
			cell = this.resources[i].getGridCells();
			for (int j = 0; j < cell.length; j++) {
				x = cell[j][0];
				y = cell[j][1];
				updateResources(x, y);
				g.fillRect(CELL_SIZE*x+1, CELL_SIZE*y+1, CELL_SIZE, CELL_SIZE);
			}
		}
	}
	
	private void updateResources(int x, int y) {
		if(grid[x][y] == 0) {
			grid[x][y] = -2;
		}
	}

	private void paintTerrainDebug(Graphics g) {
		g.setColor(new Color(130, 100, 15, 170));
		int cell[][];
		int x, y;
		for (int i = 0; i < this.terrain.length; i++) {
			cell = this.terrain[i].getGridCells();
			for (int j = 0; j < cell.length; j++) {
				x = cell[j][0];
				y = cell[j][1];
				updateResources(x, y);
				g.fillRect(CELL_SIZE*x+1, CELL_SIZE*y+1, CELL_SIZE, CELL_SIZE);
			}
		}
	}

	private void paintCollectorsDebug(Graphics g) {
		g.setColor(new Color(255, 0, 255, 150));
		for(Collector c : this.collectors) {
			c.paintCollectorArea(g);
			updateCollectors(c.getPos()[0], c.getPos()[1]);
			g.fillRect(CELL_SIZE*c.getPos()[0]+1, CELL_SIZE*c.getPos()[1]+1, CELL_SIZE, CELL_SIZE);
		}
	}

	private void paintTurretsDebug(Graphics g) {
		int x, y;
		int[] pos;
		for(Turret t : this.turrets) {
			t.paintCollectorArea(g);
			pos = t.getPos();
			g.setColor(new Color(255, 160, 0, 150));
			x = pos[0];
			y = pos[1];
			updateCollectors(x, y);
			g.fillRect(CELL_SIZE*x+1, CELL_SIZE*y+1, CELL_SIZE, CELL_SIZE);
		}
	}

	private void updateCollectors(int x, int y) {
		if(grid[x][y] == 0) {
			grid[x][y] = -3;
		}
	}

	public void updateGrid() {
		LinkedList<int[]> cellsToChange = new LinkedList<>();
		for(int i=0; i<COLS; i++) {
    		for(int j=0; j<ROWS; j++) {
    			if(i == 0 && j == 0) {
    				if((grid[i+1][j] == -1 || grid[i][j+1] == -1) && grid[i][j] != -1) {
        				this.update(i, j, cellsToChange);
        			}
    			} else if(i == 0 && j == ROWS-1) {
    				if((grid[i+1][j] == -1 || grid[i][j-1] == -1) && grid[i][j] != -1) {
        				this.update(i, j, cellsToChange);
        			}
    			} else if (i == COLS-1 && j == 0) {
    				if((grid[i-1][j] == -1 || grid[i][j+1] == -1) && grid[i][j] != -1) {
        				this.update(i, j, cellsToChange);
        			}
    			} else if (i == COLS-1 && j == ROWS-1) {
    				if((grid[i-1][j] == -1 || grid[i][j-1] == -1) && grid[i][j] != -1) {
    					this.update(i, j, cellsToChange);
        			}
    			} else if (i == 0) {
    				if((grid[i+1][j] == -1 || grid[i][j+1] == -1 || grid[i][j-1] == -1) && grid[i][j] != -1) {
    					this.update(i, j, cellsToChange);
        			}
    			} else if (j == 0) {
    				if((grid[i+1][j] == -1 || grid[i-1][j] == -1 || grid[i][j+1] == -1) && grid[i][j] != -1) {
    					this.update(i, j, cellsToChange);
        			}
    			} else if (i == COLS-1) {
    				if((grid[i-1][j] == -1 || grid[i][j+1] == -1 || grid[i][j-1] == -1) && grid[i][j] != -1) {
    					this.update(i, j, cellsToChange);
        			}
    			} else if (j == ROWS-1) {
    				if((grid[i+1][j] == -1 || grid[i-1][j] == -1 || grid[i][j-1] == -1) && grid[i][j] != -1) {
    					this.update(i, j, cellsToChange);
        			}
    			} else {
    				if((grid[i+1][j] == -1 || grid[i-1][j] == -1 || grid[i][j+1] == -1 || grid[i][j-1] == -1) && grid[i][j] != -1) {
    					this.update(i, j, cellsToChange);
        			}
    			}
    		}
    	}

		for (int[] cell : cellsToChange) {
			this.setGrid(cell, -1);
		}
	}

	public void update(int i, int j, LinkedList<int[]> cellsToChange) {
		if(grid[i][j]<1 && grid[i][j] != -6) {
			int[] toAdd = {i,j};
			cellsToChange.add(toAdd);
		} else if(grid[i][j] > 0){
			this.walls.get(grid[i][j]).setLife(-1);
			if(this.walls.get(grid[i][j]).getLife() == 0) {
				this.walls.remove(grid[i][j]);
				this.grid[i][j] = 0;
			}
		}
	}
	
	//Game Paint Methods
	
	private void paintGrid(Graphics g) {
		g.drawImage(this.spriteManager.getGridSprite()[1], 0, 220, 500, 500, this);
		g.drawImage(this.spriteManager.getGridSprite()[1], 700, 200, 200, 200, this);
		g.drawImage(this.spriteManager.getGridSprite()[1], 300, -200, 400, 400, this);
		for (int i = 0; i <= COLS; i++) {
			for (int j = 0; j <= ROWS; j++) {
				g.drawImage(this.spriteManager.getGridSprite()[0], CELL_SIZE*i, CELL_SIZE*j, this);
			}
		}		
	}
	
	private void paintContour(Graphics g) {
		try {
			if(this.state == 1) {
				if(grid[this.mousePos[0]/20][this.mousePos[1]/20] == 0 && this.money >= WALL_PRICE) {
					g.drawImage(this.spriteManager.getWallSprite()[1],this.mousePos[0], this.mousePos[1], this);
				} else {
					g.drawImage(this.spriteManager.getWallSprite()[2],this.mousePos[0], this.mousePos[1], this);
				}
			} else if(this.state == 2) {
				if(grid[this.mousePos[0]/20][this.mousePos[1]/20] == 0 && (this.money >= COLLECTOR_PRICE || this.collectors.size() == 0 && this.constructors.size() == 0) && (this.collectors.size()+this.constructors.size() < this.maxCollectors)) {
					g.setColor(Color.GREEN);
					g.drawRect(this.mousePos[0]-2*CELL_SIZE, this.mousePos[1]-2*CELL_SIZE, CELL_SIZE*5, CELL_SIZE*5);
					g.setColor(new Color(0,255,0,50));
					g.fillRect(this.mousePos[0]-2*CELL_SIZE, this.mousePos[1]-2*CELL_SIZE, CELL_SIZE*5, CELL_SIZE*5);
					g.drawImage(this.spriteManager.getCollectorSprite()[0],this.mousePos[0], this.mousePos[1], this);
				} else {
					g.setColor(Color.RED);
					g.drawRect(this.mousePos[0]-2*CELL_SIZE, this.mousePos[1]-2*CELL_SIZE, CELL_SIZE*5, CELL_SIZE*5);
					g.setColor(new Color(255,0,0,50));
					g.fillRect(this.mousePos[0]-2*CELL_SIZE, this.mousePos[1]-2*CELL_SIZE, CELL_SIZE*5, CELL_SIZE*5);
					g.drawImage(this.spriteManager.getCollectorSprite()[2],this.mousePos[0], this.mousePos[1], this);
				}
			} else if(this.state == 3) {
				if(grid[this.mousePos[0]/20][this.mousePos[1]/20] == 0 && this.money >= TURRET_PRICE && (this.turrets.size() < this.maxTurrets)) {
					g.setColor(Color.GREEN);
					g.drawRect(this.mousePos[0]-3*CELL_SIZE, this.mousePos[1]-3*CELL_SIZE, CELL_SIZE*7, CELL_SIZE*7);
					g.setColor(new Color(0,255,0,50));
					g.fillRect(this.mousePos[0]-3*CELL_SIZE, this.mousePos[1]-3*CELL_SIZE, CELL_SIZE*7, CELL_SIZE*7);
					g.drawImage(this.spriteManager.getTurretSprite()[1],this.mousePos[0], this.mousePos[1], this);
				} else {
					g.setColor(Color.RED);
					g.drawRect(this.mousePos[0]-3*CELL_SIZE, this.mousePos[1]-3*CELL_SIZE, CELL_SIZE*7, CELL_SIZE*7);
					g.setColor(new Color(255,0,0,50));
					g.fillRect(this.mousePos[0]-3*CELL_SIZE, this.mousePos[1]-3*CELL_SIZE, CELL_SIZE*7, CELL_SIZE*7);
					g.drawImage(this.spriteManager.getTurretSprite()[2],this.mousePos[0], this.mousePos[1], this);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}
	}
	
	private void paintResources(Graphics g) {
		int cell[][];
		for (Resource r : this.resources) {
			r.paintResource(g);
			cell = r.getGridCells();
			for (int j = 0; j < cell.length; j++) {
				updateResources(cell[j][0], cell[j][1]);
			}
		}
	}
	
	private void paintTerrain(Graphics g) {
		int cell[][];
		for (Terrain r : this.terrain) {
			r.paintResource(g);
			cell = r.getGridCells();
			for (int j = 0; j < cell.length; j++) {
				updateResources(cell[j][0], cell[j][1]);
			}
		}
	}

	private void paintWalls(Graphics g) {
		Set<Integer> keys = walls.keySet();
		for(Integer key : keys) {
			walls.get(key).paint(g);
		}
	}
	
	private void paintBase(Graphics g) {
		this.base.paint(g);
	}
	
	private void paintCollectors(Graphics g) {
		try {
			for(Collector c : this.collectors) {
				c.paintCollector(g);
				updateCollectors(c.getPos()[0], c.getPos()[1]);
			}
		} catch (ConcurrentModificationException e) {
		}
	}
	
	private void paintTurrets(Graphics g) {
		try {
			for(Turret t : this.turrets) {
				t.paint(g);
				updateCollectors(t.getPos()[0], t.getPos()[1]);
			}
		} catch (ConcurrentModificationException e) {
			
		}
	}
	
	private void paintVirus(Graphics g) {
		Random r = new Random();
		Graphics2D g2D = (Graphics2D) g;
		int x, y;
		for (int i = 0; i < COLS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if(grid[i][j] == -1) {
					x = Game.CELL_SIZE*i+1;
					y = Game.CELL_SIZE*j+1;
					
					float alpha = 0.75f; 
					AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);
					g2D.setComposite(ac);
					g2D.drawImage(this.spriteManager.getVirusSprite()[r.nextInt(5)], x, y, this);
				}
			}
		}
	}
	
	private void paintVirusSpawner(Graphics g) {
		this.virusSpawner.paint(g);
	}
	
	private void paintConstructors(Graphics g) {
		for(Constructor c : this.constructors) {
			c.paint(g);
		}
	}
	
	private void updateStats() {
		
		//Virus spawner health adjustment
		if (this.grid[2][2] == 0 || this.grid[2][3] == 0 || this.grid[3][2] == 0 || this.grid[3][3] == 0) {
			this.virusSpawner.setLife(-20);
		}
		
		if(this.virusSpawner.getLife() <= 0) {
			FinalScreen fs = new FinalScreen();
			fs.setLocation(this.window.getLocationOnScreen().x, this.window.getLocationOnScreen().y);
			this.audio.endSound();
			this.window.dispose();
			this.info.getCollectorList().dispose();
			this.info.getTurretList().dispose();
			this.info.getQueue().dispose();
			this.virusSpawner.setLife(1);
		}

		//Base stats adjustment
		if(this.base.getLevel() == 2) {
			this.maxConstructors = 3;
			this.maxCollectors = 4;
			this.maxTurrets = 4;
			this.baseUpdateCost = 1000;
		}else if(this.base.getLevel() == 3) {
			this.maxConstructors = 4;
			this.maxCollectors = 6;
			this.maxTurrets = 6;
		}

	}

	public void accumMoney(int money) {
		this.money += money;
	}

    @Override
    public void run() {

        long beforeTime, timeDiff, sleep;
        beforeTime = System.currentTimeMillis();
        int cont = 0;
        
        while (true) {
            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = this.DELAY - timeDiff;
            if (sleep < 0) {
                sleep = 2;
            }

            try {
                Thread.sleep(sleep);
                
                LinkedList<Constructor> deadConstructors = new LinkedList<>();
                for(Constructor c : this.constructors) {
                	if(c.isDead()) {
                		deadConstructors.add(c);
                	}
                }
                
                for (int i = 0; i < deadConstructors.size(); i++) {
                	this.constructors.poll();
				}

                cont++;
                
                if(cont%100 == 0) {
                	cont = 0;
                	
                	this.updateGrid();
                }
                this.updateStats();

                this.info.updateMoney(this.money);
                this.info.update(this.base.getLevel(), this.state);

                LinkedList<Turret> deadTurrets = new LinkedList<>();
                for(Turret t : this.turrets) {
                	if(t.isDead()) {
                		deadTurrets.add(t);
                	}
                }

                for(Turret t : deadTurrets) {
                	this.info.sendDeadTurret(t);
                	this.turrets.remove(t);
                }

                this.repaint();
            } catch (InterruptedException e) {

            }
            beforeTime = System.currentTimeMillis();

        }
    }

    //MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.mouseActions(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}


	//Setters and Getters
	public void setGrid(int[] cell, int val) {
		if(cell[0] >= 0 && cell[0] < COLS && cell[1] >=0 && cell[1] < ROWS) {
			if(grid[cell[0]][cell[1]] == -4 && val == -1) {
				if(this.gameOver) {
					GameOver gm = new GameOver();
					gm.setLocation(this.window.getLocationOnScreen().x, this.window.getLocationOnScreen().y);
					this.audio.endSound();
					this.window.dispose();
					this.info.getCollectorList().dispose();
					this.info.getTurretList().dispose();
					this.info.getQueue().dispose();
				}
				this.gameOver = false;
				
			} else {
				grid[cell[0]][cell[1]] = val;
			}
		}
	}

	public int getGrid(int[] cell) {
		if(cell[0] >= 0 && cell[0] < COLS && cell[1] >=0 && cell[1] < ROWS) {
			return grid[cell[0]][cell[1]];
		} else {
			return 0;
		}
	}

	public void addTurret(Turret t) {
		this.turrets.add(t);
	}

	public void addCollector(Collector c) {
		this.collectors.add(c);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		this.mouseActions(e);
		int x = e.getX();
		int y = e.getY();
		this.mousePos[0] = x-x%20+1;
		this.mousePos[1] = y-y%20+1;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		this.mousePos[0] = x-x%20+1;
		this.mousePos[1] = y-y%20+1;
	}

	public void mouseActions(MouseEvent e) {
		int x = e.getX()/CELL_SIZE;
		int y = e.getY()/CELL_SIZE;
		int constructorNum = 0;
		
		if(e.getButton() == MouseEvent.BUTTON3) {
			this.state = 0;
			if(grid[x][y] == -3) {
				Collector dead = null;
				int[] current = {x, y};
				int[] collect = new int[2];
				for(Collector c : this.collectors) {
					collect = c.getPos();
					if(collect[0] == current[0] && collect[1] == current[1]) {
						dead = c;
					}
				}
				this.info.sendDeadCollector(dead);
				this.collectors.remove(dead);
				this.grid[x][y] = 0;
			} else if(grid[x][y] == -5) {
				Turret dead = null;
				int[] current = {x, y};
				int[] turr = new int[2];
				for(Turret t : this.turrets) {
					turr = t.getPos();
					if(turr[0] == current[0] && turr[1] == current[1]) {
						dead = t;
					}
				}
				this.info.sendDeadTurret(dead);
				this.turrets.remove(dead);
				this.grid[x][y] = 0;
			} else if(grid[x][y] > 0) {
				this.walls.remove(grid[x][y]);
				this.grid[x][y] = 0;
			}
		} else {
			switch (this.state) {
			case 0:
				break;
			case 1:
				if(x < COLS && y < ROWS) {
					if(this.money >= WALL_PRICE) {
						if(grid[x][y] == 0) {
							this.walls.put(Integer.valueOf(this.wallCounter), new Wall(this.wallCounter, x, y, this));
							this.wallCounter++;
							this.money -= WALL_PRICE;
						}
					}

				}
				break;
			case 2:
				for(Constructor c : this.constructors) {
					if(c.getType() == 1) {
						constructorNum++;
					}
				}
				if(x < COLS && y < ROWS) {
					if(this.collectors.size() == 0 && this.constructors.size() == 0) {
						if(grid[x][y] == 0) {
							if(this.constructors.size() < this.maxConstructors) {
								this.constructors.add(new Constructor(x, y, 1, this, true));
								this.info.sendType(1);
							}
						}
					} else if(this.money >= COLLECTOR_PRICE && this.collectors.size()+constructorNum < this.maxCollectors){
						if(grid[x][y] == 0) {
							if(this.constructors.size() < this.maxConstructors) {
								this.constructors.add(new Constructor(x, y, 1, this, true));
								this.info.sendType(1);
								this.money -= COLLECTOR_PRICE;
							}
						}
					}
					
				}
				break;
			case 3:
				for(Constructor c : this.constructors) {
					if(c.getType() != 1) {
						constructorNum++;
					}
				}
				if(this.money >= TURRET_PRICE && this.turrets.size()+constructorNum < this.maxTurrets) {
					if(grid[x][y] == 0) {
						if(this.constructors.size() < this.maxConstructors) {
							this.constructors.add(new Constructor(x, y, 2, this, true));
							this.info.sendType(2);
							this.money -= TURRET_PRICE;
						}		
					}
				}
				break;	
			default:
				break;
			}
		}
	}

	public void setDebugPaint(boolean x) {
		this.debugPaint = x;
	}
	
	public boolean getDebugPaint() {
		return this.debugPaint;
	}
	
}
